package com.mattmerr.hihi.stdlib;

import com.mattmerr.hihi.HScope;
import com.mattmerr.hihi.stdlib.util.HiplMemberFunction;
import com.mattmerr.hihi.stdlib.util.HiplOperatorOverload;

import java.util.List;

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
    
    @HiplMemberFunction(value = "concat")
    @HiplOperatorOverload(value = "+")
    public HObject concat(HScope scope, List<HObject> args) {
        HObject otherObj = args.get(0);
        
        if (otherObj instanceof HInteger)
            return new HInteger(nativeValue() + ((HInteger) otherObj).nativeValue());
    
        if (otherObj instanceof HString)
            return new HString(nativeValue() + ((HString) otherObj).nativeValue());
        
        return null;
    }
}
