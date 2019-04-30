package io.github.travisdowns.polyregex;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

import io.github.travisdowns.polyregex.BackrefMatcher;
import io.github.travisdowns.polyregex.Matcher;

/** tests with backrefs */
@RunWith(Parameterized.class)
public class BackrefTest {
    
    static class JavaMatcher implements Matcher {
        private final Pattern pattern;
        
        public JavaMatcher(String patternString) {
            this.pattern = Pattern.compile(patternString);
        }

        @Override
        public boolean matches(String text) {
            return pattern.matcher(text).matches();
        }
    }
    
    @Parameters(name = "{0}")
    public static List<Class<?>[]> getMatcherFactories() {
        return ImmutableList.of(
                new Class<?>[]{ BackrefMatcher.class },
                new Class<?>[]{ JavaMatcher.class }
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
	public void testBackrefSimple() {
		assertTrue (matches("(a)\\1", "aa"));
		assertFalse(matches("(a)\\1", "ab"));
	}
	
	@Test
	public void testBackrefHarder() {
	    assertTrue (matches("(...)\\1", "aaaaaa"));
	    assertTrue (matches("(...)\\1", "abaaba"));
	    assertTrue (matches("(...).\\1", "abacaba"));
	}
	
	@Test
    public void testRepeatedBackrefs() {
	    assertTrue (matches("(.*)\\1", ""));
        assertTrue (matches("(.*)\\1", "aa"));
        assertFalse(matches("(.*)\\1", "aaa"));
        assertTrue (matches("(.*)\\1", "aaaa"));
        
        assertTrue (matches("(.*)\\1\\1", ""));
        assertFalse(matches("(.*)\\1\\1", "a"));
        assertFalse(matches("(.*)\\1\\1", "aa"));
        assertTrue (matches("(.*)\\1\\1", "aaa"));
        assertFalse(matches("(.*)\\1\\1", "aaaa"));
        
        assertTrue (matches("(.*)\\1*", ""));
        assertTrue (matches("(.*)\\1*", "a"));
        assertTrue (matches("(.*)\\1*", "abcdef"));
        
        assertTrue (matches("(.*)\\1+", ""));
        assertFalse(matches("(.*)\\1+", "a"));
        assertTrue (matches("(.*)\\1+", "aa"));
        assertTrue (matches("(.*)\\1+", "aaa"));
        assertTrue (matches("(.*)\\1+", "aaabaaabaaab"));
        assertTrue (matches("(.*)\\1+", "aaabaaabaaabxaaabaaabaaabx"));
//        assertTrue (matches("(.*)\\1+", "abcdef"));
        
        assertFalse(matches("(.*)\\1", "ab"));
    }
	
	@Test
	public void testMatchnothing() {
	    // (a) group doesn't match, so the subsequent \1 can't match anything, this is
	    // implemented with the MATCHNOTHING state when \1 is expanded. This is the most 
	    // common behavior, but others are possible, for a good summary see:
	    // https://www.regular-expressions.info/backref2.html
	    assertFalse(matches("(a)*\\1", "b"));
	    
	    // examples in the spirit of https://www.regular-expressions.info/backref2.html 
	    assertTrue(matches("(q?)b\\1", "b"));
	    assertTrue(matches("(q*)b\\1", "b"));
	    assertFalse(matches("(q)?b\\1", "b"));
	    assertFalse(matches("(q)*b\\1", "b"));
	    assertTrue(matches("(q)?b\\1?", "b")); // matches because the backref is optional
	}
	
	/**
	 * Modified version of test from
	 * <a href=https://branchfree.org/2019/04/04/question-is-matching-fixed-regexes-with-back-references-in-p/>Geoff Landale's blog</a>.
	 */
	@Test
	public void testGeoff() {
	    // original probably was: ((\wx)\2)*z
	    // but we change \w to . since we don't support char classes
	    String pattern = "((.x)\\2)*z";
	 
	    assertTrue (matches(pattern, "axaxbxbxz"));
	    assertTrue (matches(pattern, "axaxaxaxaxaxaxaxbxbxbxbxbxbxz"));
	    
	    pattern = "((.x)*\\2)*z";
	    
	    assertTrue (matches(pattern, "axaxbxbxz"));
        assertTrue (matches(pattern, "axaxaxaxaxaxaxaxbxbxbxbxbxbxz"));
	}
	
	/**
     * Test of pattern described by Geoff in https://github.com/travisdowns/polyregex/issues/2
     * but with {1,5} replaced by (.|..|...) since we don't support counted matches. 
     * .(..+)+
     */
    @Test
    public void testGeoff2() {
        // original probably was: ((\wx)\2)*z
        // but we change \w to . since we don't support char classes
        String pattern = "(?:(?:.|..|...)(.)(?:.|..|...)\\1)+";
     
//        assertTrue (matches(pattern, "fooxxxxxxxxxxbar"));
        assertTrue (matches(pattern, "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"));
    }
	
	@Test
	public void testIsPrime() {
	    assertTrue (isPrime(2));
	    assertTrue (isPrime(3));
	    assertFalse(isPrime(4));
	}
	
	@Test
	public void testFactorization() {
	    Matcher m = matcherFor("(xx+)\\1+");
	    for (int i = 2; i < 200; i++) {
	        String text = Strings.repeat("x", i);
	        assertEquals("failed for " + i, isPrime(i), !m.matches(text));
	    }
	}
	
	@Test
	public void testFind2() {
	    // looks for the two last characters in the string anywhere in the first n-2 characters
	    String find2 = ".*(.*).*(.).*\\2\\1";
	    
	    assertTrue (matches(find2, "qwertyuiopasdfghjklzxcvbnmqw"));
	    assertTrue (matches(find2, "qwertyuiopasdfghjklzxcvbnmic"));
	    assertTrue (matches(find2, "qwertyuiopasdfghjklzxcvbnmziop"));
	}
	
	@Test
	public void testBacktrackingBlowup() {
	    String bang = "(?:(.*)(.*)\\1\\2)*";
	    
	    assertFalse (matches(bang, "ababababababababababababa"));
	}
	
	private static boolean isPrime(int x) {
	    for (int d = 2; d <= Math.sqrt(x); d++) {
	        if (x % d == 0)
	            return false;
	    }
	    return true;
	}
}
