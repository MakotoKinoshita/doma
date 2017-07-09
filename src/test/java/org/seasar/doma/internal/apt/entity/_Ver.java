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
package org.seasar.doma.internal.apt.entity;

import org.seasar.doma.jdbc.holder.AbstractHolderDesc;

/**
 * @author taedium
 * 
 */
public class _Ver extends AbstractHolderDesc<Integer, Ver> {

    private _Ver() {
        super(() -> new org.seasar.doma.wrapper.IntegerWrapper());
    }

    @Override
    public Ver newHolder(Integer value) {
        return null;
    }

    @Override
    public Integer getBasicValue(Ver holder) {
        return null;
    }

    @Override
    public Class<Integer> getBasicClass() {
        return null;
    }

    @Override
    public Class<Ver> getHolderClass() {
        return null;
    }

    public static _Ver getSingletonInternal() {
        return null;
    }

}
