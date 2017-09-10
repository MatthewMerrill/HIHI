package com.mattmerr.hitch.parsetokens;

public class ParseNodeAssignedDeclaration extends ParseNodeDeclaration {
  public ParseNodeExpression assignmentExpression;

  public ParseNodeAssignedDeclaration(ParseNodeExpression expression) {
    this.assignmentExpression = expression;
  }
}
