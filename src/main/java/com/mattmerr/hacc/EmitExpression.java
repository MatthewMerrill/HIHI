package com.mattmerr.hacc;

import static java.lang.String.format;
import static org.bytedeco.javacpp.LLVM.LLVMBuildAdd;
import static org.bytedeco.javacpp.LLVM.LLVMBuildCall;
import static org.bytedeco.javacpp.LLVM.LLVMBuildExtractValue;
import static org.bytedeco.javacpp.LLVM.LLVMGetStructName;
import static org.bytedeco.javacpp.LLVM.LLVMTypeOf;

import com.mattmerr.hacc.EmitItemNativeType.EmitItemTypeInt;
import com.mattmerr.hitch.parsetokens.expression.BinaryOperation;
import com.mattmerr.hitch.parsetokens.expression.Call;
import com.mattmerr.hitch.parsetokens.expression.Dict;
import com.mattmerr.hitch.parsetokens.expression.ExpressionToken;
import com.mattmerr.hitch.parsetokens.expression.Literal;
import com.mattmerr.hitch.parsetokens.expression.Operation.OperationType;
import com.mattmerr.hitch.parsetokens.expression.Variable;
import java.util.HashMap;
import java.util.Map;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;
import org.bytedeco.javacpp.PointerPointer;

public class EmitExpression {

  private EmitExpression() { /* static */ }

  public static LLVMValueRef visitExpression(EmitContext ctx, ExpressionToken expression,
      boolean asPointer) {
    if (expression instanceof Variable) {
      Variable variable = (Variable) expression;
//      if (asPointer) {
      return ctx.scope.getEmitItemVar(ctx, variable.qualifiedName).pointer;
//      }
//      else {
//        return LLVMBuildLoad(ctx.builderRef,
//            ctx.scope.getEmitItemVar(ctx, variable.qualifiedName).pointer, "");
//      }
    }
    else if (expression instanceof Literal) {
      if (((Literal) expression).value.value instanceof Integer) {
        @SuppressWarnings("unchecked")
        Literal<Integer> literal = (Literal<Integer>) expression;
        EmitItemTypeInt intType = EmitItemTypeInt.getInstance(ctx);
        LLVMValueRef valueRef = intType.construct(ctx, literal.value.value);
//        if (asPointer) {
        return valueRef;
//        }
//        return LLVMBuildLoad(ctx.builderRef, valueRef, "");
      }
      else {
        throw ctx.compileException(
            format("Unknown Literal Type \"%s\"",
                ((Literal) expression).value.value.getClass().getName()));
      }
    }
    else if (expression instanceof BinaryOperation) {
      BinaryOperation binop = (BinaryOperation) expression;
      if (binop.type == OperationType.ADD) {
        LLVMValueRef left = visitExpression(ctx, binop.left, false);
        LLVMValueRef right = visitExpression(ctx, binop.right, false);

        String leftType = LLVMGetStructName(LLVMTypeOf(left)).getString();
        String rightType = LLVMGetStructName(LLVMTypeOf(right)).getString();

        if ("hipl_int".equals(leftType) && "hipl_int".equals(rightType)) {
          LLVMValueRef leftValue = LLVMBuildExtractValue(ctx.builderRef, left,
              EmitItemTypeInt.getInstance(ctx).memberIndex("$value", true), "");
          LLVMValueRef rightValue = LLVMBuildExtractValue(ctx.builderRef, right,
              EmitItemTypeInt.getInstance(ctx).memberIndex("$value", true), "");

          Map<String, LLVMValueRef> argRefs = new HashMap<>();
          argRefs.put("value", LLVMBuildAdd(ctx.builderRef, leftValue, rightValue, ""));
          LLVMValueRef resultVal = EmitItemTypeInt.getInstance(ctx).construct(ctx, argRefs);
          if (asPointer) {
            throw ctx.compileException("I'm lazy and don't think this should happen");
          }
          else {
            return resultVal;
          }
        }
        else {
          throw ctx.compileException(
              "Addition between " + leftType + " and " + rightType + " is not defined");
        }
      }
      else {
        throw ctx.compileException("I don't support Operation " + binop.type + " yet.");
      }
    }
    else if (expression instanceof Dict) {
      Dict dict = (Dict) expression;
      if (dict.mappings.containsKey("type")) {
        if (dict.mappings.get("type").root instanceof Literal) {
          Literal typeVal = (Literal) dict.mappings.get("type").root;
          if (typeVal.value.value instanceof String) {
            Map<String, LLVMValueRef> args = new HashMap<>();
            for (String argName : dict.mappings.keySet()) {
              if (!argName.equals("type")) {
                args.put(argName, visitExpression(ctx, dict.mappings.get(argName).root, false));
              }
            }
            return ctx.scope.getEmitItemType(ctx, (String) typeVal.value.value)
                .construct(ctx, args);
          }
          else {
            throw ctx.compileException("Dict type value must be a string literal!");
          }
        }
        else {
          throw ctx.compileException("Dict type value must be a string literal!");
        }
      }
      else {
        throw ctx.compileException("Add type to dicts!");
      }
    }
    else if (expression instanceof Call) {
      Call call = (Call) expression;
      LLVMValueRef var = visitExpression(ctx, call.variable, true);
      LLVMValueRef[] args = new LLVMValueRef[call.arguments.size()];
      for (int argIdx = 0; argIdx < args.length; argIdx++) {
        args[argIdx] = visitExpression(ctx, call.arguments.get(argIdx).root, true);
      }
      return LLVMBuildCall(ctx.builderRef, var, new PointerPointer<>(args), args.length, "");
    }
    else {
      throw ctx.compileException(
          format("Unknown ExpressionToken \"%s\"", expression.getClass().getName()));
    }
  }
}
