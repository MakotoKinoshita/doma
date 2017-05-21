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
package org.seasar.doma.internal.jdbc.command;

import java.lang.reflect.Method;

import org.seasar.doma.internal.jdbc.mock.ColumnMetaData;
import org.seasar.doma.internal.jdbc.mock.MockConfig;
import org.seasar.doma.internal.jdbc.mock.MockResultSet;
import org.seasar.doma.internal.jdbc.mock.MockResultSetMetaData;
import org.seasar.doma.internal.jdbc.mock.RowData;
import org.seasar.doma.internal.jdbc.util.SqlFileUtil;
import org.seasar.doma.jdbc.IterationCallback;
import org.seasar.doma.jdbc.IterationContext;
import org.seasar.doma.jdbc.SqlLogType;
import org.seasar.doma.jdbc.query.SqlFileSelectQuery;

import example.entity.Emp;
import example.entity._Emp;
import junit.framework.TestCase;

/**
 * @author taedium
 * 
 */
public class EntityIterationHandlerTest extends TestCase {

    private final MockConfig runtimeConfig = new MockConfig();

    private Method method;

    @Override
    protected void setUp() throws Exception {
        method = getClass().getMethod(getName());
    }

    public void testHandle() throws Exception {
        MockResultSetMetaData metaData = new MockResultSetMetaData();
        metaData.columns.add(new ColumnMetaData("id"));
        metaData.columns.add(new ColumnMetaData("name"));
        MockResultSet resultSet = new MockResultSet(metaData);
        resultSet.rows.add(new RowData(1, "aaa"));
        resultSet.rows.add(new RowData(2, "bbb"));

        SqlFileSelectQuery query = new SqlFileSelectQuery();
        query.setConfig(runtimeConfig);
        query.setSqlFilePath(SqlFileUtil.buildPath(getClass().getName(),
                getName()));
        query.setCallerClassName("aaa");
        query.setCallerMethodName("bbb");
        query.setMethod(method);
        query.setSqlLogType(SqlLogType.FORMATTED);
        query.prepare();

        EntityIterationHandler<Emp, Integer> handler = new EntityIterationHandler<>(
                _Emp.getSingletonInternal(),
                new IterationCallback<Emp, Integer>() {

                    private int count;

                    @Override
                    public Integer iterate(Emp target,
                            IterationContext iterationContext) {
                        count++;
                        return count;
                    }

                });
        Integer result = handler.handle(resultSet, query, (i, next) -> {
        }).get();
        assertEquals(Integer.valueOf(2), result);
    }

    public void testHandle_exits() throws Exception {
        MockResultSetMetaData metaData = new MockResultSetMetaData();
        metaData.columns.add(new ColumnMetaData("id"));
        metaData.columns.add(new ColumnMetaData("name"));
        MockResultSet resultSet = new MockResultSet(metaData);
        resultSet.rows.add(new RowData(1, "aaa"));
        resultSet.rows.add(new RowData(2, "bbb"));

        SqlFileSelectQuery query = new SqlFileSelectQuery();
        query.setConfig(runtimeConfig);
        query.setSqlFilePath(SqlFileUtil.buildPath(getClass().getName(),
                getName()));
        query.setCallerClassName("aaa");
        query.setCallerMethodName("bbb");
        query.setMethod(method);
        query.setSqlLogType(SqlLogType.FORMATTED);
        query.prepare();

        EntityIterationHandler<Emp, Integer> handler = new EntityIterationHandler<>(
                _Emp.getSingletonInternal(),
                new IterationCallback<Emp, Integer>() {

                    private int count;

                    @Override
                    public Integer iterate(Emp target,
                            IterationContext iterationContext) {
                        iterationContext.exit();
                        count++;
                        return count;
                    }

                });
        Integer result = handler.handle(resultSet, query, (i, next) -> {
        }).get();
        assertEquals(Integer.valueOf(1), result);
    }

}
