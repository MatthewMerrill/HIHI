package com.mattmerr.hihi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

/**
 * Created by merrillm on 2/5/17.
 */
public class HObject {
    
    protected Map<String, HObject> attributes = new HashMap<>();
    
    public HObject get(HScope scope, String identifier) {
        return attributes.get(identifier);
    }
    
    public HString stringValue(HScope scope) {
        if (attributes.containsKey("stringify")) {
            return (HString) ((HFunction)attributes.get("stringify")).call(emptyList(), scope);
        }
        
        return new HString("[Object]");
    }
    
    public static final HObject UNDEFINED = new HObject(){
        
        @Override
        public HString stringValue(HScope scope) {
            return new HString("UNDEFINED");
        }
        
        @Override
        public String toString() {
            return "UNDEFINED";
        }
        
        @Override
        public HObject get(HScope scope, String identifier) {
            throw new RuntimeException(format("Cannot get %s of undefined.", identifier));
        }
    };
}
