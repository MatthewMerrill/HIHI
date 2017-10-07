package com.mattmerr.hacc;

import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMBuilderRef;
import org.bytedeco.javacpp.LLVM.LLVMModuleRef;

public class EmitContext {

  public final EmitContext parent;
  public final LLVMModuleRef moduleRef;
  public final String contextPath;
  public final EmitScope scope;
  public final LLVMBuilderRef builderRef;

  public EmitContext(String moduleName) {
    this.parent = null;
    this.moduleRef = LLVM.LLVMModuleCreateWithName(moduleName);
    this.contextPath = "root";
    this.scope = new EmitScope();
    this.builderRef = null;
  }

  private EmitContext(EmitContext parent, EmitScope scope, String contextPath,
      LLVMBuilderRef builderRef) {
    this.parent = parent;
    this.moduleRef = parent.moduleRef;
    this.contextPath = contextPath;
    this.scope = scope;
    this.builderRef = builderRef;
  }

  public EmitContext createChildCtx(String name) {
    return new EmitContext(this, new EmitScope(scope), this.contextPath + ">" + name, null);
  }

  public EmitContext createChildCtx(String name, LLVMBuilderRef builderRef) {
    return new EmitContext(this, new EmitScope(scope), this.contextPath + ">" + name, builderRef);
  }

  public RuntimeException compileException(Exception e) {
    return new RuntimeException("Exception while compiling \"" + contextPath + "\"", e);
  }

  public RuntimeException compileException(String message) {
    return compileException(new RuntimeException(message));
  }

  public void throwCompileException(String message) {
    throw compileException(message);
  }

  public LLVMModuleRef getModuleRef() {
    return moduleRef;
  }
}
