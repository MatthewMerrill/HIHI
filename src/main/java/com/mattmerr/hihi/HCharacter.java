package com.mattmerr.hihi;

/**
 * Created by merrillm on 2/5/17.
 */
public class HCharacter extends HObject {
    
    private Character nativeValue;
    
    public HCharacter(Character nativeValue) {
        this.nativeValue = nativeValue;
    }
    
    public HString stringValue() {
        return new HString(nativeValue+"");
    }
    
    public Character nativeValue() {
        return nativeValue;
    }
}
