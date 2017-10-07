package com.mattmerr.hacc;

import com.mattmerr.hacc.EmitItem.EmitItemFunction;
import com.mattmerr.hacc.EmitItem.EmitItemType;
import com.mattmerr.hacc.EmitItem.EmitItemVar;
import java.util.NoSuchElementException;

public interface EmitAccessible {

  EmitItem peekEmitItem(EmitContext emitContext, String name);

  default EmitItem getEmitItem(EmitContext emitContext, String name) {
    EmitItem ret = peekEmitItem(emitContext, name);
    if (ret != null) {
      return ret;
    }
    throw emitContext.compileException(new NoSuchElementException(name));
  }

  default EmitItemType getEmitItemType(EmitContext emitContext, String name) {
    EmitItem emitItem = getEmitItem(emitContext, name);
    if (emitItem != null) {
      if (emitItem instanceof EmitItemType) {
        return (EmitItemType) emitItem;
      }
      throw emitContext
          .compileException("Expected EmitItemType, found " + emitItem.getClass().getName());
    }
    return null;
  }

  default EmitItemVar getEmitItemVar(EmitContext emitContext, String name) {
    EmitItem emitItem = getEmitItem(emitContext, name);
    if (emitItem != null) {
      if (emitItem instanceof EmitItemVar) {
        return (EmitItemVar) emitItem;
      }
      throw emitContext
          .compileException("Expected EmitItemVar, found " + emitItem.getClass().getName());
    }
    return null;
  }

  default EmitItemFunction getEmitItemFunction(EmitContext emitContext, String name) {
    EmitItem emitItem = getEmitItem(emitContext, name);
    if (emitItem != null) {
      if (emitItem instanceof EmitItemFunction) {
        return (EmitItemFunction) emitItem;
      }
      throw emitContext
          .compileException("Expected EmitItemFunction, found " + emitItem.getClass().getName());
    }
    return null;
  }

}
