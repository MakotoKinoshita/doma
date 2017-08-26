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
package org.seasar.doma.jdbc.dialect;

import java.util.Collections;

import org.seasar.doma.DomaNullPointerException;
import org.seasar.doma.expr.ExpressionFunctions;
import org.seasar.doma.internal.jdbc.dialect.MssqlForUpdateTransformer;
import org.seasar.doma.internal.jdbc.dialect.MssqlPagingTransformer;
import org.seasar.doma.jdbc.InParameter;
import org.seasar.doma.jdbc.JdbcMappingVisitor;
import org.seasar.doma.jdbc.PreparedSql;
import org.seasar.doma.jdbc.ScriptBlockContext;
import org.seasar.doma.jdbc.SelectForUpdateType;
import org.seasar.doma.jdbc.SqlKind;
import org.seasar.doma.jdbc.SqlLogFormattingVisitor;
import org.seasar.doma.jdbc.SqlLogType;
import org.seasar.doma.jdbc.SqlNode;
import org.seasar.doma.wrapper.Wrapper;

/**
 * Microsoft SQL Server用の方言です。
 * 
 * @author taedium
 * @since 1.30.0
 */
public class MssqlDialect extends Mssql2008Dialect {

    private boolean pagingForceOffsetFetch;

    /**
     * インスタンスを構築します。
     */
    public MssqlDialect() {
        this(new MssqlJdbcMappingVisitor(), new MssqlSqlLogFormattingVisitor(),
                new MssqlExpressionFunctions(), false);
    }

    /**
     * {@link JdbcMappingVisitor} を指定してインスタンスを構築します。
     * 
     * @param jdbcMappingVisitor
     *            {@link Wrapper} をJDBCの型とマッピングするビジター
     */
    public MssqlDialect(JdbcMappingVisitor jdbcMappingVisitor) {
        this(jdbcMappingVisitor, new MssqlSqlLogFormattingVisitor(), new MssqlExpressionFunctions(),
                false);
    }

    /**
     * {@link SqlLogFormattingVisitor} を指定してインスタンスを構築します。
     * 
     * @param sqlLogFormattingVisitor
     *            SQLのバインド変数にマッピングされる {@link Wrapper}
     *            をログ用のフォーマットされた文字列へと変換するビジター
     */
    public MssqlDialect(SqlLogFormattingVisitor sqlLogFormattingVisitor) {
        this(new MssqlJdbcMappingVisitor(), sqlLogFormattingVisitor, new MssqlExpressionFunctions(),
                false);
    }

    /**
     * {@link ExpressionFunctions} を指定してインスタンスを構築します。
     * 
     * @param expressionFunctions
     *            SQLのコメント式で利用可能な関数群
     */
    public MssqlDialect(ExpressionFunctions expressionFunctions) {
        this(new MssqlJdbcMappingVisitor(), new MssqlSqlLogFormattingVisitor(), expressionFunctions,
                false);
    }

    /**
     * {@link JdbcMappingVisitor} と {@link SqlLogFormattingVisitor}
     * を指定してインスタンスを構築します。
     * 
     * @param jdbcMappingVisitor
     *            {@link Wrapper} をJDBCの型とマッピングするビジター
     * @param sqlLogFormattingVisitor
     *            SQLのバインド変数にマッピングされる {@link Wrapper}
     *            をログ用のフォーマットされた文字列へと変換するビジター
     */
    public MssqlDialect(JdbcMappingVisitor jdbcMappingVisitor,
            SqlLogFormattingVisitor sqlLogFormattingVisitor) {
        this(jdbcMappingVisitor, sqlLogFormattingVisitor, new MssqlExpressionFunctions(), false);
    }

    /**
     * {@link JdbcMappingVisitor} と {@link SqlLogFormattingVisitor} と
     * {@link ExpressionFunctions} を指定してインスタンスを構築します。
     * 
     * @param jdbcMappingVisitor
     *            {@link Wrapper} をJDBCの型とマッピングするビジター
     * @param sqlLogFormattingVisitor
     *            SQLのバインド変数にマッピングされる {@link Wrapper}
     *            をログ用のフォーマットされた文字列へと変換するビジター
     * @param expressionFunctions
     *            SQLのコメント式で利用可能な関数群
     */
    public MssqlDialect(JdbcMappingVisitor jdbcMappingVisitor,
            SqlLogFormattingVisitor sqlLogFormattingVisitor,
            ExpressionFunctions expressionFunctions) {
        this(jdbcMappingVisitor, sqlLogFormattingVisitor, expressionFunctions, false);
    }

