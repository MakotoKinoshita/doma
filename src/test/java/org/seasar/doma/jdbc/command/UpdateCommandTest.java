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
package org.seasar.doma.jdbc.command;

import java.util.List;

import org.seasar.doma.internal.jdbc.mock.BindValue;
import org.seasar.doma.internal.jdbc.mock.MockConfig;
import org.seasar.doma.jdbc.OptimisticLockException;
import org.seasar.doma.jdbc.SqlLogType;
import org.seasar.doma.jdbc.query.AutoUpdateQuery;

import example.entity.Emp;
import example.entity._Emp;
import junit.framework.TestCase;

/**
 * @author taedium
 * 
 */
public class UpdateCommandTest extends TestCase {

    private final MockConfig runtimeConfig = new MockConfig();

    public void testExecute() throws Exception {
        Emp emp = new Emp();
        emp.setId(1);
        emp.setName("hoge");
        emp.setVersion(10);
        emp.originalStates = new Emp();

        AutoUpdateQuery<Emp> query = new AutoUpdateQuery<Emp>(_Emp.getSingletonInternal());
        query.setMethod(getClass().getDeclaredMethod(getName()));
        query.setConfig(runtimeConfig);
        query.setEntity(emp);
        query.setCallerClassName("aaa");
        query.setCallerMethodName("bbb");
        query.setSqlLogType(SqlLogType.FORMATTED);
        query.prepare();
        int rows = new UpdateCommand(query).execute();
        query.complete();

        assertEquals(1, rows);
        String sql = runtimeConfig.dataSource.connection.preparedStatement.sql;
        assertEquals("update EMP set NAME = ?, VERSION = ? + 1 where ID = ? and VERSION = ?", sql);

        List<BindValue> bindValues = runtimeConfig.dataSource.connection.preparedStatement.bindValues;
        assertEquals(4, bindValues.size());
        assertEquals("hoge", bindValues.get(0).getValue());
        assertEquals(Integer.valueOf(10), bindValues.get(1).getValue());
        assertEquals(Integer.valueOf(1), bindValues.get(2).getValue());
        assertEquals(Integer.valueOf(10), bindValues.get(3).getValue());
    }

    public void testExecute_throwsOptimisticLockException() throws Exception {
        Emp emp = new Emp();
        emp.setId(10);
        emp.setName("aaa");
        emp.setVersion(100);

        runtimeConfig.dataSource.connection.preparedStatement.updatedRows = 0;

        AutoUpdateQuery<Emp> query = new AutoUpdateQuery<Emp>(_Emp.getSingletonInternal());
        query.setMethod(getClass().getDeclaredMethod(getName()));
        query.setConfig(runtimeConfig);
        query.setEntity(emp);
        query.setCallerClassName("aaa");
        query.setCallerMethodName("bbb");
        query.setSqlLogType(SqlLogType.FORMATTED);
        query.prepare();
        UpdateCommand command = new UpdateCommand(query);
        try {
            command.execute();
            fail();
        } catch (OptimisticLockException expected) {
        }
    }

    public void testExecute_suppressesOptimisticLockException() throws Exception {
        Emp emp = new Emp();
        emp.setId(10);
        emp.setName("aaa");
        emp.setVersion(100);

        runtimeConfig.dataSource.connection.preparedStatement.updatedRows = 0;

        AutoUpdateQuery<Emp> query = new AutoUpdateQuery<Emp>(_Emp.getSingletonInternal());
        query.setMethod(getClass().getDeclaredMethod(getName()));
        query.setConfig(runtimeConfig);
        query.setEntity(emp);
        query.setOptimisticLockExceptionSuppressed(true);
        query.setCallerClassName("aaa");
        query.setCallerMethodName("bbb");
        query.setSqlLogType(SqlLogType.FORMATTED);
        query.prepare();
        new UpdateCommand(query).execute();
        query.complete();
    }

    public void testExecute_OriginalStates() throws Exception {
        Emp emp = new Emp();
        emp.setId(1);
        emp.setName("hoge");
        emp.setVersion(10);

        Emp states = new Emp();
        states.setId(1);
        states.setName("foo");
        states.setVersion(10);

        emp.originalStates = states;

        AutoUpdateQuery<Emp> query = new AutoUpdateQuery<Emp>(_Emp.getSingletonInternal());
        query.setMethod(getClass().getDeclaredMethod(getName()));
        query.setConfig(runtimeConfig);
        query.setEntity(emp);
        query.setCallerClassName("aaa");
        query.setCallerMethodName("bbb");
        query.setSqlLogType(SqlLogType.FORMATTED);
        query.prepare();
        int rows = new UpdateCommand(query).execute();
        query.complete();

        assertEquals(1, rows);
        String sql = runtimeConfig.dataSource.connection.preparedStatement.sql;
        assertEquals("update EMP set NAME = ?, VERSION = ? + 1 where ID = ? and VERSION = ?", sql);

        List<BindValue> bindValues = runtimeConfig.dataSource.connection.preparedStatement.bindValues;
        assertEquals(4, bindValues.size());
        assertEquals("hoge", bindValues.get(0).getValue());
        assertEquals(Integer.valueOf(10), bindValues.get(1).getValue());
        assertEquals(Integer.valueOf(1), bindValues.get(2).getValue());
        assertEquals(Integer.valueOf(10), bindValues.get(3).getValue());

        Emp updatedStates = emp.originalStates;
        assertEquals(Integer.valueOf(1), updatedStates.getId());
        assertEquals("hoge", updatedStates.getName());
        assertNull(updatedStates.getSalary());
        assertEquals(Integer.valueOf(11), updatedStates.getVersion());
    }

}
