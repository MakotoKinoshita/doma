package org.seasar.doma.internal.apt.processor.dao;

import org.seasar.doma.Dao;
import org.seasar.doma.Select;

/**
 * @author taedium
 * 
 */
@Dao(config = MyConfig.class)
public interface WildcardTypeParamDao {

    @Select
    int select(Height<?> height);
}