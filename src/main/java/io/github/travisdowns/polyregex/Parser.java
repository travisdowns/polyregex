package io.github.travisdowns.polyregex;

import static com.google.common.base.Preconditions.checkState;
import static io.github.travisdowns.polyregex.Token.Type.ALT;
import static io.github.travisdowns.polyregex.Token.Type.BACKREF;
import static io.github.travisdowns.polyregex.Token.Type.CHAR;
import static io.github.travisdowns.polyregex.Token.Type.CONCAT;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Based on https://swtch.com/~rsc/regexp/nfa.c.txt
 */
public class Parser {

    private static List<Token> lex(String input) {
        List<Token> ret = new ArrayList<>();
        ret.add(Token.charToOp('(')); // we surround the expression with () which makes parsing easier
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            Token t;
            if (c == '\\') {
                int digit = Integer.parseInt("" + input.charAt(++i));
                ret.add(Token.makeNonop(BACKREF, digit));
            } else if ((t = Token.charToOp(c)) != null) {
                ret.add(t);
            } else if (Character.toString(c).matches("[a-zA-Z]")) {
                ret.add(Token.makeNonop(CHAR, c));
            } else {
                throw new RuntimeException("Unexpected character while lexing: " + c);
            }           
        }
        ret.add(Token.charToOp(')'));
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
