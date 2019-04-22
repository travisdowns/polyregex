package travisdowns.github.io;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class OriginalParserTest {
    
	public static String postfixStr(String input) {
		List<Token> postfix = Parser.toPostfix(input);
		return postfix.stream().map(Token::toString).collect(Collectors.joining());
	}
	
	@Test
    public void testOneChar() {
        Assert.assertEquals(postfixStr("a"), "a");
    }
	
    @Test
    public void testBasicParsing()
    {
        Assert.assertEquals(postfixStr("abcdef"), "ab#c#d#e#f#");
        Assert.assertEquals(postfixStr("a|b|c"), "abc||");
        Assert.assertEquals(postfixStr("aa|bb|cc"), "aa#bb#cc#||");
        Assert.assertEquals(postfixStr("abc*"), "ab#c*#");
        Assert.assertEquals(postfixStr("(abc)*"), "ab#c#*");
    }
    
    @Test
    public void testBackrefs() {
    	Assert.assertEquals(postfixStr("\\1"), "\\1");
    	Assert.assertEquals(postfixStr("(\\1\\2)+"), "\\1\\2#+");
    }
    
    @Test
    public void testDoubleParens() {
    	Assert.assertEquals(postfixStr("(a)(b)"), "ab#");
    }
    
    @Test
    public void testQuestion() {
        Assert.assertEquals(postfixStr("a?"), "a?");
    }
}
