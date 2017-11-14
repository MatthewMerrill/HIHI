package com.mattmerr.hacc;

import static org.bytedeco.javacpp.LLVM.LLVMAddGlobal;
import static org.bytedeco.javacpp.LLVM.LLVMBuildAlloca;
import static org.bytedeco.javacpp.LLVM.LLVMBuildInsertElement;
import static org.bytedeco.javacpp.LLVM.LLVMBuildInsertValue;
import static org.bytedeco.javacpp.LLVM.LLVMBuildStore;
import static org.bytedeco.javacpp.LLVM.LLVMBuildStructGEP;
import static org.bytedeco.javacpp.LLVM.LLVMConstPointerNull;
import static org.bytedeco.javacpp.LLVM.LLVMConstStruct;
import static org.bytedeco.javacpp.LLVM.LLVMGetElementType;
import static org.bytedeco.javacpp.LLVM.LLVMGetTypeKind;
import static org.bytedeco.javacpp.LLVM.LLVMIsGlobalConstant;
import static org.bytedeco.javacpp.LLVM.LLVMPointerType;
import static org.bytedeco.javacpp.LLVM.LLVMPointerTypeKind;
import static org.bytedeco.javacpp.LLVM.LLVMSetGC;
import static org.bytedeco.javacpp.LLVM.LLVMStructGetTypeAtIndex;
import static org.bytedeco.javacpp.LLVM.LLVMStructSetBody;
import static org.bytedeco.javacpp.LLVM.LLVMTypeOf;

import java.util.HashMap;
import java.util.Map;
import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMTypeRef;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;
import org.bytedeco.javacpp.PointerPointer;

public interface EmitItem {

  public static class EmitItemType implements EmitItem {

    private final String name;
    private final LLVMTypeRef typeRef;
    private final boolean isNative;
    private Map<String, Integer> memberIndices = null;
    private Map<Integer, String> memberIndicesRev = null;
    private Map<String, EmitItem> members;

    public EmitItemType(String name, LLVMTypeRef typeRef, boolean isNative) {
      this.name = name;
      this.typeRef = typeRef;
      this.isNative = isNative;
    }

    public EmitItemType(String name, LLVMTypeRef typeRef) {
      this(name, typeRef, false);
    }

    public String getName() {
      return name;
    }

//    public EmitItemType(EmitContext ctx, String string) {
//      typeRef = LLVMStructCreateNamed(LLVMctx.getModuleRef()string);
//     }

    public LLVMTypeRef getTypeRef() {
      return this.typeRef;
    }

    public void populate(EmitContext ctx, Map<String, EmitItem> memberMap) {
      this.memberIndices = new HashMap<>();
      this.memberIndicesRev = new HashMap<>();
      this.members = memberMap;

      LLVMTypeRef[] types = new LLVMTypeRef[members.size()];
      int i = 0;

      for (String memberName : memberMap.keySet()) {
        EmitItem member = memberMap.get(memberName);
        if (member == null) {
          throw ctx.compileException(
              "Cannot populate type: null member, should be impossible.");
        }
        else if (member instanceof EmitItemType) {
          if (((EmitItemType) member).isNative) {
            types[i] = ((EmitItemType) member).getTypeRef();
          }
          else {
            types[i] = LLVMPointerType(((EmitItemType) member).getTypeRef(), 0);
          }
        }
        else if (member instanceof EmitItemFunction) {
          types[i] = LLVMTypeOf(((EmitItemFunction) member).pointer);
        }
        else {
          throw ctx.compileException(
              "Cannot populate type: invalid member type: " + memberName.getClass().getName());
        }
        memberIndicesRev.put(i, memberName);
        memberIndices.put(memberName, i++);
      }
      LLVMStructSetBody(typeRef, new PointerPointer<>(types), i, 0);
    }

