package com.mattmerr.visitation;


public interface Visitable<E extends Visitable> {

  void visited(Visitor<E> visitor);

}
