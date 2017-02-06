package com.mattmerr.hitch.tokens;

/**
 * Created by merrillm on 11/14/16.
 */
public class Keyword extends Token {
    
    public final String value;
    
    public Keyword(String value) {
        super(TokenType.KEYWORD);
        this.value = value;
    }
}
