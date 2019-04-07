package travisdowns.github.io;

import java.util.List;

import travisdowns.github.io.Parser.Token;

public class Matcher {
    
    private final State start;
    
    public Matcher(String pattern) {
        List<Token> tokens = Parser.toPostfix(pattern);
        this.start = NFABuilder.postToNFA(tokens);
    }

    public boolean matches(String text) {
        return NFARunner.matches(start, text);
    }

    public static boolean matches(String pattern, String text) {
        return new Matcher(pattern).matches(text); 
    }
}
