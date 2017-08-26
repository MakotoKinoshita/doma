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
package org.seasar.doma;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Statement;

import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.JdbcException;
import org.seasar.doma.jdbc.OptimisticLockException;
import org.seasar.doma.jdbc.SqlFileNotFoundException;
import org.seasar.doma.jdbc.SqlLogType;

/**
 * 削除処理を示します。
 * <p>
 * このアノテーションが注釈されるメソッドは、Daoインタフェースのメンバでなければいけません。
 * 
 * <h3>例:</h3>
 * 
 * <pre>
 * &#064;Entity
 * public class Employee {
 *     ...
 * }
 * 
 * &#064;Dao(config = AppConfig.class)
 * public interface EmployeeDao {
 * 
 *     &#064;Delete
 *     int delete(Employee employee);
 * }
 * </pre>
 * 
 * 注釈されるメソッドは、次の例外をスローすることがあります。
 * <ul>
 * <li>{@link DomaNullPointerException} パラメータに {@code null} を渡した場合
 * <li>{@link OptimisticLockException} 楽観的排他制御が有効で更新件数が0件の場合
 * <li>{@link SqlFileNotFoundException} {@code sqlFile} 要素が {@code true}
 * で、SQLファイルが見つからなかった場合
 * <li>{@link JdbcException} 上記以外でJDBCに関する例外が発生した場合
 * </ul>
 * 
 * @author taedium
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@DaoMethod
@EntityField
public @interface Delete {

    /**
     * SQLファイルにマッピングするかどうかを返します。
     * 
     * @return SQLファイルにマッピングするかどうか
     */
    boolean sqlFile() default false;

    /**
     * クエリタイムアウト（秒）を返します。
     * <p>
     * 指定しない場合、{@link Config#getQueryTimeout()}が使用されます。
     * 
     * @return クエリタイムアウト（秒）
     * @see Statement#setQueryTimeout(int)
     */
    int queryTimeout() default -1;

    /**
     * 楽観的排他制御用のバージョン番号を無視するかどうかを返します。
     * <p>
     * {@code true} の場合、削除条件にバージョン番号を含めません。
     * 
     * @return 楽観的排他制御用のバージョン番号を無視するかどうか
     */
    boolean ignoreVersion() default false;

    /**
     * 削除結果が1件でない場合にスローされる {@link OptimisticLockException}を抑制するかどうかを返します。
     * 
     * @return {@link OptimisticLockException}を抑制するかどうか
     */
    boolean suppressOptimisticLockException() default false;

    /**
     * SQLのログの出力形式を返します。
     * 
     * @return SQLログの出力形式
     * @since 2.0.0
     */
    SqlLogType sqlLog() default SqlLogType.FORMATTED;
}
