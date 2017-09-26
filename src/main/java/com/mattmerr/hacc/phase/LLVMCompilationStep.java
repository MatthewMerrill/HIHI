package com.mattmerr.hacc.phase;

import com.mattmerr.hacc.Ast2LLVM;
import com.mattmerr.hacc.CompilationEnvironment;
import com.mattmerr.hacc.CompilationPhase;
import com.mattmerr.hacc.CompilationStep;
import com.mattmerr.hacc.CompiledFile;
import com.mattmerr.hacc.HScope;
import java.util.function.Function;

public class LLVMCompilationStep extends CompilationStep {

  private final CompiledFile file;

  public LLVMCompilationStep(CompiledFile file) {
    this.file = file;
  }

  @Override
  public CompilationPhase getCompilationPhase() {
    return CompilationPhase.COMPILE_TO_IR;
  }

  @Override
  public void run(CompilationEnvironment env) {
    HScope scope = Ast2LLVM.visit(file);
  }
}
