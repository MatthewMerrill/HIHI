package com.mattmerr.hitch;

import com.mattmerr.hitch.parsetokens.ParseNodeBlock;
import com.mattmerr.hitch.parsetokens.ParseNodeStatement;
import com.mattmerr.hitch.parsetokens.ParsingScope;

/**
 * Created by merrillm on 11/16/16.
 */
public class TokenParser {
    
    private TokenStream tokenStream;
    private ParseNodeBlock result = null;
    
    public TokenParser(TokenStream tokenStream) {
        this.tokenStream = tokenStream;
    }
    
    public ParseNodeBlock parse() {
        return parse(new ParsingScope());
    }
    
    public ParseNodeBlock parse(ParsingScope scope) {
        if (result != null) return result;
        
        ParseNodeBlock program = new ParseNodeBlock();
        
        while (!tokenStream.eof()) {
            
            program.addStatement(ParseNodeStatement.parse(scope, tokenStream));
        }
        
        return program;
    }
    
}
