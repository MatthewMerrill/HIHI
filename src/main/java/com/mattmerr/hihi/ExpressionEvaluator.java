package com.mattmerr.hihi;

import com.mattmerr.hihi.stdlib.HInteger;
import com.mattmerr.hihi.stdlib.HObject;
import com.mattmerr.hihi.stdlib.HString;
import com.mattmerr.hihi.stdlib.HCharacter;
import com.mattmerr.hitch.parsetokens.ParseNodeCall;
import com.mattmerr.hitch.parsetokens.expression.*;

import static java.util.Collections.singletonList;

/**
 * Created by merrillm on 2/5/17.
 */
public class ExpressionEvaluator {
    
    public static HObject evaluate(HScope scope, ExpressionToken expr) {
        if (expr instanceof Literal) {
            Object val = ((Literal)expr).value.value;
            
            if (val instanceof String)
                return new HString((String) val);
    
            if (val instanceof Character)
                return new HCharacter((Character) val);
            
            if (val instanceof Integer)
                return new HInteger((Integer) val);
        }
        
        else if (expr instanceof Variable) {
            return scope.get(((Variable) expr).qualifiedName);
        }
        
        else if (expr instanceof BinaryOperation) {
            BinaryOperation binop = (BinaryOperation) expr;
            HObject leftVal = evaluate(scope, binop.left);
            HObject rightVal = evaluate(scope, binop.right);
            
//            if (binop.type ) {
                if (leftVal.get(scope, "_"+binop.type.name()) != HObject.UNDEFINED) {
                    HObject result = leftVal.getExpecting(HFunction.class, scope, "_"+binop.type.name())
                            .call(scope, singletonList(rightVal));
                    return result;
                } else {
                    throw new RuntimeException("_"+binop.type.name()+" is not defined for " + leftVal);
                }
//            }
        }
        
        else if (expr instanceof Call) {
            return HFunction.call((Call)expr, scope);
        }
        
        throw new RuntimeException("Unsupported Expression " + expr);
    }
    
    
}
