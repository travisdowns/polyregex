package travisdowns.github.io;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

@RunWith(Parameterized.class)
public class MatcherTest {
    
    @Parameters(name = "{0}")
    public static List<Class<?>[]> getMatcherFactories() {
        return ImmutableList.of(
                new Class<?>[]{ OriginalMatcher.class },
                new Class<?>[]{ BackrefMatcher.class }
                );
    }
    
    @Parameter
    public Class<? extends Matcher> matcherClass;
    
    Matcher matcherFor(String pattern) {
        try {
            return matcherClass.getConstructor(String.class).newInstance(pattern);
        } catch (InvocationTargetException e) {
            Throwables.throwIfUnchecked(e.getCause());
            throw new RuntimeException(e.getCause());
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
    
    boolean matches(String pattern, String text) {
        Matcher m = matcherFor(pattern);
        return m.matches(text);
    }

	@Test
	public void emptyString() {
		assertTrue(matches("", ""));
	}
	
	@Test
	public void oneChar() {
		assertTrue(matches("x", "x"));
	}
	
	@Test
	public void moreChars() {
		assertTrue(matches("abc", "abc"));
		assertFalse(matches("abc", "abcd"));
		assertFalse(matches("abc", "dabc"));
	}
	
	@Test
	public void oneDot() {
		assertTrue(matches(".", "a"));
	}
	
	@Test
	public void moreDots() {
		assertTrue(matches("..", "ab"));
		assertTrue(matches("...", "abc"));
		assertFalse(matches("...", "ab"));
		assertFalse(matches("...", "abcd"));
	}
	
	@Test
	public void oneStar() {
		assertTrue(matches("a*", ""));
		assertTrue(matches("a*", "a"));
		assertTrue(matches("a*", "aaaaaaaa"));
		assertFalse(matches("a*", "aaaaaaaab"));
		assertTrue(matches("a*b", "aaaaaaaab"));
	}
	
	@Test
	public void onePlus() {
		assertFalse(matches("a+", ""));
		assertTrue(matches("a+", "a"));
		assertTrue(matches("a+", "aaaaaaaa"));
		assertFalse(matches("a+", "aaaaaaaab"));
		assertTrue(matches("a+b", "aaaaaaaab"));
	}
	
	@Test
	public void simpleAlt() {
		assertTrue (matches("a|b", "a"));
		assertTrue (matches("a|b", "b"));
		assertFalse(matches("a|b", "c"));
		
		assertTrue(matches("aA|bB", "aA"));
		assertTrue(matches("aA|bB", "bB"));
		assertFalse(matches("aA|bB", "aB"));
	}
	
	@Test
	public void testParensStar() {
		assertTrue (matches("(ab)*", ""));
		assertTrue (matches("(ab)*", "ab"));
		assertTrue (matches("(ab)*", "ababab"));
		assertTrue (matches("(ab)*(cdef)*",    "abababcdefcdef"));
		if (matcherClass != BackrefMatcher.class) {
		    // too slow with BR matcher
		    assertTrue (matches("((ab)*(cdef)*)*", "abababcdefcdefababcdef"));		    
		}
	}
	
	@Test
	public void testDoubleParens() {
		assertTrue (matches("(a)(b)", "ab"));
	}
	
	@Test
	public void testQuestion() {
	    assertTrue (matches("a?", ""));
	    assertTrue (matches("a?", "a"));
	    assertFalse(matches("a?", "aa"));
	}

}
