package travisdowns.github.io;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Joiner;

/**
 * Based on match and associated functions from https://swtch.com/~rsc/regexp/nfa.c.txt originally written by Russ Cox,
 * converted to Java by Travis Downs.
 * 
 * MIT license, see LICENSE file.
 */
public class NFARunner {
    
    int maxdepth = 0;

    private class StateList {
        private final Set<State> stateSet;

        public StateList() {
            this.stateSet = new HashSet<>();
        }

        /* Add s to l, following unlabeled arrows. */
        void addstate(State s, int depth) {
            checkNotNull(s);
            maxdepth = Math.max(maxdepth, depth);
            if (stateSet.add(s)) {
                if (s.type == State.Type.SPLIT) {
                    /* follow unlabeled arrows */
                    addstate(s.out.s, depth + 1);
                    addstate(s.out1.s, depth + 1);
                } else {
                    checkState(!s.isParen(), "this runner doesn't support parens");
                    stateSet.add(s);
                }
            }
        }

        /* Check whether state list contains a match. */
        boolean ismatch() {
            return stateSet.stream().anyMatch(s -> s == State.MATCHSTATE);
        }

        @Override
        public String toString() {
            return Joiner.on(", ").join(stateSet);
        }
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
        // TODO: currently we can only run once against State objects because we
        // don't reset the generation counter - just get rid of that mechanism?
        StateList l = new StateList();
        l.addstate(start, 1);
        return l;
    }

    /*
     * Step the NFA from the states in clist past the character c, returns the new NFA state list.
     */
    private StateList step(StateList clist, int c) {
        StateList nlist = new StateList();
        for (State s : clist.stateSet) {
            if (s.type == State.Type.ANY || (s.type == State.Type.CHAR && s.c == c)) {
                nlist.addstate(s.out.s, 1);
            }
        }
        return nlist;
    }

    /* Run NFA to determine whether it matches s. */
    private boolean match(State start, String str) {
        StateList list = startlist(start);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            list = step(list, c);
        }
        return list.ismatch();
    }

    public static boolean matches(State start, String str) {
        NFARunner r = new NFARunner();
        boolean ret = r.match(start, str);
        Verbose.verbose("max depth while matching against %s : %s", str, r.maxdepth);
        return ret;
    }

}
