package com.mattmerr.hitch.parsetokens;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.tokens.Punctuation.PunctuationType;

public class ParseNodeExpressionStatement extends ParseNodeStatement {

  public final ParseNodeExpression expression;

  private ParseNodeExpressionStatement(ParseNodeExpression expression) {
    this.expression = expression;
  }

  public static ParseNodeExpressionStatement parse(ParsingScope scope, TokenStream tokenStream) {
    ParseNodeExpression expression = ParseNodeExpression.parseExpression(scope, tokenStream);
    tokenStream.skipPunctuation(PunctuationType.SEMICOLON);
    return new ParseNodeExpressionStatement(expression);
  }

}
