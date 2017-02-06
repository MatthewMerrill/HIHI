package com.mattmerr.hihi;

import com.mattmerr.hitch.parsetokens.*;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Created by merrillm on 2/5/17.
 */
public class HProg {
    
    public static void run(ParseNodeBlock program) {
        HScope progScope = new HScope();
        
        progScope.declare("print");
        progScope.put("print",
                new HNativeFunction(HProg::print));
    
        progScope.declare("println");
        progScope.put("println",
                new HNativeFunction(HProg::println));
        
        for (ParseNodeStatement statement : program.getStatementList()) {
            HStatement.run(statement, progScope);
        }
    }
    
    public static HObject println(List<HObject> arguments, HScope scope) {
        print(arguments, scope);
        System.out.println();
        return HObject.UNDEFINED;
    }
    
    public static HObject print(List<HObject> arguments, HScope scope) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        
        for (HObject arg : arguments)
            stringJoiner.add(arg.stringValue(scope).nativeValue());
        
        System.out.print(stringJoiner);
        return HObject.UNDEFINED;
    }
}
