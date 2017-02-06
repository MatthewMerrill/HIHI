package com.mattmerr.hitch.tokens;

/**
 * Created by merrillm on 11/14/16.
 */
public class Identifier extends Token {
    
    public final String value;
    
    public Identifier(String value) {
        super(TokenType.IDENTIFIER);
        this.value = value;
    }
    
    public String toString() {
        return value;
    }
}
