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
package org.seasar.doma.internal.jdbc.scalar;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.function.Supplier;

import org.seasar.doma.jdbc.ClassHelper;
import org.seasar.doma.wrapper.EnumWrapper;
import org.seasar.doma.wrapper.IntegerWrapper;
import org.seasar.doma.wrapper.StringWrapper;
import org.seasar.doma.wrapper.Wrapper;

import example.holder.InternationalPhoneNumber;
import example.holder.PhoneNumber;
import junit.framework.TestCase;

/**
 * @author taedium
 * 
 */
public class ScalarsTest extends TestCase {

    private final ClassHelper classHelper = new ClassHelper() {
    };

    public void testWrapBasic() throws Exception {
        assertNotNull(Scalars.wrap(true, boolean.class, false, classHelper));
        assertNotNull(Scalars.wrap(true, Boolean.class, false, classHelper));
        assertNotNull(Scalars.wrap((byte) 1, byte.class, false, classHelper));
        assertNotNull(Scalars.wrap(Byte.valueOf((byte) 1), Byte.class, false,
                classHelper));
        assertNotNull(Scalars.wrap((short) 1, short.class, false, classHelper));
        assertNotNull(Scalars.wrap(Short.valueOf((short) 1), Short.class, false,
                classHelper));
        assertNotNull(Scalars.wrap(1, int.class, false, classHelper));
        assertNotNull(Scalars.wrap(Integer.valueOf(1), Integer.class, false,
                classHelper));
        assertNotNull(Scalars.wrap(1L, long.class, false, classHelper));
        assertNotNull(
                Scalars.wrap(Long.valueOf(1), Long.class, false, classHelper));
        assertNotNull(Scalars.wrap(1f, float.class, false, classHelper));
        assertNotNull(Scalars.wrap(Float.valueOf(1), Float.class, false,
                classHelper));
        assertNotNull(Scalars.wrap(1d, double.class, false, classHelper));
        assertNotNull(Scalars.wrap(Double.valueOf(1), Double.class, false,
                classHelper));
        assertNotNull(Scalars.wrap(new byte[] { 1 }, byte[].class, false,
                classHelper));
        assertNotNull(Scalars.wrap("", String.class, false, classHelper));
        assertNotNull(Scalars.wrap(new BigDecimal("1"), BigDecimal.class,
                false, classHelper));
        assertNotNull(Scalars.wrap(new BigInteger("1"), BigInteger.class,
                false, classHelper));
        assertNotNull(Scalars.wrap(Date.valueOf("2009-01-23"), Date.class,
                false, classHelper));
        assertNotNull(Scalars.wrap(Time.valueOf("12:34:56"), Time.class, false,
                classHelper));
        assertNotNull(Scalars.wrap(Timestamp.valueOf("2009-01-23 12:34:56"),
                Timestamp.class, false, classHelper));
        assertNotNull(Scalars.wrap(new java.util.Date(), java.util.Date.class,
                false, classHelper));
        assertNotNull(Scalars.wrap(LocalDate.of(2009, 01, 23), LocalDate.class,
                false, classHelper));
        assertNotNull(Scalars.wrap(LocalDateTime.of(2009, 01, 23, 12, 34, 56),
                LocalDateTime.class, false, classHelper));
        assertNotNull(Scalars.wrap(LocalTime.of(12, 34, 56), LocalTime.class,
                false, classHelper));
        assertNotNull(Scalars.wrap(null, Array.class, false, classHelper));
        assertNotNull(Scalars.wrap(null, Blob.class, false, classHelper));
        assertNotNull(Scalars.wrap(null, Clob.class, false, classHelper));
        assertNotNull(Scalars.wrap(null, NClob.class, false, classHelper));
    }

