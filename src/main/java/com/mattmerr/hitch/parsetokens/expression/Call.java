package com.mattmerr.hitch.parsetokens.expression;

import com.mattmerr.hitch.parsetokens.ParseNodeExpression;
import java.util.ArrayList;
import java.util.List;

import static com.mattmerr.hitch.parsetokens.expression.Operation.OperationType.CALL;

/**
 * Created by merrillm on 2/10/17.
 */
public class Call extends Operation {
    
    public ExpressionToken variable = null;
    public List<ParseNodeExpression> arguments = null;
    
    public Call() {
        super.type = CALL;
    }
    
}
