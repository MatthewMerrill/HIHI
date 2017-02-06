package com.mattmerr.hitch.tokens;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by merrillm on 2/4/17.
 */
public class Operator extends Token {
    
    private static Map<String, OperatorType> map = new HashMap<>();
    
    public final OperatorType value;
    public Operator(OperatorType value) {
        super(TokenType.OPERATOR);
        this.value = value;
    }
    
    public static Operator of(char ch) {
        return new Operator(OperatorType.valueOfCode(ch));
    }
    
    public enum OperatorType {
        PLUS("+"), MINUS("-"), MUL("*"), DIV("/"), MOD("%"),
        EQ("="), GT(">"), LT("<"), NOT("!"), POW("^"),
        AND("&"), OR("|"), INV("~");
        
        
        public final String val;
        OperatorType(String val) {
            this.val = val;
            map.put(val, this);
        }
        
        public static OperatorType valueOfCode(String op) {
            return map.get(op);
        }
        public static OperatorType valueOfCode(Character op) {
            return valueOfCode(Character.toString(op));
        }
    }
    
    public String toString() {
        return value.toString();
    }
}