    public void testWrapBasic_primitiveType() throws Exception {
        Supplier<Scalar<?, ?>> supplier = Scalars.wrap(Integer.valueOf(10),
                int.class, false, classHelper);
        assertNotNull(supplier);

        Scalar<?, ?> scalar = supplier.get();
        assertEquals(Integer.valueOf(10), scalar.get());

        Wrapper<?> wrapper = scalar.getWrapper();
        assertEquals(IntegerWrapper.class, wrapper.getClass());
        assertEquals(Integer.valueOf(10), wrapper.get());
    }

    public void testWrapBasic_null() throws Exception {
        Supplier<Scalar<?, ?>> supplier = Scalars.wrap(null, Integer.class,
                false, classHelper);
        assertNotNull(supplier);

        Scalar<?, ?> scalar = supplier.get();
        assertNull(null, scalar.get());

        Wrapper<?> wrapper = scalar.getWrapper();
        assertEquals(IntegerWrapper.class, wrapper.getClass());
        assertNull(null, wrapper.get());
    }

    @SuppressWarnings("unchecked")
    public void testWrapBasic_optional() throws Exception {
        Supplier<Scalar<?, ?>> supplier = Scalars.wrap(Integer.valueOf(10),
                Integer.class, true, classHelper);
        assertNotNull(supplier);

        Scalar<?, ?> scalar = supplier.get();
        assertTrue(scalar.get() instanceof Optional);
        Optional<Integer> optional = (Optional<Integer>) scalar.get();
        assertEquals(Integer.valueOf(10), optional.get());

        Wrapper<?> wrapper = scalar.getWrapper();
        assertEquals(IntegerWrapper.class, wrapper.getClass());
        assertEquals(Integer.valueOf(10), wrapper.get());
    }

    @SuppressWarnings("unchecked")
    public void testWrapBasic_optional_null() throws Exception {
        Supplier<Scalar<?, ?>> supplier = Scalars.wrap(null, Integer.class,
                true, classHelper);
        assertNotNull(supplier);

        Scalar<?, ?> scalar = supplier.get();
        assertTrue(scalar.get() instanceof Optional);
        Optional<Integer> optional = (Optional<Integer>) scalar.get();
        assertFalse(optional.isPresent());

        Wrapper<?> wrapper = scalar.getWrapper();
        assertEquals(IntegerWrapper.class, wrapper.getClass());
        assertNull(wrapper.get());
    }

    public void testWrapEnum() throws Exception {
        Supplier<Scalar<?, ?>> supplier = Scalars.wrap(MyEnum.AAA,
                MyEnum.class, false, classHelper);
        assertNotNull(supplier);

        Scalar<?, ?> scalar = supplier.get();
        assertEquals(MyEnum.AAA, scalar.get());

        Wrapper<?> wrapper = supplier.get().getWrapper();
        assertEquals(EnumWrapper.class, wrapper.getClass());
        assertEquals(MyEnum.AAA, wrapper.get());
    }

    public void testWrapEnum_null() throws Exception {
        Supplier<Scalar<?, ?>> supplier = Scalars.wrap(null, MyEnum.class,
                false, classHelper);
        assertNotNull(supplier);

        Scalar<?, ?> scalar = supplier.get();
        assertNull(scalar.get());

        Wrapper<?> wrapper = scalar.getWrapper();
        assertEquals(EnumWrapper.class, wrapper.getClass());
        assertNull(wrapper.get());
    }

    @SuppressWarnings("unchecked")
    public void testWrapEnum_optional() throws Exception {
        Supplier<Scalar<?, ?>> supplier = Scalars.wrap(MyEnum.AAA,
                MyEnum.class, true, classHelper);
        assertNotNull(supplier);

        Scalar<?, ?> scalar = supplier.get();
        assertTrue(scalar.get() instanceof Optional);
        Optional<MyEnum> optional = (Optional<MyEnum>) scalar.get();
        assertEquals(MyEnum.AAA, optional.get());

        Wrapper<?> wrapper = scalar.getWrapper();
        assertEquals(EnumWrapper.class, wrapper.getClass());
        assertEquals(MyEnum.AAA, wrapper.get());
    }

