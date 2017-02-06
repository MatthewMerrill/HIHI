package com.mattmerr.hitch;

import com.mattmerr.hitch.parsetokens.ParseNodeExpression;
import com.mattmerr.hitch.tokens.Identifier;
import com.mattmerr.hitch.tokens.Token;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by merrillm on 11/16/16.
 */
public class VisitSink {
    
    private TokenStream tokenStream;
    private ArrayList<Token> buff;
    
    public VisitSink(String string) {
        tokenStream = new TokenStream(string);
    }
    
    public VisitSink(InputStream is) {
        tokenStream = new TokenStream(is);
    }
    
    public void process() {
        List<ParseNodeExpression> expressionList = new ArrayList<>();
        
        while (!tokenStream.eof()) {
            
        }
    }
    public void processInstruction() {
        
    }
    public void processIdentifier() {
        Identifier identifierToken = (Identifier) tokenStream.next();
        Token nextToken = tokenStream.peek();
        
        if (isType(identifierToken)) {
            
        }
        
    }
    
    private boolean isType(Identifier token) {
        return Arrays.asList(
                "int",
                "bool"
        ).contains(token.value);
    }
    
}
