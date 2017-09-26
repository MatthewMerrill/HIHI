package com.mattmerr.hacc;

import static com.mattmerr.hacc.HTypeString.visitType;
import static java.util.Arrays.asList;
import static org.bytedeco.javacpp.LLVM.LLVMAppendBasicBlock;
import static org.bytedeco.javacpp.LLVM.LLVMBuildAdd;
import static org.bytedeco.javacpp.LLVM.LLVMBuildAlloca;
import static org.bytedeco.javacpp.LLVM.LLVMBuildArrayAlloca;
import static org.bytedeco.javacpp.LLVM.LLVMBuildCall;
import static org.bytedeco.javacpp.LLVM.LLVMBuildExtractValue;
import static org.bytedeco.javacpp.LLVM.LLVMBuildInsertValue;
import static org.bytedeco.javacpp.LLVM.LLVMBuildLoad;
import static org.bytedeco.javacpp.LLVM.LLVMBuildRet;
import static org.bytedeco.javacpp.LLVM.LLVMBuildStore;
import static org.bytedeco.javacpp.LLVM.LLVMBuildStructGEP;
import static org.bytedeco.javacpp.LLVM.LLVMCCallConv;
import static org.bytedeco.javacpp.LLVM.LLVMConstInt;
import static org.bytedeco.javacpp.LLVM.LLVMGetParam;
import static org.bytedeco.javacpp.LLVM.LLVMInt1Type;
import static org.bytedeco.javacpp.LLVM.LLVMInt32Type;
import static org.bytedeco.javacpp.LLVM.LLVMInt8Type;
import static org.bytedeco.javacpp.LLVM.LLVMPositionBuilderAtEnd;
import static org.bytedeco.javacpp.LLVM.LLVMSetFunctionCallConv;

import java.util.List;
import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;
import org.bytedeco.javacpp.LLVM.LLVMBuilderRef;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;
import org.bytedeco.javacpp.PointerPointer;

public class HNativeStringFunctions {

  public static final List<Implementer> STRING_FUNCTION_IMPLEMENTERS = asList(new Implementer[]{
      HNativeStringFunctions::implementConcat,
      HNativeStringFunctions::implementToString,
  });

  public static final void implementConcat(HScope scope) {
    LLVMValueRef concatFunc = LLVM.LLVMGetNamedFunction(scope.getModuleRef(), "concat");
    LLVMBuilderRef builderRef = LLVM.LLVMCreateBuilder();
    LLVMBasicBlockRef blockRef = LLVMAppendBasicBlock(concatFunc, "");

    LLVMSetFunctionCallConv(concatFunc, LLVMCCallConv);
    LLVMPositionBuilderAtEnd(builderRef, blockRef);

    LLVMValueRef str0 = LLVMBuildLoad(builderRef, LLVMGetParam(concatFunc, 0), "");
    LLVMValueRef str1 = LLVMBuildLoad(builderRef, LLVMGetParam(concatFunc, 0), "");

    LLVMValueRef str0Val = LLVMBuildExtractValue(builderRef, str0, 1, "");
    LLVMValueRef str0Len = LLVMBuildExtractValue(builderRef, str0, 2, "");
    LLVMValueRef str1Val = LLVMBuildExtractValue(builderRef, str1, 1, "");
    LLVMValueRef str1Len = LLVMBuildExtractValue(builderRef, str1, 2, "");

    LLVMValueRef dest = LLVMBuildAlloca(builderRef, visitType(scope, "string"), "");
    LLVMValueRef destLen = LLVMBuildAdd(builderRef, str0Len, str1Len, "");
    LLVMValueRef destVal = LLVMBuildArrayAlloca(builderRef, LLVMInt8Type(), destLen, "");
    LLVMBuildStore(builderRef, destVal, LLVMBuildStructGEP(builderRef, dest, 1, ""));
    LLVMBuildStore(builderRef, destLen, LLVMBuildStructGEP(builderRef, dest, 2, ""));

    LLVMBuildCall(builderRef, scope.get("strcpy"), new PointerPointer<>(new LLVMValueRef[]{
        destVal,
        str0Val,
        str0Len,
        LLVMConstInt(LLVMInt32Type(), 0, 0),
        LLVMConstInt(LLVMInt1Type(), 0, 0),
    }), 5, "");

    LLVMBuildRet(builderRef, dest);
  }

  public static final void implementToString(HScope scope) {
    LLVMValueRef toStringFunc = LLVM.LLVMGetNamedFunction(scope.getModuleRef(), "toString");
    LLVMBuilderRef builderRef = LLVM.LLVMCreateBuilder();
    LLVMBasicBlockRef blockRef = LLVMAppendBasicBlock(toStringFunc, "");

    LLVMSetFunctionCallConv(toStringFunc, LLVMCCallConv);
    LLVMPositionBuilderAtEnd(builderRef, blockRef);

    LLVMValueRef str0 = LLVMGetParam(toStringFunc, 0);
    LLVMBuildRet(builderRef, str0);
  }

}
