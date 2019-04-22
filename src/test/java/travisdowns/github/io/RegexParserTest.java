package travisdowns.github.io;

import static org.junit.Assert.*;

import org.junit.Test;


public class RegexParserTest {
    
    @Test
    public void testParensCount() {
        checkParensCount("", 0);
        
        checkParensCount("(.)", 1);
        
        checkParensCount("(.)(.)", 2);
        checkParensCount("((.))", 2);
        
        checkParensCount("((.)(.))", 3);
    }
    
    @Test
    public void testNoncapturingGroup() {
        checkParensCount("", 0);
        
        checkParensCount("(?:.)", 0);
        
        checkParensCount("(?:.)(.)", 1);
        checkParensCount("(?:(.))", 1);
        checkParensCount("((?:.))", 1);
        checkParensCount("(?:(?:.))", 0);
    }
    
    private void checkParensCount(String pattern, int expectedCount) {
     // test that the number of captured parens are correct
        RegexParser parser = new RegexParser(pattern);
        assertTrue(parser.parse());
        assertEquals(expectedCount, parser.nparen);
    }
}
