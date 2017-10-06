import static com.google.common.truth.Truth.assertThat;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.parsetokens.ParseNodeType;
import com.mattmerr.hitch.parsetokens.ParseNodeTypingParameter;
import com.mattmerr.hitch.parsetokens.ParsingScope;
import org.junit.Test;

public class TypeTest {

  @Test
  public void testBasic() {
    String input = "type foo { var<a> b; }";
    ParsingScope scope = new ParsingScope();
    TokenStream tokenStream = new TokenStream(input);

    ParseNodeType type = ParseNodeType.parse(scope, tokenStream);

    assertThat(type).isNotNull();
    assertThat(type.block.variableDeclarations).hasSize(1);
    assertThat(type.block.variableDeclarations.iterator().next().type).isEqualTo("a");
    assertThat(type.block.variableDeclarations.iterator().next().qualifiedIdentifier)
        .isEqualTo("b");
  }

  @Test
  public void testType() {
    String input = "type foo { var<a> b; func<<>,void> bar(){} }";
    ParsingScope scope = new ParsingScope();
    TokenStream tokenStream = new TokenStream(input);

    ParseNodeType type = ParseNodeType.parse(scope, tokenStream);

    assertThat(type).isNotNull();
    assertThat(type.block.variableDeclarations).hasSize(1);
    assertThat(type.block.variableDeclarations.iterator().next().type).isEqualTo("a");
    assertThat(type.block.variableDeclarations.iterator().next().qualifiedIdentifier)
        .isEqualTo("b");
    assertThat(type.block.functions).hasSize(1);
    assertThat(type.block.functions.iterator().next().name).isEqualTo("bar");
  }

}
