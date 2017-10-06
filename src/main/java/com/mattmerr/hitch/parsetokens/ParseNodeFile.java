package com.mattmerr.hitch.parsetokens;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.tokens.Keyword;
import com.mattmerr.hitch.tokens.Token;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ParseNodeFile {

  public ParsingScope scope;
  public Collection<ParseNodeImportStatement> dependencies;
  public ParseNodeTopLevelBlock block;

  private ParseNodeFile() { /* static for no reason */ }

  public static ParseNodeFile parseFile(TokenStream tokenStream) {
    ParseNodeFile file =  new ParseNodeFile();
    file.scope = new ParsingScope();
    file.dependencies = new ArrayList<>();

    Token token = tokenStream.peek();
    while (token instanceof Keyword && "import".equals(((Keyword) token).value)) {
      file.dependencies.add(ParseNodeImportStatement.parse(file.scope, tokenStream));
      token = tokenStream.peek();
    }

    file.block = ParseNodeTopLevelBlock.parse(file.scope, tokenStream, false);

    return file;
  }

}
