package com.mattmerr.hitch;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import com.sun.javafx.UnmodifiableArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by merrillm on 11/14/16.
 */
public class Grammar {

  // KEEP THIS SORTED!
  private static final String[] keywords = {
      "class", "do", "else", "export", "for", "func", "if", "import", "in", "type", "var", "while",
  };

  private static final String IDENTIFIER_STARTER = "[a-zA-Z_]";
  private static final String IDENTIFIER = "[a-zA-Z_0-9]";

  private static final String OP_CHARS = "+-*/%=&|<>!^~";
  private static final String PUNC_CHARS = ".,;(){}[]";

  public static Map<Character, Character> ESCAPE_CHARS = new HashMap<Character, Character>() {{
    put('n', '\n');
    put('r', '\r');
    put('t', '\t');
    put('\\', '\\');
    put('"', '"');
    put('\'', '\'');
  }};

  public static final List<String> keywordList = unmodifiableList(asList(keywords));

  public static boolean isKeyword(String kw) {
    return Arrays.binarySearch(keywords, kw) >= 0;
  }

  public static boolean isDigit(char ch) {
    return Character.isDigit(ch);
  }

  public static boolean isIdentifierStarter(char ch) {
    return (ch + "").matches(IDENTIFIER_STARTER);
  }

  public static boolean isIdentifier(char ch) {
    return (ch + "").matches(IDENTIFIER);
  }

  public static boolean isOperatorChar(char ch) {
    return OP_CHARS.indexOf(ch) > -1;
  }

  public static boolean isPunctuation(char ch) {
    return PUNC_CHARS.indexOf(ch) > -1;
  }

  public static boolean isWhitespace(char ch) {
    return Character.isWhitespace(ch);
  }

}
