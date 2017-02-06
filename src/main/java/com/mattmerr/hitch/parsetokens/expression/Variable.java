package com.mattmerr.hitch.parsetokens.expression;

/**
 * Created by merrillm on 2/5/17.
 */
public class Variable extends ExpressionToken {
    
    public final String qualifiedName;
    
    public Variable(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }
    
}
