package com.mattmerr.hacc;

import static java.util.Collections.emptyMap;
import static org.bytedeco.javacpp.LLVM.LLVMBuildAlloca;
import static org.bytedeco.javacpp.LLVM.LLVMBuildGlobalStringPtr;
import static org.bytedeco.javacpp.LLVM.LLVMBuildLoad;
import static org.bytedeco.javacpp.LLVM.LLVMBuildStore;
import static org.bytedeco.javacpp.LLVM.LLVMBuildStructGEP;
import static org.bytedeco.javacpp.LLVM.LLVMConstInt;
import static org.bytedeco.javacpp.LLVM.LLVMGetModuleContext;
import static org.bytedeco.javacpp.LLVM.LLVMInt32Type;
import static org.bytedeco.javacpp.LLVM.LLVMInt8Type;
import static org.bytedeco.javacpp.LLVM.LLVMPointerType;
import static org.bytedeco.javacpp.LLVM.LLVMStructCreateNamed;

import com.mattmerr.hacc.EmitItem.EmitItemType;
import java.util.HashMap;
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
        members.put("$value", new EmitItemType("i32", LLVMInt32Type(), true));

        theInstance.populate(ctx, members);
      }
      return theInstance;
    }

    public LLVMValueRef construct(EmitContext ctx, int val) {
      if (ctx.builderRef == null) {
        throw ctx.compileException("No builder for initialized value");
      }
      LLVMValueRef ret = super.construct(ctx, emptyMap());
      LLVMBuildStore(ctx.builderRef,
          LLVMConstInt(LLVMInt32Type(), val, 0),
          LLVMBuildStructGEP(ctx.builderRef, ret, memberIndex("$value", true), ""));
      return ret;
    }
  }

  public static final class EmitItemTypeString extends EmitItemType {

    private static LLVMTypeRef theStructType = null;
    private static EmitItemTypeString theInstance = null;

    private EmitItemTypeString() {
      super("string", theStructType, true);
    }

    public static EmitItemTypeString getInstance(EmitContext ctx) {
      if (theInstance == null) {
        theStructType = LLVMStructCreateNamed(LLVMGetModuleContext(ctx.moduleRef), "hipl_string");
        theInstance = new EmitItemTypeString();

        HashMap<String, EmitItem> members = new HashMap<>();
        members.put("$value", new EmitItemType("i8*", LLVMPointerType(LLVMInt8Type(), 0), true));
        members.put("length", new EmitItemType("i32", EmitItemTypeInt.theStructType, false));

        theInstance.populate(ctx, members);
      }
      return theInstance;
    }

    public LLVMValueRef construct(EmitContext ctx, String string) {
      if (ctx.builderRef == null) {
        throw ctx.compileException("No builder for initialized value");
      }
      LLVMValueRef ret = super.construct(ctx, emptyMap());
      LLVMValueRef strValue = LLVMBuildGlobalStringPtr(ctx.builderRef, string, "");
      LLVMValueRef lenValue = EmitItemTypeInt.theInstance.construct(ctx, string.length());
//      LLVMBuildStore(ctx.builderRef,
//          LLVMConstInt(LLVMInt32Type(), string.length(), 0),
//          i32Ptr);

      LLVMBuildStore(ctx.builderRef,
          strValue,
          LLVMBuildStructGEP(ctx.builderRef, ret, memberIndex("$value", true), "sGEP"));

      LLVMBuildStore(ctx.builderRef,
          lenValue,
          LLVMBuildStructGEP(ctx.builderRef, ret, memberIndex("length", true), "sGEP2"));
      return ret;
    }
  }
}