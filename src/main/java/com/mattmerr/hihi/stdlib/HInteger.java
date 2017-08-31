package com.mattmerr.hihi.stdlib;

import com.mattmerr.hihi.HScope;
import com.mattmerr.hihi.stdlib.util.HiplMemberFunction;
import com.mattmerr.hihi.stdlib.util.HiplOperatorOverload;
import com.mattmerr.hitch.parsetokens.expression.Operation;

import java.util.List;

import static com.mattmerr.hitch.parsetokens.expression.Operation.OperationType.*;

/**
 * Created by merrillm on 3/6/17.
 */
public class HInteger extends HObject {
    
    private Integer nativeValue;
    
    public HInteger(Integer nativeValue) {
        this.nativeValue = nativeValue;
    }
    
    @HiplMemberFunction("stringify")
    public HString stringValue(HScope scope, List<HObject> args) {
        return new HString(nativeValue()+"");
    }
    
    public Integer nativeValue() {
        return nativeValue;
    }
    
    @HiplMemberFunction(value = "add")
    @HiplOperatorOverload(value = ADD, otherType = { HInteger.class, HString.class })
    public HObject add(HScope scope, List<HObject> args) {
        HObject otherObj = args.get(0);
        
        if (otherObj instanceof HInteger)
            return new HInteger(nativeValue() + ((HInteger) otherObj).nativeValue());
    
        if (otherObj instanceof HString)
            return new HString(nativeValue() + ((HString) otherObj).nativeValue());
        
        throw new IllegalArgumentException("Bad right half");
    }
    
    @HiplMemberFunction(value = "subtract")
    @HiplOperatorOverload(value = SUBTRACT, otherType = HInteger.class)
    public HObject sub(HScope scope, List<HObject> args) {
        HObject otherObj = args.get(0);
        
        if (otherObj instanceof HInteger)
            return new HInteger(nativeValue() - ((HInteger) otherObj).nativeValue());
    
        throw new IllegalArgumentException("Bad right half");
    }
    
    @HiplMemberFunction(value = "multiply")
    @HiplOperatorOverload(value = MULTIPLY, otherType = HInteger.class)
    public HObject mul(HScope scope, List<HObject> args) {
        HObject otherObj = args.get(0);
        
        if (otherObj instanceof HInteger)
            return new HInteger(nativeValue() * ((HInteger) otherObj).nativeValue());
    
        throw new IllegalArgumentException("Bad right half");
    }
    
    @HiplMemberFunction(value = "divide")
    @HiplOperatorOverload(value = DIVIDE, otherType = HInteger.class)
    public HObject divide(HScope scope, List<HObject> args) {
        HObject otherObj = args.get(0);
        
        if (otherObj instanceof HInteger)
            return new HInteger(nativeValue() / ((HInteger) otherObj).nativeValue());
    
        throw new IllegalArgumentException("Bad right half");
    }
    
    @HiplMemberFunction(value = "modulus")
    @HiplOperatorOverload(value = MODULUS, otherType = HInteger.class)
    public HObject mod(HScope scope, List<HObject> args) {
        HObject otherObj = args.get(0);
        
        Integer a = nativeValue();
        
        if (otherObj instanceof HInteger) {
            Integer b = ((HInteger) otherObj).nativeValue();
            return new HInteger((a % b + b) % b);
        }
    
        throw new IllegalArgumentException("Bad right half");
    }
}
