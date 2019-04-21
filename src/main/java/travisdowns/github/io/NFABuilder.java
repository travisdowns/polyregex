package travisdowns.github.io;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import travisdowns.github.io.State.StateRef;

/**
 * Partly based on post2nfa and associated functions from https://swtch.com/~rsc/regexp/nfa.c.txt originally written by Russ
 * Cox, converted to Java by Travis Downs.
 */
public class NFABuilder {

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

    /**
     * Convert postfix regular expression to NFA. Return start state.
     */
    public static State postToNFA(List<Token> tokens) {
        if (tokens.isEmpty()) {
            return State.MATCHSTATE;
        }

        Deque<Frag> stack = new ArrayDeque<>();

        // fprintf(stderr, "postfix: %s\n", postfix);
        int id = 1;
        for (Token t : tokens) {
            State s;
            Frag e, e1, e2;
            switch (t.type) {
            case CHAR:
                s = State.makeChar(id++, (char) t.data);
                stack.push(new Frag(s, s.out));
                break;
            case DOT:
                s = State.makeDot(id++);
                stack.push(new Frag(s, s.out));
                break;
            case CONCAT: /* catenate */
                e2 = stack.pop();
                e1 = stack.pop();
                patch(e1.out, e2.start);
                stack.push(new Frag(e1.start, e2.out));
                break;
            case ALT: /* alternate */
                e2 = stack.pop();
                e1 = stack.pop();
                s = State.makeSplit(id++, e1.start, e2.start);
                stack.push(new Frag(s, append(e1.out, e2.out)));
                break;
            case QUESTION:   /* zero or one */
                e = stack.pop();
                s = State.makeSplit(id++, e.start, null);
                stack.push(new Frag(s, append(e.out, Frag.singleton(s.out1))));
                break;
            case STAR: /* zero or more */
                e = stack.pop();
                s = State.makeSplit(id++, e.start, null);
                patch(e.out, s);
                stack.push(new Frag(s, s.out1));
                break;
            case PLUS: /* one or more */
                e = stack.pop();
                s = State.makeSplit(id++, e.start, null);
                patch(e.out, s);
                stack.push(new Frag(e.start, s.out1));
                break;
            default:
                throw new IllegalThreadStateException("unhandled token:" + t);
            }
        }

        checkState(stack.size() == 1, "fragment stack.size() != 1, was %s", stack.size());
        Frag e = stack.pop();
        patch(e.out, State.MATCHSTATE);

        return e.start;
    }

}
