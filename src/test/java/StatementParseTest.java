import static com.google.common.truth.Truth.assertThat;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.parsetokens.ParseNodeBlock;
import com.mattmerr.hitch.parsetokens.ParseNodeCall;
import com.mattmerr.hitch.parsetokens.ParseNodeFunction;
import com.mattmerr.hitch.parsetokens.ParseNodeFunctionDeclaration;
import com.mattmerr.hitch.parsetokens.ParseNodeReturnStatement;
import com.mattmerr.hitch.parsetokens.ParseNodeStatement;
import com.mattmerr.hitch.parsetokens.ParsingScope;
import com.mattmerr.hitch.parsetokens.expression.Call;
import com.mattmerr.hitch.parsetokens.expression.Literal;
import com.mattmerr.hitch.tokens.TokenType;
import org.junit.Test;

public class StatementParseTest {


  @Test
  public void returnStatement() {
    String input = "func() => 42;";
    TokenStream tokenStream = new TokenStream(input);
    ParsingScope scope = new ParsingScope();

    ParseNodeStatement statement = ParseNodeStatement.parse(scope, tokenStream);
    assertThat(statement).isInstanceOf(ParseNodeFunctionDeclaration.class);

    ParseNodeFunctionDeclaration functionDeclaration = (ParseNodeFunctionDeclaration) statement;
    ParseNodeFunction function = functionDeclaration.function;
    assertThat(function.name).isNull();
    assertThat(function.definition).isInstanceOf(ParseNodeReturnStatement.class);

    ParseNodeReturnStatement returnStatement = (ParseNodeReturnStatement) function.definition;
    assertThat(returnStatement.value.root).isInstanceOf(Literal.class);
    assertThat(((Literal)(returnStatement.value.root)).value.type).isEqualTo(TokenType.INTEGER);
    assertThat(((Literal)(returnStatement.value.root)).value.value).isEqualTo(42);
  }

  @Test
  public void blockStatement() {
    String input = ""
        + "{"
        + "a();"
        + "b();"
        + "=> c();"
        + "}";
    TokenStream tokenStream = new TokenStream(input);
    ParsingScope scope = new ParsingScope();
    scope.declare("a");
    scope.declare("b");
    scope.declare("c");

    ParseNodeStatement statement = ParseNodeStatement.parse(scope, tokenStream);
    assertThat(statement).isInstanceOf(ParseNodeBlock.class);

    ParseNodeBlock block = (ParseNodeBlock) statement;
    assertThat(block.getStatementList()).hasSize(3);

    {
      ParseNodeStatement childStatement = block.getStatementList().get(0);
      assertThat(childStatement).isInstanceOf(ParseNodeCall.class);
      ParseNodeCall call = (ParseNodeCall) childStatement;
      assertThat(call.qualifiedFunction).isEqualTo("a");
    }
    {
      ParseNodeStatement childStatement = block.getStatementList().get(1);
      assertThat(childStatement).isInstanceOf(ParseNodeCall.class);
      ParseNodeCall call = (ParseNodeCall) childStatement;
      assertThat(call.qualifiedFunction).isEqualTo("b");
    }
    {
      ParseNodeStatement childStatement = block.getStatementList().get(2);
      assertThat(childStatement).isInstanceOf(ParseNodeReturnStatement.class);
      ParseNodeReturnStatement returnStatement = (ParseNodeReturnStatement) childStatement;
      assertThat(returnStatement.value.root).isInstanceOf(Call.class);
      assertThat(((Call)(returnStatement.value.root)).variable).isEqualTo("c");
    }

  }

}
