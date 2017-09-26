package com.mattmerr.hacc;

import static com.mattmerr.hacc.HTypeString.visitType;
import static java.util.Arrays.asList;
import static org.bytedeco.javacpp.LLVM.LLVMAddFunction;
import static org.bytedeco.javacpp.LLVM.LLVMAppendBasicBlock;
import static org.bytedeco.javacpp.LLVM.LLVMBuildCall;
import static org.bytedeco.javacpp.LLVM.LLVMBuildGlobalStringPtr;
import static org.bytedeco.javacpp.LLVM.LLVMBuildLoad;
import static org.bytedeco.javacpp.LLVM.LLVMBuildRetVoid;
import static org.bytedeco.javacpp.LLVM.LLVMBuildStructGEP;
import static org.bytedeco.javacpp.LLVM.LLVMCCallConv;
import static org.bytedeco.javacpp.LLVM.LLVMCreateBuilder;
import static org.bytedeco.javacpp.LLVM.LLVMFunctionType;
import static org.bytedeco.javacpp.LLVM.LLVMGetParam;
import static org.bytedeco.javacpp.LLVM.LLVMInt32Type;
import static org.bytedeco.javacpp.LLVM.LLVMInt8Type;
import static org.bytedeco.javacpp.LLVM.LLVMPointerType;
import static org.bytedeco.javacpp.LLVM.LLVMPositionBuilderAtEnd;
import static org.bytedeco.javacpp.LLVM.LLVMSetFunctionCallConv;
import static org.bytedeco.javacpp.LLVM.LLVMVoidType;

import java.util.List;
import org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;
import org.bytedeco.javacpp.LLVM.LLVMBuilderRef;
import org.bytedeco.javacpp.LLVM.LLVMTypeRef;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;
import org.bytedeco.javacpp.PointerPointer;

public class HNativeFunctions {

  private static LLVMValueRef printf;

  public static final List<Implementer> NATIVE_FUNCTION_IMPLEMENTERS = asList(new Implementer[]{
      HNativeFunctions::defineNativePrintf,
      HNativeFunctions::definePrint,
      HNativeFunctions::definePrintln
  });

  private static void defineNativePrintf(HScope scope) {
    printf = LLVMAddFunction(scope.getModuleRef(), "printf",
        LLVMFunctionType(LLVMInt32Type(),
            new PointerPointer<>(new LLVMTypeRef[]{LLVMPointerType(LLVMInt8Type(), 0)}), 1, 1));
  }

  private static void definePrint(HScope scope) {
    LLVMTypeRef funcType = LLVMFunctionType(LLVMVoidType(), new PointerPointer<>(new LLVMTypeRef[]{
        LLVMPointerType(visitType(scope, "string"), 0),
    }), 1, 0);
    LLVMValueRef func = LLVMAddFunction(scope.getModuleRef(), "print", funcType);
    LLVMSetFunctionCallConv(func, LLVMCCallConv);
    LLVMBasicBlockRef block = LLVMAppendBasicBlock(func, "");

    LLVMBuilderRef builderRef = LLVMCreateBuilder();
    LLVMPositionBuilderAtEnd(builderRef, block);

    LLVMValueRef[] args = {
        LLVMBuildGlobalStringPtr(builderRef, "%s", ""),
        LLVMBuildLoad(builderRef, LLVMBuildStructGEP(builderRef, LLVMGetParam(func, 0), 1, ""), ""),
    };
    LLVMBuildCall(builderRef, printf, new PointerPointer<>(args), 2, "");

    LLVMPositionBuilderAtEnd(builderRef, block);
    LLVMBuildRetVoid(builderRef);

    scope.declare("print", funcType);
    scope.put("print", func);
  }

  private static void definePrintln(HScope scope) {
    LLVMTypeRef funcType = LLVMFunctionType(LLVMVoidType(), new PointerPointer<>(new LLVMTypeRef[]{
        LLVMPointerType(visitType(scope, "string"), 0),
    }), 1, 0);
    LLVMValueRef func = LLVMAddFunction(scope.getModuleRef(), "println", funcType);
    LLVMSetFunctionCallConv(func, LLVMCCallConv);
    LLVMBasicBlockRef block = LLVMAppendBasicBlock(func, "");

    LLVMBuilderRef builderRef = LLVMCreateBuilder();
    LLVMPositionBuilderAtEnd(builderRef, block);

    LLVMValueRef[] args = {
        LLVMBuildGlobalStringPtr(builderRef, "%s\n", ""),
        LLVMBuildLoad(builderRef, LLVMBuildStructGEP(builderRef, LLVMGetParam(func, 0), 1, ""), ""),
    };
    LLVMBuildCall(builderRef, printf, new PointerPointer<>(args), 2, "");

    LLVMPositionBuilderAtEnd(builderRef, block);
    LLVMBuildRetVoid(builderRef);

    scope.declare("println", funcType);
    scope.put("println", func);
  }


}
