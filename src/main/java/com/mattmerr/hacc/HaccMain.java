package com.mattmerr.hacc;

import com.mattmerr.hitch.TokenParser;
import com.mattmerr.hitch.TokenStream;

/**
 * Created by merrillm on 3/18/17.
 */
public class HaccMain {
    
    public static void main(String[] args) {
        ProgSink sink = new ProgSink(System.out);
        TokenStream ts = new TokenStream("func hello(name) println(\"Hello\"+name); hello(\"World\");");
        TokenParser parser = new TokenParser(ts);
        sink.writeNoBrackets(parser.parse());
    }
}