    public LLVMValueRef construct(EmitContext ctx, Map<String, LLVMValueRef> argRefs) {
      if (ctx.builderRef == null) {
        throw ctx.compileException("Cannot construct: no builder for context");
      }
      if (memberIndices == null) {
        throw ctx.compileException("Cannot construct: type has not been populated yet");
      }
//      LLVMValueRef valueRef = LLVMBuildAlloca(ctx.builderRef, typeRef, "");
//      for (String memberName : members.keySet()) {
//        EmitItem member = members.get(memberName);
//        if (member instanceof EmitItemFunction) {
//          LLVMBuildStore(ctx.builderRef,
//              LLVMBuildStructGEP(ctx.builderRef, valueRef, memberIndices.get(memberName), ""),
//              ((EmitItemFunction) member).pointer);
//        }
//      }
      LLVMValueRef[] args = new LLVMValueRef[memberIndices.size()];
      for (String argName : argRefs.keySet()) {
        if (memberIndices.containsKey(argName)) {
          args[memberIndices.get(argName)] = argRefs.get(argName);
        }
        else {
          throw ctx.compileExceptionWithContextName(this.getName(),
              "Unknown member of type " + this.getName() + " \"" + argName + "\"");
        }
      }
      for (int argIdx = 0; argIdx < args.length; argIdx++) {
        if (args[argIdx] == null
            && LLVMGetTypeKind(LLVMStructGetTypeAtIndex(typeRef, argIdx)) ==
            LLVMPointerTypeKind) {
          args[argIdx] = LLVMConstPointerNull(
              LLVMGetElementType(LLVMStructGetTypeAtIndex(typeRef, argIdx)));
        }
      }
      LLVMValueRef val = LLVM.LLVMBuildMalloc(ctx.builderRef, typeRef, "");
      LLVMSetGC(val, "shadow-stack");
      for (int argIdx = 0; argIdx < args.length; argIdx++) {
        if (args[argIdx] != null) {
          LLVMBuildStore(ctx.builderRef, args[argIdx],
              LLVMBuildStructGEP(ctx.builderRef, val, argIdx, ctx.contextPath.replace('>', '_')));
        }
      }
//      System.out.println(ctx.contextPath);
//      LLVMBuildStore(ctx.builderRef, LLVMConstStruct(new PointerPointer<>(args), args.length,
// 0), val);
//      for (arg)
//      LLVMValueRef ref = LLVM.LLVMBuildAlloca(ctx.builderRef, LLVMPointerType(typeRef, 0), "");
//      LLVMBuildStore(ctx.builderRef, val, ref);
      return val;
    }

    public int memberIndex(String memberName, boolean allowNativeAccess) {
      return memberIndices.get(memberName);
    }
  }

  public static class EmitItemVar implements EmitItem {

    public final String name;
    public final LLVMValueRef pointer;

    public EmitItemVar(EmitContext ctx, String name, LLVMTypeRef typeRef, boolean global) {
      this.name = name;

      if (!global) {
        if (ctx.builderRef != null) {
          this.pointer = LLVMBuildAlloca(ctx.builderRef,
              LLVMPointerType(typeRef, 0), "");
        }
        else {
          throw ctx.compileException("Can't make a var without a builder!");
        }
      }
      else {
        throw ctx.compileException("I have no idea how globals work");
//        this.pointer = LLVMAddGlobal(ctx.moduleRef,
//            LLVMPointerType(LLVMPointerType(typeRef, 0), 0), "");
      }
//      LLVMConstPointerNull(typeRef);
    }

    private EmitItemVar(EmitContext ctx, String name, LLVMValueRef pointer, boolean global) {
      this.name = name;
      if (!global) {
        if (ctx.builderRef != null) {
          this.pointer = LLVMBuildAlloca(ctx.builderRef,
              LLVMPointerType(LLVMTypeOf(pointer), 0), "");
        }
        else {
          throw ctx.compileException("Can't make a var without a builder!");
        }
        assign(ctx, pointer);
      }
      else {
        this.pointer = pointer;
//        this.pointer = LLVMAddGlobal(ctx.moduleRef,
//            LLVMPointerType(LLVMTypeOf(pointer), 0), "");
//        LLVM.Global
//        LLVMSetGlobalConstant(pointer);
      }
      if (LLVMGetTypeKind(LLVMTypeOf(this.pointer)) != LLVMPointerTypeKind) {
        throw new RuntimeException("EmitItemVar must be a pointer!");
      }
    }

    void assign(EmitContext ctx, LLVMValueRef valueRef) {
//      if (LLVMIsGlobalConstant(this.pointer) == 0) {
      LLVMValueRef store = LLVMBuildStore(ctx.builderRef, valueRef, pointer);
//      }
//      else {

//        LLVMStore
//      }
    }

  }

  public static class EmitItemFunction extends EmitItemVar {

    EmitScope scope;

    public EmitItemFunction(EmitContext ctx, String name, LLVMValueRef valRef) {
      super(ctx, name, valRef, true);
    }
  }

  public static class EmitItemDependency implements EmitItem, EmitAccessible {

    EmitScope scope;

    public EmitItemDependency(EmitContext ctx, EmitScope scope) {
      this.scope = scope;
    }

    @Override
    public EmitItem peekEmitItem(EmitContext emitContext, String name) {
      return scope.peekEmitItem(emitContext, name);
    }
  }

}
