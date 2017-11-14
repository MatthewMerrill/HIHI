package com.mattmerr.hacc;

import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;
import org.bytedeco.javacpp.LLVM.LLVMBuilderRef;
import org.bytedeco.javacpp.LLVM.LLVMModuleRef;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;

public class EmitContext {

  public final EmitContext parent;
  public final LLVMModuleRef moduleRef;
  public final String contextPath;
  public final EmitScope scope;
  public final LLVMBuilderRef builderRef;
  public final LLVMBasicBlockRef blockRef;
  public final LLVMValueRef retValRef;
  public final LLVMBasicBlockRef retBlockRef;

  public EmitContext(String moduleName) {
    this.parent = null;
    this.moduleRef = LLVM.LLVMModuleCreateWithName(moduleName);
    this.contextPath = "root";
    this.scope = new EmitScope();
    this.builderRef = null;
    this.blockRef = null;
    this.retValRef = null;
    this.retBlockRef = null;
  }

  private EmitContext(EmitContext parent, EmitScope scope, String contextPath,
      LLVMBuilderRef builderRef, LLVMBasicBlockRef blockRef, LLVMValueRef retValRef,
      LLVMBasicBlockRef retBlockRef) {
    this.parent = parent;
    this.moduleRef = parent.moduleRef;
    this.contextPath = contextPath;
    this.scope = scope;
    this.builderRef = builderRef;
    this.blockRef = blockRef;
    this.retValRef = retValRef;
    this.retBlockRef = retBlockRef;
  }

  public EmitContext createChildCtx(String name) {
    return new EmitContext(this, new EmitScope(scope), this.contextPath + ">" + name, null,
        blockRef, retValRef, retBlockRef);
  }

  public EmitContext createChildCtx(String name, LLVMBuilderRef builderRef) {
    return new EmitContext(this, new EmitScope(scope), this.contextPath + ">" + name, builderRef,
        blockRef, retValRef, retBlockRef);
  }

  public EmitContext createChildCtx(String name, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef) {
    return new EmitContext(this, new EmitScope(scope), this.contextPath + ">" + name, builderRef,
        blockRef, retValRef, retBlockRef);
  }

  public EmitContext createChildCtx(String name, LLVMBuilderRef builderRef,
      LLVMBasicBlockRef blockRef, LLVMValueRef retValRef, LLVMBasicBlockRef retBlockRef) {
    return new EmitContext(this, new EmitScope(scope), this.contextPath + ">" + name, builderRef,
        blockRef, retValRef, retBlockRef);
  }

  public RuntimeException compileException(Exception e) {
    return new RuntimeException("Exception while compiling \"" + contextPath + "\"", e);
  }

  public RuntimeException compileException(String message) {
    return compileException(new RuntimeException(message));
  }

  public RuntimeException compileExceptionWithContextName(String innerContextName, Exception e) {
    return new RuntimeException(
        "Exception while compiling \"" + contextPath + ">" + innerContextName + "\"", e);
  }

  public RuntimeException compileExceptionWithContextName(String innerContextName, String message) {
    return compileExceptionWithContextName(innerContextName, new RuntimeException(message));
  }

  public void throwCompileException(String message) {
    throw compileException(message);
  }

  public LLVMModuleRef getModuleRef() {
    return moduleRef;
  }
}
