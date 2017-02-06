package com.mattmerr.hitch;

import com.mattmerr.hitch.tokens.*;

import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static com.mattmerr.hitch.Grammar.*;
import static com.mattmerr.hitch.tokens.TokenType.*;
import static com.mattmerr.hitch.tokens.Operator.OperatorType;

/**
 * Created by merrillm on 11/14/16.
 */
public class TokenStream {
    
    private Token current = null;
    private CharStream input;
    
    
    public TokenStream(String s) {
        input = new CharStream(s);
    }
    public TokenStream(InputStream is) {
        input = new CharStream(is);
    }
    
    
    public Token peek() {
        return current != null ? current : (current=readNext());
    }
    public Token next() {
        Token ret = current;
        current = null;
        return ret != null ? ret : readNext();
    }
    public boolean eof() {
        return peek() == null;
    }
    public void throwParseException(String msg) {
        throw parseException(msg);
    }
    public RuntimeException parseException(String msg) {
        return input.parseException(msg);
    }
    public void skipPunctuation(Punctuation.PunctuationType... allowedTypes) {
        expectingPunctuation(allowedTypes);
        next();
    }
    public void expectingPunctuation(Punctuation.PunctuationType... allowedTypes){
        if (peek().type == PUNCTUATION) {
            Punctuation.PunctuationType foundType = ((Punctuation) peek()).value;
            for (Punctuation.PunctuationType type : allowedTypes) {
                if (foundType == type)
                    return;
            }
        }
        
        throw parseException("Expected one of: " + Arrays.toString(allowedTypes) + " found " + peek());
    }
    
    
    private Token readNext() {
        while (true) {
            readWhile(Grammar::isWhitespace);
            if (input.eof()) return null;
    
            char ch = input.peek();
//        if (ch == '#') {
//            skipComment();
//            return readNext();
//        }
    
            if (ch == '"') return readString();
            if (ch == '\'') return readChar();
            if (isDigit(ch)) return readNumber();
            if (isIdentifierStarter(ch)) return readIdentifier();
    
            ch = input.next();
            if (ch == '/' && input.peek() == '/') { skipLineComment(); continue; }
            if (ch == '/' && input.peek() == '*') { skipBlockComment(); continue; }
            if (isPunctuation(ch)) return Punctuation.of(ch);
            if (isOperatorChar(ch)) return Operator.of(ch);
    
            throwParseException("Unknown token start '" + ch + "'");
            return null;
        }
    }
    
    
    private String readWhile(Function<Character, Boolean> f) {
        StringBuilder sb = new StringBuilder();
        
        while (!input.eof() && f.apply(input.peek()))
            sb.append(input.next());
        
        return sb.toString();
    }
    private void skipUntil(String end) {
        String buf = "";
        while (!input.eof() && !buf.equals(end)) {
            if (buf.length() == end.length())
                buf = buf.substring(1);
            buf += input.next();
        }
    }
    
    private void skipLineComment() {
        input.next();
        skipUntil("\n");
    }
    private void skipBlockComment() {
        input.next();
        skipUntil("*/");
    }
    
    private Token readNumber() {
        AtomicBoolean isFloat = new AtomicBoolean(false);
        String number = readWhile(
                (ch) -> isDigit(ch) || (ch == '.' && !isFloat.getAndSet(true)));
        
        if (isFloat.get() || input.peek() == 'f' || input.peek() == 'd') {
            if (input.peek() == 'd') {
                input.next();
                return new Value<>(DOUBLE, Double.valueOf(number));
            }
            
            if (input.peek() == 'f')
                input.next();
            
            return new Value<>(FLOAT, Float.valueOf(number));
        }
        
        if (input.peek() == 'l') {
            input.next();
            return new Value<>(LONG, Long.valueOf(number));
        }
        
        if (input.peek() == 'i')
            input.next();
        
        return new Value<>(INTEGER, Integer.valueOf(number));
    }
    private Token readIdentifier() {
        String id = readWhile(Grammar::isIdentifier);
        return isKeyword(id) ? new Keyword(id) : new Identifier(id);
    }
    private Token readString() {
        return new Value<>(TokenType.STRING, readEscaped('"'));
    }
    private Token readChar() {
        String esc = readEscaped('\'');
        
        if (esc.length() != 1)
            throwParseException("Character literals must be of length one.");
        
        return new Value<>(TokenType.CHARACTER, esc);
    }
    private String readEscaped(char end) {
        boolean escaped = false;
        StringBuilder sb = new StringBuilder();
        
        input.next();
        while (!input.eof()) {
            char ch = input.next();
            
            if (escaped) {
                Character unescaped = (Grammar.ESCAPE_CHARS.get(ch));
                if (unescaped == null)
                    throwParseException("Unknown escape character");
                sb.append(unescaped);
            } else if (ch == '\\') {
                escaped = true;
            } else if (ch == end) {
                break;
            } else {
                sb.append(ch);
            }
        }
        
        return sb.toString();
    }
}
