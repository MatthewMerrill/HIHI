package com.mattmerr.hitch.parsetokens;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.tokens.Punctuation;
import com.mattmerr.hitch.tokens.TokenType;

import java.util.List;

import static com.mattmerr.hitch.tokens.Punctuation.PunctuationType.CLOSE_PARENTHESIS;
import static com.mattmerr.hitch.tokens.Punctuation.PunctuationType.SEMICOLON;

/**
 * Created by merrillm on 1/27/17.
 */
public class ParseNodeCall extends ParseNodeStatement {
    
    public String qualifiedFunction;
    public List<ParseNodeExpression> arguments;
    
    public static ParseNodeCall parse(ParseNodeObject obj, String qualified,
                                      ParsingScope scope, TokenStream tokenStream) {
        ParseNodeCall call = new ParseNodeCall();
    
//        if (obj instanceof ParseNodeFunction) {
            call.qualifiedFunction = qualified;
//        }

        call.arguments = ParseNodeFunction.parseCallArguments(scope, tokenStream);
        tokenStream.skipPunctuation(SEMICOLON);
        
        return call;
    }
    
}
