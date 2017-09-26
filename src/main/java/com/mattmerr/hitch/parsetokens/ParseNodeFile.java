package com.mattmerr.hitch.parsetokens;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.tokens.Keyword;
import com.mattmerr.hitch.tokens.Token;
import java.util.ArrayList;
import java.util.Collection;

public class ParseNodeFile {

  public ParsingScope scope;
  public Collection<ParseNodeImportStatement> dependencies;
  public ParseNodeBlock block;

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

    file.block = new ParseNodeBlock();

    while (!tokenStream.eof()) {
      file.block.addStatement(ParseNodeStatement.parse(file.scope, tokenStream));
    }

    return file;
  }

}
