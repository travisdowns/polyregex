package travisdowns.github.io;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static travisdowns.github.io.Verbose.verbose;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;

import travisdowns.github.io.State.Type;


public class BackrefMatcher implements Matcher {
    
    private static final boolean IS_EAGER  = Boolean.getBoolean("BackrefMatcher.eager");
    private static final boolean DEBUG_OUT = Boolean.getBoolean("BackrefMatcher.debug");
    
    /**
     * Pattern underlying this matcher, only used for display purposes (the compiled pattern is 
     * represented by {@code start}.
     */
    private final String pattern;
    /**
     * Start state for the unexpanded graph (used only to generate the subNFA graphs, not directly
     * used during simulation.
     */
    private final State start;
    /** number of captured groups referenced by a backref (i.e., "unique backrefs") */
    private final int groupCount;
    /** if true, all the possible subFNA groups are calculated before matching even starts, really slow */ 
    private final boolean isEager;

    /** instance of this class created for each match request, depends on the length of the input string */
    class BackrefRunner {

        final String text;
        private final Map<CaptureState, SubNFA> capToSub = new HashMap<>();
        int maxdepth = 0;

        SubNFA getSub(CaptureState capstate) {
            SubNFA ret = capToSub.get(capstate);
            if (ret == null) {
                checkState(!isEager, "sub for capstate not found in eager mode: %s", capstate);
                verbose("Creating SubFNA for captstate %s", capstate);
                ret = new SubNFA(start, capstate);
                capToSub.put(capstate, ret);
            }
            return ret;
        }

        /**
         * The original NFA is duplicated once for each possible combination of start/end
         * positions for each match. Each duplicated FNA is held by a SubNFA.
         */
        private class SubNFA {

            final StateEx start;
            final CaptureState capstate;

            /** extended state capable of handling backrefs */
            private class StateEx extends State {

                public StateEx(State s) {
                    super(checkNotNull(s));
                }

                SubNFA getOuter() {
                    return SubNFA.this;
                }
                
                @Override
                public String toString() {
                    return super.toString() + " (capstate : " + capstate + ")";
                }
            }

            final Map<Integer, StateEx> idToState = new HashMap<>();

            SubNFA(State start, CaptureState capstate) {
                this.capstate = capstate;

                // duplicate the graph
                StateEx cloned = State.cloneGraph(start, s -> new StateEx(s));

                // replace the backrefs
                State.expandBackrefs(cloned, text, capstate, StateEx::new);

                // create the ID -> node mapping
                List<StateEx> clonedList = State.allStates(cloned);

                for (State s : clonedList) {
                    checkState(s instanceof StateEx, "not a StateEx: %s", s);

                    if (s.id >= 0) {
                        checkState(idToState.put(s.id, (StateEx)s) == null);
                    } else {
                        checkState(s.id == -1 && s.type == Type.CHAR, "state had invalid id %s : %s", s.id, s);
                    }
                }

                this.start = clonedList.get(0);
            }

        }


        private class StateList {
            final HashSet<SubNFA.StateEx> states, visited;

            public StateList() {
                this.states  = new HashSet<>();
                this.visited = new HashSet<>();
            }
            
            /**
             * Only includes states that stay in the state list after all addstates calls have been
             * resolved, i.e., those like CHAR, ANY, etc which need to be processed in the next step.
             * Does not include states that are simply visited not not retained, such as SPLIT and FORWARD.
             * <p>
             * You can get a count of all visited states with {@link #visitedSize()}. 
             * @return count of contained states.
             */
            public int size() {
                return states.size();
            }
            
            /**
             * @return count of visited states.
             * @see #size()
             */
            public int visitedSize() {
                return visited.size();
            }

            /* Add s to l, following unlabeled arrows. */
            void addstate(State s_, int textIdx, int depth) {
                checkNotNull(s_);
                maxdepth = Math.max(depth, maxdepth);
                SubNFA.StateEx s = (SubNFA.StateEx)s_;
                if (visited.add(s)) {
                    switch (s.type) {
                    case SPLIT:
                        /* follow unlabeled arrows */
                        addstate(s.out.s, textIdx, depth + 1);
                        addstate(s.out1.s, textIdx, depth + 1);
                        break;
                    case LPAREN:
                        checkState(s.out1 == null);
                        if (s.c == 0) { // parens 0 is special, don't jump to a sub in that case
                            addstate(s.out.s, textIdx, depth + 1);
                        } else {
                            // jump to a new subNFA reflecting starting a new capture at the current text position + 1
                            CaptureState newcap = s.getOuter().capstate.withStart(s.c, textIdx + 1);
                            SubNFA sub = getSub(newcap);
                            checkState(sub != null, "sub was null for capstate %s", newcap);
                            // normally we'd addstate(s.out.s), so now look up the corresponding state
                            // in sub based on id
                            int stateId = s.out.s.id;
                            checkState(stateId >= 0);
                            SubNFA.StateEx newstate = sub.idToState.get(stateId);
                            checkState(newstate != null);
                            addstate(newstate, textIdx, depth + 1);
                        }
                        break;
                    case RPAREN:
                        checkState(s.out1 == null);
                        if (s.c == 0) { // parens 0 is special, don't jump to a sub in that case
                            addstate(s.out.s, textIdx, depth + 1);
                        } else {
                            // jump to a new subNFA reflecting starting a new capture at the current text position + 1
                            CaptureState newcap = s.getOuter().capstate.withEnd(s.c, textIdx + 1);
                            SubNFA sub = getSub(newcap);
                            // normally we'd addstate(s.out.s), so now look up the corresponding state
                            // in sub based on id
                            int stateId = s.out.s.id;
                            checkState(stateId >= 0);
                            SubNFA.StateEx newstate = sub.idToState.get(stateId);
                            checkState(newstate != null);
                            addstate(newstate, textIdx, depth + 1);
                        }
                        break;
                    case ANY:
                    case CHAR:
                    case MATCH:
                        states.add(s);
                        break;
                    case INVALID:
                        throw new RuntimeException("INVALID reached");
                    case FORWARD:
                        addstate(s.out.s, textIdx, depth + 1);
                        break;
                    case MATCHNOTHING:
                        // this state is a dead end, never matches, stop here
                        break;
                    default:
                        throw new RuntimeException("unhandled state type in addstate: " + s.type);
                    }
                }
            }


