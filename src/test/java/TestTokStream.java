import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.VisitSink;
import com.mattmerr.hitch.tokens.*;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;

/**
 * Created by merrillm on 11/14/16.
 */
public class TestTokStream {
    
    @Test
    public void general() {
        TokenStream ts = new TokenStream("for a while 123 1.234 123f this.that");
        Token tok;
        
        tok = ts.peek();
        assertEquals(TokenType.KEYWORD, tok.type);
        assertEquals("for", ((Keyword)tok).value);
        
        tok = ts.next();
        assertEquals(TokenType.KEYWORD, tok.type);
        assertEquals("for", ((Keyword)tok).value);
    
        tok = ts.next();
        assertEquals(TokenType.IDENTIFIER, tok.type);
        assertEquals("a", ((Identifier)tok).value);
    
        tok = ts.next();
        assertEquals(TokenType.KEYWORD, tok.type);
        assertEquals("while", ((Keyword)tok).value);
    
        tok = ts.next();
        assertEquals(TokenType.INTEGER, tok.type);
        assertEquals(123, (long)((Value<Integer>)tok).value);
    
        tok = ts.next();
        assertEquals(TokenType.FLOAT, tok.type);
        assertEquals(1.234f, ((Value<Float>)tok).value, 0.00001);
    
        tok = ts.next();
        assertEquals(TokenType.FLOAT, tok.type);
        assertEquals(123f, ((Value<Float>)tok).value, 0.00001);
    }
    
    @Test
    public void commentTest() {
        TokenStream ts = new TokenStream("// asdf\n" +
                "/* comment 2! */  \n" +
                "12.34f");
        
        Token tok = ts.next();
        assertEquals(TokenType.FLOAT, tok.type);
        assertEquals(12.34f, ((Value<Float>)tok).value, 0.00001);
    }
    
    
    @Test
    public void readHello() throws FileNotFoundException {
        TokenStream tokenStream = new TokenStream(getClass().getResourceAsStream("helloworld.hipl"));
        
        while (!tokenStream.eof()) {
            Token next = tokenStream.next();
            System.out.printf("Read Token of ParseNodeClass %s\n" +
                    "\t- %s\n", next.type, next.toString());
        }
    }
    
}
