package com.mattmerr.hihi.stdlib;

import com.mattmerr.hihi.HScope;
import com.mattmerr.hihi.stdlib.util.HiplMemberFunction;

import java.util.List;

/**
 * Created by merrillm on 2/5/17.
 */
public class HCharacter extends HObject {
    
    private Character nativeValue;
    
    public HCharacter(Character nativeValue) {
        this.nativeValue = nativeValue;
    }
    
    
    
    @HiplMemberFunction("stringify")
    public HString stringValue(HScope scope, List<HObject> args) {
        return new HString(nativeValue+"");
    }
    
    public Character nativeValue() {
        return nativeValue;
    }
}
