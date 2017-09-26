package com.mattmerr.hacc;

import static org.bytedeco.javacpp.LLVM.LLVMModuleCreateWithName;

import com.mattmerr.hitch.parsetokens.ParseNodeFile;
import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMModuleRef;

public class CompiledFile {

  public final String id;
  public final ParseNodeFile parseTree;

  public final LLVMModuleRef mod;
  public final HScope scope;

  public CompiledFile(String id, ParseNodeFile parseTree) {
    this.id = id;
    this.parseTree = parseTree;
    this.mod = LLVMModuleCreateWithName(id);
    this.scope = new HScope(this.mod);
  }


}
