package com.mattmerr.hihi;

import java.util.List;

/**
 * Created by merrillm on 2/5/17.
 */
public abstract class HFunction extends HObject {
    
    public abstract HObject call(List<HObject> arguments, HScope scope);

    public static void call(HObject function, List<HObject> arguments, HScope scope) {
        if (!(function instanceof HFunction))
            throw new RuntimeException(function + " is not a function!");
        
        
    }
    
}
