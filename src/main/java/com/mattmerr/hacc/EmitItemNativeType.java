package com.mattmerr.hacc;

import static java.util.Collections.emptyMap;
import static org.bytedeco.javacpp.LLVM.LLVMAlloca;
import static org.bytedeco.javacpp.LLVM.LLVMBuildAlloca;
import static org.bytedeco.javacpp.LLVM.LLVMBuildLoad;
import static org.bytedeco.javacpp.LLVM.LLVMBuildStore;
import static org.bytedeco.javacpp.LLVM.LLVMBuildStructGEP;
import static org.bytedeco.javacpp.LLVM.LLVMConstInt;
import static org.bytedeco.javacpp.LLVM.LLVMGetModuleContext;
import static org.bytedeco.javacpp.LLVM.LLVMInt32Type;
import static org.bytedeco.javacpp.LLVM.LLVMStructCreateNamed;

import com.mattmerr.hacc.EmitItem.EmitItemType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMTypeRef;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;

public class EmitItemNativeType {

  /* singleton */
  public static final class EmitItemTypeInt extends EmitItemType {

    private static LLVMTypeRef theStructType = null;
    private static EmitItemTypeInt theInstance = null;

    private EmitItemTypeInt() {
      super("int", theStructType);
    }

    public static EmitItemTypeInt getInstance(EmitContext ctx) {
      if (theInstance == null) {
        theStructType = LLVMStructCreateNamed(LLVMGetModuleContext(ctx.moduleRef), "hipl_int");
        theInstance = new EmitItemTypeInt();

        HashMap<String, EmitItem> members = new HashMap<>();
        members.put("$value", new EmitItemType("i32", LLVMInt32Type()));

        theInstance.populate(ctx, members);
      }
      return theInstance;
    }

    public LLVMValueRef construct(EmitContext ctx, int val) {
      if (ctx.builderRef == null) {
        throw ctx.compileException("No builder for initialized value");
      }
      LLVMValueRef ret = super.construct(ctx, emptyMap());
      LLVMValueRef i32Ptr = LLVMBuildAlloca(ctx.builderRef, LLVMInt32Type(), "");
      LLVMBuildStore(ctx.builderRef,
          LLVMConstInt(LLVMInt32Type(), val, 0),
          i32Ptr);

      LLVMBuildStore(ctx.builderRef,
          i32Ptr,
          LLVMBuildStructGEP(ctx.builderRef, ret, memberIndex("$value", true), ""));
      return ret;
    }

  }

//  public static final class EmitItemTypeString extends EmitItemType {
//
//    private static EmitItemTypeInt theInstance = null;
//
//    private EmitItemTypeString() { /* singleton */}
//
//    public static EmitItemTypeInt getInstance() {
//      if (theInstance == null) {
//        theInstance = new EmitItemTypeInt();
//      }
//      return theInstance;
//    }
//  }

}