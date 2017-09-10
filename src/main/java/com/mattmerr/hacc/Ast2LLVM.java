package com.mattmerr.hacc;

import static java.lang.String.format;
import static org.bytedeco.javacpp.LLVM.LLVMAddFunction;
import static org.bytedeco.javacpp.LLVM.LLVMAppendBasicBlock;
import static org.bytedeco.javacpp.LLVM.LLVMBuildAdd;
import static org.bytedeco.javacpp.LLVM.LLVMBuildAlloca;
import static org.bytedeco.javacpp.LLVM.LLVMBuildBr;
import static org.bytedeco.javacpp.LLVM.LLVMBuildCall;
import static org.bytedeco.javacpp.LLVM.LLVMBuildCondBr;
import static org.bytedeco.javacpp.LLVM.LLVMBuildGlobalStringPtr;
import static org.bytedeco.javacpp.LLVM.LLVMBuildICmp;
import static org.bytedeco.javacpp.LLVM.LLVMBuildLoad;
import static org.bytedeco.javacpp.LLVM.LLVMBuildRet;
import static org.bytedeco.javacpp.LLVM.LLVMBuildStore;
import static org.bytedeco.javacpp.LLVM.LLVMCCallConv;
import static org.bytedeco.javacpp.LLVM.LLVMConstInt;
import static org.bytedeco.javacpp.LLVM.LLVMConstPointerNull;
import static org.bytedeco.javacpp.LLVM.LLVMCreateBuilder;
import static org.bytedeco.javacpp.LLVM.LLVMFunctionType;
import static org.bytedeco.javacpp.LLVM.LLVMGetParam;
import static org.bytedeco.javacpp.LLVM.LLVMInt32Type;
import static org.bytedeco.javacpp.LLVM.LLVMInt8Type;
import static org.bytedeco.javacpp.LLVM.LLVMIntNE;
import static org.bytedeco.javacpp.LLVM.LLVMModuleCreateWithName;
import static org.bytedeco.javacpp.LLVM.LLVMPointerType;
import static org.bytedeco.javacpp.LLVM.LLVMPositionBuilderAtEnd;
import static org.bytedeco.javacpp.LLVM.LLVMSetFunctionCallConv;
import static org.bytedeco.javacpp.LLVM.LLVMTypeOf;
import static org.bytedeco.javacpp.LLVM.LLVMVoidType;
import static org.bytedeco.javacpp.LLVM.thinlto_codegen_add_cross_referenced_symbol;

import com.mattmerr.hihi.HProg;
import com.mattmerr.hitch.parsetokens.ParseNodeAssignedDeclaration;
import com.mattmerr.hitch.parsetokens.ParseNodeBlock;
import com.mattmerr.hitch.parsetokens.ParseNodeCall;
import com.mattmerr.hitch.parsetokens.ParseNodeDeclaration;
import com.mattmerr.hitch.parsetokens.ParseNodeExpression;
import com.mattmerr.hitch.parsetokens.ParseNodeFunction;
import com.mattmerr.hitch.parsetokens.ParseNodeFunctionDeclaration;
import com.mattmerr.hitch.parsetokens.ParseNodeIfStatement;
import com.mattmerr.hitch.parsetokens.ParseNodeStatement;
import com.mattmerr.hitch.parsetokens.expression.ExpressionToken;
import com.mattmerr.hitch.parsetokens.expression.Literal;
import com.mattmerr.hitch.parsetokens.expression.Variable;
import com.mattmerr.hitch.tokens.TokenType;
import com.mattmerr.hitch.tokens.Value;
import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;
import org.bytedeco.javacpp.LLVM.LLVMBuilderRef;
import org.bytedeco.javacpp.LLVM.LLVMModuleRef;
import org.bytedeco.javacpp.LLVM.LLVMTypeRef;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;
import org.bytedeco.javacpp.PointerPointer;

public class Ast2LLVM {

  public static LLVMModuleRef create(HProg prog) {
    LLVMModuleRef mod = LLVMModuleCreateWithName("my_module");

    LLVMTypeRef paramTypes[] = {LLVMInt32Type(), LLVMInt32Type()};
    LLVMTypeRef retType = LLVMFunctionType(LLVMInt32Type(), new PointerPointer<>(paramTypes), 2, 0);
    LLVMValueRef sum = LLVMAddFunction(mod, "sum", retType);
    LLVMBasicBlockRef entry = LLVMAppendBasicBlock(sum, "entry");
    LLVMBuilderRef builder = LLVMCreateBuilder();
    LLVMPositionBuilderAtEnd(builder, entry);

    LLVMValueRef tmp = LLVMBuildAdd(builder, LLVMGetParam(sum, 0), LLVMGetParam(sum, 1), "tmp");
    LLVMBuildRet(builder, tmp);

    return mod;
  }

