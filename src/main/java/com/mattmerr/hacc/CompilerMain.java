package com.mattmerr.hacc;

import static com.mattmerr.hacc.Emitter.visitFile;
import static com.mattmerr.hitch.parsetokens.ParseNodeFile.parseFile;
import static org.bytedeco.javacpp.LLVM.LLVMDumpModule;

import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.parsetokens.ParseNodeFile;
import org.bytedeco.javacpp.LLVM;

public class CompilerMain {

  public static void main(String[] args) {
    String[] srcPaths = null;
    String fileId = null;

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-c")) {
        srcPaths = args[i + 1].split(":");
      }
      else {
        fileId = args[i];
      }
    }

    TokenStream tokenStream = new TokenStream(System.in);
    ParseNodeFile file = parseFile(tokenStream);

    EmitContext ctx = new EmitContext("module");
    visitFile(ctx, file);

    System.out.println("woot");

    LLVMDumpModule(ctx.getModuleRef());
  }

}
