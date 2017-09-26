package com.mattmerr.hacc;

import static com.mattmerr.hacc.HTypeString.visitType;
import static org.bytedeco.javacpp.LLVM.LLVMAddFunction;
import static org.bytedeco.javacpp.LLVM.LLVMAddGlobal;
import static org.bytedeco.javacpp.LLVM.LLVMConstArray;
import static org.bytedeco.javacpp.LLVM.LLVMConstStruct;
import static org.bytedeco.javacpp.LLVM.LLVMFunctionType;
import static org.bytedeco.javacpp.LLVM.LLVMPointerType;
import static org.bytedeco.javacpp.LLVM.LLVMSetGlobalConstant;
import static org.bytedeco.javacpp.LLVM.LLVMSetInitializer;
import static org.bytedeco.javacpp.LLVM.LLVMTypeOf;

import com.mattmerr.hitch.Type;
import java.util.HashMap;
import java.util.Map;
import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMTypeRef;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;
import org.bytedeco.javacpp.PointerPointer;

public class VTableBuilder {

  private static HashMap<Type, LLVMValueRef> functionsOfTypes = null;
  private static int size = -1;

  private static boolean sunk = false;

  public static LLVMValueRef sinkVTable(HScope scope) {
    if (sunk) { return null; }
    if (functionsOfTypes == null) {
      throw new RuntimeException("Cannot sink VTable before generating it");
    }

    LLVMValueRef[] vtable = new LLVMValueRef[size];
    int index = 0;

    for (Map.Entry<Type, LLVMValueRef> entry : functionsOfTypes.entrySet()) {
      vtable[index++] = entry.getValue();
    }

    LLVMValueRef valueRef = LLVMConstArray(LLVM.LLVMInt64Type(), new PointerPointer<>(vtable),
        size);
    LLVMSetGlobalConstant(valueRef, 1);
    LLVMSetInitializer(LLVMAddGlobal(scope.getModuleRef(), LLVMTypeOf(valueRef), ""), valueRef);

    sunk = true;
    return valueRef;
  }

  public static void generateVTable(HScope scope) {
    if (sunk) { return; }
    if (functionsOfTypes != null) {
      throw new RuntimeException("VTable has already been generated!");
    }

    functionsOfTypes = new HashMap<>();

    LLVMTypeRef stringType = visitType(scope, "string");

    LLVMTypeRef toStrFuncType = LLVMFunctionType(LLVMPointerType(stringType, 0),
        new PointerPointer<>(new LLVMTypeRef[]{
            LLVMPointerType(stringType, 0)
        }), 1, 0);
    LLVMValueRef toStrFunc = LLVMAddFunction(scope.getModuleRef(), "toString", toStrFuncType);

    LLVMTypeRef concatFuncType = LLVMFunctionType(
        LLVMPointerType(visitType(scope, "string"), 0),
        new PointerPointer<>(new LLVMTypeRef[]{
            LLVMPointerType(visitType(scope, "string"), 0)
        }), 1, 0);
    LLVMValueRef concatFunc = LLVMAddFunction(scope.getModuleRef(), "concat", concatFuncType);

//    LLVMTypeRef strStruct = LLVMStructCreateNamed(LLVMGetModuleContext(scope.getModuleRef()), "");
//    LLVMStructSetBody(strStruct, new PointerPointer<>(
//        toStrFuncType, concatFuncType
//    ), 1, 0);
//    LLVMValueRef strFuncs = LLVMAddGlobal(scope.getModuleRef(), strStruct, "");

    functionsOfTypes
        .put(Type.StringType, LLVMConstStruct(new PointerPointer<>(toStrFunc, concatFunc), 2, 0));
    size = 1;
  }


}
