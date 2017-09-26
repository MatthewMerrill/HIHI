package com.mattmerr.hacc;

public enum CompilationPhase implements Comparable<CompilationPhase> {

  PARSE,
  FIND_DEPENDENCIES,
  GENERATE_SIGNATURE,
  LINK,
  COMPILE_TO_IR,
  EMIT,

}
