package travisdowns.github.io;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.List;

public class RegexLexer implements RegexTokens {
    
    
    private final List<LexToken> tokens;
    private int index = 0;
    
    public RegexLexer(String input) {
        this.tokens = lex(input);
    }
    
    public int nextToken() {
        index++;
        return getToken();
    }
    
    public int getToken() {
        return current().token;
    }
    
    public Object getSemantic() {
        LexToken t = current();
//        checkState(t.value != null, "tried to get semantic for semanticless token %s", t);
        return t.value;
    }
    
    private LexToken current() {
        return tokens.get(index);
    }
    
    private static class LexToken {
        int token;
        Object value;
        public LexToken(int token, Object value) {
            this.token = token;
            this.value = value;
        }
        @Override
        public String toString() {
            switch (token) {
            case CHAR:
                return Character.toString((Character)value);
            case ENDINPUT:
                return "ENDINPUT";
            case error:
                return "error";
            default:
                return Character.toString((char)token);
            }
        }
    }
    
    private static List<LexToken> lex(String input) {
        List<LexToken> ret = new ArrayList<>();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\\') {
                checkLex(++i < input.length(), "dangling \\");
                int digit = Integer.parseInt("" + input.charAt(i));
                checkLex(digit >= 1 && digit <= 9, "backref out of range: %s", digit);
                ret.add(new LexToken(BACKREF, digit));
            } else if ("()*+.:?|".indexOf(c) != -1) {
                ret.add(new LexToken(c, null));
            } else if (Character.toString(c).matches("[a-zA-Z]")) {
                ret.add(new LexToken(CHAR, c));
            } else {
                throw new RuntimeException("Unexpected character while lexing: " + c);
            }           
        }
        ret.add(new LexToken(ENDINPUT, null));
        return ret;
    }
    
    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + " tokens=" + tokens + "]";
    }
    
    private static void checkLex(boolean condition, String fmtstring, Object... fmtargs) {
        if (!condition) {
            throw new LexerException(String.format(fmtstring, fmtargs));
        }
    }

    public static class LexerException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        
        public LexerException(String message) {
            super(message);
        }
    }
}
