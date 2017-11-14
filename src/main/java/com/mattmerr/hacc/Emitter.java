package com.mattmerr.hacc;

import static com.mattmerr.hacc.EmitExpression.visitExpression;
import static com.mattmerr.hacc.EmitExpression.visitTruthy;
import static java.lang.String.format;
import static org.bytedeco.javacpp.LLVM.LLVMAddFunction;
import static org.bytedeco.javacpp.LLVM.LLVMAlloca;
import static org.bytedeco.javacpp.LLVM.LLVMAppendBasicBlock;
import static org.bytedeco.javacpp.LLVM.LLVMBuildAlloca;
import static org.bytedeco.javacpp.LLVM.LLVMBuildBr;
import static org.bytedeco.javacpp.LLVM.LLVMBuildCondBr;
import static org.bytedeco.javacpp.LLVM.LLVMBuildLoad;
import static org.bytedeco.javacpp.LLVM.LLVMBuildRet;
import static org.bytedeco.javacpp.LLVM.LLVMBuildRetVoid;
import static org.bytedeco.javacpp.LLVM.LLVMBuildStore;
import static org.bytedeco.javacpp.LLVM.LLVMCreateBuilder;
import static org.bytedeco.javacpp.LLVM.LLVMFunctionType;
import static org.bytedeco.javacpp.LLVM.LLVMGetBasicBlockParent;
import static org.bytedeco.javacpp.LLVM.LLVMGetBasicBlockTerminator;
import static org.bytedeco.javacpp.LLVM.LLVMGetElementType;
import static org.bytedeco.javacpp.LLVM.LLVMGetInsertBlock;
import static org.bytedeco.javacpp.LLVM.LLVMGetModuleContext;
import static org.bytedeco.javacpp.LLVM.LLVMGetParam;
import static org.bytedeco.javacpp.LLVM.LLVMInsertBasicBlock;
import static org.bytedeco.javacpp.LLVM.LLVMPointerType;
import static org.bytedeco.javacpp.LLVM.LLVMPointerTypeKind;
import static org.bytedeco.javacpp.LLVM.LLVMPositionBuilderAtEnd;
import static org.bytedeco.javacpp.LLVM.LLVMSetGC;
import static org.bytedeco.javacpp.LLVM.LLVMStructCreateNamed;
import static org.bytedeco.javacpp.LLVM.LLVMTypeOf;
import static org.bytedeco.javacpp.LLVM.lto_module_create_from_fd_at_offset;

import com.mattmerr.hacc.EmitItem.EmitItemFunction;
import com.mattmerr.hacc.EmitItem.EmitItemType;
import com.mattmerr.hacc.EmitItem.EmitItemVar;
import com.mattmerr.hitch.parsetokens.ParseNodeAssignedDeclaration;
import com.mattmerr.hitch.parsetokens.ParseNodeBlock;
import com.mattmerr.hitch.parsetokens.ParseNodeDeclaration;
import com.mattmerr.hitch.parsetokens.ParseNodeExpressionStatement;
import com.mattmerr.hitch.parsetokens.ParseNodeFile;
import com.mattmerr.hitch.parsetokens.ParseNodeFunction;
import com.mattmerr.hitch.parsetokens.ParseNodeIfStatement;
import com.mattmerr.hitch.parsetokens.ParseNodeReturnStatement;
import com.mattmerr.hitch.parsetokens.ParseNodeStatement;
import com.mattmerr.hitch.parsetokens.ParseNodeType;
import com.mattmerr.hitch.parsetokens.ParseNodeWhileStatement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;
import org.bytedeco.javacpp.LLVM.LLVMBuilderRef;
import org.bytedeco.javacpp.LLVM.LLVMTypeRef;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;
import org.bytedeco.javacpp.PointerPointer;

