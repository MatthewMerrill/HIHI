package com.mattmerr.hihi;

import com.mattmerr.hitch.parsetokens.ParseNodeExpression;
import com.mattmerr.hitch.parsetokens.expression.*;
import com.mattmerr.hitch.tokens.Value;

import java.util.Collections;
import java.util.Stack;

import static java.util.Collections.singletonList;

/**
 * Created by merrillm on 2/5/17.
 */
public class ExpressionEvaluator {
    
    public static HObject evaluate(HScope scope, ExpressionToken expr) {
        if (expr instanceof Literal<?>) {
            Object val = ((Literal)expr).value.value;
            
            if (val instanceof String)
                return new HString((String) val);
    
            if (val instanceof Character)
                return new HCharacter((Character) val);
        }
        
        if (expr instanceof Variable) {
            return scope.get(((Variable) expr).qualifiedName);
        }
        
        if (expr instanceof BinaryOperation) {
            BinaryOperation binop = (BinaryOperation) expr;
            HObject leftVal = evaluate(scope, binop.left);
            HObject rightVal = evaluate(scope, binop.right);
            
            if (binop.type == Operation.OperationType.ADD) {
                if (leftVal.attributes.containsKey("+")) {
                    HObject result = ((HFunction) leftVal.get(scope, "+"))
                            .call(singletonList(rightVal), scope);
                    
                    return result;
                } else {
                    throw new RuntimeException("+ is not defined for " + leftVal);
                }
            }
        }
        
        throw new RuntimeException("Unsupported Expression " + expr);
    }
    
    
}
