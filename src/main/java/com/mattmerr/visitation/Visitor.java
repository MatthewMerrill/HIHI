package com.mattmerr.visitation;

@FunctionalInterface
public interface Visitor<E extends Visitable> {

  void visit(E e, VisitContext ctx);

}
