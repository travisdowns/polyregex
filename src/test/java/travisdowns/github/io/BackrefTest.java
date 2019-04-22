package travisdowns.github.io;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

/** tests with backrefs */
@RunWith(Parameterized.class)
public class BackrefTest {
    
    @Parameters(name = "{0}")
    public static List<Class<?>[]> getMatcherFactories() {
        return ImmutableList.of(
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
	
	

}
