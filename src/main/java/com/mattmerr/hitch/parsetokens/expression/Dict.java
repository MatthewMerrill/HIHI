package com.mattmerr.hitch.parsetokens.expression;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.parsetokens.ParseNodeExpression;
import com.mattmerr.hitch.parsetokens.ParsingScope;
import com.mattmerr.hitch.tokens.Identifier;
import com.mattmerr.hitch.tokens.Keyword;
import com.mattmerr.hitch.tokens.Punctuation;
import com.mattmerr.hitch.tokens.Punctuation.PunctuationType;
import java.util.HashMap;
import java.util.Map;

public class Dict extends ExpressionToken {

  public Map<String, ParseNodeExpression> mappings = new HashMap<>();

  public static Dict parseDict(ParsingScope scope, TokenStream tokenStream) {
    Dict dict = new Dict();

    tokenStream.skipPunctuation(PunctuationType.OPEN_BRACKET);
    while (tokenStream.peek() instanceof Identifier || tokenStream.peek() instanceof Keyword) {
      String key;

      if (tokenStream.peek() instanceof Identifier) {
        key = ((Identifier) tokenStream.next()).value;
      }
      else {
        key = ((Keyword) tokenStream.next()).value;
      }

      tokenStream.skipPunctuation(PunctuationType.COLON);

      ParseNodeExpression expression = ParseNodeExpression.parseExpression(scope, tokenStream);
      dict.mappings.put(key, expression);

      tokenStream.expectingPunctuation(PunctuationType.COMMA, PunctuationType.CLOSE_BRACKET);

      if (((Punctuation) tokenStream.peek()).value == PunctuationType.COMMA) {
        tokenStream.skipPunctuation(PunctuationType.COMMA);
      }
    }
    tokenStream.skipPunctuation(PunctuationType.CLOSE_BRACKET);

    return dict;
  }

}