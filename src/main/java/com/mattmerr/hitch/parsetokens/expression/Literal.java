package com.mattmerr.hitch.parsetokens.expression;

import com.mattmerr.hitch.tokens.Value;

/**
 * Created by merrillm on 2/5/17.
 */
public class Literal<T> extends ExpressionToken {
    
    public Value<T> value;
    
    public Literal() {
        
    }
    
    public Literal(Value<T> value) {
        this.value = value;
    }
}
