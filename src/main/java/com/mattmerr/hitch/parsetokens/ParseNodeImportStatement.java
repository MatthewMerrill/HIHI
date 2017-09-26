package com.mattmerr.hitch.parsetokens;

import static com.mattmerr.hitch.tokens.Punctuation.PunctuationType.DOT;
import static com.mattmerr.hitch.tokens.Punctuation.PunctuationType.SEMICOLON;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.tokens.Identifier;
import com.mattmerr.hitch.tokens.Keyword;
import com.mattmerr.hitch.tokens.Punctuation;
import com.mattmerr.hitch.tokens.Token;
import java.util.StringJoiner;

public class ParseNodeImportStatement extends ParseNodeStatement {

  public final String importPath;

  public ParseNodeImportStatement(String importPath) {
    this.importPath = importPath;
  }

  public static ParseNodeImportStatement parse(ParsingScope scope, TokenStream tokenStream) {
    Token token = tokenStream.peek();

    if (!(token instanceof Keyword) || !"import".equals(((Keyword) token).value)) {
      throw tokenStream.parseException("Expected \"import\" keyword");
    }
    tokenStream.next();

    return new ParseNodeImportStatement(parseImportPath(scope, tokenStream));
  }

  private static String parseImportPath(ParsingScope scope, TokenStream tokenStream) {
    StringJoiner sj = new StringJoiner(".");
    Token token;

    do {
      token = tokenStream.next();

      if (!(token instanceof Identifier)) {
        throw tokenStream.parseException("Expecting <identifier>");
      }
      sj.add(((Identifier)token).value);

      token = tokenStream.next();

      if (token instanceof Punctuation) {
        Punctuation punctuation = (Punctuation) token;
        if (punctuation.value == DOT) {
          continue;
        }
        else if (punctuation.value == SEMICOLON) {
          break;
        }
      }

      throw tokenStream.parseException("Expecting one of [\'.\', \';\']");
    } while (true);

    return sj.toString();
  }

}