    /**
     * {@link JdbcMappingVisitor} と {@link SqlLogFormattingVisitor} と
     * {@link ExpressionFunctions} を指定してインスタンスを構築します。
     * 
     * @param jdbcMappingVisitor
     *            {@link Wrapper} をJDBCの型とマッピングするビジター
     * @param sqlLogFormattingVisitor
     *            SQLのバインド変数にマッピングされる {@link Wrapper}
     *            をログ用のフォーマットされた文字列へと変換するビジター
     * @param expressionFunctions
     *            SQLのコメント式で利用可能な関数群
     * @param pagingForceOffsetFetch
     *            ページングを行う際、常に OFFSET-FETCH で行うかどうか
     */
    public MssqlDialect(JdbcMappingVisitor jdbcMappingVisitor,
            SqlLogFormattingVisitor sqlLogFormattingVisitor,
            ExpressionFunctions expressionFunctions, boolean pagingForceOffsetFetch) {
        super(jdbcMappingVisitor, sqlLogFormattingVisitor, expressionFunctions);
        this.pagingForceOffsetFetch = pagingForceOffsetFetch;
    }

    @Override
    public String getName() {
        return "mssql";
    }

    @Override
    protected SqlNode toForUpdateSqlNode(SqlNode sqlNode, SelectForUpdateType forUpdateType,
            int waitSeconds, String... aliases) {
        MssqlForUpdateTransformer transformer = new MssqlForUpdateTransformer(forUpdateType,
                waitSeconds, aliases);
        return transformer.transform(sqlNode);
    }

    @Override
    protected SqlNode toPagingSqlNode(SqlNode sqlNode, long offset, long limit) {
        MssqlPagingTransformer transformer = new MssqlPagingTransformer(offset, limit,
                this.pagingForceOffsetFetch);
        return transformer.transform(sqlNode);
    }

    @Override
    public PreparedSql getSequenceNextValSql(String qualifiedSequenceName, long allocationSize) {
        if (qualifiedSequenceName == null) {
            throw new DomaNullPointerException("qualifiedSequenceName");
        }
        String rawSql = "select next value for " + qualifiedSequenceName;
        return new PreparedSql(SqlKind.SELECT, rawSql, rawSql, null,
                Collections.<InParameter<?>> emptyList(), SqlLogType.FORMATTED);
    }

    @Override
    public boolean supportsSequence() {
        return true;
    }

    @Override
    public ScriptBlockContext createScriptBlockContext() {
        return new MssqlScriptBlockContext();
    }

    /**
     * Microsoft SQL Server用の {@link JdbcMappingVisitor} の実装です。
     * 
     * @author taedium
     * 
     */
    public static class MssqlJdbcMappingVisitor extends Mssql2008JdbcMappingVisitor {
    }

    /**
     * Microsoft SQL Server用の {@link SqlLogFormattingVisitor} の実装です。
     * 
     * @author taedium
     * 
     */
    public static class MssqlSqlLogFormattingVisitor extends Mssql2008SqlLogFormattingVisitor {
    }

    /**
     * Microsoft SQL Server用の {@link ExpressionFunctions} です。
     * 
     * @author taedium
     * 
     */
    public static class MssqlExpressionFunctions extends Mssql2008ExpressionFunctions {

        public MssqlExpressionFunctions() {
            super();
        }

        public MssqlExpressionFunctions(char[] wildcards) {
            super(wildcards);
        }

        protected MssqlExpressionFunctions(char escapeChar, char[] wildcards) {
            super(escapeChar, wildcards);
        }

    }

    /**
     * Microsoft SQL Server用の {@link ScriptBlockContext} です。
     * 
     * @author taedium
     */
    public static class MssqlScriptBlockContext extends Mssql2008ScriptBlockContext {
    }
}
