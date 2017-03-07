package com.mattmerr.hitch.parsetokens.expression;

/**
 * Created by merrillm on 2/5/17.
 */
public class BinaryOperation extends Operation {
    
    public ExpressionToken left;
    public ExpressionToken right;
    
    public BinaryOperation(){}
    public BinaryOperation(Operation.OperationType type){
        this.type = type;
    }
    
}
