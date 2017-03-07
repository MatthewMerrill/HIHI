import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.parsetokens.ParseNodeExpression;
import com.mattmerr.hitch.parsetokens.ParseNodeFunction;
import com.mattmerr.hitch.parsetokens.ParseNodeVariable;
import com.mattmerr.hitch.parsetokens.ParsingScope;
import com.mattmerr.hitch.parsetokens.expression.*;
import org.junit.Test;

import java.util.Queue;

import static org.junit.Assert.assertEquals;

/**
 * Created by merrillm on 2/8/17.
 */
public class ExpressionTest {
    
    @Test
    public void arithmetic0() {
        String input = "1 + 2";
        TokenStream tokenStream = new TokenStream(input);
    
        ParsingScope scope = new ParsingScope();
        Queue<ExpressionToken> output = ParseNodeExpression.shuntingYard(scope, tokenStream);
        ExpressionToken root = ParseNodeExpression.buildTree(output, tokenStream);
        
        assertEquals(BinaryOperation.class, root.getClass());
        assertEquals(Operation.OperationType.ADD, ((BinaryOperation)root).type);
        assertEquals(Literal.class, ((BinaryOperation)root).left.getClass());
        assertEquals(1, ((Literal)((BinaryOperation)root).left).value.value);
        assertEquals(Literal.class, ((BinaryOperation)root).right.getClass());
        assertEquals(2, ((Literal)((BinaryOperation)root).right).value.value);
    }
    
    @Test
    public void multiplicative0() {
        String input = "1 + 2 * 3";
        TokenStream tokenStream = new TokenStream(input);
        
        ParsingScope scope = new ParsingScope();
        Queue<ExpressionToken> output = ParseNodeExpression.shuntingYard(scope, tokenStream);
        ExpressionToken root = ParseNodeExpression.buildTree(output, tokenStream);
    
        assertEquals(BinaryOperation.class, root.getClass());
        assertEquals(Operation.OperationType.ADD, ((BinaryOperation)root).type);
    
        assertEquals(Literal.class, ((BinaryOperation)root).left.getClass());
        assertEquals(1, ((Literal)((BinaryOperation)root).left).value.value);
        
        assertEquals(BinaryOperation.class, ((BinaryOperation)((BinaryOperation)root).right).getClass());
        assertEquals(Operation.OperationType.MULTIPLY, ((BinaryOperation)((BinaryOperation)root).right).type);
        
        assertEquals(Literal.class, ((BinaryOperation)((BinaryOperation)root).right).left.getClass());
        assertEquals(2, ((Literal)((BinaryOperation)((BinaryOperation)root).right).left).value.value);
    
        assertEquals(Literal.class, ((BinaryOperation)((BinaryOperation)root).right).right.getClass());
        assertEquals(3, ((Literal)((BinaryOperation)((BinaryOperation)root).right).right).value.value);
    }
    
    
    @Test
    public void access0() {
        String input = "a.b.c(\"Hello\")";
        TokenStream tokenStream = new TokenStream(input);
        
        ParsingScope scope = new ParsingScope();
    
        ParseNodeVariable a = new ParseNodeVariable();
        scope.declare("a");
        scope.put("a", a);
    
        ParseNodeVariable b = new ParseNodeVariable();
        a.set("b", b);
    
        ParseNodeFunction c = new ParseNodeFunction();
        b.set("c", c);
        
        Queue<ExpressionToken> output = ParseNodeExpression.shuntingYard(scope, tokenStream);
        ExpressionToken root = ParseNodeExpression.buildTree(output, tokenStream);
        
        assertEquals(Call.class, root.getClass());
        assertEquals(BinaryOperation.class, ((Call)root).variable.getClass());
        assertEquals(Operation.OperationType.ACCESS, ((BinaryOperation)((Call)root).variable).type);
        assertEquals(BinaryOperation.class, ((BinaryOperation)((Call)root).variable).left.getClass());
        assertEquals(Operation.OperationType.ACCESS, ((BinaryOperation)((BinaryOperation)((Call)root).variable).left).type);
        assertEquals(Variable.class, ((BinaryOperation)((BinaryOperation)((Call)root).variable).left).left.getClass());
        assertEquals("a", ((Variable)((BinaryOperation)((BinaryOperation)((Call)root).variable).left).left).qualifiedName);
        assertEquals(Variable.class, ((BinaryOperation)((BinaryOperation)((Call)root).variable).left).right.getClass());
        assertEquals("b", ((Variable)((BinaryOperation)((BinaryOperation)((Call)root).variable).left).right).qualifiedName);
        assertEquals(Variable.class, ((BinaryOperation)((Call)root).variable).right.getClass());
        assertEquals("c", ((Variable)((BinaryOperation)((Call)root).variable).right).qualifiedName);
        assertEquals(1, ((Call)root).arguments.size());
        assertEquals(Literal.class, ((Call)root).arguments.get(0).getClass());
        assertEquals(String.class, ((Literal)((Call)root).arguments.get(0)).value.value.getClass());
        assertEquals("Hello", ((Literal)((Call)root).arguments.get(0)).value.value);
    
        nop();
    }
    
    public void nop() {
        
    }
}
