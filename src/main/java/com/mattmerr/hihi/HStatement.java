package com.mattmerr.hihi;

import com.mattmerr.hitch.parsetokens.*;
import com.mattmerr.hitch.parsetokens.expression.Literal;

import java.util.stream.Collectors;

/**
 * Created by merrillm on 2/5/17.
 */
public class HStatement {
    
    private final ParseNodeStatement statement;
    
    public HStatement(ParseNodeStatement definition) {
        this.statement = definition;
    }
    
    public void run(HScope scope) {
        run(this.statement, scope);
    }
    
    public static void run(ParseNodeStatement statement, HScope scope) {
        if (statement instanceof ParseNodeDeclaration) {
            scope.declare(((ParseNodeDeclaration)statement).qualifiedIdentifier);
        }
        else if (statement instanceof ParseNodeAssignment) {
            ParseNodeAssignment assignment = (ParseNodeAssignment) statement;
            scope.put(assignment.identifier, ExpressionEvaluator.evaluate(scope, assignment.value.root));
        }
        else if (statement instanceof ParseNodeFunctionDeclaration) {
            ParseNodeFunctionDeclaration decl = (ParseNodeFunctionDeclaration) statement;
            scope.declare(decl.function.name);
            scope.put(decl.function.name, new HDeclaredFunction(decl.function));
        }
        else if (statement instanceof ParseNodeCall) {
            ParseNodeCall call = (ParseNodeCall) statement;
            ((HFunction)scope.get(call.qualifiedFunction))
                    .call(  scope,
                            call.arguments
                                    .stream()
                                    .map(exp -> ExpressionEvaluator.evaluate(scope, exp.root))
                                    .collect(Collectors.toList())
                    );
        }
        else if (statement instanceof ParseNodeBlock) {
            HProg.run((ParseNodeBlock) statement, new HScope(scope));
        }
    }
}
