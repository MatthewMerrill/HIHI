package com.mattmerr.hitch.parsetokens;

import com.mattmerr.hihi.HObject;
import com.mattmerr.hihi.HScope;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by merrillm on 2/4/17.
 */
public class ParsingScope {
    
    private Map<String, ParseNodeObject> map = new HashMap<>();
    
    private ParsingScope parent = null;
    
    public ParsingScope() {
        map.put("print", new ParseNodeFunction());
        map.put("println", new ParseNodeFunction());
    }
    
    public ParsingScope(ParsingScope parent) {
        map.put("print", new ParseNodeFunction());
        map.put("println", new ParseNodeFunction());
        this.parent = parent;
    }
    
    public ParseNodeObject get(String identifier) {
        ParsingScope curScope = this;
        
        while (curScope != null) {
            if (curScope.map.containsKey(identifier))
                return curScope.map.get(identifier);
            
            curScope = curScope.parent;
        }
        
        return ParseNodeObject.UNDEFINED;
    }
    
    
    public void declare(String identifier) {
        map.put(identifier, ParseNodeObject.UNDEFINED);
    }
    
    public void put(String identifier, ParseNodeObject value) {
        ParsingScope curScope = this;
        
        while (curScope != null) {
            if (curScope.map.containsKey(identifier)) {
                curScope.map.put(identifier, value);
                return;
            }
            
            curScope = curScope.parent;
        }
        
        throw new RuntimeException("Unknown value to set: " + identifier);
    }
    
    public ParsingScope childScope() {
        return new ParsingScope(this);
    }
    
}
