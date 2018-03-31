package org.seasar.doma.internal.apt.processor.entity;

import org.seasar.doma.Entity;
import org.seasar.doma.SequenceGenerator;

/** @author taedium */
@Entity
public class SequenceGeneratorWithoutGeneratedValueEntity {

  @SequenceGenerator(sequence = "SEQ")
  Integer id;
}
