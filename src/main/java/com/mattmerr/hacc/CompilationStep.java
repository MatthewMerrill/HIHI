package com.mattmerr.hacc;

public abstract class CompilationStep implements Comparable<CompilationStep> {

  private static int _next_compilationstep_id = 0;
  private final int _compilationstep_id = _next_compilationstep_id++;

  public abstract CompilationPhase getCompilationPhase();

  public abstract void run(CompilationEnvironment env);

  @Override
  public final int compareTo(CompilationStep o) {
    int cmp = this.getCompilationPhase().compareTo(o.getCompilationPhase());

    if (cmp == 0) {
      return Integer.compare(this._compilationstep_id, o._compilationstep_id);
    }

    return cmp;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CompilationStep) {
      CompilationStep other = (CompilationStep) obj;
      return this._compilationstep_id == other._compilationstep_id;
    }
    return false;
  }

}
