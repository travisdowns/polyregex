package io.github.travisdowns.polyregex.obsolete;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.Map;

public class Token {

    enum Type {
        STAR("*"), DOT("."), PLUS("+"), ALT("|"), OPENP("("), CLOSEP(")"), QUESTION("?"),
        CONCAT("#"), BACKREF(null), CHAR(null);

        final String symbol;

        String getSymbol() {
            checkState(symbol != null, "% has no symbol", this);
            return symbol;
        }

        private Type(String symbol) {
            this.symbol = symbol;
        }
    }
    
    static final Map<Character,Token> CHAR_TO_TOKEN = new HashMap<>();

    static {
        for (Token.Type type : Token.Type.values()) {
            if (type.symbol != null && type != Token.Type.CONCAT) {
                CHAR_TO_TOKEN.put(type.symbol.charAt(0), new Token(type, null));
            }
        }
    }

    Token.Type type;
    Object data;

    Token(Token.Type type, Object data) {
        this.type = checkNotNull(type);
        this.data = data;
    }

    public static Token makeOp(Token.Type type) {
        return new Token(type, null);
    }

    public static Token makeNonop(Token.Type type, Object obj) {
        return new Token(type, checkNotNull(obj));
    }
    
    public static Token charToOp(char c) {
        return CHAR_TO_TOKEN.get(c);
    }

    public boolean isOp() {
        return data == null;
    }

    @Override
    public String toString() {
        if (isOp()) {
            return type.symbol;
        } else {
            return (type == Type.BACKREF ? "\\" : "") + String.valueOf(data);
        }
    }
    
}