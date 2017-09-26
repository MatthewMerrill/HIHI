package com.mattmerr.hacc.phase;

import static org.bytedeco.javacpp.LLVM.LLVMAbortProcessAction;
import static org.bytedeco.javacpp.LLVM.LLVMAddCFGSimplificationPass;
import static org.bytedeco.javacpp.LLVM.LLVMAddConstantPropagationPass;
import static org.bytedeco.javacpp.LLVM.LLVMAddDemoteMemoryToRegisterPass;
import static org.bytedeco.javacpp.LLVM.LLVMAddGVNPass;
import static org.bytedeco.javacpp.LLVM.LLVMAddInstructionCombiningPass;
import static org.bytedeco.javacpp.LLVM.LLVMAddModule;
import static org.bytedeco.javacpp.LLVM.LLVMAddPromoteMemoryToRegisterPass;
import static org.bytedeco.javacpp.LLVM.LLVMCreateGenericValueOfInt;
import static org.bytedeco.javacpp.LLVM.LLVMCreateJITCompilerForModule;
import static org.bytedeco.javacpp.LLVM.LLVMCreatePassManager;
import static org.bytedeco.javacpp.LLVM.LLVMDisposeMessage;
import static org.bytedeco.javacpp.LLVM.LLVMDumpModule;
import static org.bytedeco.javacpp.LLVM.LLVMInitializeNativeAsmParser;
import static org.bytedeco.javacpp.LLVM.LLVMInitializeNativeAsmPrinter;
import static org.bytedeco.javacpp.LLVM.LLVMInitializeNativeDisassembler;
import static org.bytedeco.javacpp.LLVM.LLVMInitializeNativeTarget;
import static org.bytedeco.javacpp.LLVM.LLVMInt32Type;
import static org.bytedeco.javacpp.LLVM.LLVMLinkInMCJIT;
import static org.bytedeco.javacpp.LLVM.LLVMRunFunction;
import static org.bytedeco.javacpp.LLVM.LLVMRunPassManager;
import static org.bytedeco.javacpp.LLVM.LLVMVerifyModule;

import com.mattmerr.hacc.CompilationEnvironment;
import com.mattmerr.hacc.CompilationPhase;
import com.mattmerr.hacc.CompilationStep;
import com.mattmerr.hacc.CompiledFile;
import com.mattmerr.hacc.HScope;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.LLVM.LLVMExecutionEngineRef;
import org.bytedeco.javacpp.LLVM.LLVMGenericValueRef;
import org.bytedeco.javacpp.LLVM.LLVMModuleRef;
import org.bytedeco.javacpp.LLVM.LLVMPassManagerRef;
import org.bytedeco.javacpp.Pointer;

public class RunInJITStep extends CompilationStep {

  private final HScope scope;

  public RunInJITStep(HScope scope) {
    this.scope = scope;
  }

  @Override
  public CompilationPhase getCompilationPhase() {
    return CompilationPhase.EMIT;
  }

  @Override
  public void run(CompilationEnvironment env) {
    BytePointer error = new BytePointer((Pointer)null); // Used to retrieve messages from functions
    LLVMLinkInMCJIT();
    LLVMInitializeNativeAsmPrinter();
    LLVMInitializeNativeAsmParser();
    LLVMInitializeNativeDisassembler();
    LLVMInitializeNativeTarget();

    LLVMModuleRef mod = scope.getModuleRef();
//    LLVMDumpModule(mod);

    LLVMVerifyModule(mod, LLVMAbortProcessAction, error);

    LLVMExecutionEngineRef engine = new LLVMExecutionEngineRef();
    if(LLVMCreateJITCompilerForModule(engine, mod, 2, error) != 0) {
      System.err.println(error.getString());
      LLVMDisposeMessage(error);
      System.exit(-1);
    }

    for (String depId : env.getParsedIds()) {
      LLVMDumpModule(env.getFile(depId).mod);
      LLVMAddModule(engine, env.getFile(depId).mod);
    }

    LLVMPassManagerRef pass = LLVMCreatePassManager();
    LLVMAddConstantPropagationPass(pass);
    LLVMAddInstructionCombiningPass(pass);
    LLVMAddPromoteMemoryToRegisterPass(pass);
    LLVMAddDemoteMemoryToRegisterPass(pass); // Demotes every possible value to memory
    LLVMAddGVNPass(pass);
    LLVMAddCFGSimplificationPass(pass);
    LLVMRunPassManager(pass, mod);
//    LLVMDumpModule(mod);
//

    LLVMGenericValueRef exec_args = LLVMCreateGenericValueOfInt(LLVMInt32Type(), 10, 0);
    LLVMGenericValueRef exec_res = LLVMRunFunction(engine, scope.get("main"), 1, exec_args);
//    System.err.println();
//    System.err.println("; Running fac(10) with JIT...");
//    System.err.println("; Result: " + LLVMGenericValueToInt(exec_res, 0));
//
//    LLVMDisposePassManager(pass);
////    LLVMDisposeBuilder(builder);
//    LLVMDisposeExecutionEngine(engine);
  }
}
