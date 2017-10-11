package com.mattmerr.hitch.parsetokens;

import static com.mattmerr.hitch.parsetokens.ParseNodeDeclaration.parseTopLevelDeclaration;
import static com.mattmerr.hitch.parsetokens.ParseNodeFunction.parseFunction;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.tokens.Keyword;
import com.mattmerr.hitch.tokens.Punctuation;
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
        else if ((((Keyword) tok).value).endsWith("func")) {
          block.functions.add(parseFunction(parsingScope, tokenStream));
          continue;
        }
        else if ("type".equals(((Keyword) tok).value)) {
          block.typeDefinitions.add(ParseNodeType.parse(parsingScope, tokenStream));
          continue;
        }
      }
      else if (tok instanceof Punctuation
          && ((Punctuation) tok).value == PunctuationType.CLOSE_BRACKET) {
        if (brackets) {
          break;
        }
        else {
          throw tokenStream.parseException("Unexpected close bracket");
        }
      }
      throw tokenStream
          .parseException("Top level blocks may only contain vars, funcs, or types. Found: " + tok);
    }

    if (brackets) {
      tokenStream.skipPunctuation(PunctuationType.CLOSE_BRACKET);
    }

    return block;
  }

}
