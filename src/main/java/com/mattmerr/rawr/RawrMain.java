package com.mattmerr.rawr;

import com.mattmerr.hihi.ExpressionEvaluator;
import com.mattmerr.hihi.HProg;
import com.mattmerr.hihi.HScope;
import com.mattmerr.hihi.stdlib.HObject;
import com.mattmerr.hitch.TokenParser;
import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.parsetokens.ParseNodeBlock;
import com.mattmerr.hitch.parsetokens.ParseNodeExpression;
import com.mattmerr.hitch.parsetokens.ParsingScope;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.Scanner;

/**
 * Created by merrillm on 3/3/17.
 */
public class RawrMain {
    
    public static boolean QUIET = false;
    
    public static void main(String[] args) {
        
        if (args.length > 0) {
            try {
                TokenStream tokenStream = new TokenStream(new FileInputStream(new File(args[0])));
                TokenParser parser = new TokenParser(tokenStream);
                ParseNodeBlock block = parser.parse();
                HScope scope = new HScope();
                scope.declareStandardFunctions();
                HProg.run(block, scope);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        
        Scanner scn = new Scanner(System.in);
        
        ParsingScope scope = new ParsingScope();
        HScope hScope = new HScope();
        hScope.declareStandardFunctions();
        
        printPrompt();
        while (scn.hasNextLine()) {
            String line = scn.nextLine().trim();
            
            if (!line.isEmpty()) {
                if (line.endsWith(";")) {
                    TokenStream ts = new TokenStream(line);
                    TokenParser parser = new TokenParser(ts);
                    ParseNodeBlock block = parser.parse(scope);
                    HProg.run(block, hScope);
                } else {
                    TokenStream ts = new TokenStream(line);
                    ParseNodeExpression exp = ParseNodeExpression.parseExpression(scope, ts);
                    HObject res = ExpressionEvaluator.evaluate(hScope, exp.root);
                    System.out.println(res.stringValue(hScope, Collections.emptyList()).nativeValue());
                }
            }
            
            printPrompt();
        }
    }
    
    private static void printPrompt() {
        if (!QUIET) System.out.print("> ");
    }
}
