package travisdowns.github.io;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static travisdowns.github.io.Parser.Token.Type.ALT;
import static travisdowns.github.io.Parser.Token.Type.BACKREF;
import static travisdowns.github.io.Parser.Token.Type.CHAR;
import static travisdowns.github.io.Parser.Token.Type.CONCAT;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Based on https://swtch.com/~rsc/regexp/nfa.c.txt
 */
public class Parser {

    static class Token {

        enum Type {
            STAR("*"), DOT("."), PLUS("+"), ALT("|"), OPENP("("), CLOSEP(")"), QUESTION("?"),
            CONCAT("#"), BACKREF(null), CHAR(null);

            final String symbol;

            String getSymbol() {
                checkState(symbol != null, "% has no symbol", this);
                return symbol;
            }

            private Type(String symbol) {
                this.symbol = symbol;
            }
        }

        Type type;
        Object data;

        private Token(Type type, Object data) {
            this.type = checkNotNull(type);
            this.data = data;
        }

        public static Token makeOp(Type type) {
            return new Token(type, null);
        }

        public static Token makeNonop(Type type, Object obj) {
            return new Token(type, checkNotNull(obj));
        }

        public boolean isOp() {
            return data == null;
        }

        @Override
        public String toString() {
            if (isOp()) {
                return type.symbol;
            } else {
                return (type == BACKREF ? "\\" : "") + String.valueOf(data);
            }
        }
    }
    
    private static final Map<Character,Token> CHAR_TO_TOKEN = new HashMap<>();
    
    static {
        for (Token.Type type : Token.Type.values()) {
            if (type.symbol != null && type != Token.Type.CONCAT) {
                CHAR_TO_TOKEN.put(type.symbol.charAt(0), new Token(type, null));
            }
        }
    }

    private static List<Token> lex(String input) {
        List<Token> ret = new ArrayList<>();
        ret.add(CHAR_TO_TOKEN.get('(')); // we surround the expression with () which makes parsing easier
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            Token t;
            if (c == '\\') {
                int digit = Integer.parseInt("" + input.charAt(++i));
                ret.add(new Token(BACKREF, digit));
            } else if ((t = CHAR_TO_TOKEN.get(c)) != null) {
                ret.add(t);
            } else if (Character.toString(c).matches("[a-zA-Z]")) {
                ret.add(new Token(CHAR, c));
            } else {
                throw new RuntimeException("Unexpected character while lexing: " + c);
            }           
        }
        ret.add(CHAR_TO_TOKEN.get(')'));
        return ret;
    }

    static class Parens {
        int nalt;
        int natom;

        public Parens() {
            this(0, 0);
        }

        public Parens(int nalt, int natom) {
            this.nalt = nalt;
            this.natom = natom;
        }
    };

    /**
     * Based on <a href="https://swtch.com/~rsc/regexp/nfa.c.txt">re2post by Russ Cox.</a>
     * 
     * @return list of tokens in postfix order
     */
    public static List<Token> toPostfix(String input) {
        if (input.isEmpty()) {
            return Collections.emptyList(); // special case this since it breaks otherwise
        }
        List<Token> dst = new ArrayList<>();
        Deque<Parens> parens = new ArrayDeque<>();
        // this Parens is never be used, it is immediately replaced by the first token which is
        // always OPENP since lex brackets the token stream in OPENP ... CLOSEP
        Parens root = new Parens(), p = root;
        List<Token> infixTokens = lex(input);
        for (Token token : infixTokens) {
            switch (token.type) {
            case OPENP:
                if (p.natom > 1) {
                    p.natom--;
                    dst.add(Token.makeOp(CONCAT));
                }
                parens.push(p);
                p = new Parens();
                break;
            case ALT:
                checkState(p.natom > 0, "unexpected alternation with no atoms");
                while (--p.natom > 0)
                    dst.add(Token.makeOp(CONCAT));
                p.nalt++;
                break;
            case CLOSEP:
                checkState(p.natom > 0, "empty parens");
                while (--p.natom > 0)
                    dst.add(Token.makeOp(CONCAT));
                for (; p.nalt > 0; p.nalt--)
                    dst.add(Token.makeOp(ALT));
                p = parens.pop();
                p.natom++;
                break;
            case STAR:
            case PLUS:
            case QUESTION:
                checkState(p.natom > 0, "unexpected %s with no atoms", token.type);
                dst.add(token);
                break;
            case CHAR:
            case BACKREF:
            case DOT:
                // TODO: do we really need this?
                if (p.natom > 1) {
                    p.natom--;
                    dst.add(Token.makeOp(CONCAT));
                }
                dst.add(token);
                p.natom++;
                break;
            default:
                checkState(false, "unknown token type in parsing: %s", token.type);
            }
        }
        if (!parens.isEmpty()) {
            throw new IllegalStateException("too many open parens");
        }
        checkState(p == root);
        return dst;
    }

}
