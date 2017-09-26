package com.mattmerr.hacc.phase;

import com.mattmerr.hacc.CompilationEnvironment;
import com.mattmerr.hacc.CompilationPhase;
import com.mattmerr.hacc.CompilationStep;
import com.mattmerr.hacc.CompiledFile;
import com.mattmerr.hitch.parsetokens.ParseNodeImportStatement;
import com.mattmerr.hitch.parsetokens.ParseNodeStatement;

public class DependencyLinkStep extends CompilationStep {

  private final CompiledFile file;

  public DependencyLinkStep(CompiledFile result) {
    this.file = result;
  }

  @Override
  public CompilationPhase getCompilationPhase() {
    return CompilationPhase.LINK;
  }

  @Override
  public void run(CompilationEnvironment env) {
    for (ParseNodeStatement statement : file.parseTree.dependencies) {
      String depId = ((ParseNodeImportStatement) statement).importPath;
      if (!env.hasBeenParsed(depId)) {
        throw new RuntimeException(String
            .format("Cannot link: \"%s\" (dependency of \"%s\") hasn't been parsed yet!",
                depId,
                file.id));
      }
      CompiledFile dep = env.getFile(depId);
      file.scope.declareDependency(depId, dep);
    }

  }
}
