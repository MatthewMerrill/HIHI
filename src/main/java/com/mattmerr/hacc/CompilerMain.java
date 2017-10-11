package com.mattmerr.hacc;

import static com.mattmerr.hacc.Emitter.visitFile;
import static com.mattmerr.hitch.parsetokens.ParseNodeFile.parseFile;
import static org.bytedeco.javacpp.LLVM.LLVMDumpModule;
import static org.bytedeco.javacpp.LLVM.LLVMVoidType;

import com.mattmerr.hacc.EmitItem.EmitItemType;
import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.parsetokens.ParseNodeFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CompilerMain {

  public static void main(String[] args) {
    String[] srcPaths = null;
    String[] deps = null;
    String fileId = null;

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-c")) {
        srcPaths = args[++i].split(":");
      }
      if (args[i].equals("--dep")) {
        deps = args[++i].split(":");
      }
      else {
        fileId = args[i];
      }
    }

    TokenStream tokenStream;

    if (fileId == null) {
      tokenStream = new TokenStream(System.in);
    }
    else {
      try {
        tokenStream = new TokenStream(Files.newInputStream(Paths.get(fileId)));
      } catch (IOException exception) {
        System.err.printf("Cannot read file \"%s\": %s", fileId, exception.getMessage());
        System.exit(1);
        return;
      }
    }

    ParseNodeFile file = parseFile(tokenStream);

    EmitContext ctx = new EmitContext("module");
    ctx.scope.declare(ctx, "void", new EmitItemType("void", LLVMVoidType()));
    ctx.scope.declare(ctx, "int", EmitItemNativeType.EmitItemTypeInt.getInstance(ctx));

    visitFile(ctx, file);
    LLVMDumpModule(ctx.getModuleRef());
    HiplJIT.run(ctx, deps);
  }

}
