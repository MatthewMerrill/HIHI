package com.mattmerr.hitch.parsetokens.expression;

/**
 * Created by merrillm on 2/5/17.
 */
public class Operation extends ExpressionToken {
    
    public enum OperationType {
        ACCESS(1), CALL(1),
        POST_INCREMENT(1), POST_DECREMENT(1),
        
        PRE_INCREMENT(2), PRE_DECREMENT(2),
        POSITIVE(2), NEGATIVE(2),
        LOGICAL_NOT(2), BINARY_NOT(2),
        CAST(2),
        
        MULTIPLY(3), DIVIDE(3), MODULUS(3),
        
        ADD(4), SUBTRACT(4),
        
        SHIFT_LEFT(5), SHIFT_RIGHT(5),
        
        GREATER_THAN(6), LESS_THAN(6),
        GREATER_EQUAL(6), LESS_EQUAL(6),
        
        EQUALS(7), NOT_EQUALS(7),
        
        BITWISE_AND(8),
        
        BITWISE_XOR(9),
        
        BITWISE_OR(10),
        
        LOGICAL_AND(11),
        
        LOGICAL_OR(12),
        
        TERNARY(13),
        
        ASSIGN(14), ADD_ASSIGN(14), SUBTRACT_ASSIGN(14), MULTIPLY_ASSIGN(14), DIVIDE_ASSIGN(14);
        
        public final int precedence;
        OperationType(int precedence) {
            this.precedence = precedence;
        }
    }
    
}
