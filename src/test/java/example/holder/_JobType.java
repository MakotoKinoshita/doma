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

public class _JobType extends AbstractHolderType<Integer, JobType> {

    private static final _JobType singleton = new _JobType();

    private _JobType() {
        super(() -> new org.seasar.doma.wrapper.IntegerWrapper());
    }

    @Override
    public JobType newHolder(Integer value) {
        if (value == null) {
            return new JobType(0);
        }
        return new JobType(value);
    }

    @Override
    public Integer getBasicValue(JobType holder) {
        return holder.getValue();
    }

    @Override
    public Class<Integer> getBasicClass() {
        return Integer.class;
    }

    @Override
    public Class<JobType> getHolderClass() {
        return JobType.class;
    }

    public static _JobType getSingletonInternal() {
        return singleton;
    }

}
