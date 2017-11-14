package com.mattmerr.hacc;

import static org.bytedeco.javacpp.LLVM.LLVMAddCFGSimplificationPass;
import static org.bytedeco.javacpp.LLVM.LLVMAddConstantPropagationPass;
import static org.bytedeco.javacpp.LLVM.LLVMAddDemoteMemoryToRegisterPass;
import static org.bytedeco.javacpp.LLVM.LLVMAddGVNPass;
import static org.bytedeco.javacpp.LLVM.LLVMAddInstructionCombiningPass;
import static org.bytedeco.javacpp.LLVM.LLVMAddPromoteMemoryToRegisterPass;
import static org.bytedeco.javacpp.LLVM.LLVMCreateGenericValueOfInt;
import static org.bytedeco.javacpp.LLVM.LLVMCreatePassManager;
import static org.bytedeco.javacpp.LLVM.LLVMInt32Type;
import static org.bytedeco.javacpp.LLVM.*;

import com.mattmerr.hacc.EmitContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMGenericValueRef;
import org.bytedeco.javacpp.LLVM.LLVMPassManagerRef;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;

public class HiplJIT {

  public static void run(EmitContext ctx, String[] deps, String mainFileId) {
    BytePointer error = new BytePointer((Pointer) null); // Used to retrieve messages from functions
    LLVMLinkInMCJIT();
    LLVMInitializeNativeAsmPrinter();
    LLVMInitializeNativeAsmParser();
    LLVMInitializeNativeDisassembler();
    LLVMInitializeNativeTarget();

    LLVMModuleRef mod = ctx.moduleRef;
//    LLVMDumpModule(mod);

    LLVMVerifyModule(mod, LLVMAbortProcessAction, error);

    LLVMExecutionEngineRef engine = new LLVMExecutionEngineRef();
    if (LLVMCreateJITCompilerForModule(engine, mod, 2, error) != 0) {
      System.err.println(error.getString());
      LLVMDisposeMessage(error);
      System.exit(-1);
    }

    for (String dep : deps) {
      LLVMMemoryBufferRef[] bufferRef = new LLVMMemoryBufferRef[]{
          new LLVMMemoryBufferRef()
      };
      LLVMModuleRef[] moduleRefs = new LLVMModuleRef[]{
          new LLVMModuleRef()
      };
      byte[] buf = new byte[512];
      if (0 != LLVMCreateMemoryBufferWithContentsOfFile(
          new BytePointer(dep),
          bufferRef[0],
          new BytePointer(buf))) {
        System.err.printf("Error reading memorybuffer at \"%s\"", dep);
        System.err.println(new String(buf));
        System.err.println(bufferRef[0]);
        return;
      }
      LLVMParseBitcode(bufferRef[0], moduleRefs[0], buf);
      LLVMAddModule(engine, moduleRefs[0]);
    }

//    for (String depId : env.getParsedIds()) {
//      LLVMDumpModule(env.getFile(depId).mod);
//      LLVMAddModule(engine, env.getFile(depId).mod);
//    }

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

//    LLVMGenericValueRef exec_args = LLVMCreateGenericValueOfInt(LLVMInt32Type(), 10, 0);
    LLVMGenericValueRef exec_res = LLVMRunFunction(engine,
        ctx.scope
            .getEmitItemDep(ctx, mainFileId)
            .getEmitItemFunction(ctx, "main").pointer, 0,
        (LLVMGenericValueRef) null);
//    System.err.println();
//    System.err.println("; Running fac(10) with JIT...");
//    System.err.println("; Result: " + LLVMGenericValueToInt(exec_res, 0));
//
//    LLVMDisposePassManager(pass);
////    LLVMDisposeBuilder(builder);
//    LLVMDisposeExecutionEngine(engine);
  }

}
