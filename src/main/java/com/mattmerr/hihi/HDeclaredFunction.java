package com.mattmerr.hihi;

import com.mattmerr.hitch.parsetokens.ParseNodeFunction;
import com.mattmerr.hitch.parsetokens.ParseNodeStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by merrillm on 2/5/17.
 */
public class HDeclaredFunction extends HFunction {
    
    private final List<String> argumentMapping;
    private final HStatement definition;
    
    public HDeclaredFunction(ParseNodeFunction function) {
        this.argumentMapping = function.argumentMapping;
        definition = new HStatement(function.definition);
    }
    
    @Override
    public HObject call(List<HObject> arguments, HScope scope) {
        scope = scope.childScope();
        
        for (String arg : argumentMapping) {
            scope.declare(arg);
        }
        
        for (int i = 0; i < arguments.size(); i++) {
            if (i > argumentMapping.size())
                throw new RuntimeException("Too many arguments!");
            
            scope.put(argumentMapping.get(i), arguments.get(i));
        }
        
        definition.run(scope);
        return HObject.UNDEFINED;
    }
}
