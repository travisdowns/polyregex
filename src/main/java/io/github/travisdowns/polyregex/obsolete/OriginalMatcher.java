package io.github.travisdowns.polyregex.obsolete;

import java.util.List;

import io.github.travisdowns.polyregex.Matcher;
import io.github.travisdowns.polyregex.State;

public class OriginalMatcher implements Matcher {
    
    private final State start;
    
    public OriginalMatcher(String pattern) {
        List<Token> tokens = Parser.toPostfix(pattern);
        this.start = NFABuilder.postToNFA(tokens);
    }

    @Override
    public boolean matches(String text) {
        return NFARunner.matches(start, text);
    }

    public static boolean matches(String pattern, String text) {
        return new OriginalMatcher(pattern).matches(text); 
    }
}
