package com.mattmerr.hitch.parsetokens;

import static com.mattmerr.hitch.parsetokens.ParseNodeDeclaration.parseTopLevelDeclaration;
import static com.mattmerr.hitch.parsetokens.ParseNodeFunction.parseFunction;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.tokens.Keyword;
import com.mattmerr.hitch.tokens.Punctuation.PunctuationType;
import com.mattmerr.hitch.tokens.Token;
import java.util.ArrayList;
import java.util.Collection;

public class ParseNodeTopLevelBlock {

  public Collection<ParseNodeType> typeDefinitions = new ArrayList<>();
  public Collection<ParseNodeDeclaration> variableDeclarations = new ArrayList<>();
  public Collection<ParseNodeFunction> functions = new ArrayList<>();

  public static ParseNodeTopLevelBlock parse(ParsingScope parsingScope, TokenStream tokenStream) {
    return parse(parsingScope, tokenStream, true);
  }

  public static ParseNodeTopLevelBlock parse(ParsingScope parsingScope, TokenStream tokenStream,
      boolean brackets) {
    ParseNodeTopLevelBlock block = new ParseNodeTopLevelBlock();

    if (brackets) {
      tokenStream.skipPunctuation(PunctuationType.OPEN_BRACKET);
    }

    Token tok;
    while ((tok = tokenStream.peek()) != null) {
      if (tok instanceof Keyword) {
        if ("var".equals(((Keyword) tok).value)) {
          block.variableDeclarations.add(parseTopLevelDeclaration(parsingScope, tokenStream));
          continue;
        }
        else if ("func".equals(((Keyword) tok).value)) {
          block.functions.add(parseFunction(parsingScope, tokenStream));
          continue;
        }
        else if ("type".equals(((Keyword) tok).value)) {
          block.typeDefinitions.add(ParseNodeType.parse(parsingScope, tokenStream));
          continue;
        }
      }
      break;
    }

    if (brackets) {
      tokenStream.skipPunctuation(PunctuationType.CLOSE_BRACKET);
    }

    return block;
  }

}
