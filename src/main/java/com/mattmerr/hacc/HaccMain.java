package com.mattmerr.hacc;

import com.mattmerr.hacc.phase.RunInJITStep;
import java.io.IOException;
import java.util.TreeSet;

/**
 * Created by merrillm on 3/18/17.
 */
public class HaccMain {

  public static void main(String[] args) throws IOException {
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

    CompilationEnvironment compilationEnvironment;

    if (srcPaths != null) {
      compilationEnvironment = new CompilationEnvironment(srcPaths);
    }
    else {
      compilationEnvironment = new CompilationEnvironment();
    }

    compilationEnvironment.addFile(fileId);
    compilationEnvironment.runAll();

    compilationEnvironment
        .addCompilationStep(new RunInJITStep(compilationEnvironment.getFile(fileId).scope));
    compilationEnvironment.runAll();
  }
}
