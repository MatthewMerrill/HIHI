package com.mattmerr.hitch.parsetokens;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.tokens.Keyword;
import com.mattmerr.hitch.tokens.Punctuation.PunctuationType;
import com.mattmerr.hitch.tokens.Token;

public class ParseNodeWhileStatement extends ParseNodeStatement {

  public ParseNodeStatement statement;
  public ParseNodeExpression conditionExpression;

  public static ParseNodeWhileStatement parse(ParsingScope scope, TokenStream tokenStream) {
    Token token = tokenStream.next();

    if (token instanceof Keyword && "while".equals(((Keyword)token).value)) {
      ParseNodeWhileStatement whileStatement = new ParseNodeWhileStatement();

      tokenStream.skipPunctuation(PunctuationType.OPEN_PARENTHESIS);
      whileStatement.conditionExpression = ParseNodeExpression.parseExpression(scope, tokenStream);
      tokenStream.skipPunctuation(PunctuationType.CLOSE_PARENTHESIS);

      whileStatement.statement = ParseNodeStatement.parse(scope, tokenStream);
      return whileStatement;
    }
    else {
      throw tokenStream.parseException("Expected \"while\"");
    }
  }

}
