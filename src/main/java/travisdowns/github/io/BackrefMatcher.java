package travisdowns.github.io;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.HashSet;
import java.util.List;

import com.google.common.base.Joiner;

import travisdowns.github.io.State.Type;


public class BackrefMatcher implements Matcher {
    
    private final State start;
    /** number of captured groups referenced by a backref (i.e., "unique backrefs") */
    private final int brCount;
    
    /** instance of this class created for each match request, depends on the length of the input string */
    private class BackrefRunner {
        
        private final String text;
    
        private class CaptureState {
            int[] starts;
            int[] ends;
        }
        
        /**
         * The original NFA is duplicated once for each possible combination of start/end
         * positions for each match. Each duplicated FNA is held by a SubNFA.
         */
        private class SubNFA {
            
//            SubFNA(State start, ) {
//                
//            }
            
        }
        
        /** extended state capable of handling backrefs */
        private class StateEx {
            /* original underlying state */
            State s;
            int[] captureStart;
            int[] captureEnd;
        }
    
        private class StateList {
            final HashSet<State> states, visited;

            public StateList() {
                this.states  = new HashSet<>();
                this.visited = new HashSet<>();
            }

            /* Add s to l, following unlabeled arrows. */
            void addstate(State s) {
                checkNotNull(s);
                if (visited.add(s)) {
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
                    case ANY:
                    case CHAR:
                    case MATCH:
                        states.add(s);
                        break;
                    case INVALID:
                        throw new RuntimeException("INVALID reached");
                    case FORWARD:
                        addstate(s.out.s);
                        break;
                    default:
                        throw new RuntimeException("unhandled state type in addstate: " + s.type);
                    }
                }
            }
            

            /* Check whether state list contains a match. */
            boolean ismatch() {
                return states.stream().anyMatch(s -> s == State.MATCHSTATE);
            }

            @Override
            public String toString() {
                return "states: " + Joiner.on(", ").join(states) + "\nvisited: " + Joiner.on(", ").join(visited);
            }
            
        }
        
        public BackrefRunner(String text) {
            this.text = text;
        }

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
            for (State s : clist.states) {
                if (s.type == State.Type.ANY || (s.type == State.Type.CHAR && s.c == c)) {
                    nlist.addstate(s.out.s);
                }
            }
            return nlist;
        }
        
        /* Run NFA to determine whether it matches s. */
        public boolean matches() {
            StateList list = startlist(start);
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                list = step(list, c);
            }
            return list.ismatch();
        }
    }
    
    public BackrefMatcher(String pattern) {
        this(ParserBase.doParse(pattern));
    }

    BackrefMatcher(State start) {
        this.start = start;
        List<State> allStates = State.allStates(start);
        System.out.println("Got " + allStates.size() + " total states");
        this.brCount = (int)allStates.stream().filter(s -> s.type == Type.LPAREN).count();
        System.out.println("Got " + brCount + " unique captured groups");
    }
    
    
    @Override
    /* Run NFA to determine whether it matches s. */
    public boolean matches(String text) {
        return new BackrefRunner(text).matches();
    }


    public static boolean matches(String pattern, String text) {
        return new BackrefMatcher(pattern).matches(text); 
    }
}
