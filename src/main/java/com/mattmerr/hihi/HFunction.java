package com.mattmerr.hihi;

import com.mattmerr.hihi.stdlib.HObject;
import com.mattmerr.hitch.parsetokens.ParseNodeCall;
import com.mattmerr.hitch.parsetokens.expression.Call;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by merrillm on 2/5/17.
 */
public abstract class HFunction extends HObject {
    
    public abstract HObject call(HScope scope, List<HObject> arguments);

    public HObject call(HScope scope) {
        return call(scope, Collections.emptyList());
    }
    
    public <T extends HObject> T callExpecting(Class<T> clazz, HScope scope, List<HObject> arguments) {
        HObject res = call(scope, arguments);
    
        if (!(clazz.isInstance(res)))
            throw new RuntimeException("Returned type of "+res+" could not be cast to "+clazz.getTypeName());
        
        return clazz.cast(res);
    }
    
    public <T extends HObject> T callExpecting(Class<T> clazz, HScope scope) {
        return callExpecting(clazz, scope, Collections.emptyList());
    }
    
    
    public static HObject call(Call call, HScope scope) {
        return ((HFunction) ExpressionEvaluator.evaluate(scope, call.variable))
                .call(  scope,
                        call.arguments
                                .stream()
                                .map(exp -> ExpressionEvaluator.evaluate(scope, exp.root))
                                .collect(Collectors.toList())
                );
    }
    public static HObject call(ParseNodeCall call, HScope scope) {
        return ((HFunction)scope.get(call.qualifiedFunction))
                .call(  scope,
                        call.arguments
                                .stream()
                                .map(exp -> ExpressionEvaluator.evaluate(scope, exp.root))
                                .collect(Collectors.toList())
                );
    }
    
}
