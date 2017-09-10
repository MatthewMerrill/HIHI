package com.mattmerr.hitch.parsetokens;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.tokens.Keyword;
import com.mattmerr.hitch.tokens.Punctuation.PunctuationType;
import com.mattmerr.hitch.tokens.Token;
import com.mattmerr.hitch.tokens.TokenType;

public class ParseNodeIfStatement extends ParseNodeStatement {

  public ParseNodeExpression conditionExpression;
  public ParseNodeStatement ifTrueStatement;
  public ParseNodeStatement ifFalseStatement;

  public static ParseNodeIfStatement parse(ParsingScope scope, TokenStream tokenStream) {
    ParseNodeIfStatement statement = new ParseNodeIfStatement();
    tokenStream.next();

    tokenStream.skipPunctuation(PunctuationType.OPEN_PARENTHESIS);
    statement.conditionExpression = ParseNodeExpression.parseExpression(scope, tokenStream);
    tokenStream.skipPunctuation(PunctuationType.CLOSE_PARENTHESIS);

    statement.ifTrueStatement = ParseNodeStatement.parse(scope, tokenStream);

    Token tok = tokenStream.peek();
    if (tok != null && tok.type == TokenType.KEYWORD && "else".equals(((Keyword)tok).value)) {
      tokenStream.next();
      statement.ifFalseStatement = ParseNodeStatement.parse(scope, tokenStream);
    }

    return statement;
  }
}
