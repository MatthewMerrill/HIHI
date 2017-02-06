package com.mattmerr.hitch.parsetokens.expression;

/**
 * Created by merrillm on 2/5/17.
 */
public class BinaryOperation extends ExpressionToken {
    
    public Operation.OperationType type;
    public ExpressionToken left;
    public ExpressionToken right;
    
}
