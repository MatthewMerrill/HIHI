package com.mattmerr.hacc;

import static org.bytedeco.javacpp.LLVM.LLVMAddFunction;
import static org.bytedeco.javacpp.LLVM.LLVMAppendBasicBlock;
import static org.bytedeco.javacpp.LLVM.LLVMBuildAdd;
import static org.bytedeco.javacpp.LLVM.LLVMBuildBr;
import static org.bytedeco.javacpp.LLVM.LLVMBuildCall;
import static org.bytedeco.javacpp.LLVM.LLVMBuildGlobalStringPtr;
import static org.bytedeco.javacpp.LLVM.LLVMBuildPhi;
import static org.bytedeco.javacpp.LLVM.LLVMBuildRet;
import static org.bytedeco.javacpp.LLVM.LLVMCCallConv;
import static org.bytedeco.javacpp.LLVM.LLVMConstBitCast;
import static org.bytedeco.javacpp.LLVM.LLVMConstGEP;
import static org.bytedeco.javacpp.LLVM.LLVMConstInt;
import static org.bytedeco.javacpp.LLVM.LLVMConstIntToPtr;
import static org.bytedeco.javacpp.LLVM.LLVMConstPointerCast;
import static org.bytedeco.javacpp.LLVM.LLVMConstPointerNull;
import static org.bytedeco.javacpp.LLVM.LLVMConstString;
import static org.bytedeco.javacpp.LLVM.LLVMCreateBuilder;
import static org.bytedeco.javacpp.LLVM.LLVMFunctionType;
import static org.bytedeco.javacpp.LLVM.LLVMGetElementPtr;
import static org.bytedeco.javacpp.LLVM.LLVMGetElementType;
import static org.bytedeco.javacpp.LLVM.LLVMGetParam;
import static org.bytedeco.javacpp.LLVM.LLVMInt32Type;
import static org.bytedeco.javacpp.LLVM.LLVMInt8Type;
import static org.bytedeco.javacpp.LLVM.LLVMIntToPtr;
import static org.bytedeco.javacpp.LLVM.LLVMIsAGetElementPtrInst;
import static org.bytedeco.javacpp.LLVM.LLVMMDString;
import static org.bytedeco.javacpp.LLVM.LLVMModuleCreateWithName;
import static org.bytedeco.javacpp.LLVM.LLVMPointerType;
import static org.bytedeco.javacpp.LLVM.LLVMPositionBuilderAtEnd;
import static org.bytedeco.javacpp.LLVM.LLVMSetFunctionCallConv;
import static org.bytedeco.javacpp.LLVM.LLVMVoidType;
import static org.bytedeco.javacpp.LLVM.lto_codegen_set_assembler_args;

import com.mattmerr.hihi.HProg;
import com.mattmerr.hitch.parsetokens.ParseNodeBlock;
import com.mattmerr.hitch.parsetokens.ParseNodeCall;
import com.mattmerr.hitch.parsetokens.ParseNodeExpression;
import com.mattmerr.hitch.parsetokens.ParseNodeFunction;
import com.mattmerr.hitch.parsetokens.ParseNodeFunctionDeclaration;
import com.mattmerr.hitch.parsetokens.ParseNodeStatement;
import com.mattmerr.hitch.parsetokens.expression.Literal;
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

  public static HScope visit(ParseNodeBlock block) {
    LLVMModuleRef mod = LLVMModuleCreateWithName("my_module");
    LLVMBuilderRef builder = LLVMCreateBuilder();

    HScope scope = new HScope(mod);
    scope.declareStandardFunctions();
    visit(scope, builder, null, block);
    return scope;
  }

  public static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef, ParseNodeStatement statement) {
    if (statement instanceof ParseNodeBlock) {
      return visit(scope, builderRef, blockRef, (ParseNodeBlock) statement);
    }
    if (statement instanceof ParseNodeFunctionDeclaration) {
      return visit(scope, builderRef, blockRef, (ParseNodeFunctionDeclaration) statement);
    }
    if (statement instanceof ParseNodeCall) {
      return visit(scope, builderRef, blockRef, (ParseNodeCall) statement);
    }
    throw new UnsupportedOperationException("No known visit for " + statement.getClass());
  }

  public static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef,
      ParseNodeFunctionDeclaration decl) {
    ParseNodeFunction function = decl.function;
    LLVMTypeRef paramTypes[] = new LLVMTypeRef[function.argumentMapping.size()];

    for (int i = 0; i < paramTypes.length; i++) {
      paramTypes[i] = LLVMInt32Type();
    }

    LLVMTypeRef retType = LLVMFunctionType(LLVMVoidType(), new PointerPointer<>(paramTypes),
        paramTypes.length, 0);
    LLVMValueRef func = LLVMAddFunction(scope.getModuleRef(), decl.function.name, retType);
    LLVMSetFunctionCallConv(func, LLVMCCallConv);

    LLVMBasicBlockRef entry = LLVMAppendBasicBlock(func, "funcBlock");
    LLVMBuilderRef builder = LLVMCreateBuilder();

    LLVMPositionBuilderAtEnd(builder, entry);
    visit(scope, builder, entry, function.definition);

    LLVMPositionBuilderAtEnd(builder, entry);
//    LLVMValueRef res = LLVMBuildPhi(builder, LLVMInt32Type(), "result");
    LLVMBuildRet(builder, null);

//    LLVM.Termin

//    LLVMPositionBuilderAtEnd(builder, entry);

    scope.declare(decl.function.name);
    scope.put(decl.function.name, func);

    // TODO(matthewmerrill): Return val
//    LLVMValueRef val = visit(mod, function.definition);
//    LLVMBuildRet(builder, val);

    return func;
  }

  public static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef, ParseNodeBlock block) {
    for (ParseNodeStatement statement : block.getStatementList()) {
      visit(scope, builderRef, blockRef, statement);
    }
    return null;
  }

  public static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef, ParseNodeCall call) {
    LLVMValueRef callArgs[] = new LLVMValueRef[call.arguments.size()];

    for (int i = 0; i < callArgs.length; i++) {
      callArgs[i] = visit(scope, builderRef, call.arguments.get(i));
    }

    LLVMValueRef callRef = LLVMBuildCall(builderRef, scope.get(call.qualifiedFunction),
        new PointerPointer<>(callArgs), callArgs.length, "wtf");
//    LLVMBuildBr(builderRef, blockRef);

    return null;
  }

  public static LLVMValueRef visit(HScope scope, LLVMBuilderRef builderRef,
      ParseNodeExpression expression) {

    if (expression.root instanceof Literal) {
      Literal literal = (Literal) expression.root;
      Value value = literal.value;

      if (value.type == TokenType.STRING)
        return LLVMBuildGlobalStringPtr(builderRef, (String) value.value, "hw");
    }

    throw new UnsupportedOperationException("Only Literal String Expressions supported");
  }

}
