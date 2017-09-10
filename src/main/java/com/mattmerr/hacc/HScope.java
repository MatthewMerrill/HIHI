package com.mattmerr.hacc;

import static org.bytedeco.javacpp.LLVM.LLVMAddFunction;
import static org.bytedeco.javacpp.LLVM.LLVMFunctionType;
import static org.bytedeco.javacpp.LLVM.LLVMInt1Type;
import static org.bytedeco.javacpp.LLVM.LLVMInt32Type;
import static org.bytedeco.javacpp.LLVM.LLVMInt8Type;
import static org.bytedeco.javacpp.LLVM.LLVMPointerType;
import static org.bytedeco.javacpp.LLVM.LLVMTypeOf;

import java.util.HashMap;
import java.util.Map;
import org.bytedeco.javacpp.LLVM.LLVMModuleRef;
import org.bytedeco.javacpp.LLVM.LLVMTypeRef;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;
import org.bytedeco.javacpp.PointerPointer;

/**
 * Created by merrillm on 2/5/17.
 */
public class HScope {

  private final LLVMModuleRef mod;
  private HScope parent = null;

  private Map<String, LLVMTypeRef> typeMap = new HashMap<>();
  private Map<String, LLVMValueRef> valueMap = new HashMap<>();

  public HScope(LLVMModuleRef mod) {
    this.mod = mod;
  }

  public HScope(LLVMModuleRef mod, HScope parentScope) {
    this.mod = mod;
    this.parent = parentScope;
  }

  public LLVMModuleRef getModuleRef() {
    return mod;
  }

  public void declareStandardFunctions() {
    LLVMValueRef printf = LLVMAddFunction(mod, "printf",
        LLVMFunctionType(LLVMInt32Type(),
            new PointerPointer<>(new LLVMTypeRef[]{LLVMPointerType(LLVMInt8Type(), 0)}), 1, 1));
    declare("printf", LLVMTypeOf(printf));
    put("printf", printf);

    LLVMValueRef memcpy = LLVMAddFunction(mod, "llvm.memcpy.p0i8.p0i8.i32",
        LLVMFunctionType(LLVMInt32Type(),
            new PointerPointer<>(
                LLVMPointerType(LLVMInt8Type(), 0), // dest
                LLVMPointerType(LLVMInt8Type(), 0), // src
                LLVMInt32Type(), // len
                LLVMInt32Type(),
                LLVMInt1Type()
            ), 5, 0));
    declare("memcpy", LLVMTypeOf(memcpy));
    put("memcpy", memcpy);
  }

  public HScope childScope() {
    return new HScope(mod, this);
  }

  public LLVMValueRef get(String identifier) {
    HScope curScope = this;

    while (curScope != null) {
      if (curScope.valueMap.containsKey(identifier)) { return curScope.valueMap.get(identifier); }

      curScope = curScope.parent;
    }

    throw new RuntimeException("Not declared: '" + identifier + "'");
//        return HObject.UNDEFINED;
  }


  public void declare(String identifier, LLVMTypeRef typeRef) {
    valueMap.put(identifier, null);
    typeMap.put(identifier, typeRef);
  }

  public void put(String identifier, LLVMValueRef value) {
    HScope curScope = this;

    while (curScope != null) {
      if (curScope.valueMap.containsKey(identifier)) {
        curScope.valueMap.put(identifier, value);
        return;
      }

      curScope = curScope.parent;
    }

    throw new RuntimeException("Unknown value to set: " + identifier);
  }

  public LLVMTypeRef getType(String identifier) {
    return typeMap.get(identifier);
  }
}
