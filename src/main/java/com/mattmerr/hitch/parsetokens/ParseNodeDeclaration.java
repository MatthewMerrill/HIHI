package com.mattmerr.hitch.parsetokens;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.tokens.Identifier;
import com.mattmerr.hitch.tokens.Keyword;
import com.mattmerr.hitch.tokens.Punctuation.PunctuationType;
import com.mattmerr.hitch.tokens.Token;

/**
 * Created by merrillm on 2/4/17.
 */
public class ParseNodeDeclaration extends ParseNodeStatement {
    public String type;
    public String qualifiedIdentifier;

    public static ParseNodeDeclaration parseTopLevelDeclaration(ParsingScope scope, TokenStream tokenStream) {
        Token varKw = tokenStream.next();
        if (!(varKw instanceof Keyword) || !"var".equals(((Keyword) varKw).value)) {
            throw tokenStream.parseException("Expected keyword \"var\", found \"" + varKw + "\"");
        }

        ParseNodeTypingParameter typingParameter = ParseNodeTypingParameter.parse(scope, tokenStream);
        if (typingParameter.getTypings().size() != 1) {
            throw tokenStream
                .parseException("I only know how to make type member variables with one type!");
        }
        Token idTok = tokenStream.next();
        if (!(idTok instanceof Identifier)) {
            throw tokenStream.parseException("Expected identifier, found \"" + idTok + "\"");
        }

        tokenStream.skipPunctuation(PunctuationType.SEMICOLON);

        ParseNodeDeclaration declaration = new ParseNodeDeclaration();
        declaration.type = typingParameter.getTypings().get(0).getValue();
        declaration.qualifiedIdentifier = ((Identifier) idTok).value;

        return declaration;
    }

}
