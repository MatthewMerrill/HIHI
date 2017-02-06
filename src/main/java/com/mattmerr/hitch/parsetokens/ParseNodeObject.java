package com.mattmerr.hitch.parsetokens;

import com.mattmerr.hitch.TokenStream;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by merrillm on 2/4/17.
 */
public class ParseNodeObject extends ParseNode {
    
    public static final ParseNodeObject UNDEFINED = new ParseNodeObject() {
        @Override
        public ParseNodeObject get(String key) {
            throw new RuntimeException("Cannot access "+key+" of undefined");
        }
    
        public ParseNode parse(ParsingScope scope, TokenStream tokenStream) {
            throw new UnsupportedOperationException();
        }
    };
    
    protected Map<String, ParseNodeObject> attributes = new HashMap<>();
    public ParseNodeObject get(String key) {
        return attributes.get(key);
    }
    
}
