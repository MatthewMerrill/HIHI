package com.mattmerr.hacc;

import static com.mattmerr.hacc.Emitter.createFunction;
import static com.mattmerr.hacc.Emitter.createType;
import static com.mattmerr.hacc.Emitter.populateFunction;
import static com.mattmerr.hacc.Emitter.populateType;
import static com.mattmerr.hitch.parsetokens.ParseNodeFile.parseFile;
import static java.util.Objects.requireNonNull;
import static org.bytedeco.javacpp.LLVM.LLVMDumpModule;
import static org.bytedeco.javacpp.LLVM.LLVMVoidType;

import com.mattmerr.hacc.EmitItem.EmitItemDependency;
import com.mattmerr.hacc.EmitItem.EmitItemFunction;
import com.mattmerr.hacc.EmitItem.EmitItemType;
import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.parsetokens.ParseNodeFile;
import com.mattmerr.hitch.parsetokens.ParseNodeFunction;
import com.mattmerr.hitch.parsetokens.ParseNodeImportStatement;
import com.mattmerr.hitch.parsetokens.ParseNodeType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CompilerMain {

  private static String[] srcPaths = null;

  public static void main(String[] args) {
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

//    TokenStream tokenStream;
//
//    if (fileId == null) {
//      tokenStream = new TokenStream(System.in);
//      fileId = "$stdin";
//    }
//    else {
//      try {
//        tokenStream = new TokenStream(Files.newInputStream(Paths.get(fileId)));
//      } catch (IOException exception) {
//        System.err.printf("Cannot read file \"%s\": %s", fileId, exception.getMessage());
//        System.exit(1);
//        return;
//      }
//    }
//
//    ParseNodeFile file = parseFile(tokenStream);

    EmitContext ctx = new EmitContext("module");
    ctx.scope.declare(ctx, "void", new EmitItemType("void", LLVMVoidType(), true));
    ctx.scope.declare(ctx, "int", EmitItemNativeType.EmitItemTypeInt.getInstance(ctx));
    ctx.scope.declare(ctx, "string", EmitItemNativeType.EmitItemTypeString.getInstance(ctx));

    addFile(ctx, fileId);
    runEmitter();

    LLVMDumpModule(ctx.getModuleRef());
    HiplJIT.run(ctx, deps, fileId);
  }


  public static Set<String> depsFound = new HashSet<>();

  public static ArrayDeque<Runnable> depRun = new ArrayDeque<>();
  public static ArrayDeque<Runnable> typeCreateRun = new ArrayDeque<>();
  public static ArrayDeque<Runnable> typePopRun = new ArrayDeque<>();
  public static ArrayDeque<Runnable> linkRun = new ArrayDeque<>();
  public static ArrayDeque<Runnable> funcCreateRun = new ArrayDeque<>();
  public static ArrayDeque<Runnable> funcPopRun = new ArrayDeque<>();

  public static Path resolveIdInPaths(String id, String[] srcPaths) {
    if (requireNonNull(id).isEmpty()) {
      throw new IllegalArgumentException("ID May not be empty");
    }

    String[] resolveParts = id.split("\\.");
    resolveParts[resolveParts.length - 1] += ".hipl";

    srcPathLoop:
    for (String srcPath : srcPaths) {
      Path path = Paths.get(srcPath);

      for (String resolvePart : resolveParts) {
        path = path.resolve(resolvePart);
        if (!Files.exists(path)) {
          continue srcPathLoop;
        }
      }

      return path;
    }
    throw new RuntimeException("Could not resolve \"" + id + "\" in " + Arrays.toString(srcPaths));
  }

  public static EmitContext addFile(EmitContext ctx, String id) {
    if (!depsFound.contains(id)) {
      depsFound.add(id);
      Path path = resolveIdInPaths(id, srcPaths);
      InputStream is = null;
      try {
        is = Files.newInputStream(path);
      } catch (IOException e) {
        e.printStackTrace();
        throw new Error("Could not find file: " + id);
      }
      TokenStream ts = new TokenStream(is);
      return addParseFile(ctx, id, ParseNodeFile.parseFile(ts));
    }
    return null;
  }

  public static EmitContext addParseFile(EmitContext ctx, String id, ParseNodeFile parseNodeFile) {
    EmitContext thisFileContext = ctx.createChildCtx(id);
    ctx.scope.declare(ctx, id, new EmitItemDependency(ctx, thisFileContext.scope));

    for (ParseNodeImportStatement importStatement : parseNodeFile.dependencies) {
      String fileId = importStatement.importPath;
      if (!depsFound.contains(fileId)) {
        addFile(ctx, fileId);
      }
    }
    typeCreateRun.add(() -> {
      for (ParseNodeType type : parseNodeFile.block.typeDefinitions) {
        createType(thisFileContext, type);
      }
    });
    typePopRun.add(() -> {
      for (ParseNodeType type : parseNodeFile.block.typeDefinitions) {
        populateType(thisFileContext, type);
      }
    });
    linkRun.add(() -> {
      for (ParseNodeImportStatement importStatement : parseNodeFile.dependencies) {
        String fileId = importStatement.importPath;
        thisFileContext.scope
            .declare(thisFileContext, fileId.substring(fileId.lastIndexOf('.') + 1),
                ctx.scope.getEmitItemDep(thisFileContext, fileId));
      }
    });
    funcCreateRun.add(() -> {
      for (ParseNodeFunction func : parseNodeFile.block.functions) {
        createFunction(thisFileContext, func);
      }
    });
    funcPopRun.add(() -> {
      for (ParseNodeFunction func : parseNodeFile.block.functions) {
        populateFunction(thisFileContext, func);
      }
    });

    return thisFileContext;
  }

  public static void runEmitter() {
    while (true) {
      if (!depRun.isEmpty()) {
        depRun.pop().run();
      }
      else if (!typeCreateRun.isEmpty()) {
        typeCreateRun.pop().run();
      }
      else if (!funcCreateRun.isEmpty()) {
        funcCreateRun.pop().run();
      }
      else if (!linkRun.isEmpty()) {
        linkRun.pop().run();
      }
      else if (!typePopRun.isEmpty()) {
        typePopRun.pop().run();
      }
      else if (!funcPopRun.isEmpty()) {
        funcPopRun.pop().run();
      }
      else {
        break;
      }
    }

//    for (ParseNodeType type : parseNodeFile.block.typeDefinitions) {
//      createType(ctx, type);
//    }
//
//    for (ParseNodeType type : parseNodeFile.block.typeDefinitions) {
//      populateType(ctx, type);
//    }
//
//    for (ParseNodeFunction function : parseNodeFile.block.functions) {
//      createFunction(ctx, function);
//    }
//
//    for (ParseNodeFunction function : parseNodeFile.block.functions) {
//      populateFunction(ctx, function);
//    }
  }

}