  static HScope visit(ParseNodeBlock block) {
    LLVMModuleRef mod = LLVMModuleCreateWithName("my_module");
    LLVMBuilderRef builder = LLVMCreateBuilder();

    HScope scope = new HScope(mod);
    scope.declareStandardFunctions();
    visit(scope, builder, null, block);
    return scope;
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef, ParseNodeStatement statement) {
    if (statement instanceof ParseNodeBlock) {
      return visit(scope, builderRef, blockRef, (ParseNodeBlock) statement);
    }
    else if (statement instanceof ParseNodeFunctionDeclaration) {
      return visit(scope, builderRef, blockRef, (ParseNodeFunctionDeclaration) statement);
    }
    else if (statement instanceof ParseNodeCall) {
      return visit(scope, builderRef, blockRef, (ParseNodeCall) statement);
    }
    else if (statement instanceof ParseNodeIfStatement) {
      return visit(scope, builderRef, blockRef, (ParseNodeIfStatement) statement);
    }
    else if (statement instanceof ParseNodeDeclaration) {
      return visit(scope, builderRef, blockRef, (ParseNodeDeclaration) statement);
    }
    throw new UnsupportedOperationException("No known visit for " + statement.getClass());
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef,
      ParseNodeFunctionDeclaration decl) {
    ParseNodeFunction function = decl.function;
    LLVMTypeRef paramTypes[] = new LLVMTypeRef[function.argumentMapping.size()];

    for (int i = 0; i < paramTypes.length; i++) {
      paramTypes[i] = LLVMPointerType(LLVMInt8Type(), 0);
    }

    LLVMTypeRef retType = LLVMFunctionType(visitType(scope, builderRef, function.returnType),
        new PointerPointer<>(paramTypes), paramTypes.length, 0);
    LLVMValueRef func = LLVMAddFunction(scope.getModuleRef(), decl.function.name, retType);
    LLVMSetFunctionCallConv(func, LLVMCCallConv);

    for (int paramIdx = 0; paramIdx < paramTypes.length; paramIdx++) {
      String paramName = function.argumentMapping.get(paramIdx);
      scope.declare(paramName, visitType(scope, builderRef, function.argumentTypes.get(paramIdx)));
      scope.put(paramName, LLVMGetParam(func, paramIdx));
    }

    LLVMBasicBlockRef entry = LLVMAppendBasicBlock(func, "");
    LLVMBuilderRef builder = LLVMCreateBuilder();

    LLVMPositionBuilderAtEnd(builder, entry);
    visit(scope, builder, entry, function.definition);

//    LLVMValueRef res = LLVMBuildPhi(builder, LLVMInt32Type(), "result");
    LLVMBuildRet(builder, null);

//    LLVM.Termin

//    LLVMPositionBuilderAtEnd(builder, entry);

    scope.declare(decl.function.name, retType);
    scope.put(decl.function.name, func);

    // TODO(matthewmerrill): Return val
//    LLVMValueRef val = visit(mod, function.definition);
//    LLVMBuildRet(builder, val);

    return func;
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef, ParseNodeIfStatement ifStatement) {

    if (ifStatement.ifFalseStatement == null) {
      LLVMBasicBlockRef ifTrue = LLVMAppendBasicBlock(LLVM.LLVMGetBasicBlockParent(blockRef), "");
      LLVMBasicBlockRef end = LLVMAppendBasicBlock(LLVM.LLVMGetBasicBlockParent(blockRef), "end");

      LLVMValueRef conditionVal = visit(scope, builderRef, ifStatement.conditionExpression);
      LLVMValueRef ifRef = LLVMBuildICmp(builderRef, LLVMIntNE, conditionVal,
          LLVMConstInt(LLVMInt32Type(), 0, 0), "");
      LLVMBuildCondBr(builderRef, ifRef, ifTrue, end);

      LLVMPositionBuilderAtEnd(builderRef, ifTrue);
      visit(scope, builderRef, ifTrue, ifStatement.ifTrueStatement);
      LLVMBuildBr(builderRef, end);

      LLVMPositionBuilderAtEnd(builderRef, end);
      return ifRef;
    }
    else {
      LLVMBasicBlockRef ifTrue = LLVMAppendBasicBlock(LLVM.LLVMGetBasicBlockParent(blockRef), "");
      LLVMBasicBlockRef ifFalse = LLVMAppendBasicBlock(LLVM.LLVMGetBasicBlockParent(blockRef), "");
      LLVMBasicBlockRef end = LLVMAppendBasicBlock(LLVM.LLVMGetBasicBlockParent(blockRef), "");

      LLVMValueRef conditionVal = visit(scope, builderRef, ifStatement.conditionExpression);
      LLVMValueRef ifRef = LLVMBuildICmp(builderRef, LLVMIntNE, conditionVal,
          LLVMConstInt(LLVMInt32Type(), 0, 0), "");
      LLVMBuildCondBr(builderRef, ifRef, ifTrue, ifFalse);

      LLVMPositionBuilderAtEnd(builderRef, ifTrue);
      visit(scope, builderRef, ifTrue, ifStatement.ifTrueStatement);
      LLVMBuildBr(builderRef, end);

      LLVMPositionBuilderAtEnd(builderRef, ifFalse);
      visit(scope, builderRef, ifFalse, ifStatement.ifFalseStatement);
      LLVMBuildBr(builderRef, end);

      LLVMPositionBuilderAtEnd(builderRef, end);
      return ifRef;
    }
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef, ParseNodeDeclaration declaration) {

    LLVMTypeRef type = visitType(scope, builderRef, declaration.type);
    LLVMValueRef valueRef = LLVMBuildAlloca(builderRef, type, "");
    scope.declare(declaration.qualifiedIdentifier, type);
    scope.put(declaration.qualifiedIdentifier, valueRef);

    if (declaration instanceof ParseNodeAssignedDeclaration) {
      ParseNodeAssignedDeclaration assignment = (ParseNodeAssignedDeclaration) declaration;
      LLVMBuildStore(builderRef, visit(scope, builderRef, assignment.assignmentExpression),
          valueRef);
    }

    return valueRef;
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef, ParseNodeBlock block) {
    for (ParseNodeStatement statement : block.getStatementList()) {
      visit(scope, builderRef, blockRef, statement);
    }
    return null;
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef, ParseNodeCall call) {
    LLVMValueRef callArgs[] = new LLVMValueRef[call.arguments.size()];

    for (int i = 0; i < callArgs.length; i++) {
      callArgs[i] = visit(scope, builderRef, call.arguments.get(i));
    }

    LLVMValueRef callRef = LLVMBuildCall(builderRef, scope.get(call.qualifiedFunction),
        new PointerPointer<>(callArgs), callArgs.length, "");
//    LLVMBuildBr(builderRef, blockRef);

    return null;
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      ParseNodeExpression expression) {
    return visit(scope, builderRef, expression.root);
  }

