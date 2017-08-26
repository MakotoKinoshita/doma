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
import org.seasar.doma.jdbc.IterationCallback;
import org.seasar.doma.jdbc.JdbcException;
import org.seasar.doma.jdbc.NoResultException;
import org.seasar.doma.jdbc.NonSingleColumnException;
import org.seasar.doma.jdbc.NonUniqueResultException;
import org.seasar.doma.jdbc.ResultMappingException;
import org.seasar.doma.jdbc.SelectOptions;
import org.seasar.doma.jdbc.SqlFileNotFoundException;
import org.seasar.doma.jdbc.SqlLogType;
import org.seasar.doma.jdbc.UnknownColumnException;

/**
 * 検索処理を示します。
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
 *     &#064;Select
 *     String selectNameById(Integer id);
 *     
 *     &#064;Select
 *     List&lt;Employee&gt; selectNamesByAgeAndSalary(Integer age, BigDecimal salary);
 * 
 *     &#064;Select
 *     Employee selectById(Integer id);
 *     
 *     &#064;Select
 *     List&lt;Employee&gt; selectByExample(Employee example);
 *     
 *     &#064;Select(strategy = SelectStrategyType.STREAM)
 *     &lt;R&gt; R selectSalary(Integer departmentId, Function&lt;Stream&lt;BigDecimal&gt;, R&gt; mapper);
 * }
 * </pre>
 * 
 * 注釈されるメソッドは、次の例外をスローすることがあります。
 * <ul>
 * <li>{@link DomaNullPointerException} パラメータに {@code null} を渡した場合
 * <li>{@link SqlFileNotFoundException} SQLファイルが見つからなかった場合
 * <li>{@link UnknownColumnException} 結果セットに含まれるカラムにマッピングされたプロパティが見つからなかった場合
 * <li>{@link NonUniqueResultException} 戻り値の型が {@code List}
 * でない場合で、かつ結果が2件以上返された場合
 * <li>{@link NonSingleColumnException} 戻り値の型が、基本型やドメインクラスもしくは基本型やドメインクラスの
 * {@code List} 場合で、かつ結果セットに複数のカラムが含まれている場合
 * <li>{@link NoResultException} {@link #ensureResult} に {@code true}
 * が指定され、結果が0件の場合
 * <li>{@link ResultMappingException} {@link #ensureResultMapping()} に
 * {@code true} が指定され、マッピングされないプロパティが存在する場合
 * <li>{@link JdbcException} 上記以外でJDBCに関する例外が発生した場合
 * </ul>
 * 
 * @author taedium
 * @see SelectOptions
 * @see IterationCallback
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@DaoMethod
public @interface Select {

    /**
     * クエリタイムアウト（秒）を返します。
     * <p>
     * 指定しない場合、 {@link Config#getQueryTimeout()} が使用されます。
     * 
     * @return クエリタイムアウト（秒）
     * @see Statement#setQueryTimeout(int)
     */
    int queryTimeout() default -1;

    /**
     * フェッチサイズを返します。
     * <p>
     * 指定しない場合、 {@link Config#getFetchSize()} が使用されます。
     * 
     * @return フェッチサイズ
     * @see Statement#setFetchSize(int)
     */
    int fetchSize() default -1;

    /**
     * 最大行数の制限値を返します。
     * <p>
     * 指定しない場合、 {@link Config#getMaxRows()} が使用されます。
     * 
     * @return 最大行数の制限値
     * @see Statement#setMaxRows(int)
     */
    int maxRows() default -1;

    /**
     * 検索結果を扱う戦略を返します。
     * 
     * @return 検索結果を扱う戦略
     * @since 2.0.0
     */
    SelectType strategy() default SelectType.RETURN;

    /**
     * フェッチのタイプを返します。
     * 
     * @return フェッチのタイプを返します。
     * @since 2.0.0
     */
    FetchType fetch() default FetchType.LAZY;

    /**
     * 結果が少なくとも1件以上存在することを保証するかどうかを返します。
     * <p>
     * {@code true} の場合に結果が存在しなければ、このアノテーションが注釈されたメソッドから
     * {@link NoResultException} がスローされます。
     * 
     * @return 1件以上存在することを保証するかどうか
     */
    boolean ensureResult() default false;

    /**
     * 結果がエンティティやエンティティのリストの場合、
     * エンティティのすべてのプロパティに結果セットのカラムがマッピングされることを保証するかどうかを返します。
     * <p>
     * {@code true} の場合、マッピングされないプロパティが存在すれば、このアノテーションが注釈されたメソッドから
     * {@link ResultMappingException} がスローされます。
     * 
     * @return エンティティのすべてのプロパティに結果セットのカラムがマッピングされることを保証するかどうか
     * @since 1.34.0
     */
    boolean ensureResultMapping() default false;

    /**
     * 検索結果を {@code Map<Object, String>} もしくは {@code List<Map<Object, String>>}
     * として取得する場合のマップのキーに対するネーミング規約を返します。
     * 
     * @return ネーミング規約
     * @since 1.7.0
     */
    MapKeyNamingType mapKeyNaming() default MapKeyNamingType.NONE;

    /**
     * SQLのログの出力形式を返します。
     * 
     * @return SQLログの出力形式
     * @since 2.0.0
     */
    SqlLogType sqlLog() default SqlLogType.FORMATTED;
}
