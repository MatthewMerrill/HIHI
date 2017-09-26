package com.mattmerr.hacc;

import static org.bytedeco.javacpp.LLVM.LLVMAddFunction;
import static org.bytedeco.javacpp.LLVM.LLVMFunctionType;
import static org.bytedeco.javacpp.LLVM.LLVMFunctionTypeKind;
import static org.bytedeco.javacpp.LLVM.LLVMGetElementType;
import static org.bytedeco.javacpp.LLVM.LLVMGetTypeKind;
import static org.bytedeco.javacpp.LLVM.LLVMInt1Type;
import static org.bytedeco.javacpp.LLVM.LLVMInt32Type;
import static org.bytedeco.javacpp.LLVM.LLVMInt8Type;
import static org.bytedeco.javacpp.LLVM.LLVMPointerType;
import static org.bytedeco.javacpp.LLVM.LLVMTypeOf;
import static org.bytedeco.javacpp.LLVM.LLVMVoidType;

import com.mattmerr.hitch.parsetokens.expression.Variable;
import com.sun.istack.internal.Pool.Impl;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import org.bytedeco.javacpp.LLVM;
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
  private Map<String, CompiledFile> dependencyMap = new HashMap<>();

  private Queue<Implementer> implementerQueue = new ArrayDeque<>();

  public HScope(LLVMModuleRef mod) {
    this.mod = mod;
  }

  public HScope(LLVMModuleRef mod, HScope parentScope) {
    this.mod = mod;
    this.parent = parentScope;
    this.dependencyMap = parentScope.dependencyMap;
  }

  public LLVMModuleRef getModuleRef() {
    return mod;
  }

  public void declareStandardFunctions() {
    LLVMValueRef memcpy = LLVMAddFunction(mod, "llvm.memcpy.p0i8.p0i8.i32",
        LLVMFunctionType(LLVMVoidType(),
            new PointerPointer<>(
                LLVMPointerType(LLVMInt8Type(), 0), // dest
                LLVMPointerType(LLVMInt8Type(), 0), // src
                LLVMInt32Type(), // len
                LLVMInt32Type(),
                LLVMInt1Type()
            ), 5, 0));
    declare("strcpy", LLVMTypeOf(memcpy));
    put("strcpy", memcpy);
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

  public void declareDependency(String identifier, CompiledFile compiledFile) {
    dependencyMap.put(identifier.substring(identifier.lastIndexOf(".")+1), compiledFile);

    for (Map.Entry<String, LLVMValueRef> entry : compiledFile.scope.valueMap.entrySet()) {
      if (LLVMGetTypeKind(LLVMGetElementType(LLVMTypeOf(entry.getValue())))
          == LLVMFunctionTypeKind) {
        LLVMValueRef func = LLVMAddFunction(mod, compiledFile.id + "." + entry.getKey(),
            LLVMGetElementType(LLVMTypeOf(entry.getValue())));
        declare(compiledFile.id + "." + entry.getKey(), LLVMTypeOf(func));
        put(compiledFile.id + "." + entry.getKey(), func);
      }
    }
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

  public void addImplementer(Implementer implementer) {
    implementerQueue.add(implementer);
  }

  public void addImplementers(Collection<Implementer> implementers) {
    implementerQueue.addAll(implementers);
  }

  public void implement() {
    while (!implementerQueue.isEmpty()) { implementerQueue.remove().implement(this); }
  }

  public boolean isDependency(String id) {
    return dependencyMap.containsKey(id);
  }

  public CompiledFile getDependency(String id) {
    return dependencyMap.get(id);
  }

  public boolean isTopLevel() {
    return parent == null;
  }

  public Collection<CompiledFile> getDependencies() {
    return dependencyMap.values();
  }
}
