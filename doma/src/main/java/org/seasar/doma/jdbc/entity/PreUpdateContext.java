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
package org.seasar.doma.jdbc.entity;

/**
 * 更新処理の前処理のコンテキストです。
 * 
 * @author taedium
 * @since 1.11.0
 */
public interface PreUpdateContext {

    /**
     * エンティティが変更されたかどうかを返します。
     * 
     * @return エンティティが変更されたかどうか
     */
    public boolean isEntityChanged();

    /**
     * プロパティがエンティティに定義されているかどうかを返します。
     * 
     * @param propertyName
     *            プロパティ名
     * @return プロパティがエンティティに定義されているかどうか
     */
    public boolean isPropertyDefined(String propertyName);

    /**
     * プロパティが変更されているかどうかを返します。
     * 
     * @param propertyName
     *            プロパティ名
     * @return プロパティが変更されているかどうか
     * @exception EntityPropertyNotDefinedException
     *                プロパティがエンティティに定義されていない場合
     */
    public boolean isPropertyChanged(String propertyName);

    /**
     * エンティティのメタタイプを返します。
     * 
     * @return エンティティのメタタイプ
     */
    public EntityType<?> getEntityType();

}