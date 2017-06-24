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
package org.seasar.doma.internal.apt.cttype;

import static org.seasar.doma.internal.util.AssertionUtil.assertUnreachable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor8;

import org.seasar.doma.internal.apt.Context;
import org.seasar.doma.wrapper.ArrayWrapper;
import org.seasar.doma.wrapper.BigDecimalWrapper;
import org.seasar.doma.wrapper.BigIntegerWrapper;
import org.seasar.doma.wrapper.BlobWrapper;
import org.seasar.doma.wrapper.BooleanWrapper;
import org.seasar.doma.wrapper.ByteWrapper;
import org.seasar.doma.wrapper.BytesWrapper;
import org.seasar.doma.wrapper.ClobWrapper;
import org.seasar.doma.wrapper.DateWrapper;
import org.seasar.doma.wrapper.DoubleWrapper;
import org.seasar.doma.wrapper.EnumWrapper;
import org.seasar.doma.wrapper.FloatWrapper;
import org.seasar.doma.wrapper.IntegerWrapper;
import org.seasar.doma.wrapper.LocalDateTimeWrapper;
import org.seasar.doma.wrapper.LocalDateWrapper;
import org.seasar.doma.wrapper.LocalTimeWrapper;
import org.seasar.doma.wrapper.LongWrapper;
import org.seasar.doma.wrapper.NClobWrapper;
import org.seasar.doma.wrapper.ObjectWrapper;
import org.seasar.doma.wrapper.SQLXMLWrapper;
import org.seasar.doma.wrapper.ShortWrapper;
import org.seasar.doma.wrapper.StringWrapper;
import org.seasar.doma.wrapper.TimeWrapper;
import org.seasar.doma.wrapper.TimestampWrapper;
import org.seasar.doma.wrapper.UtilDateWrapper;

public class WrapperCtType extends AbstractCtType {

    protected BasicCtType basicCtType;

    public WrapperCtType(Context ctx, TypeMirror type) {
        super(ctx, type);
    }

    public BasicCtType getBasicCtType() {
        return basicCtType;
    }

    @Override
    public <R, P, TH extends Throwable> R accept(
            CtTypeVisitor<R, P, TH> visitor, P p) throws TH {
        return visitor.visitWrapperCtType(this, p);
    }

    protected static class WrapperTypeMappingVisitor extends
            SimpleTypeVisitor8<Class<?>, Void> {

        protected final Context ctx;

        protected WrapperTypeMappingVisitor(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        public Class<?> visitArray(ArrayType t, Void p) {
            if (t.getComponentType().getKind() == TypeKind.BYTE) {
                return BytesWrapper.class;
            }
            return null;
        }

        @Override
        public Class<?> visitDeclared(DeclaredType t, Void p) {
            TypeElement typeElement = ctx.getTypes().toTypeElement(t);
            if (typeElement == null) {
                return null;
            }
            if (typeElement.getKind() == ElementKind.ENUM) {
                return EnumWrapper.class;
            }
            String name = typeElement.getQualifiedName().toString();
            if (String.class.getName().equals(name)) {
                return StringWrapper.class;
            }
            if (Boolean.class.getName().equals(name)) {
                return BooleanWrapper.class;
            }
            if (Byte.class.getName().equals(name)) {
                return ByteWrapper.class;
            }
            if (Short.class.getName().equals(name)) {
                return ShortWrapper.class;
            }
            if (Integer.class.getName().equals(name)) {
                return IntegerWrapper.class;
            }
            if (Long.class.getName().equals(name)) {
                return LongWrapper.class;
            }
            if (Float.class.getName().equals(name)) {
                return FloatWrapper.class;
            }
            if (Double.class.getName().equals(name)) {
                return DoubleWrapper.class;
            }
            if (Object.class.getName().equals(name)) {
                return ObjectWrapper.class;
            }
            if (ctx.getTypes().isAssignable(t, BigDecimal.class)) {
                return BigDecimalWrapper.class;
            }
            if (ctx.getTypes().isAssignable(t, BigInteger.class)) {
                return BigIntegerWrapper.class;
            }
            if (ctx.getTypes().isAssignable(t, Time.class)) {
                return TimeWrapper.class;
            }
            if (ctx.getTypes().isAssignable(t, Timestamp.class)) {
                return TimestampWrapper.class;
            }
            if (ctx.getTypes().isAssignable(t, Date.class)) {
                return DateWrapper.class;
            }
            if (ctx.getTypes().isAssignable(t, java.util.Date.class)) {
                return UtilDateWrapper.class;
            }
            if (ctx.getTypes().isAssignable(t, LocalTime.class)) {
                return LocalTimeWrapper.class;
            }
            if (ctx.getTypes().isAssignable(t, LocalDateTime.class)) {
                return LocalDateTimeWrapper.class;
            }
            if (ctx.getTypes().isAssignable(t, LocalDate.class)) {
                return LocalDateWrapper.class;
            }
            if (ctx.getTypes().isAssignable(t, Array.class)) {
                return ArrayWrapper.class;
            }
            if (ctx.getTypes().isAssignable(t, Blob.class)) {
                return BlobWrapper.class;
            }
            if (ctx.getTypes().isAssignable(t, NClob.class)) {
                return NClobWrapper.class;
            }
            if (ctx.getTypes().isAssignable(t, Clob.class)) {
                return ClobWrapper.class;
            }
            if (ctx.getTypes().isAssignable(t, SQLXML.class)) {
                return SQLXMLWrapper.class;
            }
            return null;
        }

        @Override
        public Class<?> visitPrimitive(PrimitiveType t, Void p) {
            switch (t.getKind()) {
            case BOOLEAN:
                return BooleanWrapper.class;
            case BYTE:
                return ByteWrapper.class;
            case SHORT:
                return ShortWrapper.class;
            case INT:
                return IntegerWrapper.class;
            case LONG:
                return LongWrapper.class;
            case FLOAT:
                return FloatWrapper.class;
            case DOUBLE:
                return DoubleWrapper.class;
            case CHAR:
                return null;
            default:
                return assertUnreachable();
            }
        }

    }
}
