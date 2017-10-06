package com.mattmerr.hitch.parsetokens;

import static java.util.Collections.unmodifiableList;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.tokens.Identifier;
import com.mattmerr.hitch.tokens.Operator;
import com.mattmerr.hitch.tokens.Operator.OperatorType;
import com.mattmerr.hitch.tokens.Punctuation;
import com.mattmerr.hitch.tokens.Punctuation.PunctuationType;
import com.mattmerr.hitch.tokens.Token;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParseNodeTypingParameter {

  private List<ParseNodeTypingParameter> typings = null;
  private String value = null;

  public static ParseNodeTypingParameter parse(ParsingScope scope, TokenStream tokenStream) {
    tokenStream.expectingOperator(OperatorType.LT);
    return parseRecursive(scope, tokenStream);
  }

  private static ParseNodeTypingParameter parseRecursive(ParsingScope scope,
      TokenStream tokenStream) {
    ParseNodeTypingParameter parseNodeTypingParameter = new ParseNodeTypingParameter();

    if (tokenStream.peek() instanceof Identifier) {
      parseNodeTypingParameter.value = ((Identifier) tokenStream.next()).value;
      Token possiblyDot;
      while ((possiblyDot = tokenStream.peek()) != null && possiblyDot instanceof Punctuation
          && ((Punctuation) possiblyDot).value == PunctuationType.DOT) {
        tokenStream.next();

        if (tokenStream.peek() instanceof Identifier) {
          parseNodeTypingParameter.value += "." + ((Identifier) tokenStream.next()).value;
        }
        else {
          throw tokenStream.parseException("Expected identifier");
        }
      }
      return parseNodeTypingParameter;
    }
    else if (tokenStream.peek() instanceof Operator
        && ((Operator) (tokenStream.peek())).value == OperatorType.LT) {
      tokenStream.skipOperator(OperatorType.LT);

      parseNodeTypingParameter.typings = new ArrayList<>();
      if (tokenStream.peek() instanceof Operator
          && ((Operator) tokenStream.peek()).value == OperatorType.GT) {
        tokenStream.skipOperator(OperatorType.GT);
        return parseNodeTypingParameter;
      }

      while (true) {
        parseNodeTypingParameter.typings.add(parseRecursive(scope, tokenStream));

        if (tokenStream.peek() instanceof Operator
            && ((Operator) tokenStream.peek()).value == OperatorType.GT) { break; }
        else {
          tokenStream.skipPunctuation(PunctuationType.COMMA);
        }
      }
      tokenStream.skipOperator(OperatorType.GT);
      return parseNodeTypingParameter;
    }

    throw tokenStream.parseException("Expecting one of: '<', <identifier>");
  }

  public boolean isValueNode() {
    return value != null;
  }

  public String getValue() {
    return value;
  }

  public List<ParseNodeTypingParameter> getTypings() {
    return unmodifiableList(typings);
  }

}
