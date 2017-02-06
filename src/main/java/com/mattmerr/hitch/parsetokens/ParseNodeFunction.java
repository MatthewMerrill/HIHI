package com.mattmerr.hitch.parsetokens;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.tokens.Identifier;
import com.mattmerr.hitch.tokens.Keyword;
import com.mattmerr.hitch.tokens.Punctuation;
import com.mattmerr.hitch.tokens.TokenType;

import java.util.ArrayList;
import java.util.List;

import static com.mattmerr.hitch.tokens.Punctuation.PunctuationType.CLOSE_PARENTHESIS;
import static com.mattmerr.hitch.tokens.Punctuation.PunctuationType.COMMA;
import static com.mattmerr.hitch.tokens.Punctuation.PunctuationType.OPEN_PARENTHESIS;

/**
 * Created by merrillm on 1/27/17.
 */
public class ParseNodeFunction extends ParseNodeObject {
    
    public String name = null;
    public List<String> argumentMapping;
    public ParseNodeStatement definition;
    
    public static List<ParseNodeExpression> parseCallArguments(ParsingScope scope, TokenStream tokenStream) {
        return ParseNodeExpression.parseDelimited(
                scope, tokenStream, ParseNodeExpression::parseExpression,
                COMMA, OPEN_PARENTHESIS, CLOSE_PARENTHESIS
        );
    }
    public static List<String> parseArgumentDeclarations(ParsingScope scope, TokenStream tokenStream) {
        return ParseNodeExpression.parseDelimited(
                scope, tokenStream, (s, t) -> ((Identifier) t.next()).value,
                COMMA, OPEN_PARENTHESIS, CLOSE_PARENTHESIS
        );
    }
    
    public static ParseNodeFunction parseFunction(ParsingScope scope, TokenStream tokenStream) {
        if (tokenStream.peek().type == TokenType.KEYWORD
                && ((Keyword)tokenStream.peek()).value.equals("func")) {
            tokenStream.next();
            
            ParseNodeFunction function = new ParseNodeFunction();
            
            if (tokenStream.peek().type == TokenType.IDENTIFIER) {
                String functionName = ((Identifier)tokenStream.next()).value;
    
                scope.declare(functionName);
                scope.put(functionName, function);
                
                function.name = functionName;
            }
            
            function.argumentMapping = parseArgumentDeclarations(scope, tokenStream);
            
            ParsingScope innerScope = scope.childScope();
            
            for (String arg : function.argumentMapping)
                innerScope.declare(arg);
            
            function.definition = ParseNodeStatement.parse(innerScope, tokenStream);
            
            return function;
        }
        
        throw tokenStream.parseException("Expected keyword 'func'");
    }
    
}
