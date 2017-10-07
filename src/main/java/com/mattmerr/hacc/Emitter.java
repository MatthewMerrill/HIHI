package com.mattmerr.hacc;

import static java.lang.String.format;
import static org.bytedeco.javacpp.LLVM.LLVMAddFunction;
import static org.bytedeco.javacpp.LLVM.LLVMAppendBasicBlock;
import static org.bytedeco.javacpp.LLVM.LLVMBuildAlloca;
import static org.bytedeco.javacpp.LLVM.LLVMBuildLoad;
import static org.bytedeco.javacpp.LLVM.LLVMConstInt;
import static org.bytedeco.javacpp.LLVM.LLVMCreateBuilder;
import static org.bytedeco.javacpp.LLVM.LLVMFunctionType;
import static org.bytedeco.javacpp.LLVM.LLVMGetParam;
import static org.bytedeco.javacpp.LLVM.LLVMInt32Type;
import static org.bytedeco.javacpp.LLVM.LLVMPositionBuilderAtEnd;
import static org.bytedeco.javacpp.LLVM.LLVMVoidType;

import com.mattmerr.hacc.EmitItem.EmitItemFunction;
import com.mattmerr.hacc.EmitItem.EmitItemType;
import com.mattmerr.hacc.EmitItem.EmitItemVar;
import com.mattmerr.hitch.parsetokens.ParseNodeAssignedDeclaration;
import com.mattmerr.hitch.parsetokens.ParseNodeBlock;
import com.mattmerr.hitch.parsetokens.ParseNodeDeclaration;
import com.mattmerr.hitch.parsetokens.ParseNodeFile;
import com.mattmerr.hitch.parsetokens.ParseNodeFunction;
import com.mattmerr.hitch.parsetokens.ParseNodeStatement;
import com.mattmerr.hitch.parsetokens.ParseNodeType;
import com.mattmerr.hitch.parsetokens.expression.ExpressionToken;
import com.mattmerr.hitch.parsetokens.expression.Variable;
import org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;
import org.bytedeco.javacpp.LLVM.LLVMBuilderRef;
import org.bytedeco.javacpp.LLVM.LLVMTypeRef;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;
import org.bytedeco.javacpp.PointerPointer;

public class Emitter {

  public static void visitFile(EmitContext ctx, ParseNodeFile parseNodeFile) {
    EmitScope scope = ctx.scope;
    scope.declare(ctx, "void", new EmitItemType(LLVMVoidType()));
    scope.declare(ctx, "int", new EmitItemType(LLVMInt32Type()));

    for (ParseNodeFunction function : parseNodeFile.block.functions) {
      createFunction(ctx, function);
    }

    for (ParseNodeFunction function : parseNodeFile.block.functions) {
      populateFunction(ctx, function);
    }
  }

  public static void visitBlock(EmitContext ctx, ParseNodeBlock parseNodeBlock) {
    EmitContext innerContext = ctx.createChildCtx("(block)", ctx.builderRef);
    for (ParseNodeStatement statement : parseNodeBlock.getStatementList()) {
      visitStatement(ctx, statement);
    }
  }

  public static void createFunction(EmitContext ctx, ParseNodeFunction function) {
    LLVMTypeRef retType = ctx.scope.getEmitItemType(ctx, function.returnType).getTypeRef();
    LLVMTypeRef[] argTypes = new LLVMTypeRef[function.argumentTypes.size()];

    for (int argIdx = 0; argIdx < argTypes.length; argIdx++) {
      argTypes[argIdx] = ctx.scope.getEmitItemType(ctx, function.argumentTypes.get(argIdx))
          .getTypeRef();
    }

    LLVMTypeRef typeRef = LLVMFunctionType(
        retType,
        new PointerPointer<>(argTypes),
        argTypes.length,
        0);
    LLVMValueRef funcRef = LLVMAddFunction(ctx.getModuleRef(), function.name, typeRef);
    EmitItemFunction emitFunc = new EmitItemFunction(function.name, funcRef);
    emitFunc.scope = new EmitScope(ctx.scope);
    ctx.scope.declare(ctx, function.name, emitFunc);
  }

  public static void populateFunction(EmitContext ctx, ParseNodeFunction function) {
    EmitItemFunction emitFunc = ctx.scope.getEmitItemFunction(ctx, function.name);
    EmitContext innerCtx = ctx.createChildCtx(function.name);

    LLVMValueRef func = emitFunc.pointer;

    for (int argIdx = 0; argIdx < function.argumentMapping.size(); argIdx++) {
      EmitItemType argType = ctx.scope.getEmitItemType(ctx, function.argumentTypes.get(argIdx));
      String argName = function.argumentMapping.get(argIdx);
      EmitItem argItem = new EmitItemVar(argName, LLVMGetParam(emitFunc.pointer, argIdx));
      emitFunc.scope.declare(innerCtx, argName, argItem);
    }

    LLVMBasicBlockRef block = LLVMAppendBasicBlock(func, "");
    LLVMBuilderRef builderRef = LLVMCreateBuilder();

    LLVMPositionBuilderAtEnd(builderRef, block);
    visitStatement(innerCtx, function.definition);
  }

  public static void visitType(EmitContext ctx, ParseNodeType parseNodeType) {
    EmitItemType type = new EmitItemType();
  }

  public static void visitStatement(EmitContext ctx, ParseNodeStatement statement) {
    if (statement instanceof ParseNodeBlock) {
      visitBlock(ctx, (ParseNodeBlock) statement);
    }
    else if (statement instanceof ParseNodeDeclaration) {
      visit(ctx, (ParseNodeDeclaration) statement);
    }
    else {
      throw ctx.compileException(
          format("Unknown Statement to emit: \"%s\"", statement.getClass().getName()));
    }
  }

  public static void visit(EmitContext ctx, ParseNodeDeclaration declaration) {
    if (declaration instanceof ParseNodeAssignedDeclaration) {
      EmitItemType type = ctx.scope.getEmitItemType(ctx, declaration.type);
      LLVMValueRef valueRef = visitExpression(ctx,
          ((ParseNodeAssignedDeclaration) declaration).assignmentExpression.root, false);
      EmitItemVar var = new EmitItemVar(declaration.qualifiedIdentifier, valueRef);
      var.assign(ctx, ctx.builderRef, LLVMConstInt(LLVMInt32Type(), 0, 0));
      ctx.scope.declare(ctx, declaration.qualifiedIdentifier, var);
    }
    else {
      EmitItemType type = ctx.scope.getEmitItemType(ctx, declaration.type);
      LLVMValueRef valueRef = LLVMBuildAlloca(ctx.builderRef, type.getTypeRef(),
          declaration.qualifiedIdentifier);
      EmitItemVar var = new EmitItemVar(declaration.qualifiedIdentifier, valueRef);
      var.assign(ctx, ctx.builderRef, LLVMConstInt(LLVMInt32Type(), 0, 0));
      ctx.scope.declare(ctx, declaration.qualifiedIdentifier, var);
    }
  }

  public static LLVMValueRef visitExpression(EmitContext ctx, ExpressionToken expression,
      boolean asPointer) {
    if (expression instanceof Variable) {
      Variable variable = (Variable) expression;
      if (asPointer) {
        return ctx.scope.getEmitItemVar(ctx, variable.qualifiedName).pointer;
      }
      else {
        return LLVMBuildLoad(ctx.builderRef,
            ctx.scope.getEmitItemVar(ctx, variable.qualifiedName).pointer, "");
      }
    }
    throw ctx.compileException(
        format("Unknown ExpressionToken \"%s\"", expression.getClass().getName()));
  }

}
