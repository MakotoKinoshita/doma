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
import java.math.BigDecimal;
import java.util.Collections;

import org.seasar.doma.FetchType;
import org.seasar.doma.internal.jdbc.mock.ColumnMetaData;
import org.seasar.doma.internal.jdbc.mock.MockConfig;
import org.seasar.doma.internal.jdbc.mock.MockResultSet;
import org.seasar.doma.internal.jdbc.mock.MockResultSetMetaData;
import org.seasar.doma.internal.jdbc.mock.RowData;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.PreparedSql;
import org.seasar.doma.jdbc.SelectOptions;
import org.seasar.doma.jdbc.SqlKind;
import org.seasar.doma.jdbc.SqlLogType;
import org.seasar.doma.jdbc.UnknownColumnException;
import org.seasar.doma.jdbc.UnknownColumnHandler;
import org.seasar.doma.jdbc.entity.EntityDesc;
import org.seasar.doma.jdbc.query.Query;
import org.seasar.doma.jdbc.query.SelectQuery;

import example.entity.Emp;
import example.entity._Emp;
import junit.framework.TestCase;

/**
 * @author taedium
 * 
 */
public class EntityProviderTest extends TestCase {

    public void testGetEntity() throws Exception {
        MockResultSetMetaData metaData = new MockResultSetMetaData();
        metaData.columns.add(new ColumnMetaData("id"));
        metaData.columns.add(new ColumnMetaData("name"));
        metaData.columns.add(new ColumnMetaData("salary"));
        metaData.columns.add(new ColumnMetaData("version"));
        MockResultSet resultSet = new MockResultSet(metaData);
        resultSet.rows.add(new RowData(1, "aaa", new BigDecimal(10), 100));
        resultSet.next();

        _Emp entityDesc = _Emp.getSingletonInternal();
        EntityProvider<Emp> provider = new EntityProvider<>(entityDesc,
                new MySelectQuery(new MockConfig()), false);
        Emp emp = provider.get(resultSet);

        assertEquals(Integer.valueOf(1), emp.getId());
        assertEquals("aaa", emp.getName());
        assertEquals(new BigDecimal(10), emp.getSalary());
        assertEquals(Integer.valueOf(100), emp.getVersion());
    }

    public void testGetEntity_UnknownColumnException() throws Exception {
        MockResultSetMetaData metaData = new MockResultSetMetaData();
        metaData.columns.add(new ColumnMetaData("id"));
        metaData.columns.add(new ColumnMetaData("name"));
        metaData.columns.add(new ColumnMetaData("salary"));
        metaData.columns.add(new ColumnMetaData("version"));
        metaData.columns.add(new ColumnMetaData("unknown"));
        MockResultSet resultSet = new MockResultSet(metaData);
        resultSet.rows
                .add(new RowData(1, "aaa", new BigDecimal(10), 100, "bbb"));
        resultSet.next();

        _Emp entityDesc = _Emp.getSingletonInternal();
        EntityProvider<Emp> provider = new EntityProvider<>(entityDesc,
                new MySelectQuery(new MockConfig()), false);
        try {
            provider.get(resultSet);
            fail();
        } catch (UnknownColumnException expected) {
        }
    }

    public void testGetEntity_EmptyUnknownColumnHandler() throws Exception {
        MockResultSetMetaData metaData = new MockResultSetMetaData();
        metaData.columns.add(new ColumnMetaData("id"));
        metaData.columns.add(new ColumnMetaData("name"));
        metaData.columns.add(new ColumnMetaData("salary"));
        metaData.columns.add(new ColumnMetaData("version"));
        metaData.columns.add(new ColumnMetaData("unknown"));
        MockResultSet resultSet = new MockResultSet(metaData);
        resultSet.rows
                .add(new RowData(1, "aaa", new BigDecimal(10), 100, "bbb"));
        resultSet.next();

        _Emp entityDesc = _Emp.getSingletonInternal();
        EntityProvider<Emp> provider = new EntityProvider<>(entityDesc,
                new MySelectQuery(new EmptyUnknownColumnHandlerConfig()), false);
        Emp emp = provider.get(resultSet);

        assertEquals(Integer.valueOf(1), emp.getId());
        assertEquals("aaa", emp.getName());
        assertEquals(new BigDecimal(10), emp.getSalary());
        assertEquals(Integer.valueOf(100), emp.getVersion());
    }

    protected class MySelectQuery implements SelectQuery {

        private final Config config;

        MySelectQuery(Config config) {
            this.config = config;
        }

        @Override
        public SelectOptions getOptions() {
            return SelectOptions.get();
        }

        @Override
        public Config getConfig() {
            return config;
        }

        @Override
        public String getClassName() {
            return null;
        }

        @Override
        public String getMethodName() {
            return null;
        }

        @Override
        public PreparedSql getSql() {
            return new PreparedSql(SqlKind.SELECT, "dummy", "dummy", "dummy",
                    Collections.emptyList(), SqlLogType.FORMATTED);
        }

        @Override
        public boolean isResultEnsured() {
            return false;
        }

        @Override
        public boolean isResultMappingEnsured() {
            return false;
        }

        @Override
        public FetchType getFetchType() {
            return FetchType.LAZY;
        }

        @Override
        public int getFetchSize() {
            return 0;
        }

        @Override
        public int getMaxRows() {
            return 0;
        }

        @Override
        public int getQueryTimeout() {
            return 0;
        }

        @Override
        public void prepare() {

        }

        @Override
        public void complete() {
        }

        @Override
        public Method getMethod() {
            return null;
        }

        @Override
        public SqlLogType getSqlLogType() {
            return null;
        }

        @Override
        public String comment(String sql) {
            return sql;
        }

        @Override
        public boolean isResultStream() {
            return false;
        }

    }

    protected static class EmptyUnknownColumnHandler implements
            UnknownColumnHandler {
        @Override
        public void handle(Query query, EntityDesc<?> entityDesc,
                String unknownColumnName) {
        }
    }

    protected static class EmptyUnknownColumnHandlerConfig extends MockConfig {
        @Override
        public UnknownColumnHandler getUnknownColumnHandler() {
            return new EmptyUnknownColumnHandler();
        }

    }
}
