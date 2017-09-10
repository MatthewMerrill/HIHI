import static com.google.common.truth.Truth.assertThat;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.parsetokens.ParseNodeTypingParameter;
import com.mattmerr.hitch.parsetokens.ParsingScope;
import org.junit.Test;

public class TypingParameterTest {

  @Test
  public void testSingle() {
    String input = "<A>";
    ParsingScope scope = new ParsingScope();
    TokenStream tokenStream = new TokenStream(input);

    assertIsAnANestedValue(ParseNodeTypingParameter.parse(scope, tokenStream));
  }

  private void assertIsAnANestedValue(ParseNodeTypingParameter root) {
    assertThat(root.isValueNode()).isFalse();
    assertThat(root.getTypings()).hasSize(1);
    assertThat(root.getTypings().get(0)).isNotNull();
    assertThat(root.getTypings().get(0).isValueNode()).isTrue();
    assertThat(root.getTypings().get(0).getValue()).isEqualTo("A");
  }

  @Test
  public void testNested() {
    {
      String input = "<<A>>";
      ParsingScope scope = new ParsingScope();
      TokenStream tokenStream = new TokenStream(input);

      ParseNodeTypingParameter root = ParseNodeTypingParameter.parse(scope, tokenStream);
      assertThat(root.isValueNode()).isFalse();
      assertThat(root.getTypings()).hasSize(1);
      assertIsAnANestedValue(root.getTypings().get(0));
    }
    {
      String input = "<<<A>>>";
      ParsingScope scope = new ParsingScope();
      TokenStream tokenStream = new TokenStream(input);

      ParseNodeTypingParameter root = ParseNodeTypingParameter.parse(scope, tokenStream);
      assertThat(root.isValueNode()).isFalse();
      assertThat(root.getTypings()).hasSize(1);
      root = root.getTypings().get(0);
      assertThat(root.isValueNode()).isFalse();
      assertThat(root.getTypings()).hasSize(1);
      assertIsAnANestedValue(root.getTypings().get(0));
    }
  }

  @Test
  public void testAdjacent() {
    String input = "<B,<A>>";
    ParsingScope scope = new ParsingScope();
    TokenStream tokenStream = new TokenStream(input);

    ParseNodeTypingParameter root = ParseNodeTypingParameter.parse(scope, tokenStream);
    assertThat(root.isValueNode()).isFalse();
    assertThat(root.getTypings()).hasSize(2);
    assertThat(root.getTypings().get(0).isValueNode()).isTrue();
    assertThat(root.getTypings().get(0).getValue()).isEqualTo("B");
    assertIsAnANestedValue(root.getTypings().get(1));
  }

}
