package com.mattmerr.hacc;

import static org.bytedeco.javacpp.LLVM.LLVMBuildAlloca;
import static org.bytedeco.javacpp.LLVM.LLVMBuildStore;
import static org.bytedeco.javacpp.LLVM.LLVMConstPointerNull;
import static org.bytedeco.javacpp.LLVM.LLVMStructCreateNamed;

import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMBuilderRef;
import org.bytedeco.javacpp.LLVM.LLVMTypeRef;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;

public interface EmitItem {

  public static class EmitItemType implements EmitItem {

    private final LLVMTypeRef typeRef;

    @Deprecated
    public EmitItemType() {
      this.typeRef = LLVM.LLVMInt32Type();
    }

    public EmitItemType(LLVMTypeRef typeRef) {
      this.typeRef = typeRef;
    }

//    public EmitItemType(EmitContext ctx, String string) {
//      typeRef = LLVMStructCreateNamed(LLVMctx.getModuleRef()string);
//     }

    public LLVMTypeRef getTypeRef() {
      return this.typeRef;
    }
  }

  public static class EmitItemVar implements EmitItem {

    public final String name;
    public final LLVMValueRef pointer;

//    public EmitItemVar(String name, LLVMTypeRef typeRef) {
//      this.name = name;
//      this.pointer = LLVMBuildAlloca(null, typeRef, "");
//    }

    protected EmitItemVar(String name, LLVMValueRef pointer) {
      this.name = name;
      this.pointer = pointer;
    }

    void assign(EmitContext emitContext, LLVMBuilderRef builderRef, LLVMValueRef valueRef) {
      LLVMBuildStore(builderRef, valueRef, pointer);
    }

  }

  public static class EmitItemFunction extends EmitItemVar {

    EmitScope scope;

    public EmitItemFunction(String name, LLVMValueRef valRef) {
      super(name, valRef);
    }
  }

}
