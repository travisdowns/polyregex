package travisdowns.github.io;

import java.util.List;

import travisdowns.github.io.Parser.Token;

public class Matcher {

	public static boolean matches(String pattern, String text) {
		List<Token> tokens = Parser.toPostfix(pattern);
		State start = NFABuilder.postToNFA(tokens);
		return NFARunner.matches(start, text);
	}
}
