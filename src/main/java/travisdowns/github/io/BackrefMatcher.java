package travisdowns.github.io;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;

import travisdowns.github.io.State.Type;


public class BackrefMatcher implements Matcher {

    private final State start;
    /** number of captured groups referenced by a backref (i.e., "unique backrefs") */
    private final int groupCount;

    /** instance of this class created for each match request, depends on the length of the input string */
    class BackrefRunner {

        final String text;
        private final Map<CaptureState, SubNFA> capToSub = new HashMap<>();

        SubNFA getSub(CaptureState state) {
            return capToSub.get(state);
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

            /* Add s to l, following unlabeled arrows. */
            void addstate(State s_, int textIdx) {
                checkNotNull(s_);
                SubNFA.StateEx s = (SubNFA.StateEx)s_;
                if (visited.add(s)) {
                    switch (s.type) {
                    case SPLIT:
                        /* follow unlabeled arrows */
                        addstate(s.out.s, textIdx);
                        addstate(s.out1.s, textIdx);
                        break;
                    case LPAREN:
                        checkState(s.out1 == null);
                        if (s.c == 0) { // parens 0 is special, don't jump to a sub in that case
                            addstate(s.out.s, textIdx);
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
                            addstate(newstate, textIdx);
                        }
                        break;
                    case RPAREN:
                        checkState(s.out1 == null);
                        if (s.c == 0) { // parens 0 is special, don't jump to a sub in that case
                            addstate(s.out.s, textIdx);
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
                            addstate(newstate, textIdx);
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
                        addstate(s.out.s, textIdx);
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

            // build the SubNFA list greedily
            int[] starts = new int[groupCount];
            int[]   ends = new int[groupCount];
            buildSubNFAs(0, starts, ends);
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
            l.addstate(getSub(new CaptureState(indexes, indexes,text.length())).start, -1);
            return l;
        }

        /*
         * Step the NFA from the states in clist past the character c, returns the new NFA state list.
         */
        private StateList step(StateList clist, int c, int textIdx) {
            StateList nlist = new StateList();
            for (State s : clist.states) {
                if (s.type == State.Type.ANY || (s.type == State.Type.CHAR && s.c == c)) {
                    nlist.addstate(s.out.s, textIdx);
                }
            }
            return nlist;
        }

        /* Run NFA to determine whether it matches s. */
        public boolean matches() {
            StateList list = startlist();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                list = step(list, c, i);
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
//        System.out.println("Got " + allStates.size() + " total states");
        int groupCount = (int)allStates.stream().filter(s -> s.type == Type.LPAREN).count();
        if (allStates.get(0).type == Type.LPAREN) {
            groupCount--; // don't count outer \0 group
        }
        this.groupCount = groupCount;
//        System.out.println("Got " + groupCount + " unique captured groups");
        State.assignIds(start);
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
