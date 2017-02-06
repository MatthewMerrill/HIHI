package com.mattmerr.hitch.parsetokens;

import com.mattmerr.hihi.HObject;
import com.mattmerr.hihi.HScope;
import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.parsetokens.expression.*;
import com.mattmerr.hitch.tokens.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.BiFunction;

import static com.mattmerr.hitch.tokens.Punctuation.PunctuationType.CLOSE_PARENTHESIS;
import static com.mattmerr.hitch.tokens.Punctuation.PunctuationType.COMMA;

/**
 * Created by merrillm on 1/27/17.
 */
public class ParseNodeExpression extends ParseNode {
    
    public ExpressionToken root;
    
    private ParseNodeExpression(ExpressionToken expressionToken) {
        this.root = expressionToken;
    }
    
    public static <T> List<T> parseDelimited(ParsingScope scope, TokenStream tokenStream,
                                             BiFunction<ParsingScope, TokenStream, T> map,
                                             Punctuation.PunctuationType join,
                                             Punctuation.PunctuationType start,
                                             Punctuation.PunctuationType end) {
        List<T> arguments = new ArrayList<>();
        tokenStream.skipPunctuation(start);
        
        while (tokenStream.peek().type != TokenType.PUNCTUATION
                || ((Punctuation)tokenStream.peek()).value != end) {
            arguments.add(map.apply(scope, tokenStream));
            
            tokenStream.expectingPunctuation(join, end);
            if (((Punctuation) tokenStream.next()).value == end)
                return arguments;
        }
        
        tokenStream.skipPunctuation(end);
        return arguments;
    }
    
    public static ParseNodeExpression parseExpression(ParsingScope scope, TokenStream tokenStream) {
        ExpressionToken ret = null;
    
        if (tokenStream.peek().type == TokenType.STRING) {
            Literal<String> val = new Literal<>();
            val.value = ((Value<String>) tokenStream.next());
            ret = val;
            
        } else if (tokenStream.peek().type == TokenType.IDENTIFIER) {
            ret = new Variable(((Identifier) tokenStream.next()).value);
        }
        
        while (tokenStream.peek().type == TokenType.OPERATOR) {
            if (((Operator)tokenStream.peek()).value == Operator.OperatorType.PLUS) {
                tokenStream.next();
                
                BinaryOperation binop = new BinaryOperation();
                binop.type = Operation.OperationType.ADD;
                binop.left = ret;
                binop.right = parseExpression(scope, tokenStream).root;
                
                ret = binop;
            }
        }
        
        return new ParseNodeExpression(ret);
//        Stack<ExpressionToken> stack = new Stack<>();
//
//        while {
//
//
//        }
    }
    
    public static ParseNodeExpression parseParenthesis(ParsingScope scope, TokenStream tokenStream) {
        return null;
    }
}
