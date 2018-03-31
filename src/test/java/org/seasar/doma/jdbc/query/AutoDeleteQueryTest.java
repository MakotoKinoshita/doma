package org.seasar.doma.jdbc.query;

import example.entity.Emp;
import example.entity._Emp;
import java.util.List;
import junit.framework.TestCase;
import org.seasar.doma.internal.jdbc.mock.MockConfig;
import org.seasar.doma.jdbc.InParameter;
import org.seasar.doma.jdbc.PreparedSql;
import org.seasar.doma.jdbc.SqlLogType;

/** @author taedium */
public class AutoDeleteQueryTest extends TestCase {

  private final MockConfig runtimeConfig = new MockConfig();

  public void testPrepare() throws Exception {
    Emp emp = new Emp();

    AutoDeleteQuery<Emp> query = new AutoDeleteQuery<Emp>(_Emp.getSingletonInternal());
    query.setMethod(getClass().getDeclaredMethod(getName()));
    query.setConfig(runtimeConfig);
    query.setEntity(emp);
    query.setCallerClassName("aaa");
    query.setCallerMethodName("bbb");
    query.setSqlLogType(SqlLogType.FORMATTED);
    query.prepare();

    DeleteQuery deleteQuery = query;
    assertNotNull(deleteQuery.getSql());
  }

  public void testOption_default() throws Exception {
    Emp emp = new Emp();
    emp.setId(10);
    emp.setName("aaa");
    emp.setVersion(100);

    AutoDeleteQuery<Emp> query = new AutoDeleteQuery<Emp>(_Emp.getSingletonInternal());
    query.setMethod(getClass().getDeclaredMethod(getName()));
    query.setConfig(runtimeConfig);
    query.setEntity(emp);
    query.setCallerClassName("aaa");
    query.setCallerMethodName("bbb");
    query.setSqlLogType(SqlLogType.FORMATTED);
    query.prepare();

    PreparedSql sql = query.getSql();
    assertEquals("delete from EMP where ID = ? and VERSION = ?", sql.getRawSql());
    List<InParameter<?>> parameters = sql.getParameters();
    assertEquals(2, parameters.size());
    assertEquals(Integer.valueOf(10), parameters.get(0).getWrapper().get());
    assertEquals(Integer.valueOf(100), parameters.get(1).getWrapper().get());
  }

  public void testOption_ignoreVersion() throws Exception {
    Emp emp = new Emp();
    emp.setId(10);
    emp.setName("aaa");
    emp.setVersion(100);

    AutoDeleteQuery<Emp> query = new AutoDeleteQuery<Emp>(_Emp.getSingletonInternal());
    query.setMethod(getClass().getDeclaredMethod(getName()));
    query.setConfig(runtimeConfig);
    query.setEntity(emp);
    query.setVersionIgnored(true);
    query.setCallerClassName("aaa");
    query.setCallerMethodName("bbb");
    query.setSqlLogType(SqlLogType.FORMATTED);
    query.prepare();

    PreparedSql sql = query.getSql();
    assertEquals("delete from EMP where ID = ?", sql.getRawSql());
    List<InParameter<?>> parameters = sql.getParameters();
    assertEquals(1, parameters.size());
    assertEquals(Integer.valueOf(10), parameters.get(0).getWrapper().get());
  }
}
