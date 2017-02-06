package com.mattmerr.hitch.parsetokens;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.tokens.Identifier;
import com.mattmerr.hitch.tokens.Keyword;
import com.mattmerr.hitch.tokens.Punctuation;
import com.mattmerr.hitch.tokens.TokenType;

import java.security.Key;

import static com.mattmerr.hitch.tokens.Punctuation.PunctuationType.*;

/**
 * Created by merrillm on 1/27/17.
 */
public abstract class ParseNodeStatement extends ParseNode {
    
    public static ParseNodeStatement parse(ParsingScope scope, TokenStream tokenStream) {
        TokenType peekType = tokenStream.peek().type;
        
        if (peekType == TokenType.IDENTIFIER) {
            Identifier identifier = (Identifier) tokenStream.next();
            
            String qualified = identifier.value;
            ParseNodeObject obj = scope.get(identifier.value);
            
            while (tokenStream.peek().type == TokenType.PUNCTUATION
                    && ((Punctuation) tokenStream.peek()).value == DOT) {
                tokenStream.next();
                
                if (tokenStream.peek().type != TokenType.IDENTIFIER)
                    throw tokenStream.parseException("Expected identifier");
                
                Identifier nextId = (Identifier) (tokenStream.next());
                qualified += "." + nextId.value;
                obj = obj.get(nextId.value);
            }
            
            if (tokenStream.peek().type == TokenType.PUNCTUATION) {
                Punctuation punc = (Punctuation) tokenStream.peek();
                
                if (punc.value == OPEN_PARENTHESIS) {
                    return ParseNodeCall.parse(obj, qualified, scope, tokenStream);
                }
                
                throw tokenStream.parseException("Unexpected punctuation");
            }
        }
        
        if (peekType == TokenType.KEYWORD) {
            Keyword keyword = (Keyword) tokenStream.peek();
            
            if (keyword.value.equals("func")) {
                ParseNodeFunctionDeclaration decl = new ParseNodeFunctionDeclaration();
                decl.function = ParseNodeFunction.parseFunction(scope, tokenStream);
                return decl;
            }
            
            
        }

        throw tokenStream.parseException("Couldn't parse statement");
    }
    
}
