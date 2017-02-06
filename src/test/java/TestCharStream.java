import com.mattmerr.hitch.CharStream;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by merrillm on 11/14/16.
 */
public class TestCharStream {
    
    @Test
    public void general() {
        CharStream cs = new CharStream("asdf");
        
        assertEquals(false, cs.eof());
        assertEquals('a', cs.next());
        assertEquals('s', cs.peek());
        assertEquals('s', cs.peek());
        assertEquals('s', cs.next());
        assertEquals(false, cs.eof());
        assertEquals(false, cs.eof());
        assertEquals('d', cs.next());
        assertEquals('f', cs.next());
        assertEquals(true, cs.eof());
    }
    
}
