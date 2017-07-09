/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.jdbc.query;

import java.util.ArrayList;
import java.util.List;

import org.seasar.doma.internal.jdbc.sql.SqlContext;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.Naming;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.entity.EntityPropertyDesc;
import org.seasar.doma.jdbc.entity.EntityDesc;
import org.seasar.doma.jdbc.entity.Property;

/**
 * @author nakamura-to
 * @since 2.3.0
 */
public class BatchUpdateQueryHelper<E> {

    protected final Config config;

    protected final EntityDesc<E> entityDesc;

    protected final boolean versionIgnored;

    protected final boolean optimisticLockExceptionSuppressed;

    protected final String[] includedPropertyNames;

    protected final String[] excludedPropertyNames;

    public BatchUpdateQueryHelper(Config config, EntityDesc<E> entityType,
            String[] includedPropertyNames, String[] excludedPropertyNames,
            boolean versionIgnored, boolean optimisticLockExceptionSuppressed) {
        this.config = config;
        this.entityDesc = entityType;
        this.versionIgnored = versionIgnored;
        this.optimisticLockExceptionSuppressed = optimisticLockExceptionSuppressed;
        this.includedPropertyNames = includedPropertyNames;
        this.excludedPropertyNames = excludedPropertyNames;
    }

    public List<EntityPropertyDesc<E, ?>> getTargetPropertyTypes() {
        List<EntityPropertyDesc<E, ?>> targetPropertyTypes = new ArrayList<EntityPropertyDesc<E, ?>>(
                entityDesc.getEntityPropertyDescs().size());
        for (EntityPropertyDesc<E, ?> p : entityDesc.getEntityPropertyDescs()) {
            if (!p.isUpdatable()) {
                continue;
            }
            if (p.isId()) {
                continue;
            }
            if (p.isVersion()) {
                targetPropertyTypes.add(p);
                continue;
            }
            if (!isTargetPropertyName(p.getName())) {
                continue;
            }
            targetPropertyTypes.add(p);
        }
        return targetPropertyTypes;
    }

    protected boolean isTargetPropertyName(String name) {
        if (includedPropertyNames.length > 0) {
            for (String includedName : includedPropertyNames) {
                if (includedName.equals(name)) {
                    for (String excludedName : excludedPropertyNames) {
                        if (excludedName.equals(name)) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        if (excludedPropertyNames.length > 0) {
            for (String excludedName : excludedPropertyNames) {
                if (excludedName.equals(name)) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    public void populateValues(E entity,
            List<EntityPropertyDesc<E, ?>> targetPropertyTypes,
            EntityPropertyDesc<E, ?> versionPropertyType, SqlContext context) {
        Dialect dialect = config.getDialect();
        Naming naming = config.getNaming();
        for (EntityPropertyDesc<E, ?> propertyType : targetPropertyTypes) {
            Property<E, ?> property = propertyType.createProperty();
            property.load(entity);
            context.appendSql(propertyType.getColumnName(naming::apply,
                    dialect::applyQuote));
            context.appendSql(" = ");
            context.appendParameter(property.asInParameter());
            if (propertyType.isVersion() && !versionIgnored) {
                context.appendSql(" + 1");
            }
            context.appendSql(", ");
        }
        context.cutBackSql(2);
    }

}
