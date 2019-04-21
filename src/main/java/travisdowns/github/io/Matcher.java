package travisdowns.github.io;

public interface Matcher {
    /** true iff the given text matches the pattern represetned by this Matcher instance */  
    boolean matches(String text);
}
