package io.github.travisdowns.polyregex;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class State {
    /* indirection to allow state pointers to be recorded and updated in fragments */
    public static class StateRef {
        public State s;
        public StateRef(State s) {
            this.s = s;
        }
    }

    public static State MATCHSTATE = new State(Type.MATCH, 0);

    public enum Type {
        CHAR, ANY, SPLIT, MATCH, LPAREN, RPAREN, BACKREF, FORWARD, INVALID, MATCHNOTHING;
    }

    int id;
    public int c;
    public final Type type;
    public StateRef out, out1;

    private State(State.Type type, int id) {
        this.type = type;
        this.id = id;
    }
    
    private State(State.Type type) {
        this(type, -1);
    }

    /**
     * Creates a copy of s. Note that the states pointed to by out and out1 are shallow copied,
     * i.e., new StateRef objects are created (if they existed in s), but they point to the same
     * objects as s, no copy is made of the referred to states.
     * @param s
     */
    public State(State s) {
        this.type = s.type;
        this.c = s.c;
        this.id = s.id;
        if (s.out != null) {
            this.out = new StateRef(s.out.s);
        }
        if (s.out1 != null) {
            this.out1 = new StateRef(s.out1.s);
        }    
    }

    public boolean isParen() {
        return type == Type.LPAREN || type == Type.RPAREN;
    }
    
    /** how many outgoing refs this state has (throws if any are dangling) */
    public int outRefs() {
        checkState(isComplete());
        if (out == null)  return 0;
        if (out1 == null) return 1;
        return 2;
    }
    
    /** true if there are no dangling references */
    public boolean isComplete() {
        checkState(out1 == null || out != null); // if out isn't set, out1 cannot be 
        return (out  == null ||  out.s != null) &&
               (out1 == null || out1.s != null); 
    }

    /** make a State with a data and a single dangling out pointer */
    private static State makeWithData(Type type, int data) {
        State s = new State(type);
        s.c = data;
        s.out = new StateRef(null);
        return s;
    }

    /** like {@link #makeWithData(Type, int, int)} but without data */
    private static State makeNoData(Type type) {
        return makeWithData(type, 0);
    }

    private static State makeNoData(Type type, int id) {
        State s = makeNoData(type);
        s.id = id;
        return s;
    }

    public static State makeChar(char c) {
        return makeWithData(Type.CHAR, c);
    }

    public static State makeDot() {
        return makeNoData(Type.ANY);
    }

    public static State makeSplit(State out, State out1) {
        State s = new State(Type.SPLIT);
        s.out = new StateRef(out);
        s.out1 = new StateRef(out1);
        return s;
    }

    public static State makeLParen(int parenIdx, State out) {
        State s = makeWithData(Type.LPAREN, parenIdx);
        s.out = new StateRef(out);
        return s;
    }

    public static State makeRParen(int parenIdx) {
        return makeWithData(Type.RPAREN, parenIdx);
    }

    public static State makeBackref(int backrefIdx) {
        return makeWithData(Type.BACKREF, backrefIdx);
    }

    @Override
    public String toString() {
        return toString(true);
    }

    private String toString(boolean recurse) {
        switch (type) {
        case CHAR:
            return "CHAR[" + (char) c + "]";
        case SPLIT:
            if (recurse) {
                return "SPLIT[out=" + out.s.toString(false) + ",out1=" + out1.s.toString(false) + "]";
            } else {
                return "SPLIT[...]";
            }
        case LPAREN:
            return "(";
        case RPAREN:
            return ")";
        default:
            return type.toString();
        }
    }

    /** return a list of all states reachable from the current state */
    public static <T extends State> List<T> allStates(T s) {
        LinkedHashSet<T> all = new LinkedHashSet<>();
        allStatesHelper(s, all);
        return new ArrayList<>(all);
    }
    
    /** assigns IDs starting from 1 to all the states reachable from s */
    public static void assignIds(State start) {
        List<State> states = allStates(start);
        checkState(states.get(0) == start);
        int id = 1;
        for (State s : states) {
            if (s != State.MATCHSTATE) {
                s.id = id++;
            }
        }
    }
    
    /**
     * Given a starting state s, clone all the entire reachable graph and return the 
     * pointer to the new start state s.
     * @param s
     * @return a new graph with the same stucture and nodes, independent of the original graph
     */
    public static <T extends State> T cloneGraph(State start, Function<State, T> cloner) {
        List<State> original = allStates(start);
        checkState(original.get(0) == start);
        List<T> clonedList = new ArrayList<>(original.size());
        HashMap<State, State> oldToNew = new HashMap<>(original.size());
        // create a clone of all states, but the references in the cloned list will still
        // point to the original
        for (State s : original) {
            T clone = cloner.apply(s);
            clonedList.add(clone);
            oldToNew.put(s, clone);
        }
        checkState(oldToNew.size() == original.size());
        
        // now fix up the pointers using the old -> new mapping
        replaceNodes(clonedList, oldToNew, true);
        
        return clonedList.get(0);
    }

    /**
     * In the given list, change any outgoing links present as a key in the oldToNew map
     * with the associated value.
     * 
     * @param completeMapping if true, the map is assumed to contain a mapping for every referenced node,
     * and an error is thrown if not present - otherwise, the mapping may be partial
     */
    private static void replaceNodes(List<? extends State> list, Map<State, State> oldToNew, boolean completeMapping) {
        for (State s : list) {
            fixup(s.out,  oldToNew, completeMapping);
            fixup(s.out1, oldToNew, completeMapping);
        }
    }
    
    /**
     * Remove all backref nodes in the graph pointed to by start and replace them with 
     * a series of CHAR nodes based on the actual characters in the input string with the given
     * start/stop indices.
     */
    public static void expandBackrefs(State start, String text, CaptureState capstate,
            Function<State, ? extends State> cloner) {
        checkArgument(start.type != Type.BACKREF); // first node cannot be a backref
        
        List<State> allStates = allStates(start);
        Map<State, State> oldToNew = new HashMap<>();
        
        for (State s : allStates) {
            if (s.type == Type.BACKREF) {
                checkState(s.outRefs() == 1);
                int refidx = s.c;
                checkState(refidx <= capstate.size());
                List<State> charSeries = makeStringMatcher(s.id, text, capstate.start(refidx),
                        capstate.end(refidx), cloner);
                charSeries.get(charSeries.size() - 1).out = s.out;
                oldToNew.put(s, charSeries.get(0));
            }
        }
        
        replaceNodes(allStates, oldToNew, false);
        
        List<State> newStates = allStates(start);
        checkState(newStates.size() >= allStates.size());
        checkState(!newStates.stream().anyMatch(s -> s.type == Type.BACKREF)); // no more backrefs!
    }
    
    private static List<State> makeStringMatcher(int id, String text, int start, int end,
            Function<State, ? extends State> cloner) {
        checkState(start <= text.length());
        checkState(end <= text.length());

        if ((start == -1 && end != -1) || (start > end)) {
            // not possible
            return Collections.singletonList(cloner.apply(State.makeNoData(Type.INVALID, id)));
        }
        
        if (end == -1) {
            // group hasn't been captured yet - note that if start != -1 we have a nested backref and
            // may want to handle those differently
            return Collections.singletonList(cloner.apply(State.makeNoData(Type.MATCHNOTHING, id)));
        }
        
        // the list of nodes matching the backref always starts with a FORWARD node which does nothing
        // but forward to the first matching node, but preserves the ID of the backref node
        List<State> ret = new ArrayList<>(end - start);
        State previous = cloner.apply(State.makeNoData(Type.FORWARD, id));
        ret.add(previous);
        for (int i = start; i < end; i++) {
            char c = text.charAt(i);
            State s = cloner.apply(State.makeChar(c));
            if (previous != null) {
                previous.out = new StateRef(s);
            }
            ret.add(s);
            previous = s;
        }
        
        return ret;
    }


    /**
     * For the given reference, if not null, replace the existing reference 
     * with a new one by looking up the existing reference in the map (which must exist).
     * @param oldToNew
     */
    private static void fixup(StateRef ref, Map<State, State> oldToNew, boolean throwOnMissing) {
        if (ref != null) {
            State newout = oldToNew.get(ref.s);
            if (newout == null) {
                checkState(!throwOnMissing);
            } else {
                ref.s = newout;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends State> void allStatesHelper(T s, LinkedHashSet<T> all) {
        if (!all.contains(s)) {
            all.add(s);
            if (s.out != null) {
                allStatesHelper((T)s.out.s, all);
                if (s.out1 != null) {
                    allStatesHelper((T)s.out1.s, all);
                }
            } else {
                checkState(s.out1 == null);  // out1 can't be set if out isn't
            }
        }
    }
}
