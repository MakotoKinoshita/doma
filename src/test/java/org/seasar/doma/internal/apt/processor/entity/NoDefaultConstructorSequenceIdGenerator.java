package org.seasar.doma.internal.apt.processor.entity;

import org.seasar.doma.jdbc.id.BuiltinSequenceIdGenerator;

/** @author taedium */
public class NoDefaultConstructorSequenceIdGenerator extends BuiltinSequenceIdGenerator {

  private NoDefaultConstructorSequenceIdGenerator() {}
}
