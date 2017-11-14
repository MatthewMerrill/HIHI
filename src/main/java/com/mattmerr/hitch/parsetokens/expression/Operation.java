package com.mattmerr.hitch.parsetokens.expression;

import com.mattmerr.hitch.tokens.Operator;
import com.mattmerr.hitch.tokens.Punctuation;

import static com.mattmerr.hitch.parsetokens.expression.Operation.Associativity.LEFT;
import static com.mattmerr.hitch.parsetokens.expression.Operation.Associativity.RIGHT;

/**
 * Created by merrillm on 2/5/17.
 */
public class Operation extends ExpressionToken {

    public OperationType type;

    public enum Associativity {
        LEFT,
        RIGHT
    }

    public enum OperationType {
        ACCESS(1, LEFT), CALL(1, LEFT),
        POST_INCREMENT(1, LEFT), POST_DECREMENT(1, LEFT),

        PRE_INCREMENT(2, RIGHT), PRE_DECREMENT(2, RIGHT),
        POSITIVE(2, RIGHT), NEGATIVE(2, RIGHT),
        LOGICAL_NOT(2, RIGHT), BINARY_NOT(2, RIGHT),
        CAST(2, RIGHT),

        MULTIPLY(3, LEFT), DIVIDE(3, LEFT), MODULUS(3, LEFT),

        ADD(4, LEFT), SUBTRACT(4, LEFT),

        SHIFT_LEFT(5, LEFT), SHIFT_RIGHT(5, LEFT),

        GREATER_THAN(6, LEFT), LESS_THAN(6, LEFT),
        GREATER_EQUAL(6, LEFT), LESS_EQUAL(6, LEFT),

        EQUALS(7, LEFT), NOT_EQUALS(7, LEFT),

        BITWISE_AND(8, LEFT),

        BITWISE_XOR(9, LEFT),

        BITWISE_OR(10, LEFT),

        LOGICAL_AND(11, LEFT),

        LOGICAL_OR(12, LEFT),

        TERNARY(13, RIGHT),

        ASSIGN(14, RIGHT),
        ADD_ASSIGN(14, RIGHT), SUBTRACT_ASSIGN(14, RIGHT),
        MULTIPLY_ASSIGN(14, RIGHT), DIVIDE_ASSIGN(14, RIGHT),
        MODULUS_ASSIGN(14, RIGHT), SHIFT_LEFT_ASSIGN(14, RIGHT),
        SHIFT_RIGHT_ASSIGN(14, RIGHT), AND_ASSIGN(14, RIGHT),
        XOR_ASSIGN(14, RIGHT), OR_ASSIGN(14, RIGHT);

        public final int precedence;
        public final Associativity associativity;
        OperationType(int precedence, Associativity associativity) {
            this.precedence = precedence;
            this.associativity = associativity;
        }
    }

    public static Operation getOperation(Operator.OperatorType operator) {
        switch (operator) {
            case PLUS: return new BinaryOperation(OperationType.ADD);
            case MINUS: return new BinaryOperation(OperationType.SUBTRACT);
            case MUL: return new BinaryOperation(OperationType.MULTIPLY);
            case DIV: return new BinaryOperation(OperationType.DIVIDE);
            case EQ: return new BinaryOperation(OperationType.ASSIGN);
            case AND: return new BinaryOperation(OperationType.LOGICAL_AND);
            case OR: return new BinaryOperation(OperationType.LOGICAL_OR);

            default: throw new IllegalArgumentException("Unknown type " + operator);
        }

    }
    public static Operation getOperation(Punctuation.PunctuationType punctuation) {
        switch (punctuation) {
            case DOT: return new BinaryOperation(OperationType.ACCESS);

            default: return null;
        }
    }

}
