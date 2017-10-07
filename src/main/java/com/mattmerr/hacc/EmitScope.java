package com.mattmerr.hacc;

import static java.lang.String.format;

import java.util.HashMap;

public class EmitScope implements EmitAccessible {

  private EmitScope parent;
  private HashMap<String, EmitItem> items = new HashMap<>();

  public EmitScope() {
  }

  public EmitScope(EmitScope scope) {
    this.parent = scope;
  }

  @Override
  public EmitItem peekEmitItem(EmitContext emitContext, String name) {
    EmitItem ret = null;

    if (this.items.containsKey(name)) {
      ret = this.items.get(name);
      if (ret != null) {
        return ret;
      }
      throw emitContext.compileException(format("\"%s\" was declared, but holds no value.", name));
    }

    if (this.parent != null) {
     return this.parent.peekEmitItem(emitContext, name);
    }
    return null;
  }

  public void declare(EmitContext emitContext, String name, EmitItem item) {
    if (!this.items.containsKey(name)) {
      items.put(name, item);
    }
    else {
      throw emitContext.compileException(format("\"%s\" is already declared in this scope!", name));
    }
  }

//  public void set(EmitContext emitContext, String name, EmitItem emitItem) {
//    if (this.items.containsKey(name)) {
//      this.items.put(name, emitItem);
//    }
//    else {
//      throw emitContext.compileException(format("Cannot set \"%s\" without declaring!", name));
//    }
//  }
}
