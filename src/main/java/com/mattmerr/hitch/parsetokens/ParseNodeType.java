package com.mattmerr.hitch.parsetokens;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.tokens.Identifier;
import com.mattmerr.hitch.tokens.Keyword;
import com.mattmerr.hitch.tokens.Token;

public class ParseNodeType {

  public String typeName = null;
  public ParseNodeTopLevelBlock block;

  public static ParseNodeType parse(ParsingScope scope, TokenStream tokenStream) {
    Token typeKw = tokenStream.next();
    if (!(typeKw instanceof Keyword) || !"type".equals(((Keyword) typeKw).value)) {
      throw tokenStream.parseException("Expected keyword \"type\", found \"" + typeKw + "\"");
    }

    ParseNodeType type = new ParseNodeType();

    Token nameId = tokenStream.next();
    if (!(nameId instanceof Identifier)) {
      throw tokenStream.parseException("Expected identifier, found \"" + nameId + "\"");
    }
    type.typeName = ((Identifier) nameId).value;
    type.block = ParseNodeTopLevelBlock.parse(scope, tokenStream);

    return type;
  }

}
