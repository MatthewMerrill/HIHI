package com.mattmerr.hacc.phase;

import com.mattmerr.hacc.CompilationEnvironment;
import com.mattmerr.hacc.CompilationPhase;
import com.mattmerr.hacc.CompilationStep;
import com.mattmerr.hacc.CompiledFile;
import com.mattmerr.hitch.parsetokens.ParseNodeImportStatement;
import com.mattmerr.hitch.parsetokens.ParseNodeStatement;
import java.io.IOException;
import java.util.function.Function;

public class DependencyFinderStep extends CompilationStep {

  private final CompiledFile file;
  private final Function<CompiledFile, CompilationStep> sink;

  public DependencyFinderStep(CompiledFile result,
      Function<CompiledFile, CompilationStep> sink) {
    this.file = result;
    this.sink = sink;
  }

  @Override
  public CompilationPhase getCompilationPhase() {
    return CompilationPhase.FIND_DEPENDENCIES;
  }

  @Override
  public void run(CompilationEnvironment env) {
    for (ParseNodeStatement statement : file.parseTree.dependencies) {
      String id = ((ParseNodeImportStatement) statement).importPath;
      if (!env.hasBeenParsed(id)) {
        try {
          env.addFile(id);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }

  }
}
