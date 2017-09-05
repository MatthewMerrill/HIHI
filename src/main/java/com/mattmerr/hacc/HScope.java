package com.mattmerr.hacc;

import static org.bytedeco.javacpp.LLVM.LLVMAddFunction;
import static org.bytedeco.javacpp.LLVM.LLVMFunctionType;
import static org.bytedeco.javacpp.LLVM.LLVMInt32Type;
import static org.bytedeco.javacpp.LLVM.LLVMInt8Type;
import static org.bytedeco.javacpp.LLVM.LLVMPointerType;

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

  private Map<String, LLVMValueRef> valueMap = new HashMap<>();
//    private Map<String, HType> typeMap = new HashMap<>();

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
    LLVMValueRef func = LLVMAddFunction(mod, "printf",
        LLVMFunctionType(LLVMInt32Type(),
            new PointerPointer<>(new LLVMTypeRef[]{LLVMPointerType(LLVMInt8Type(), 0)}),
            1, 1));

    declare("printf");
    put("printf", func);
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


  public void declare(String identifier) {
    valueMap.put(identifier, null);
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
}
