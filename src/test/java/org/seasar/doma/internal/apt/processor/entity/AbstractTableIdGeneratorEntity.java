package org.seasar.doma.internal.apt.processor.entity;

import org.seasar.doma.*;

@Entity
public class AbstractTableIdGeneratorEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.TABLE)
  @TableGenerator(pkColumnValue = "aaa", implementer = AbstractTableIdGenerator.class)
  Integer id;
}
