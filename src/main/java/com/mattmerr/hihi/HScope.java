package com.mattmerr.hihi;

import com.mattmerr.hitch.tokens.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by merrillm on 2/5/17.
 */
public class HScope {
    
    private HScope parent = null;
    
    private Map<String, HObject> valueMap = new HashMap<>();
//    private Map<String, HType> typeMap = new HashMap<>();
    
    public HScope() {}
    public HScope(HScope parentScope) {
        this.parent = parentScope;
    }
    
    public HScope childScope() {
        return new HScope(this);
    }
    
    public HObject get(String identifier) {
        HScope curScope = this;
        
        while (curScope != null) {
            if (curScope.valueMap.containsKey(identifier))
                return curScope.valueMap.get(identifier);
            
            curScope = curScope.parent;
        }
        
        return HObject.UNDEFINED;
    }
    
    
    public void declare(String identifier) {
        valueMap.put(identifier, HObject.UNDEFINED);
    }
    
    public void put(String identifier, HObject value) {
        HScope curScope = this;
        
        while (curScope != null) {
            if (curScope.valueMap.containsKey(identifier)) {
                curScope.valueMap.put(identifier, value);
                return;
            }
            
            curScope = curScope.parent;
        }
        
        throw new RuntimeException("Unknown value to set: " + identifier);
    }
}
