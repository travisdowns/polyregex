package io.github.travisdowns.polyregex;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableList;

import io.github.travisdowns.polyregex.BackrefMatcher;
import io.github.travisdowns.polyregex.Matcher;
import io.github.travisdowns.polyregex.OriginalMatcher;

@RunWith(Parameterized.class)
public class MatcherTest {
    
    @Parameters(name = "{0}")
    public static List<Object[]> getMatcherFactories() {
        List<Object[]> all = ImmutableList.of(
                params("Original",      s -> new OriginalMatcher(s)),
                params("Backref-lazy",  s -> new BackrefMatcher(s, false)), // lazy  subNFA creation
                params("Backref-eager", s -> new BackrefMatcher(s, true))   // eager subNFA creation
                );
        String testonly = System.getProperty("MatcherTest.matcher");
        if (testonly != null) {
            return all.stream().filter(p -> p[0].equals(testonly)).collect(Collectors.toList());
        } else {
            return all;
        }
    }
    
    private static Object[] params(String name, Function<String, Matcher> f) {
        return new Object[]{name, f};
    }
    
    @Parameter(0)
    public String name;
    
    @Parameter(1)
    public Function<String, Matcher> matcherFactory;
    
    Matcher matcherFor(String pattern) {
        return matcherFactory.apply(pattern);
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
	}
	
	@Test
	public void testParensStar2() {
	    if (!name.equals("Backref-eager")) { // too slow with BR eager matcher
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