public class Emitter {

//  public static void runEmitter() {
//    while (true) {
//      if (!depRun.isEmpty()) {
//        depRun.pop().run();
//      }
//      else if (!typeCreateRun.isEmpty()) {
//        typeCreateRun.pop().run();
//      }
//      else if (!typePopRun.isEmpty()) {
//        typePopRun.pop().run();
//      }
//      else if (!funcCreateRun.isEmpty()) {
//        funcCreateRun.pop().run();
//      }
//      else if (!funcPopRun.isEmpty()) {
//        funcPopRun.pop().run();
//      }
//      else {
//        break;
//      }
//    }
//
//    for (ParseNodeType type : parseNodeFile.block.typeDefinitions) {
//      createType(ctx, type);
//    }
//
//    for (ParseNodeType type : parseNodeFile.block.typeDefinitions) {
//      populateType(ctx, type);
//    }
//
//    for (ParseNodeFunction function : parseNodeFile.block.functions) {
//      createFunction(ctx, function);
//    }
//
//    for (ParseNodeFunction function : parseNodeFile.block.functions) {
//      populateFunction(ctx, function);
//    }
//  }

  public static void visitBlock(EmitContext ctx, ParseNodeBlock parseNodeBlock) {
    EmitContext innerContext = ctx.createChildCtx("(block)", ctx.builderRef);
    for (ParseNodeStatement statement : parseNodeBlock.getStatementList()) {
      visitStatement(ctx, statement);
    }
  }

  public static void createType(EmitContext ctx, ParseNodeType parseNodeType) {
    EmitItemType type = new EmitItemType(parseNodeType.typeName,
        LLVMStructCreateNamed(LLVMGetModuleContext(ctx.moduleRef), parseNodeType.typeName), false);
    ctx.scope.declare(ctx, parseNodeType.typeName, type);
  }

  public static void populateType(EmitContext ctx, ParseNodeType parseNodeType) {
    EmitItemType type = ctx.scope.getEmitItemType(ctx, parseNodeType.typeName);
    Map<String, EmitItem> members = new HashMap<>();

    for (ParseNodeDeclaration variable : parseNodeType.block.variableDeclarations) {
      if (members.containsKey(variable.qualifiedIdentifier)) {
        throw ctx.compileExceptionWithContextName(parseNodeType.typeName,
            "Member already exists: " + variable.qualifiedIdentifier);
      }
      members.put(variable.qualifiedIdentifier, ctx.scope.getEmitItemType(ctx, variable.type));
    }

    for (ParseNodeFunction function : parseNodeType.block.functions) {
//      if (members.containsKey(function.name)) {
//        throw ctx.compileExceptionWithContextName(parseNodeType.typeName,
//            "Member already exists: " + function.name);
//      }
//      members.put(function.name, );
      throw ctx.compileExceptionWithContextName(parseNodeType.typeName,
          "Member functions not yet supported");
    }

    type.populate(ctx, members);
  }

  public static void createFunction(EmitContext ctx, ParseNodeFunction function) {
    EmitItemType retType = ctx.scope.getEmitItemType(ctx, function.returnType);
    LLVMTypeRef[] argTypes = new LLVMTypeRef[function.argumentTypes.size()];

    for (int argIdx = 0; argIdx < argTypes.length; argIdx++) {
      argTypes[argIdx] = LLVMPointerType(
          ctx.scope.getEmitItemType(ctx, function.argumentTypes.get(argIdx))
              .getTypeRef(), 0);
    }

    LLVMTypeRef typeRef = LLVMFunctionType(
        retType.getName().equals("void")
            ? retType.getTypeRef()
            : LLVMPointerType(retType.getTypeRef(), 0),
        new PointerPointer<>(argTypes),
        argTypes.length,
        0);
    LLVMValueRef funcRef = LLVMAddFunction(ctx.getModuleRef(), function.name, typeRef);
    EmitItemFunction emitFunc = new EmitItemFunction(ctx, function.name, funcRef);
    LLVMSetGC(funcRef, "shadow-stack");
//    emitFunc.scope = new EmitScope(ctx.scope);
    ctx.scope.declare(ctx, function.name, emitFunc);
  }

