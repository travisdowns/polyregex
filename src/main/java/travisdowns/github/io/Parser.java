package travisdowns.github.io;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static travisdowns.github.io.Parser.Token.Type.ALT;
import static travisdowns.github.io.Parser.Token.Type.BACKREF;
import static travisdowns.github.io.Parser.Token.Type.CHAR;
import static travisdowns.github.io.Parser.Token.Type.CLOSEP;
import static travisdowns.github.io.Parser.Token.Type.CONCAT;
import static travisdowns.github.io.Parser.Token.Type.DOT;
import static travisdowns.github.io.Parser.Token.Type.OPENP;
import static travisdowns.github.io.Parser.Token.Type.PLUS;
import static travisdowns.github.io.Parser.Token.Type.STAR;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * https://eddmann.com/posts/shunting-yard-implementation-in-java/
 * 
 * @author Edd Mann
 * @author tdowns
 */
public class Parser {

	static class Token {
		
		enum Type {
			STAR("*"),
			DOT("."),
			PLUS("+"),
			ALT("|"),
			OPENP("("),
			CLOSEP(")"),
			CONCAT("#"),
			BACKREF(null),
			CHAR(null);
			
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
			this.type = type;
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

	private static List<Token> lex(String input) {
		List<Token> ret = new ArrayList<>();
		ret.add(Token.makeOp(OPENP)); // we surround the expression with () which makes parsing easier
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			Token t;
			switch (c) {
			case '*':
				t = new Token(STAR  , null); break;
			case '.':
				t = new Token(DOT   , null); break;
			case '+':
				t = new Token(PLUS   , null); break;
			case '|':
				t = new Token(ALT   , null); break;
			case '(':
				t = new Token(OPENP , null); break;
			case ')':
				t = new Token(CLOSEP, null); break;
			case '\\':
				int digit = Integer.parseInt("" + input.charAt(++i));
				t = new Token(BACKREF, digit);
				break;
			default:
				if (Character.toString(c).matches("[a-zA-Z]")) {
					t = new Token(CHAR, c);
					break;
				}
				throw new RuntimeException("Unexpected character while lexing: " + c);
			}
			ret.add(t);
		}
		ret.add(Token.makeOp(CLOSEP));
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
	public static List<Token> toPostfix(String input)
	{
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
			switch(token.type){
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
				for(; p.nalt > 0; p.nalt--)
					dst.add(Token.makeOp(ALT));
				p = parens.pop();
				p.natom++;
				break;
			case STAR:
			case PLUS:
				checkState(p.natom > 0, "unexpected %s with no atoms", token.type);
				dst.add(token);
				break;
			default:
				// TODO: do we really need this?
				if (p.natom > 1) {
					p.natom--;
					dst.add(Token.makeOp(CONCAT));
				}
				dst.add(token);
				p.natom++;
				break;
			}
		}
		if (!parens.isEmpty()) {
			throw new IllegalStateException("too many open parens");
		}
		checkState(p == root);
		return dst;
	}

}
