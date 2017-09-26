import com.mattmerr.hihi.HProg;
import com.mattmerr.hitch.TokenParser;
import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.parsetokens.ParseNodeBlock;
import org.junit.Test;

/**
 * Created by merrillm on 2/5/17.
 */
public class HelloWorld {
    
    @Test
    public void helloworldHipl() {
        TokenStream tokenStream = new TokenStream(getClass().getResourceAsStream("helloworld.hipl"));
        TokenParser parser = new TokenParser(tokenStream);
        ParseNodeBlock parseNodeProgram = parser.parse().block;
        HProg.run(parseNodeProgram);
    }
    
    @Test
    public void functionsHipl() {
        TokenStream tokenStream = new TokenStream(getClass().getResourceAsStream("functions.hipl"));
        TokenParser parser = new TokenParser(tokenStream);
        ParseNodeBlock parseNodeProgram = parser.parse().block;
        HProg.run(parseNodeProgram);
    }
}