  public static void populateFunction(EmitContext ctx, ParseNodeFunction function) {
    EmitItemFunction emitFunc = ctx.scope.getEmitItemFunction(ctx, function.name);

    if (!function.isNative) {
      LLVMValueRef func = emitFunc.pointer;
      LLVMBasicBlockRef block = LLVMAppendBasicBlock(func, "");
      LLVMBasicBlockRef retblock = LLVMAppendBasicBlock(func, "returnblock");
      LLVMBuilderRef builderRef = LLVMCreateBuilder();
      LLVMPositionBuilderAtEnd(builderRef, block);

      EmitItemType retType = ctx.scope.getEmitItemType(ctx, function.returnType);
      LLVMValueRef retVal = null;
      if (!retType.getName().equals("void")) {
        retVal = LLVMBuildAlloca(builderRef, LLVMPointerType(retType.getTypeRef(), 0), "retval");
      }

      EmitContext innerCtx = ctx.createChildCtx(function.name, builderRef, block, retVal, retblock);

      for (int argIdx = 0; argIdx < function.argumentMapping.size(); argIdx++) {
        EmitItemType argType = ctx.scope.getEmitItemType(ctx, function.argumentTypes.get(argIdx));
        String argName = function.argumentMapping.get(argIdx);
        LLVMValueRef paramRef = LLVMGetParam(emitFunc.pointer, argIdx);
        EmitItemVar argItem = new EmitItemVar(innerCtx, argName,
            LLVMGetElementType(LLVMTypeOf(paramRef)), false);
        argItem.assign(innerCtx, paramRef);
        innerCtx.scope.declare(innerCtx, argName, argItem);
      }

      visitStatement(innerCtx, function.definition);

      if (LLVMGetBasicBlockTerminator(LLVMGetInsertBlock(builderRef)) == null) {
        LLVMBuildBr(builderRef, retblock);
      }

      LLVMPositionBuilderAtEnd(builderRef, retblock);
      if (retType.getName().equals("void")) {
        LLVMBuildRetVoid(builderRef);
      }
      else {
        LLVMBuildRet(builderRef, LLVMBuildLoad(builderRef, retVal, ""));
      }
    }
  }

  public static void visitStatement(EmitContext ctx, ParseNodeStatement statement) {
    if (statement instanceof ParseNodeBlock) {
      visitBlock(ctx, (ParseNodeBlock) statement);
    }
    else if (statement instanceof ParseNodeDeclaration) {
      visit(ctx, (ParseNodeDeclaration) statement);
    }
    else if (statement instanceof ParseNodeIfStatement) {
      visitIfStatement(ctx, (ParseNodeIfStatement) statement);
    }
    else if (statement instanceof ParseNodeExpressionStatement) {
      visitExpression(ctx, ((ParseNodeExpressionStatement) statement).expression.root, false);
    }
    else if (statement instanceof ParseNodeWhileStatement) {
      visitWhile(ctx, (ParseNodeWhileStatement) statement);
    }
    else if (statement instanceof ParseNodeReturnStatement) {
      visitReturn(ctx, (ParseNodeReturnStatement) statement);
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
      EmitItemVar var = new EmitItemVar(ctx, declaration.qualifiedIdentifier, type.getTypeRef(),
          false);
      var.assign(ctx, valueRef);
      ctx.scope.declare(ctx, declaration.qualifiedIdentifier, var);
    }
    else {
      EmitItemType type = ctx.scope.getEmitItemType(ctx, declaration.type);
//      LLVMValueRef valueRef = LLVMBuildAlloca(ctx.builderRef, type.getTypeRef(),
//          declaration.qualifiedIdentifier);
      EmitItemVar var = new EmitItemVar(ctx, declaration.qualifiedIdentifier, type.getTypeRef(),
          false);
      ctx.scope.declare(ctx, declaration.qualifiedIdentifier, var);
    }
  }

