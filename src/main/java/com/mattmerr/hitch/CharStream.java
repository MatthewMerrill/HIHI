package com.mattmerr.hitch;

import java.io.InputStream;
import java.util.Scanner;

import static java.lang.String.format;

/**
 * Created by merrillm on 11/14/16.
 */
public class CharStream {
    
    private int pos = 0, line = 1, col = 0;
    private Scanner scn;
    private Character ch;
    
    public CharStream(String s) {
        this.scn = new Scanner(s);
        this.scn.useDelimiter("");
        read();
    }
    
    public CharStream(InputStream is) {
        this.scn = new Scanner(is);
        this.scn.useDelimiter("");
        read();
    }
    
    private void read() {
        if (scn.hasNext()) {
            ch = (char) scn.next().charAt(0);
        }
        else {
            ch = null;
        }
    }
    
    public char next() {
        char ret = ch;
        
        pos++;
        if (ch == '\n') {
            line++;
            col = 0;
        } else {
            col++;
        }
        
        read();
        return ret;
    }
    
    public char peek() {
        return ch;
    }
    
    public boolean eof() {
        return ch == null;
    }
    
    public void throwParseException(String msg) {
        throw parseException(msg);
    }
    
    public RuntimeException parseException(String msg) {
        return new RuntimeException(format("%s: %d:%d", msg, line, col));
    }
}