    @SuppressWarnings("unchecked")
    public void testWrapEnum_optional_null() throws Exception {
        Supplier<Scalar<?, ?>> supplier = Scalars.wrap(null, MyEnum.class,
                true, classHelper);
        assertNotNull(supplier);

        Scalar<?, ?> scalar = supplier.get();
        assertTrue(scalar.get() instanceof Optional);
        Optional<MyEnum> optional = (Optional<MyEnum>) scalar.get();
        assertFalse(optional.isPresent());

        Wrapper<?> wrapper = scalar.getWrapper();
        assertEquals(EnumWrapper.class, wrapper.getClass());
        assertNull(wrapper.get());
    }

    public void testWrapHolder() throws Exception {
        PhoneNumber phoneNumber = new PhoneNumber("123-456-789");
        Supplier<Scalar<?, ?>> supplier = Scalars.wrap(phoneNumber,
                PhoneNumber.class, false, classHelper);
        assertNotNull(supplier);

        Scalar<?, ?> scalar = supplier.get();
        assertEquals(phoneNumber, scalar.get());

        Wrapper<?> wrapper = scalar.getWrapper();
        assertEquals(StringWrapper.class, wrapper.getClass());
        assertEquals("123-456-789", wrapper.get());
    }

    public void testWrapHolder_subclass() throws Exception {
        PhoneNumber phoneNumber = new InternationalPhoneNumber("123-456-789");
        Supplier<Scalar<?, ?>> supplier = Scalars.wrap(phoneNumber,
                InternationalPhoneNumber.class, false, classHelper);
        assertNotNull(supplier);

        Scalar<?, ?> scalar = supplier.get();
        assertTrue(scalar.get() instanceof InternationalPhoneNumber);

        Wrapper<?> wrapper = scalar.getWrapper();
        assertEquals(StringWrapper.class, wrapper.getClass());
        assertEquals("123-456-789", wrapper.get());
    }

    public void testWrapHolder_null() throws Exception {
        Supplier<Scalar<?, ?>> supplier = Scalars.wrap(null, PhoneNumber.class,
                false, classHelper);
        assertNotNull(supplier);

        Scalar<?, ?> scalar = supplier.get();
        assertTrue(scalar.get() instanceof PhoneNumber);
        PhoneNumber phoneNumber = (PhoneNumber) scalar.get();
        assertNull(phoneNumber.getValue());

        Wrapper<?> wrapper = scalar.getWrapper();
        assertEquals(StringWrapper.class, wrapper.getClass());
        assertNull(wrapper.get());
    }

    @SuppressWarnings("unchecked")
    public void testWrapHolder_option() throws Exception {
        PhoneNumber phoneNumber = new PhoneNumber("123-456-789");
        Supplier<Scalar<?, ?>> supplier = Scalars.wrap(phoneNumber,
                PhoneNumber.class, true, classHelper);
        assertNotNull(supplier);

        Scalar<?, ?> scalar = supplier.get();
        assertTrue(scalar.get() instanceof Optional);
        Optional<PhoneNumber> optional = (Optional<PhoneNumber>) scalar.get();
        assertEquals(phoneNumber, optional.get());

        Wrapper<?> wrapper = scalar.getWrapper();
        assertEquals(StringWrapper.class, wrapper.getClass());
        assertEquals("123-456-789", wrapper.get());
    }

    @SuppressWarnings("unchecked")
    public void testWrapHolder_option_null() throws Exception {
        Supplier<Scalar<?, ?>> supplier = Scalars.wrap(null, PhoneNumber.class,
                true, classHelper);
        assertNotNull(supplier);

        Scalar<?, ?> scalar = supplier.get();
        assertTrue(scalar.get() instanceof Optional);
        Optional<PhoneNumber> optional = (Optional<PhoneNumber>) scalar.get();
        assertFalse(optional.isPresent());

        Wrapper<?> wrapper = scalar.getWrapper();
        assertEquals(StringWrapper.class, wrapper.getClass());
        assertEquals(null, wrapper.get());
    }

    public enum MyEnum {
        AAA, BBB, CCC
    }
}
