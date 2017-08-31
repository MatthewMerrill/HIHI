package com.mattmerr.hitch.parsetokens;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.parsetokens.expression.ExpressionToken;
import com.mattmerr.hitch.tokens.Punctuation;
import com.mattmerr.hitch.tokens.TokenType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by merrillm on 2/5/17.
 */
public class ParseNodeBlock extends ParseNodeStatement {
    
    private List<ParseNodeStatement> statementList = new ArrayList<>();
    public ExpressionToken returnExpression;
    
    public void addStatement(ParseNodeStatement statement) {
        statementList.add(statement);
    }
    
    public List<ParseNodeStatement> getStatementList() {
        return Collections.unmodifiableList(statementList);
    }
    
    public static ParseNodeStatement parse(ParsingScope scope, TokenStream tokenStream) {
        tokenStream.skipPunctuation(Punctuation.PunctuationType.OPEN_BRACKET);
        
        ParseNodeBlock block = new ParseNodeBlock();
        
        while (tokenStream.peek().type != TokenType.PUNCTUATION
                || ((Punctuation)tokenStream.peek()).value != Punctuation.PunctuationType.CLOSE_BRACKET) {
            
            block.statementList.add(ParseNodeStatement.parse(scope, tokenStream));
        }
        
        tokenStream.skipPunctuation(Punctuation.PunctuationType.CLOSE_BRACKET);
        return block;
    }
}
