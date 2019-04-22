package travisdowns.github.io;

import static com.google.common.base.Preconditions.checkState;
import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Test;

import com.google.common.base.Joiner;

public class StateTest {

    /** return the starting state for the NFA for the given pattern */
    State stateFor(String pattern) {
        RegexParser parser = new RegexParser(pattern);
        checkState(parser.parse(), "parse() returned false");
        return parser.start;
    }

    @Test
    public void testCopyCtor() {
        copyCtorTest("a");
        copyCtorTest("a*");
        copyCtorTest(".*");
        copyCtorTest("a?");
        copyCtorTest("a+");
//        copyCtorTest("(a)\\1");
    }
    
    @Test
    public void testCloneGraph() {
        cloneGraphTest("a");
        cloneGraphTest("a*");
        cloneGraphTest(".*");
        cloneGraphTest("a?");
        cloneGraphTest("a+");
        cloneGraphTest("((a.)+|b)*");
    }
    
    @Test
    public void testReplaceBackrefs() {
        checkBackrefMatch("abc\\1", "abca",   0, 1);
        checkBackrefMatch("abc\\1", "abcabc", 0, 3);
        checkBackrefMatch("...\\1", "abcabc", 0, 3);
        checkBackrefMatch("abc\\1*", "abcabab", 0, 2);
        
        // FORWARD cases
        checkBackrefMatch("abc\\1", "abc",   0, 0);
        checkBackrefMatch("abc\\1", "abc",   1, 1);
        checkBackrefMatch("abc\\1def", "abcdef",   0, 0);
    }

    private void checkBackrefMatch(String pattern, String text, int i, int j) {
        State start = stateFor(pattern);
        State.expandBackrefs(start, text, new int[]{-1, i}, new int[]{-1, j});
        assertTrue(new BackrefMatcher(start).matches(text));
    }
    

    private void copyCtorTest(String pattern) {
        State start = stateFor(pattern);
        for (State s : State.allStates(start)) {
            State cloned = new State(s);
            if (!EqualsBuilder.reflectionEquals(s, cloned,
                    true,  /* testTransients */
                    Object.class, /* test */
                    true, /* testRecursive */
                    "zzzz"  /* ignoreFields */))
            {
                fail("failed for " + s);
            }
            
            // check that any StateRef objects are new instances
            if (s.out != null || cloned.out != null) {
                assertFalse(s.out == cloned.out);
            }
            if (s.out1 != null || cloned.out1 != null) {
                assertFalse(s.out1 == cloned.out1);
            }
        }
    }
    
    private void cloneGraphTest(String pattern) {
        State start  = stateFor(pattern);
        State cloned = State.cloneGraph(start);
        
        // do a recursive reflective equals, which checks that they graphs are equal, reflective
        // means that StateRef references will be considered equal if their corresponding pointees
        // reflectively compare equal
        if (!EqualsBuilder.reflectionEquals(start, cloned,
                true,  /* testTransients */
                Object.class, /* test */
                true, /* testRecursive */
                "zzzz"  /* ignoreFields */))
        {
            fail("not equal after cloning");
        }
        
        List<State> startList = State.allStates(start);
        List<State> cloneList = State.allStates(cloned);
        
        // check that String representations are equal
        assertEquals(toString(startList), toString(cloneList));
        
        // check that there is no overlap in the state objects in the two graphs
        startList.retainAll(cloneList);
        assertEquals(0, startList.size());
    }
    
    private static String toString(List<State> list) {
        return Joiner.on(", ").join(list);
    }

}
