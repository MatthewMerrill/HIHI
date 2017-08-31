package com.mattmerr.hitch.parsetokens;

/**
 * Created by merrillm on 4/26/17.
 */
public class ParseNodeReturnStatement extends ParseNodeStatement {
    
    public ParseNodeExpression value;
    
    public ParseNodeReturnStatement(ParseNodeExpression expression) {
        this.value = expression;
    }
}
