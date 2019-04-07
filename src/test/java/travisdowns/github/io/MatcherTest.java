package travisdowns.github.io;

import static org.junit.Assert.*;

import org.junit.Test;

public class MatcherTest {

	@Test
	public void emptyString() {
		assertTrue(Matcher.matches("", ""));
	}
	
	@Test
	public void oneChar() {
		assertTrue(Matcher.matches("x", "x"));
	}
	
	@Test
	public void moreChars() {
		assertTrue(Matcher.matches("abc", "abc"));
		assertFalse(Matcher.matches("abc", "abcd"));
		assertFalse(Matcher.matches("abc", "dabc"));
	}
	
	@Test
	public void oneDot() {
		assertTrue(Matcher.matches(".", "a"));
	}
	
	@Test
	public void moreDots() {
		assertTrue(Matcher.matches("..", "ab"));
		assertTrue(Matcher.matches("...", "abc"));
		assertFalse(Matcher.matches("...", "ab"));
		assertFalse(Matcher.matches("...", "abcd"));
	}
	
	@Test
	public void oneStar() {
		assertTrue(Matcher.matches("a*", ""));
		assertTrue(Matcher.matches("a*", "a"));
		assertTrue(Matcher.matches("a*", "aaaaaaaa"));
		assertFalse(Matcher.matches("a*", "aaaaaaaab"));
		assertTrue(Matcher.matches("a*b", "aaaaaaaab"));
	}
	
	@Test
	public void onePlus() {
		assertFalse(Matcher.matches("a+", ""));
		assertTrue(Matcher.matches("a+", "a"));
		assertTrue(Matcher.matches("a+", "aaaaaaaa"));
		assertFalse(Matcher.matches("a+", "aaaaaaaab"));
		assertTrue(Matcher.matches("a+b", "aaaaaaaab"));
	}
	
	@Test
	public void simpleAlt() {
		assertTrue (Matcher.matches("a|b", "a"));
		assertTrue (Matcher.matches("a|b", "b"));
		assertFalse(Matcher.matches("a|b", "c"));
		
		assertTrue(Matcher.matches("aA|bB", "aA"));
		assertTrue(Matcher.matches("aA|bB", "bB"));
		assertFalse(Matcher.matches("aA|bB", "aB"));
	}
	
	@Test
	public void testParensStar() {
		assertTrue (Matcher.matches("(ab)*", ""));
		assertTrue (Matcher.matches("(ab)*", "ab"));
		assertTrue (Matcher.matches("(ab)*", "ababab"));
		assertTrue (Matcher.matches("(ab)*(cdef)*", "abababcdefcdef"));
		assertTrue (Matcher.matches("((ab)*(cdef)*)*", "abababcdefcdefababcdef"));
	}
	
	@Test
	public void testDoubleParens() {
		assertTrue (Matcher.matches("(a)(b)", "ab"));
	}
	
	@Test
	public void testQuestion() {
	    assertTrue (Matcher.matches("a?", ""));
	    assertTrue (Matcher.matches("a?", "a"));
	    assertFalse(Matcher.matches("a?", "aa"));
	}

}
