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
package example.holder;

import org.seasar.doma.jdbc.holder.AbstractHolderType;

public class _InternationalPhoneNumber extends
        AbstractHolderType<String, InternationalPhoneNumber> {

    private static final _InternationalPhoneNumber singleton = new _InternationalPhoneNumber();

    private _InternationalPhoneNumber() {
        super(() -> new org.seasar.doma.wrapper.StringWrapper());
    }

    @Override
    public InternationalPhoneNumber newHolder(String value) {
        return new InternationalPhoneNumber(value);
    }

    @Override
    public String getBasicValue(InternationalPhoneNumber holder) {
        return holder.getValue();
    }

    @Override
    public Class<String> getBasicClass() {
        return String.class;
    }

    @Override
    public Class<InternationalPhoneNumber> getHolderClass() {
        return InternationalPhoneNumber.class;
    }

    public static _InternationalPhoneNumber getSingletonInternal() {
        return singleton;
    }

}
