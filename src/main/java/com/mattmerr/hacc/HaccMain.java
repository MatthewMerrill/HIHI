package com.mattmerr.hacc;

import static org.bytedeco.javacpp.LLVM.LLVMAbortProcessAction;
import static org.bytedeco.javacpp.LLVM.LLVMAddCFGSimplificationPass;
import static org.bytedeco.javacpp.LLVM.LLVMAddConstantPropagationPass;
import static org.bytedeco.javacpp.LLVM.LLVMAddDemoteMemoryToRegisterPass;
import static org.bytedeco.javacpp.LLVM.LLVMAddGVNPass;
import static org.bytedeco.javacpp.LLVM.LLVMAddInstructionCombiningPass;
import static org.bytedeco.javacpp.LLVM.LLVMAddPromoteMemoryToRegisterPass;
import static org.bytedeco.javacpp.LLVM.LLVMCreateGenericValueOfInt;
import static org.bytedeco.javacpp.LLVM.LLVMCreateJITCompilerForModule;
import static org.bytedeco.javacpp.LLVM.LLVMCreatePassManager;
import static org.bytedeco.javacpp.LLVM.LLVMDisposeBuilder;
import static org.bytedeco.javacpp.LLVM.LLVMDisposeExecutionEngine;
import static org.bytedeco.javacpp.LLVM.LLVMDisposeMessage;
import static org.bytedeco.javacpp.LLVM.LLVMDisposePassManager;
import static org.bytedeco.javacpp.LLVM.LLVMDumpModule;
import static org.bytedeco.javacpp.LLVM.LLVMGenericValueToInt;
import static org.bytedeco.javacpp.LLVM.LLVMInitializeNativeAsmParser;
import static org.bytedeco.javacpp.LLVM.LLVMInitializeNativeAsmPrinter;
import static org.bytedeco.javacpp.LLVM.LLVMInitializeNativeDisassembler;
import static org.bytedeco.javacpp.LLVM.LLVMInitializeNativeTarget;
import static org.bytedeco.javacpp.LLVM.LLVMInt32Type;
import static org.bytedeco.javacpp.LLVM.LLVMLinkInMCJIT;
import static org.bytedeco.javacpp.LLVM.LLVMRunFunction;
import static org.bytedeco.javacpp.LLVM.LLVMRunPassManager;
import static org.bytedeco.javacpp.LLVM.LLVMVerifyModule;

import com.mattmerr.hitch.TokenParser;
import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.parsetokens.ParseNodeBlock;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMExecutionEngineRef;
import org.bytedeco.javacpp.LLVM.LLVMGenericValueRef;
import org.bytedeco.javacpp.LLVM.LLVMModuleRef;
import org.bytedeco.javacpp.LLVM.LLVMPassManagerRef;
import org.bytedeco.javacpp.Pointer;

/**
 * Created by merrillm on 3/18/17.
 */
public class HaccMain {

  public static void main(String[] args) {
    ProgSink sink = new ProgSink(System.out);
    String input;
    if (args.length == 1) {
      try {
        input = String.join("\n", Files.readAllLines(Paths.get(args[0])));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    else {
      input = ""
          + "func<<string>, void> hello(name) {\n"
          + "  if (1)\n"
          + "    printf(\"Hello %s!\\n\", \"Alpha\");\n"
          + "  if (1) {\n"
          + "    var<string> b = \"Beta\";\n"
          + "    printf(\"Hello %s!\\n\", b);\n"
          + "  }\n"
          + "  else\n"
          + "    printf(\"Hello %s!\\n\", \"Gamma\");\n"
          + "}\n";
    }
    System.out.printf("Input: \"%s\"\n", input);
    TokenStream ts = new TokenStream(input);
    TokenParser parser = new TokenParser(ts);

    BytePointer error = new BytePointer((Pointer)null); // Used to retrieve messages from functions
    LLVMLinkInMCJIT();
    LLVMInitializeNativeAsmPrinter();
    LLVMInitializeNativeAsmParser();
    LLVMInitializeNativeDisassembler();
    LLVMInitializeNativeTarget();

    HScope scope = Ast2LLVM.visit(parser.parse());
    LLVMModuleRef mod = scope.getModuleRef();
    LLVMDumpModule(mod);

    LLVMVerifyModule(mod, LLVMAbortProcessAction, error);

    LLVMExecutionEngineRef engine = new LLVMExecutionEngineRef();
    if(LLVMCreateJITCompilerForModule(engine, mod, 2, error) != 0) {
      System.err.println(error.getString());
      LLVMDisposeMessage(error);
      System.exit(-1);
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
    LLVMGenericValueRef exec_res = LLVMRunFunction(engine, scope.get("hello"), 1, exec_args);
//    System.err.println();
//    System.err.println("; Running fac(10) with JIT...");
//    System.err.println("; Result: " + LLVMGenericValueToInt(exec_res, 0));
//
//    LLVMDisposePassManager(pass);
////    LLVMDisposeBuilder(builder);
//    LLVMDisposeExecutionEngine(engine);

  }
}
