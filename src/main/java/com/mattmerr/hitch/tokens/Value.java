package com.mattmerr.hitch.tokens;

/**
 * Created by merrillm on 11/14/16.
 */
public class Value<T> extends Token {
    
    public final T value;
    
    public Value(TokenType type, T value) {
        super(type);
        this.value = value;
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}
