package com.mattmerr.hihi;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Created by merrillm on 2/5/17.
 */
public class HNativeFunction extends HFunction {
    
    private BiFunction<List<HObject>, HScope, HObject> nativeFunction;
    
    public HNativeFunction(BiFunction<List<HObject>, HScope, HObject> nativeFunction) {
        this.nativeFunction = nativeFunction;
    }
    
    @Override
    public HObject call(List<HObject> arguments, HScope scope) {
        return nativeFunction.apply(arguments, scope);
    }
}
