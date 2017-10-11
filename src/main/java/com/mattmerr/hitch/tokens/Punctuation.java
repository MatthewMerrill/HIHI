package com.mattmerr.hitch.tokens;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by merrillm on 2/4/17.
 */
public class Punctuation extends Token {
    
    private static Map<String, PunctuationType> map = new HashMap<>();
    
    public final PunctuationType value;
    public Punctuation(PunctuationType value) {
        super(TokenType.PUNCTUATION);
        this.value = value;
    }
    
    public static Punctuation of(char ch) {
        return new Punctuation(PunctuationType.valueOfCode(ch));
    }

    // ,:;(){}[]
    public enum PunctuationType {
        DOT("."), COMMA(","), COLON(":"), SEMICOLON(";"), OPEN_PARENTHESIS("("), CLOSE_PARENTHESIS(")"),
        OPEN_BRACKET("{"), CLOSE_BRACKET("}"), OPEN_SQUARE("["), CLOSE_SQUARE("]");
        
        
        public final String val;
        PunctuationType(String val) {
            this.val = val;
            map.put(val, this);
        }
        
        public static PunctuationType valueOfCode(String op) {
            return map.get(op);
        }
        public static PunctuationType valueOfCode(Character op) {
            return valueOfCode(Character.toString(op));
        }
    }
    
    public String toString() {
        return value.toString();
    }
}