            /* Check whether state list contains a match. */
            boolean ismatch() {
                return states.stream().anyMatch(s -> s.type == Type.MATCH);
            }

            @Override
            public String toString() {
                return "states: " + Joiner.on(", ").join(states) + "\nvisited: " + Joiner.on(", ").join(visited);
            }

        }

        public BackrefRunner(String text) {
            this.text = text;

            if (isEager) {
                // build the SubNFA list eagerly
                int[] starts = new int[groupCount];
                int[]   ends = new int[groupCount];
                buildSubNFAs(0, starts, ends);
            }
        }

        private void buildSubNFAs(int groupIdx, int[] starts, int[] ends) {
            if (groupIdx == groupCount) {
                CaptureState cstate = new CaptureState(starts.clone(), ends.clone(), text.length());
                capToSub.put(cstate, new SubNFA(start, cstate));
            } else {
                for (int s = -1; s <= text.length(); s++) {
                    starts[groupIdx] = s;
                    for (int e = -1; e <= text.length(); e++) {
                        ends[groupIdx] = e;
                        buildSubNFAs(groupIdx + 1, starts, ends);
                    }
                }
            }
        }

        /* Compute initial state list */
        private StateList startlist() {
            StateList l = new StateList();
            int[] indexes = new int[groupCount];
            Arrays.fill(indexes, -1);
            l.addstate(getSub(new CaptureState(indexes, indexes,text.length())).start, -1, 1);
            verbose("Created starting state list with %s states (%s visited)", l.size(), l.visitedSize());
            for (SubNFA.StateEx s : l.states) {
                debug("  %s", s);
            }
            return l;
        }

        /*
         * Step the NFA from the states in clist past the character c, returns the new NFA state list.
         */
        private StateList step(StateList clist, int c, int textIdx) {
            StateList nlist = new StateList();
            for (State s : clist.states) {
                if (s.type == State.Type.ANY || (s.type == State.Type.CHAR && s.c == c)) {
                    nlist.addstate(s.out.s, textIdx, 1);
                }
            }
            return nlist;
        }

        /* Run NFA to determine whether it matches s. */
        public boolean matches() {
            boolean startlistOK = false;
            try {
                StateList list = startlist();
                startlistOK = true;
                for (int i = 0; i < text.length(); i++) {
                    char c = text.charAt(i);
                    list = step(list, c, i);
                }
                return list.ismatch();
            } catch (StackOverflowError e) {
                System.err.println("Died during " + (startlistOK ? "startlist() generation" : "step()") + ", maxdepth " + maxdepth);
                throw e;
            }
        }
    }

    public BackrefMatcher(String pattern) {
        // use the default IS_EAGER state, which can be set on the command line
        // with -DBackrefMatcher.isEager=true|flase
        this(pattern, IS_EAGER);
    }

    public BackrefMatcher(String pattern, boolean isEager) {
        this(pattern, ParserBase.doParse(pattern), isEager);
    }
    
    BackrefMatcher(String pattern, State start, boolean isEager) {
        verbose("Creating %s BackrefMatcher for pattern %s", isEager ? "eager" : "lazy", pattern);
        this.pattern = pattern;
        this.start = start;
        this.isEager = isEager;
        List<State> allStates = State.allStates(start);
        verbose("Got %s total unexpanded states", allStates.size());
        int groupCount = (int)allStates.stream().filter(s -> s.type == Type.LPAREN).count();
        if (allStates.get(0).type == Type.LPAREN) {
            groupCount--; // don't count outer \0 group
        }
        this.groupCount = groupCount;
        verbose("Got %s unique captured groups", groupCount);
        State.assignIds(start);
    }


    @Override
    /* Run NFA to determine whether it matches s. */
    public boolean matches(String text) {
        verbose("Matching text %s against pattern %s", text, pattern);
        return new BackrefRunner(text).matches();
    }


    public static boolean matches(String pattern, String text) {
        return new BackrefMatcher(pattern).matches(text); 
    }
    
    public static void debug(String fmt, Object... args) {
        if (DEBUG_OUT) {
            System.out.println(String.format(fmt, args));
        }
    }
}
