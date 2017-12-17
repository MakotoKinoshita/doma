package org.seasar.doma.internal.jdbc.command;

import java.lang.reflect.Method;
import java.util.List;

import org.seasar.doma.internal.jdbc.mock.ColumnMetaData;
import org.seasar.doma.internal.jdbc.mock.MockConfig;
import org.seasar.doma.internal.jdbc.mock.MockResultSet;
import org.seasar.doma.internal.jdbc.mock.MockResultSetMetaData;
import org.seasar.doma.internal.jdbc.mock.RowData;
import org.seasar.doma.internal.jdbc.util.SqlFileUtil;
import org.seasar.doma.jdbc.SqlLogType;
import org.seasar.doma.jdbc.query.SqlFileSelectQuery;

import example.holder.PhoneNumber;
import example.holder._PhoneNumber;
import junit.framework.TestCase;

/**
 * @author taedium
 * 
 */
public class HolderResultListHandlerTest extends TestCase {

    private final MockConfig runtimeConfig = new MockConfig();

    private Method method;

    @Override
    protected void setUp() throws Exception {
        method = getClass().getMethod(getName());
    }

    public void testHandle() throws Exception {
        MockResultSetMetaData metaData = new MockResultSetMetaData();
        metaData.columns.add(new ColumnMetaData("x"));
        MockResultSet resultSet = new MockResultSet(metaData);
        resultSet.rows.add(new RowData("01-2345-6789"));
        resultSet.rows.add(new RowData("12-3456-7890"));

        SqlFileSelectQuery query = new SqlFileSelectQuery();
        query.setConfig(runtimeConfig);
        query.setSqlFilePath(SqlFileUtil.buildPath(getClass().getName(), getName()));
        query.setCallerClassName("aaa");
        query.setCallerMethodName("bbb");
        query.setMethod(method);
        query.setSqlLogType(SqlLogType.FORMATTED);
        query.prepare();

        ScalarResultListHandler<String, PhoneNumber> handler = new ScalarResultListHandler<>(
                () -> _PhoneNumber.getSingletonInternal().createScalar());
        List<PhoneNumber> results = handler.handle(resultSet, query, (i, next) -> {
        }).get();
        assertEquals(2, results.size());
        assertEquals("01-2345-6789", results.get(0).getValue());
        assertEquals("12-3456-7890", results.get(1).getValue());
    }

}
