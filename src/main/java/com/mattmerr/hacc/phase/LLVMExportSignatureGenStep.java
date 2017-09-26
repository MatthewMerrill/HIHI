package com.mattmerr.hacc.phase;

import static com.mattmerr.hacc.HTypeString.visitType;
import static org.bytedeco.javacpp.LLVM.LLVMAddFunction;
import static org.bytedeco.javacpp.LLVM.LLVMCCallConv;
import static org.bytedeco.javacpp.LLVM.LLVMFunctionType;
import static org.bytedeco.javacpp.LLVM.LLVMPointerType;
import static org.bytedeco.javacpp.LLVM.LLVMSetFunctionCallConv;

import com.mattmerr.hacc.CompilationEnvironment;
import com.mattmerr.hacc.CompilationPhase;
import com.mattmerr.hacc.CompilationStep;
import com.mattmerr.hacc.CompiledFile;
import com.mattmerr.hacc.HScope;
import com.mattmerr.hitch.parsetokens.ParseNodeFunction;
import com.mattmerr.hitch.parsetokens.ParseNodeFunctionDeclaration;
import com.mattmerr.hitch.parsetokens.ParseNodeStatement;
import org.bytedeco.javacpp.LLVM.LLVMTypeRef;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;
import org.bytedeco.javacpp.PointerPointer;

public class LLVMExportSignatureGenStep extends CompilationStep {

  private final CompiledFile file;

  public LLVMExportSignatureGenStep(CompiledFile file) {
    this.file = file;
  }

  @Override
  public CompilationPhase getCompilationPhase() {
    return CompilationPhase.GENERATE_SIGNATURE;
  }

  @Override
  public void run(CompilationEnvironment env) {
    for (ParseNodeStatement statement : file.parseTree.block.getStatementList()) {
      if (statement instanceof ParseNodeFunctionDeclaration) {
        HScope scope = file.scope;
        ParseNodeFunctionDeclaration functionDeclaration = (ParseNodeFunctionDeclaration) statement;
        ParseNodeFunction function = functionDeclaration.function;

        LLVMTypeRef paramTypes[] = new LLVMTypeRef[function.argumentMapping.size()];

        for (int i = 0; i < paramTypes.length; i++) {
          paramTypes[i] = LLVMPointerType(visitType(scope, function.argumentTypes.get(i)),
              0);
        }

        LLVMTypeRef retType = LLVMFunctionType(visitType(scope, function.returnType),
            new PointerPointer<>(paramTypes), paramTypes.length, 0);
        LLVMValueRef func = LLVMAddFunction(scope.getModuleRef(), file.id + "." + function.name, retType);
        LLVMSetFunctionCallConv(func, LLVMCCallConv);

        scope.declare(function.name, retType);
        scope.put(function.name, func);
      }
    }
  }
}
