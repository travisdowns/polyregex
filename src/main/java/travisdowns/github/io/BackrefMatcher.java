package travisdowns.github.io;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Joiner;


public class BackrefMatcher implements Matcher {
    
    private final State start;
    
    private class StateList {
        final ArrayList<State> list;
        final Set<Integer> ids;

        public StateList() {
            this.list = new ArrayList<>();
            this.ids = new HashSet<>();
        }

        /* Add s to l, following unlabeled arrows. */
        void addstate(State s) {
            checkNotNull(s);
            if (!ids.contains(s.id)) {
                ids.add(s.id);
                switch (s.type) {
                case SPLIT:
                    /* follow unlabeled arrows */
                    addstate(s.out.s);
                    addstate(s.out1.s);
                    break;
                case LPAREN:
                case RPAREN:
                    checkState(s.out1 == null);
                    // just add the next state
                    addstate(s.out.s);
                    break;
                default:
                    list.add(s);
                }
            }
        }

        /* Check whether state list contains a match. */
        boolean ismatch() {
            return list.stream().anyMatch(s -> s == State.MATCHSTATE);
        }

        @Override
        public String toString() {
            return Joiner.on(", ").join(list);
        }
    }
    
    public BackrefMatcher(String pattern) {
        RegexParser parser = new RegexParser(pattern);
        checkState(parser.parse(), "parse() returned false");
        this.start = parser.start;
    }

    /*
     * typedef struct List List; struct List { State **s; int n; };
     * 
     * List l1, l2; static int listid;
     * 
     * void addstate(List*, State*); void step(List*, int, List*);
     */

    /* Compute initial state list */
    private StateList startlist(State start) {
        StateList l = new StateList();
        l.addstate(start);
        return l;
    }

    /*
     * Step the NFA from the states in clist past the character c, returns the new NFA state list.
     */
    private StateList step(StateList clist, int c) {
        StateList nlist = new StateList();
        for (State s : clist.list) {
            if (s.type == State.Type.ANY || (s.type == State.Type.CHAR && s.c == c)) {
                nlist.addstate(s.out.s);
            }
        }
        return nlist;
    }


    @Override
    /* Run NFA to determine whether it matches s. */
    public boolean matches(String text) {
        StateList list = startlist(start);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            list = step(list, c);
        }
        return list.ismatch();
    }

    public static boolean matches(String pattern, String text) {
        return new BackrefMatcher(pattern).matches(text); 
    }
}
