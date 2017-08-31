package com.mattmerr.hacc;

import com.mattmerr.hitch.parsetokens.*;
import com.mattmerr.hitch.parsetokens.expression.BinaryOperation;
import com.mattmerr.hitch.parsetokens.expression.ExpressionToken;
import com.mattmerr.hitch.parsetokens.expression.Literal;
import com.mattmerr.hitch.parsetokens.expression.Variable;

import java.io.PrintStream;
import java.util.StringJoiner;

/**
 * Created by merrillm on 3/18/17.
 */
public class ProgSink extends PrintStream {
    
    private int indentation = 0;
    
    public ProgSink(PrintStream out) {
        super(out);
    }
    
    public void writeNoBrackets(ParseNodeBlock block) {
        for (ParseNodeStatement statement : block.getStatementList()) {
            write(statement);
        }
    }
    
    public void write(ParseNodeBlock block) {
        println("{");
        writeNoBrackets(block);
        println("}");
    }
    
    public void write(ParseNodeStatement statement) {
        if (statement instanceof ParseNodeCall) {
            print(((ParseNodeCall) statement).qualifiedFunction);
            print("(");
            
            boolean first = true;
            for (ParseNodeExpression exp : ((ParseNodeCall) statement).arguments) {
                if (first) {
                    first = false;
                } else {
                    print(", ");
                }
                write(exp);
            }
            
            println(");");
        } else if (statement instanceof ParseNodeFunctionDeclaration) {
            ParseNodeFunction function = ((ParseNodeFunctionDeclaration) statement).function;
            
            print("hobject ");
            print(function.name);
            
            StringJoiner sj = new StringJoiner(", ");
            for (String arg : function.argumentMapping) {
                sj.add("hobject " + arg);
            }
            
            print("(" + sj + ")\n");
            
            if (function.definition instanceof ParseNodeBlock) {
                print(function.definition);
            } else {
                println("{");
                indentation += 1;
                
                write(function.definition);
                
                indentation -= 1;
                println("}\n");
            }
        }
    }
    
    public void write(ParseNodeExpression expression) {
        write(expression.root);
    }
    
    public void write(ExpressionToken root) {
        if (root instanceof BinaryOperation) {
            BinaryOperation binop = (BinaryOperation) root;
            print("(");
            write(binop.left);
            
            switch (binop.type) {
                case ADD: print("+"); break;
                case SUBTRACT: print("-"); break;
                case MULTIPLY: print("*"); break;
                case DIVIDE: print("/"); break;
            }
            
            write(binop.right);
            print(")");
        } else if (root instanceof Literal){
            Literal literal = (Literal) root;
            
            if (literal.value.value instanceof String) {
                print("\"" + literal.value + "\"");
            } else {
                print(literal.value.toString());
            }
        } else if (root instanceof Variable) {
            Variable var = (Variable) root;
            print(var.qualifiedName);
        }
    }
    
}
