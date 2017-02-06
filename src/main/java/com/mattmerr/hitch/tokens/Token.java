package com.mattmerr.hitch.tokens;

/**
 * Created by merrillm on 11/14/16.
 */
public abstract class Token {
    
    public final TokenType type;
    
    public Token(TokenType type) {
        this.type = type;
    }
}
