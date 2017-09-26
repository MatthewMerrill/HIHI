import static com.google.common.truth.Truth.assertThat;

import com.mattmerr.hitch.Grammar;
import org.junit.Test;

public class GrammarTest {

  @Test
  public void keywordsSorted() {
    for (int i = 1; i < Grammar.keywordList.size(); i++) {
      assertThat(Grammar.keywordList.get(i)).isGreaterThan(Grammar.keywordList.get(i-1));
    }
  }

}
