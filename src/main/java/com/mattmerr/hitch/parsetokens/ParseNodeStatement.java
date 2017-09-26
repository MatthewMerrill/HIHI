package com.mattmerr.hitch.parsetokens;

import static com.mattmerr.hitch.tokens.Punctuation.PunctuationType.DOT;
import static com.mattmerr.hitch.tokens.Punctuation.PunctuationType.OPEN_PARENTHESIS;
import static com.mattmerr.hitch.tokens.Punctuation.PunctuationType.SEMICOLON;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.tokens.Identifier;
import com.mattmerr.hitch.tokens.Keyword;
import com.mattmerr.hitch.tokens.Operator;
import com.mattmerr.hitch.tokens.Operator.OperatorType;
import com.mattmerr.hitch.tokens.Punctuation;
import com.mattmerr.hitch.tokens.Token;
import com.mattmerr.hitch.tokens.TokenType;

/**
 * Created by merrillm on 1/27/17.
 */
public abstract class ParseNodeStatement extends ParseNode {

  public static ParseNodeStatement parse(ParsingScope scope, TokenStream tokenStream) {
    TokenType peekType = tokenStream.peek().type;

//    if (peekType == TokenType.IDENTIFIER) {
//      Identifier identifier = (Identifier) tokenStream.next();
//
//      StringBuilder qualified = new StringBuilder(identifier.value);
//      ParseNodeObject obj = scope.get(identifier.value);
//
//      while (tokenStream.peek().type == TokenType.PUNCTUATION
//          && ((Punctuation) tokenStream.peek()).value == DOT) {
//        tokenStream.next();
//
//        if (tokenStream.peek().type != TokenType.IDENTIFIER) {
//          throw tokenStream.parseException("Expected identifier");
//        }
//
//        Identifier nextId = (Identifier) (tokenStream.next());
//        qualified.append(".").append(nextId.value);
//        obj = obj.get(nextId.value);
//      }
//
//      if (tokenStream.peek().type == TokenType.PUNCTUATION) {
//        Punctuation punc = (Punctuation) tokenStream.peek();
//
//        if (punc.value == OPEN_PARENTHESIS) {
//          return ParseNodeCall.parse(obj, qualified.toString(), scope, tokenStream);
//        }
//
//        throw tokenStream.parseException("Unexpected punctuation");
//      }
//    }
    if (peekType == TokenType.KEYWORD) {
      Keyword keyword = (Keyword) tokenStream.peek();

      if (keyword.value.equals("export")) {

      }
      else if (keyword.value.equals("func")) {
        ParseNodeFunctionDeclaration decl = new ParseNodeFunctionDeclaration();
        decl.function = ParseNodeFunction.parseFunction(scope, tokenStream);
        return decl;
      }
      else if (keyword.value.equals("if")) {
        return ParseNodeIfStatement.parse(scope, tokenStream);
      }
      else if (keyword.value.equals("import")) {
        return ParseNodeImportStatement.parse(scope, tokenStream);
      }
      else if (keyword.value.equals("var")) {
        tokenStream.next();
        ParseNodeTypingParameter typing = ParseNodeTypingParameter.parse(scope, tokenStream);

        if ((typing.getTypings().size() != 1) || (!typing.getTypings().get(0).isValueNode())) {
          throw tokenStream.parseException("Expected type parameter of format '<T>'");
        }

        ParseNodeDeclaration declaration;

        Token token = tokenStream.next();
        if (!(token instanceof Identifier)) {
          throw tokenStream.parseException("expected identifier, found " + token);
        }
        Identifier identifier = (Identifier) token;

        token = tokenStream.peek();
        if (token instanceof Operator && ((Operator) token).value == OperatorType.EQ) {
          tokenStream.skipOperator(OperatorType.EQ);
          declaration = new ParseNodeAssignedDeclaration(ParseNodeExpression.parseExpression(scope, tokenStream));
        }
        else {
          declaration = new ParseNodeDeclaration();
        }

        declaration.type = typing.getTypings().get(0).getValue();
        declaration.qualifiedIdentifier = identifier.value;
        tokenStream.skipPunctuation(SEMICOLON);

        if (!"int,string,".contains(declaration.type+","))
          throw tokenStream.parseException("var Type may only be one of: [int, string]");

        scope.declare(declaration.qualifiedIdentifier);
        return declaration;
      }
      else if (keyword.value.equals("while")) {
        return ParseNodeWhileStatement.parse(scope, tokenStream);
      }
    }
    else if (peekType == TokenType.PUNCTUATION) {
      Punctuation punc = (Punctuation) tokenStream.peek();
      if (punc.value == Punctuation.PunctuationType.OPEN_BRACKET) {
        return ParseNodeBlock.parse(scope, tokenStream);
      }
    }
    else if (peekType == TokenType.OPERATOR) {
      Operator op = (Operator) tokenStream.peek();

      if (op.value.equals(Operator.OperatorType.EQ)) {
        tokenStream.next();

        if (tokenStream.peek().type == TokenType.OPERATOR
            && ((Operator) tokenStream.peek()).value == Operator.OperatorType.GT) {
          tokenStream.next();

          ParseNodeReturnStatement returnStatement = new ParseNodeReturnStatement(
              ParseNodeExpression.parseExpression(scope, tokenStream));
          tokenStream.skipPunctuation(SEMICOLON);

          return returnStatement;
        }

      }
    }

    // Last ditch effort: Expression!
    // TODO: Evaluate redundencies caused by adding this. (eg Call statements)
    return ParseNodeExpressionStatement.parse(scope, tokenStream);

//    throw tokenStream.parseException("Couldn't parse statement");
  }

}