  private static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      ExpressionToken expressionToken) {
    if (expressionToken instanceof Literal) {
      Literal literal = (Literal) expressionToken;
      Value value = literal.value;

      if (value.type == TokenType.STRING) {
        return LLVMBuildGlobalStringPtr(builderRef, (String) value.value, "");
      }
      else if (value.type == TokenType.INTEGER) {
        return LLVMConstInt(LLVMInt32Type(), (Integer) value.value, 0);
      }
    }
    else if (expressionToken instanceof Variable) {
      Variable var = (Variable) expressionToken;
      LLVMValueRef valueRef = scope.get(var.qualifiedName);

      if (valueRef == null) {
        throw new RuntimeException("Undeclared variable: " + var.qualifiedName);
      }

      return LLVMBuildLoad(builderRef, valueRef, "");
    }
//    else if (expressionToken instanceof Operation) {
//      Operation operation = (Operation) expressionToken;
//
//      if (operation.type == OperationType.ADD) {
//        BinaryOperation binop = (BinaryOperation) operation;
//
//        if ("String".equals(getType(scope, binop.left).name) && "String".equals(getType(scope,
// binop.right).getType().name)) {
//          LLVMValueRef leftRef = visit(scope, builderRef, binop.left);
//          LLVMValueRef rightRef = visit(scope, builderRef, binop.left);
//          return LLVMBuildCall(builderRef, scope.get("llvm.memcpy.p0i8.p0i8.i32"), new
// PointerPointer<>(
//              leftRef,
//              rightRef,
//              LLVMConstInt(LLVMInt32Type(), 1, 0),
//              LLVMConstInt(LLVMInt32Type(), 1, 0),
//              LLVMConstInt(LLVMInt1Type(), 1, 0)
//          ), 5, "");
//        }
//      }
//    }

    throw new UnsupportedOperationException("Not all expressions supported.");
  }

  private static LLVMTypeRef visitType(HScope scope, LLVMBuilderRef builderRef, String typeName) {
    if ("int".equals(typeName)) {
      return LLVMInt32Type();
    }
    else if ("string".equals(typeName)) {
      return LLVMPointerType(LLVMInt8Type(), 0);
    }
    else if ("void".equals(typeName)) {
      return LLVMVoidType();
    }
    else {
      throw new IllegalArgumentException(format("Unknown Type: \"%s\"", typeName));
    }
  }

}
