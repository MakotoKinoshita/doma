package org.seasar.doma.internal.apt.processor.entity;

import org.seasar.doma.jdbc.entity.*;

public class GenericListener6<E> implements GenericListener5<E> {

  @Override
  public void preInsert(E entity, PreInsertContext<E> context) {}

  @Override
  public void preUpdate(E entity, PreUpdateContext<E> context) {}

  @Override
  public void preDelete(E entity, PreDeleteContext<E> context) {}

  @Override
  public void postInsert(E entity, PostInsertContext<E> context) {}

  @Override
  public void postUpdate(E entity, PostUpdateContext<E> context) {}

  @Override
  public void postDelete(E entity, PostDeleteContext<E> context) {}
}