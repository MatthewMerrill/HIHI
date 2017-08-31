package com.mattmerr.hihi.stdlib;

import com.mattmerr.hihi.HFunction;
import com.mattmerr.hihi.HScope;
import com.mattmerr.hihi.stdlib.util.HiplMemberFunction;
import com.mattmerr.hihi.stdlib.util.HiplOperatorOverload;
import com.mattmerr.hitch.parsetokens.expression.Operation;

import java.util.List;

import static com.mattmerr.hitch.parsetokens.expression.Operation.OperationType.ADD;

/**
 * Created by merrillm on 2/5/17.
 */
public class HString extends HList {
    
    public HString(String value) {
        for (char ch : value.toCharArray())
            add(new HCharacter(ch));
    }
    
    @HiplMemberFunction("stringify")
    public HString stringValue(HScope scope, List<HObject> args) {
        return this;
    }
    
    @HiplMemberFunction("reverse")
    public HString reverse(HScope scope, List<HObject> args) {
        return new HString(new StringBuffer(nativeValue()).reverse().toString());
    }
    
    public String nativeValue() {
        StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < length(); i++) {
            builder.append(((HCharacter) get(i)).nativeValue());
        }
        
        return builder.toString();
    }
    
    @HiplMemberFunction(value = "concat")
    @HiplOperatorOverload(value = ADD)
    public HObject concat(HScope scope, List<HObject> args) {
        HString otherStringified = args.get(0)
                .getExpecting(HFunction.class, scope, "stringify")
                .callExpecting(HString.class, scope);
        
        return new HString(nativeValue() + otherStringified.nativeValue());
    }
}
