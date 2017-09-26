package com.mattmerr.hacc;

import static java.lang.String.format;
import static org.bytedeco.javacpp.LLVM.LLVMGetModuleContext;
import static org.bytedeco.javacpp.LLVM.LLVMInt32Type;
import static org.bytedeco.javacpp.LLVM.LLVMInt64Type;
import static org.bytedeco.javacpp.LLVM.LLVMInt8Type;
import static org.bytedeco.javacpp.LLVM.LLVMPointerType;
import static org.bytedeco.javacpp.LLVM.LLVMStructCreateNamed;
import static org.bytedeco.javacpp.LLVM.LLVMStructSetBody;
import static org.bytedeco.javacpp.LLVM.LLVMVoidType;

import java.util.HashMap;
import java.util.Objects;
import org.bytedeco.javacpp.LLVM.LLVMTypeRef;
import org.bytedeco.javacpp.PointerPointer;

public class HTypeString {

  private static final HashMap<LLVMTypeRef, HashMap<String, Integer>> memberIndices = new
      HashMap<>();
  private static final HashMap<String, LLVMTypeRef> visitedTypes = new HashMap<>();

  public static LLVMTypeRef visitType(HScope scope, String typeName) {
    if (visitedTypes.containsKey(typeName)) { return visitedTypes.get(typeName); }

    if ("int".equals(typeName)) {
      LLVMTypeRef[] types = {
          LLVMPointerType(LLVMInt64Type(), 0),
          LLVMInt32Type(),
      };
      LLVMTypeRef type = LLVMStructCreateNamed(LLVMGetModuleContext(scope.getModuleRef()),
          "int");
      LLVMStructSetBody(type, new PointerPointer<>(types), types.length, 0);
      visitedTypes.put("int", type);

      HashMap<String, Integer> indexMap = new HashMap<>();
      indexMap.put("class", 0);
      indexMap.put("value", 1);
      memberIndices.put(type, indexMap);

      return type;
    }
    else if ("string".equals(typeName)) {
      LLVMTypeRef[] types = {
          LLVMPointerType(LLVMInt64Type(), 0),
          LLVMPointerType(LLVMInt8Type(), 0),
          LLVMInt32Type(),
      };
      LLVMTypeRef type = LLVMStructCreateNamed(LLVMGetModuleContext(scope.getModuleRef()),
          "string");
      LLVMStructSetBody(type, new PointerPointer<>(types), types.length, 0);
      visitedTypes.put(typeName, type);

      HashMap<String, Integer> indexMap = new HashMap<>();
      indexMap.put("class", 0);
      indexMap.put("value", 1);
      memberIndices.put(type, indexMap);

      return type;
    }
    else if ("void".equals(typeName)) {
      return LLVMVoidType();
    }
    else {
      throw new IllegalArgumentException(format("Unknown Type: \"%s\"", typeName));
    }
  }

  public static LLVMTypeRef getType(String name) {
    return visitedTypes.get(name);
  }

  public static int getMemberIndex(LLVMTypeRef llvmTypeRef, String memberName) {
    return Objects.requireNonNull(memberIndices.get(llvmTypeRef).get(memberName));
  }

}
