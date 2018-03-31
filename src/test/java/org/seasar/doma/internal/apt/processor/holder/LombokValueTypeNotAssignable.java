package org.seasar.doma.internal.apt.processor.holder;

import org.seasar.doma.Holder;
import org.seasar.doma.internal.apt.lombok.Value;

/** @author nakamura-to */
@Holder(valueType = String.class)
@Value
public class LombokValueTypeNotAssignable {

  @SuppressWarnings("unused")
  private Integer value;
}