  public static void visitIfStatement(EmitContext ctx, ParseNodeIfStatement ifStatement) {
    if (ifStatement.ifFalseStatement == null) {
      LLVMBasicBlockRef ifTrue = LLVMAppendBasicBlock(LLVMGetBasicBlockParent(ctx.blockRef), "");
      LLVMBasicBlockRef end = LLVMAppendBasicBlock(LLVMGetBasicBlockParent(ctx.blockRef), "end");

      LLVMValueRef conditionVal = visitExpression(ctx, ifStatement.conditionExpression.root, false);

      LLVMValueRef ifRef = visitTruthy(ctx, conditionVal);
      LLVMBuildCondBr(ctx.builderRef, ifRef, ifTrue, end);

      LLVMPositionBuilderAtEnd(ctx.builderRef, ifTrue);
      visitStatement(ctx.createChildCtx("ifTrue", ctx.builderRef, ifTrue),
          ifStatement.ifTrueStatement);

      if (LLVMGetBasicBlockTerminator(LLVMGetInsertBlock(ctx.builderRef)) == null) {
        LLVMBuildBr(ctx.builderRef, end);
      }

      LLVMPositionBuilderAtEnd(ctx.builderRef, end);
    }
    else {
      LLVMBasicBlockRef ifTrue = LLVMAppendBasicBlock(LLVMGetBasicBlockParent(ctx.blockRef), "");
      LLVMBasicBlockRef ifFalse = LLVMAppendBasicBlock(LLVMGetBasicBlockParent(ctx.blockRef), "");
      LLVMBasicBlockRef end = LLVMAppendBasicBlock(LLVMGetBasicBlockParent(ctx.blockRef), "");

      LLVMValueRef conditionVal = visitExpression(ctx, ifStatement.conditionExpression.root, false);
      LLVMValueRef ifRef = visitTruthy(ctx, conditionVal);
      LLVMBuildCondBr(ctx.builderRef, ifRef, ifTrue, ifFalse);

      LLVMPositionBuilderAtEnd(ctx.builderRef, ifTrue);
      visitStatement(ctx.createChildCtx("ifTrue", ctx.builderRef, ifTrue),
          ifStatement.ifTrueStatement);
      if (LLVMGetBasicBlockTerminator(LLVMGetInsertBlock(ctx.builderRef)) == null) {
        LLVMBuildBr(ctx.builderRef, end);
      }

      LLVMPositionBuilderAtEnd(ctx.builderRef, ifFalse);
      visitStatement(ctx.createChildCtx("ifFalse", ctx.builderRef, ifFalse),
          ifStatement.ifFalseStatement);
      if (LLVMGetBasicBlockTerminator(LLVMGetInsertBlock(ctx.builderRef)) == null) {
        LLVMBuildBr(ctx.builderRef, end);
      }

      LLVMPositionBuilderAtEnd(ctx.builderRef, end);
    }
  }

  public static void visitWhile(EmitContext ctx, ParseNodeWhileStatement whileStatement) {

    LLVMBasicBlockRef loopBody = LLVMAppendBasicBlock(LLVM.LLVMGetBasicBlockParent(ctx.blockRef),
        "");
    LLVMBasicBlockRef end = LLVMAppendBasicBlock(LLVM.LLVMGetBasicBlockParent(ctx.blockRef), "end");

    LLVMBuildCondBr(ctx.builderRef,
        visitTruthy(ctx,
            visitExpression(ctx, whileStatement.conditionExpression.root, false)),
        loopBody, end);

    LLVMPositionBuilderAtEnd(ctx.builderRef, loopBody);
    visitStatement(ctx.createChildCtx("while", ctx.builderRef, loopBody), whileStatement.statement);
    LLVMBuildCondBr(ctx.builderRef,
        visitTruthy(ctx,
            visitExpression(ctx, whileStatement.conditionExpression.root, false)),
        loopBody, end);

    LLVMPositionBuilderAtEnd(ctx.builderRef, end);
  }

  public static void visitReturn(EmitContext ctx, ParseNodeReturnStatement returnStatement) {
    if (returnStatement.value != null) {
      LLVMBuildStore(ctx.builderRef, visitExpression(ctx, returnStatement.value.root, false),
          ctx.retValRef);
    }
    LLVMBuildBr(ctx.builderRef, ctx.retBlockRef);
  }
}