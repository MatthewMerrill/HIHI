package com.mattmerr.hitch.parsetokens;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.tokens.Identifier;
import com.mattmerr.hitch.tokens.Keyword;
import com.mattmerr.hitch.tokens.TokenType;

/**
 * Created by merrillm on 1/27/17.
 */
public class ParseNodeVariable extends ParseNodeObject {
    
    ParseNodeClass type;
    String identifier;
    
    public static ParseNodeVariable parseArgumentDeclaration(ParsingScope scope, TokenStream tokenStream) {
        if (tokenStream.peek().type == TokenType.IDENTIFIER) {
            Identifier identifier = ((Identifier) tokenStream.next());
            
//            if (tokenStream.peek().type == TokenType.IDENTIFIER) {
//            }
            
        }
//                && (tokenStream.peek().type != TokenType.KEYWORD
//                    || !((Keyword) tokenStream.peek()).value.equals("var"))) {
//            throw tokenStream.parseException("Expecting identifier");
//        }
//        if (scope.isClass(identifier))
        
        // TODO
        return null;
    }
}
