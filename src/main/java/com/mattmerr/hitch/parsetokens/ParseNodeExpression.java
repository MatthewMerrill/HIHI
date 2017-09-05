package com.mattmerr.hitch.parsetokens;

import static com.mattmerr.hitch.parsetokens.expression.ExpressionToken.ExpressionTokenType
    .OPERATOR;
import static com.mattmerr.hitch.parsetokens.expression.ExpressionToken.ExpressionTokenType.VALUE;
import static com.mattmerr.hitch.parsetokens.expression.Operation.Associativity.LEFT;
import static com.mattmerr.hitch.parsetokens.expression.Operation.Associativity.RIGHT;
import static com.mattmerr.hitch.tokens.Punctuation.PunctuationType.CLOSE_PARENTHESIS;
import static com.mattmerr.hitch.tokens.Punctuation.PunctuationType.OPEN_PARENTHESIS;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.parsetokens.expression.BinaryOperation;
import com.mattmerr.hitch.parsetokens.expression.Call;
import com.mattmerr.hitch.parsetokens.expression.ExpressionToken;
import com.mattmerr.hitch.parsetokens.expression.Literal;
import com.mattmerr.hitch.parsetokens.expression.Operation;
import com.mattmerr.hitch.parsetokens.expression.Parenthesis;
import com.mattmerr.hitch.parsetokens.expression.Variable;
import com.mattmerr.hitch.tokens.Identifier;
import com.mattmerr.hitch.tokens.Operator;
import com.mattmerr.hitch.tokens.Punctuation;
import com.mattmerr.hitch.tokens.Token;
import com.mattmerr.hitch.tokens.TokenType;
import com.mattmerr.hitch.tokens.Value;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.function.BiFunction;

/**
 * Created by merrillm on 1/27/17.
 */
public class ParseNodeExpression extends ParseNode {

  public ExpressionToken root;

  private ParseNodeExpression(ExpressionToken expressionToken) {
    this.root = expressionToken;
  }

  public static <T> List<T> parseDelimited(ParsingScope scope, TokenStream tokenStream,
      BiFunction<ParsingScope, TokenStream, T> map,
      Punctuation.PunctuationType join,
      Punctuation.PunctuationType start,
      Punctuation.PunctuationType end) {
    List<T> arguments = new ArrayList<>();
    tokenStream.skipPunctuation(start);

    while (tokenStream.peek().type != TokenType.PUNCTUATION
        || ((Punctuation) tokenStream.peek()).value != end) {
      arguments.add(map.apply(scope, tokenStream));

      tokenStream.expectingPunctuation(join, end);
      if (((Punctuation) tokenStream.next()).value == end) { return arguments; }
    }

    tokenStream.skipPunctuation(end);
    return arguments;
  }

  public static ParseNodeExpression parseExpression(ParsingScope scope, TokenStream tokenStream) {
    return new ParseNodeExpression(buildTree(shuntingYard(scope, tokenStream), tokenStream));
  }

  public static Queue<ExpressionToken> shuntingYard(ParsingScope scope, TokenStream tokenStream) {
    Queue<ExpressionToken> output = new LinkedList<>();
    Stack<ExpressionToken> buffer = new Stack<>();
    ExpressionToken.ExpressionTokenType lastTokenType = null;
    int parenDepth = 0;

    while (!tokenStream.eof()) {
      Token tok = tokenStream.peek();

      if (tok instanceof Value) {
        output.add(new Literal<>((Value) tok));
        lastTokenType = ExpressionToken.ExpressionTokenType.VALUE;

      }
      else if (tok instanceof Identifier) {
        output.add(new Variable(((Identifier) tok).value));
        lastTokenType = ExpressionToken.ExpressionTokenType.VALUE;

      }
      else if (tok instanceof Operator) {
        pushOperation(output, buffer, Operation.getOperation(((Operator) tok).value));
        lastTokenType = ExpressionToken.ExpressionTokenType.OPERATOR;

      }
      else if (tok instanceof Punctuation) {
        if (((Punctuation) tok).value == OPEN_PARENTHESIS) {

          if (lastTokenType == VALUE) {
            Call call = new Call();
            call.arguments = parseDelimited(scope, tokenStream,
                (sc, ts) -> parseExpression(sc, ts).root,
                Punctuation.PunctuationType.COMMA,
                Punctuation.PunctuationType.OPEN_PARENTHESIS,
                Punctuation.PunctuationType.CLOSE_PARENTHESIS);
            pushOperation(output, buffer, call);
            lastTokenType = ExpressionToken.ExpressionTokenType.VALUE;
            continue;
          }
          else {
            buffer.push(new Parenthesis());
            lastTokenType = ExpressionToken.ExpressionTokenType.OPERATOR;
            parenDepth++;
          }
        }
        else if (((Punctuation) tok).value == CLOSE_PARENTHESIS) {

          if (parenDepth == 0) {
            if (lastTokenType != OPERATOR) {
              while (!buffer.isEmpty()) { output.add(buffer.pop()); }
              return output;
            }
            else { throw tokenStream.parseException("Expected value, found " + tok); }
          }

          while (!buffer.isEmpty() &&
              !(buffer.peek() instanceof Parenthesis
                  || buffer.peek() instanceof Call)) {
            output.add(buffer.pop());
          }
          if (buffer.isEmpty()) { throw tokenStream.parseException("Unmatched close parenthesis"); }

          parenDepth--;

          ExpressionToken popped = buffer.pop();
          lastTokenType = ExpressionToken.ExpressionTokenType.VALUE;
        }
        else {
          Operation op = Operation.getOperation(((Punctuation) tok).value);
          if (op != null) {
            pushOperation(output, buffer, op);
            lastTokenType = ExpressionToken.ExpressionTokenType.OPERATOR;
          }
          else {
            break;
          }
        }
      }

      tokenStream.next();
    }

    while (!buffer.isEmpty()) {
      if (buffer.peek() instanceof Parenthesis) {
        throw tokenStream.parseException("Unmatched left parenthesis");
      }
      output.add(buffer.pop());
    }

    return output;
  }

  private static void pushOperation(Queue<ExpressionToken> output, Stack<ExpressionToken> buffer,
      Operation toAdd) {
    while (!buffer.empty() && buffer.peek() instanceof Operation) {
      Operation o2 = (Operation) buffer.peek();

      if ((toAdd.type.associativity == LEFT && toAdd.type.precedence >= o2.type.precedence)
          || (toAdd.type.associativity == RIGHT && toAdd.type.precedence > o2.type.precedence)) {
        output.add(o2);
        buffer.pop();
      }
      else {
        break;
      }
    }

    buffer.add(toAdd);
  }

  public static ExpressionToken buildTree(Queue<ExpressionToken> queue, TokenStream tokenStream) {

    if (queue.isEmpty()) {
      throw tokenStream.parseException("Unexpected end of expression");
    }

    Stack<ExpressionToken> stack = new Stack<>();

    while (!queue.isEmpty()) {
      ExpressionToken cur = queue.remove();

      if (cur instanceof Literal || cur instanceof Variable) {
        stack.push(cur);

      }
      else if (cur instanceof BinaryOperation) {
        BinaryOperation binop = (BinaryOperation) cur;
        binop.right = stack.pop();
        binop.left = stack.pop();
        stack.push(binop);

      }
      else if (cur instanceof Call) {
        Call call = (Call) cur;
        call.variable = stack.pop();
        stack.push(call);

      }
    }

    if (stack.size() != 1) {
      throw tokenStream.parseException("Missing operator somewhere in this expression");
    }

    return stack.pop();
  }
}
