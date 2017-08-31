package com.mattmerr.hihi;

import com.mattmerr.hihi.stdlib.HObject;
import com.mattmerr.hitch.parsetokens.*;

import java.net.URI;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by merrillm on 2/5/17.
 */
public class HProg {
    
    public static HObject run(ParseNodeBlock program) {
        return run(program, new HScope());
    }
    
    public static HObject run(ParseNodeBlock program, HScope progScope) {
        for (ParseNodeStatement statement : program.getStatementList()) {
            HStatement.run(statement, progScope);
        }
        if (program.returnExpression != null)
            return ExpressionEvaluator.evaluate(progScope, program.returnExpression);
        
        return HObject.UNDEFINED;
    }
    
    public static HObject println(HScope scope, List<HObject> arguments) {
        print(scope, arguments);
        System.out.println();
        return HObject.UNDEFINED;
    }
    
    public static HObject print(HScope scope, List<HObject> arguments) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        
        for (HObject arg : arguments)
            stringJoiner.add(arg.stringValue(scope, arguments).nativeValue());
        
        System.out.print(stringJoiner);
        return HObject.UNDEFINED;
    }
}
