package travisdowns.github.io;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collections;
import java.util.List;

import travisdowns.github.io.State.StateRef;

public class ParserBase {
    
    protected final RegexLexer lexer;
    int nparen; // number of captured parenthesized groups
    State start; // the first state, populated at the end of parsing
    
    public ParserBase(String input) {
        this.lexer = new RegexLexer(input);
    }
    
    /**
     * Convenience method that calls parse on the parser, checks errors and returns the initial State
     * object.
     *  
     * @return the initial State object for the NFA
     */
    public static State doParse(String pattern) {
        RegexParser parser = new RegexParser(pattern);
        checkState(parser.parse(), "parse() returned false");
        return parser.start;
    }
    
    /* Patch the list of states at out to point to start. */
    static void patch(List<StateRef> reflist, State s) {
        checkNotNull(reflist);
        for (StateRef ref : reflist) {
            checkState(ref.s == null, "ref wasn't null in patch");
            ref.s = s;
        }
    }

    /** concatenates l2 to l1 and returns l1 */
    static List<StateRef> append(List<StateRef> l1, List<StateRef> l2) {
        l1.addAll(l2);
        return l1;
    }
    
    /** concatenates l2 to l1 and returns l1 */
    static List<StateRef> append(List<StateRef> l1, StateRef s) {
        return append(l1, Collections.singletonList(s));
    }
    
    Frag paren(Frag f, int n)
    {
        State s1 = State.makeLParen(id++, n, f.start);
        State s2 = State.makeRParen(id++, n);
        patch(f.out, s2);
        return new Frag(s1, s2.out);
    }

    void yyerror(String msg) {
        throw new RuntimeException("Parser error: " + msg);
    }
}
