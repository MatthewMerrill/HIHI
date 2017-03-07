package com.mattmerr.hihi;

import com.mattmerr.hihi.stdlib.HObject;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Created by merrillm on 2/5/17.
 */
public class HNativeFunction extends HFunction {
    
    private BiFunction<HScope, List<HObject>, HObject> nativeFunction;
    
    public HNativeFunction(BiFunction<HScope, List<HObject>, HObject> nativeFunction) {
        this.nativeFunction = nativeFunction;
    }
    
    @Override
    public HObject call(HScope scope, List<HObject> arguments) {
        return nativeFunction.apply(scope, arguments);
    }
}
