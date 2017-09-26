package com.mattmerr.hacc.phase;

import com.mattmerr.hacc.CompilationEnvironment;
import com.mattmerr.hacc.CompilationPhase;
import com.mattmerr.hacc.CompilationStep;
import com.mattmerr.hacc.CompiledFile;
import com.mattmerr.hitch.TokenStream;
import com.mattmerr.hitch.parsetokens.ParseNodeFile;
import java.util.function.Function;

public class ParseStep extends CompilationStep {

  private final String id;
  private final TokenStream tokenStream;
  private final Function<CompiledFile, CompilationStep> sink;

  public ParseStep(String id, TokenStream tokenStream,
      Function<CompiledFile, CompilationStep> sink) {
    this.id = id;
    this.tokenStream = tokenStream;
    this.sink = sink;
  }

  @Override
  public CompilationPhase getCompilationPhase() {
    return CompilationPhase.PARSE;
  }

  @Override
  public void run(CompilationEnvironment env) {
    if (!env.hasBeenParsed(id)) {
      try {
        ParseNodeFile result = ParseNodeFile.parseFile(tokenStream);
        CompiledFile file = new CompiledFile(id, result);
        env.setParsed(id, file);
        env.addCompilationStep(new LLVMExportSignatureGenStep(file));
        env.addCompilationStep(new DependencyFinderStep(file, sink));
        env.addCompilationStep(new DependencyLinkStep(file));
        env.addCompilationStep(sink.apply(file));
      } catch (Exception e) {
        throw new RuntimeException("Exception while parsing \"" + id + "\"", e);
      }
    }
  }
}
