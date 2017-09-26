package com.mattmerr.hitch;

import com.mattmerr.hitch.parsetokens.ParseNodeBlock;
import com.mattmerr.hitch.parsetokens.ParsingScope;

public class ParseResult {

  public final ParsingScope scope;
  public final ParseNodeBlock block;

  public ParseResult(ParsingScope scope, ParseNodeBlock block) {
    this.scope = scope;
    this.block = block;
  }

}
