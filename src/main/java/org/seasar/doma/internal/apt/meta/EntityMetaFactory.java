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
package org.seasar.doma.internal.apt.meta;

import static org.seasar.doma.internal.util.AssertionUtil.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;

import org.seasar.doma.Entity;
import org.seasar.doma.EntityField;
import org.seasar.doma.OriginalStates;
import org.seasar.doma.ParameterName;
import org.seasar.doma.Transient;
import org.seasar.doma.internal.Constants;
import org.seasar.doma.internal.apt.AptException;
import org.seasar.doma.internal.apt.AptIllegalStateException;
import org.seasar.doma.internal.apt.Notifier;
import org.seasar.doma.internal.apt.reflection.AllArgsConstructorReflection;
import org.seasar.doma.internal.apt.reflection.EntityReflection;
import org.seasar.doma.internal.apt.reflection.Reflections;
import org.seasar.doma.internal.apt.reflection.TableReflection;
import org.seasar.doma.internal.apt.reflection.ValueReflection;
import org.seasar.doma.internal.apt.util.AnnotationValueUtil;
import org.seasar.doma.internal.apt.util.ElementUtil;
import org.seasar.doma.internal.apt.util.TypeMirrorUtil;
import org.seasar.doma.jdbc.entity.EntityListener;
import org.seasar.doma.jdbc.entity.NamingType;
import org.seasar.doma.jdbc.entity.NullEntityListener;
import org.seasar.doma.message.Message;

/**
 * 
 * @author taedium
 * 
 */
public class EntityMetaFactory implements TypeElementMetaFactory<EntityMeta> {

    protected final ProcessingEnvironment env;

    protected final EntityPropertyMetaFactory propertyMetaFactory;

    public EntityMetaFactory(ProcessingEnvironment env,
            EntityPropertyMetaFactory propertyMetaFactory) {
        assertNotNull(env, propertyMetaFactory);
        this.env = env;
        this.propertyMetaFactory = propertyMetaFactory;
    }

    @Override
    public EntityMeta createTypeElementMeta(TypeElement classElement) {
        assertNotNull(classElement);
        EntityReflection entityReflection = new Reflections(env)
                .newEntityReflection(classElement);
        if (entityReflection == null) {
            throw new AptIllegalStateException("entityReflection.");
        }
        EntityMeta entityMeta = new EntityMeta(entityReflection, classElement);
        TypeMirror entityListener = resolveEntityListener(classElement);
        entityMeta.setEntityListener(entityListener);
        TypeElement entityListenerElement = TypeMirrorUtil.toTypeElement(
                entityListener, env);
        if (entityListenerElement == null) {
            throw new AptIllegalStateException("entityListener.");
        }
        entityMeta.setEntityListenerElement(entityListenerElement);
        entityMeta.setGenericEntityListener(!entityListenerElement
                .getTypeParameters().isEmpty());
        NamingType namingType = resolveNamingType(classElement);
        entityMeta.setNamingType(namingType);
        boolean immutable = resolveImmutable(classElement, entityReflection);
        entityMeta.setImmutable(immutable);
        entityMeta.setEntityName(classElement.getSimpleName().toString());
        entityMeta.setEntityTypeName(TypeMirrorUtil.getTypeName(
                classElement.asType(), env));
        Strategy strategy = createStrategy(classElement, entityMeta);
        strategy.doClassElement(classElement, entityMeta);
        strategy.doFieldElements(classElement, entityMeta);
        strategy.validateGeneratedId(classElement, entityMeta);
        strategy.validateOriginalStates(classElement, entityMeta);
        strategy.doConstructor(classElement, entityMeta);
        return entityMeta;
    }

    protected Strategy createStrategy(TypeElement classElement,
            EntityMeta entityMeta) {
        ValueReflection valueReflection = new Reflections(env)
                .newValueReflection(classElement);
        if (valueReflection != null) {
            return new ValueStrategy(env, propertyMetaFactory, valueReflection);
        }
        AllArgsConstructorReflection allArgsConstructorReflection = new Reflections(
                env).newAllArgsConstructorReflection(classElement);
        if (allArgsConstructorReflection != null) {
            return new AllArgsConstructorStrategy(env, propertyMetaFactory,
                    allArgsConstructorReflection);
        }
        return new DefaultStrategy(env, propertyMetaFactory);
    }

    protected TypeMirror resolveEntityListener(TypeElement classElement) {
        TypeMirror result = TypeMirrorUtil.getTypeMirror(
                NullEntityListener.class, env);
        for (AnnotationValue value : getEntityElementValueList(classElement,
                "listener")) {
            if (value != null) {
                TypeMirror listenerType = AnnotationValueUtil.toType(value);
                if (listenerType == null) {
                    throw new AptIllegalStateException("listener");
                }
                result = listenerType;
            }
        }
        return result;
    }

    protected NamingType resolveNamingType(TypeElement classElement) {
        NamingType result = null;
        for (AnnotationValue value : getEntityElementValueList(classElement,
                "naming")) {
            if (value != null) {
                VariableElement enumConstant = AnnotationValueUtil
                        .toEnumConstant(value);
                if (enumConstant == null) {
                    throw new AptIllegalStateException("naming");
                }
                result = NamingType.valueOf(enumConstant.getSimpleName()
                        .toString());
            }
        }
        return result;
    }

    protected boolean resolveImmutable(TypeElement classElement,
            EntityReflection entityReflection) {
        boolean result = false;
        List<Boolean> resolvedList = new ArrayList<Boolean>();
        for (AnnotationValue value : getEntityElementValueList(classElement,
                "immutable")) {
            if (value != null) {
                Boolean immutable = AnnotationValueUtil.toBoolean(value);
                if (immutable == null) {
                    throw new AptIllegalStateException("immutable");
                }
                result = immutable.booleanValue();
                resolvedList.add(immutable);
            }
        }
        if (resolvedList.contains(Boolean.TRUE)
                && resolvedList.contains(Boolean.FALSE)) {
            throw new AptException(Message.DOMA4226, env, classElement,
                    entityReflection.getAnnotationMirror(),
                    entityReflection.getImmutable(),
                    new Object[] { classElement.getQualifiedName() });
        }
        return result;
    }

    protected List<AnnotationValue> getEntityElementValueList(
            TypeElement classElement, String entityElementName) {
        List<AnnotationValue> list = new LinkedList<AnnotationValue>();
        for (TypeElement t = classElement; t != null
                && t.asType().getKind() != TypeKind.NONE; t = TypeMirrorUtil
                .toTypeElement(t.getSuperclass(), env)) {
            AnnotationMirror annMirror = ElementUtil.getAnnotationMirror(t,
                    Entity.class, env);
            if (annMirror == null) {
                continue;
            }
            AnnotationValue value = null;
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annMirror
                    .getElementValues().entrySet()) {
                ExecutableElement element = entry.getKey();
                if (entityElementName
                        .equals(element.getSimpleName().toString())) {
                    value = entry.getValue();
                    break;
                }
            }
            list.add(value);
        }
        Collections.reverse(list);
        return list;
    }

    protected interface Strategy {

        void doClassElement(TypeElement classElement, EntityMeta entityMeta);

        void doFieldElements(TypeElement classElement, EntityMeta entityMeta);

        void validateGeneratedId(TypeElement classElement, EntityMeta entityMeta);

        void validateOriginalStates(TypeElement classElement,
                EntityMeta entityMeta);

        void doConstructor(TypeElement classElement, EntityMeta entityMeta);
    }

    protected static class DefaultStrategy implements Strategy {

        protected final ProcessingEnvironment env;

        protected final EntityPropertyMetaFactory propertyMetaFactory;

        public DefaultStrategy(ProcessingEnvironment env,
                EntityPropertyMetaFactory propertyMetaFactory) {
            assertNotNull(env, propertyMetaFactory);
            this.env = env;
            this.propertyMetaFactory = propertyMetaFactory;
        }

        public void doClassElement(TypeElement classElement,
                EntityMeta entityMeta) {
            validateClass(classElement, entityMeta);
            validateEntityListener(classElement, entityMeta);

            doTable(classElement, entityMeta);
        }

        protected void validateClass(TypeElement classElement,
                EntityMeta entityMeta) {
            EntityReflection entityReflection = entityMeta.getEntityReflection();
            if (classElement.getKind() != ElementKind.CLASS) {
                throw new AptException(Message.DOMA4015, env, classElement,
                        entityReflection.getAnnotationMirror(),
                        new Object[] { classElement.getQualifiedName() });
            }
            if (!classElement.getTypeParameters().isEmpty()) {
                throw new AptException(Message.DOMA4051, env, classElement,
                        new Object[] { classElement.getQualifiedName() });
            }
            validateEnclosingElement(classElement);
        }

        protected void validateEnclosingElement(Element element) {
            TypeElement typeElement = ElementUtil.toTypeElement(element, env);
            if (typeElement == null) {
                return;
            }
            String simpleName = typeElement.getSimpleName().toString();
            if (simpleName.contains(Constants.BINARY_NAME_DELIMITER)
                    || simpleName.contains(Constants.METATYPE_NAME_DELIMITER)) {
                throw new AptException(Message.DOMA4317, env, typeElement,
                        new Object[] { typeElement.getQualifiedName() });
            }
            NestingKind nestingKind = typeElement.getNestingKind();
            if (nestingKind == NestingKind.TOP_LEVEL) {
                return;
            } else if (nestingKind == NestingKind.MEMBER) {
                Set<Modifier> modifiers = typeElement.getModifiers();
                if (modifiers.containsAll(Arrays.asList(Modifier.STATIC,
                        Modifier.PUBLIC))) {
                    validateEnclosingElement(typeElement.getEnclosingElement());
                } else {
                    throw new AptException(Message.DOMA4315, env, typeElement,
                            new Object[] { typeElement.getQualifiedName() });
                }
            } else {
                throw new AptException(Message.DOMA4316, env, typeElement,
                        new Object[] { typeElement.getQualifiedName() });
            }
        }

        protected void validateEntityListener(TypeElement classElement,
                EntityMeta entityMeta) {
            EntityReflection entityReflection = entityMeta.getEntityReflection();
            TypeMirror listenerType = entityReflection.getListenerValue();
            TypeElement listenerElement = TypeMirrorUtil.toTypeElement(
                    listenerType, env);
            if (listenerElement == null) {
                throw new AptIllegalStateException(
                        "failed to convert to TypeElement");
            }

            if (listenerElement.getModifiers().contains(Modifier.ABSTRACT)) {
                throw new AptException(Message.DOMA4166, env, classElement,
                        entityReflection.getAnnotationMirror(),
                        entityReflection.getListener(),
                        new Object[] { listenerElement.getQualifiedName() });
            }

            ExecutableElement constructor = ElementUtil.getNoArgConstructor(
                    listenerElement, env);
            if (constructor == null
                    || !constructor.getModifiers().contains(Modifier.PUBLIC)) {
                throw new AptException(Message.DOMA4167, env, classElement,
                        entityReflection.getAnnotationMirror(),
                        entityReflection.getListener(),
                        new Object[] { listenerElement.getQualifiedName() });
            }
            if (listenerElement.getTypeParameters().size() > 0) {
                validateGenericEntityListener(classElement, entityMeta,
                        listenerElement);
            } else {
                validateNonGenericEntityListener(classElement, entityMeta,
                        listenerType);
            }

            TypeElement inheritedListenerElement = entityMeta
                    .getEntityListenerElement();
            if (!TypeMirrorUtil.isSameType(listenerType,
                    inheritedListenerElement.asType(), env)) {
                validateInheritedEntityListener(classElement, entityMeta,
                        inheritedListenerElement);
            }
        }

        protected void validateGenericEntityListener(TypeElement classElement,
                EntityMeta entityMeta, TypeElement listenerElement) {
            EntityReflection entityReflection = entityMeta.getEntityReflection();
            List<? extends TypeParameterElement> typeParams = listenerElement
                    .getTypeParameters();
            if (typeParams.size() == 0) {
                throw new AptIllegalStateException(
                        "typeParams size should be more than 0");
            }
            if (typeParams.size() > 1) {
                throw new AptException(Message.DOMA4227, env, classElement,
                        entityReflection.getAnnotationMirror(),
                        entityReflection.getListener(),
                        new Object[] { classElement.getQualifiedName() });
            }
            TypeParameterElement typeParam = typeParams.get(0);
            for (TypeMirror bound : typeParam.getBounds()) {
                if (!TypeMirrorUtil.isAssignable(classElement.asType(), bound,
                        env)) {
                    throw new AptException(Message.DOMA4229, env, classElement,
                            entityReflection.getAnnotationMirror(),
                            entityReflection.getListener(), new Object[] {
                                    typeParam.getSimpleName(), bound,
                                    classElement.getQualifiedName() });
                }
            }
            if (findListenerTypeParam(listenerElement, 0) == null) {
                throw new AptException(Message.DOMA4228, env, classElement,
                        entityReflection.getAnnotationMirror(),
                        entityReflection.getListener(), new Object[] {
                                typeParam.getSimpleName(),
                                classElement.getQualifiedName() });
            }
        }

        protected TypeParameterElement findListenerTypeParam(
                TypeElement listenerElement, int typeParamIndex) {
            TypeParameterElement typeParam = listenerElement
                    .getTypeParameters().get(typeParamIndex);

            for (TypeMirror interfase : listenerElement.getInterfaces()) {
                DeclaredType declaredType = TypeMirrorUtil.toDeclaredType(
                        interfase, env);
                if (declaredType == null) {
                    continue;
                }
                int i = -1;
                for (TypeMirror typeArg : declaredType.getTypeArguments()) {
                    i++;
                    TypeVariable typeVariable = TypeMirrorUtil.toTypeVariable(
                            typeArg, env);
                    if (typeVariable == null) {
                        continue;
                    }
                    if (typeParam.getSimpleName().equals(
                            typeVariable.asElement().getSimpleName())) {
                        if (TypeMirrorUtil.isSameType(declaredType,
                                EntityListener.class, env)) {
                            return typeParam;
                        }
                        TypeElement typeElement = TypeMirrorUtil.toTypeElement(
                                declaredType, env);
                        if (typeElement == null) {
                            throw new AptIllegalStateException(
                                    declaredType.toString());
                        }
                        TypeParameterElement candidate = findListenerTypeParam(
                                typeElement, i);
                        if (candidate != null) {
                            return candidate;
                        }
                    }
                }
            }

            TypeMirror superclass = listenerElement.getSuperclass();
            DeclaredType declaredType = TypeMirrorUtil.toDeclaredType(
                    superclass, env);
            if (declaredType == null) {
                return null;
            }
            int i = -1;
            for (TypeMirror typeArg : declaredType.getTypeArguments()) {
                i++;
                TypeVariable typeVariable = TypeMirrorUtil.toTypeVariable(
                        typeArg, env);
                if (typeVariable == null) {
                    continue;
                }
                if (typeParam.getSimpleName().equals(
                        typeVariable.asElement().getSimpleName())) {
                    if (TypeMirrorUtil.isSameType(declaredType,
                            EntityListener.class, env)) {
                        return typeParam;
                    }
                    TypeElement typeElement = TypeMirrorUtil.toTypeElement(
                            declaredType, env);
                    if (typeElement == null) {
                        throw new AptIllegalStateException(
                                declaredType.toString());
                    }
                    TypeParameterElement candidate = findListenerTypeParam(
                            typeElement, i);
                    if (candidate != null) {
                        return candidate;
                    }
                }
            }

            return null;
        }

        protected void validateNonGenericEntityListener(
                TypeElement classElement, EntityMeta entityMeta,
                TypeMirror listenerType) {
            EntityReflection entityReflection = entityMeta.getEntityReflection();
            TypeMirror argumentType = getListenerArgumentType(listenerType);
            if (argumentType == null) {
                throw new AptException(Message.DOMA4202, env, classElement,
                        entityReflection.getAnnotationMirror(),
                        entityReflection.getListener(),
                        new Object[] { classElement.getQualifiedName() });
            }
            if (!TypeMirrorUtil.isAssignable(classElement.asType(),
                    argumentType, env)) {
                throw new AptException(Message.DOMA4038, env, classElement,
                        entityReflection.getAnnotationMirror(),
                        entityReflection.getListener(), new Object[] {
                                listenerType, argumentType,
                                classElement.getQualifiedName() });
            }
        }

        protected void validateInheritedEntityListener(
                TypeElement classElement, EntityMeta entityMeta,
                TypeElement inheritedListenerElement) {
            EntityReflection entityReflection = entityMeta.getEntityReflection();
            List<? extends TypeParameterElement> typeParams = inheritedListenerElement
                    .getTypeParameters();
            if (typeParams.size() == 0) {
                throw new AptException(Message.DOMA4230, env, classElement,
                        entityReflection.getAnnotationMirror(), new Object[] {
                                inheritedListenerElement.getQualifiedName(),
                                classElement.getQualifiedName() });
            }
            TypeParameterElement typeParam = typeParams.get(0);
            for (TypeMirror bound : typeParam.getBounds()) {
                if (!TypeMirrorUtil.isAssignable(classElement.asType(), bound,
                        env)) {
                    throw new AptException(
                            Message.DOMA4231,
                            env,
                            classElement,
                            entityReflection.getAnnotationMirror(),
                            new Object[] {
                                    inheritedListenerElement.getQualifiedName(),
                                    typeParam.getSimpleName(), bound,
                                    classElement.getQualifiedName() });
                }
            }
        }

        protected TypeMirror getListenerArgumentType(TypeMirror typeMirror) {
            for (TypeMirror supertype : env.getTypeUtils().directSupertypes(
                    typeMirror)) {
                if (!TypeMirrorUtil.isAssignable(supertype,
                        EntityListener.class, env)) {
                    continue;
                }
                if (TypeMirrorUtil.isSameType(supertype, EntityListener.class,
                        env)) {
                    DeclaredType declaredType = TypeMirrorUtil.toDeclaredType(
                            supertype, env);
                    if (declaredType == null) {
                        throw new AptIllegalStateException("declaredType");
                    }
                    List<? extends TypeMirror> args = declaredType
                            .getTypeArguments();
                    if (args.size() != 1) {
                        return null;
                    }
                    return args.get(0);
                }
                TypeMirror argumentType = getListenerArgumentType(supertype);
                if (argumentType != null) {
                    return argumentType;
                }
            }
            return null;
        }

        protected void doTable(TypeElement classElement, EntityMeta entityMeta) {
            TableReflection tableReflection = new Reflections(env)
                    .newTableReflection(classElement);
            if (tableReflection == null) {
                return;
            }
            entityMeta.setTableMirror(tableReflection);
        }

        public void doFieldElements(TypeElement classElement,
                EntityMeta entityMeta) {
            for (VariableElement fieldElement : getFieldElements(classElement)) {
                try {
                    if (fieldElement.getAnnotation(Transient.class) != null) {
                        continue;
                    } else if (fieldElement.getModifiers().contains(
                            Modifier.STATIC)) {
                        continue;
                    } else if (fieldElement.getAnnotation(OriginalStates.class) != null) {
                        doOriginalStatesField(classElement, fieldElement,
                                entityMeta);
                    } else {
                        doEntityPropertyMeta(classElement, fieldElement,
                                entityMeta);
                    }
                } catch (AptException e) {
                    Notifier.notify(env, e);
                    entityMeta.setError(true);
                }
            }
        }

        protected List<VariableElement> getFieldElements(
                TypeElement classElement) {
            List<VariableElement> results = new LinkedList<VariableElement>();
            for (TypeElement t = classElement; t != null
                    && t.asType().getKind() != TypeKind.NONE; t = TypeMirrorUtil
                    .toTypeElement(t.getSuperclass(), env)) {
                if (t.getAnnotation(Entity.class) == null) {
                    continue;
                }
                List<VariableElement> fields = new LinkedList<VariableElement>();
                for (VariableElement field : ElementFilter.fieldsIn(t
                        .getEnclosedElements())) {
                    fields.add(field);
                }
                Collections.reverse(fields);
                results.addAll(fields);
            }
            Collections.reverse(results);

            List<VariableElement> hiderFields = new LinkedList<VariableElement>(
                    results);
            for (Iterator<VariableElement> it = results.iterator(); it
                    .hasNext();) {
                VariableElement hidden = it.next();
                for (VariableElement hider : hiderFields) {
                    if (env.getElementUtils().hides(hider, hidden)) {
                        it.remove();
                    }
                }
            }
            return results;
        }

        protected void doOriginalStatesField(TypeElement classElement,
                VariableElement fieldElement, EntityMeta entityMeta) {
            if (entityMeta.hasOriginalStatesMeta()) {
                throw new AptException(Message.DOMA4125, env, fieldElement,
                        new Object[] { classElement.getQualifiedName(),
                                fieldElement.getSimpleName() });
            }
            if (classElement.equals(fieldElement.getEnclosingElement())) {
                if (!TypeMirrorUtil.isSameType(fieldElement.asType(),
                        classElement.asType(), env)) {
                    throw new AptException(Message.DOMA4135, env, fieldElement,
                            new Object[] { classElement.getQualifiedName() });
                }
            }
            TypeElement enclosingElement = ElementUtil.toTypeElement(
                    fieldElement.getEnclosingElement(), env);
            if (enclosingElement == null) {
                throw new AptIllegalStateException(fieldElement.toString());
            }
            if (entityMeta.isImmutable()
                    && classElement.equals(enclosingElement)) {
                throw new AptException(Message.DOMA4224, env, fieldElement,
                        new Object[] { classElement.getQualifiedName(),
                                fieldElement.getSimpleName() });
            }
            OriginalStatesMeta originalStatesMeta = new OriginalStatesMeta(
                    classElement, fieldElement, enclosingElement, env);
            entityMeta.setOriginalStatesMeta(originalStatesMeta);
        }

        protected void doEntityPropertyMeta(TypeElement classElement,
                VariableElement fieldElement, EntityMeta entityMeta) {
            validateFieldAnnotation(fieldElement, entityMeta);
            EntityPropertyMeta propertyMeta = propertyMetaFactory
                    .createEntityPropertyMeta(fieldElement, entityMeta);
            entityMeta.addPropertyMeta(propertyMeta);
            validateField(classElement, fieldElement, entityMeta);
        }

        protected void validateFieldAnnotation(VariableElement fieldElement,
                EntityMeta entityMeta) {
            TypeElement foundAnnotationTypeElement = null;
            for (AnnotationMirror annotation : fieldElement
                    .getAnnotationMirrors()) {
                DeclaredType declaredType = annotation.getAnnotationType();
                TypeElement typeElement = TypeMirrorUtil.toTypeElement(
                        declaredType, env);
                if (typeElement.getAnnotation(EntityField.class) != null) {
                    if (foundAnnotationTypeElement != null) {
                        throw new AptException(Message.DOMA4086, env,
                                fieldElement,
                                new Object[] {
                                        foundAnnotationTypeElement
                                                .getQualifiedName(),
                                        typeElement.getQualifiedName(),
                                        entityMeta.getEntityElement()
                                                .getQualifiedName(),
                                        fieldElement.getSimpleName() });
                    }
                    foundAnnotationTypeElement = typeElement;
                }
            }
        }

        protected void validateField(TypeElement classElement,
                VariableElement fieldElement, EntityMeta entityMeta) {
            if (entityMeta.isImmutable()
                    && !fieldElement.getModifiers().contains(Modifier.FINAL)) {
                throw new AptException(Message.DOMA4225, env, fieldElement,
                        new Object[] { classElement.getQualifiedName(),
                                fieldElement.getSimpleName() });
            }
        }

        public void validateGeneratedId(TypeElement classElement,
                EntityMeta entityMeta) {
            if (entityMeta.hasGeneratedIdPropertyMeta()
                    && entityMeta.getIdPropertyMetas().size() > 1) {
                throw new AptException(Message.DOMA4036, env, classElement,
                        new Object[] { classElement.getQualifiedName() });
            }
        }

        public void validateOriginalStates(TypeElement classElement,
                EntityMeta entityMeta) {
            if (entityMeta.hasOriginalStatesMeta()
                    && entityMeta.hasEmbeddedProperties()) {
                throw new AptException(Message.DOMA4305, env, classElement,
                        new Object[] { classElement.getQualifiedName() });
            }
        }

        public void doConstructor(TypeElement classElement,
                EntityMeta entityMeta) {
            if (classElement.getModifiers().contains(Modifier.ABSTRACT)) {
                return;
            }
            if (entityMeta.isImmutable()) {
                EntityConstructorMeta constructorMeta = getConstructorMeta(
                        classElement, entityMeta);
                if (constructorMeta == null) {
                    throw new AptException(Message.DOMA4281, env, classElement,
                            new Object[] { classElement.getQualifiedName() });
                }
                if (constructorMeta.getConstructorElement().getModifiers()
                        .contains(Modifier.PRIVATE)) {
                    throw new AptException(Message.DOMA4221, env, classElement,
                            new Object[] { classElement.getQualifiedName() });
                }
                entityMeta.setConstructorMeta(constructorMeta);
            } else {
                ExecutableElement constructor = ElementUtil
                        .getNoArgConstructor(classElement, env);
                if (constructor == null
                        || constructor.getModifiers()
                                .contains(Modifier.PRIVATE)) {
                    throw new AptException(Message.DOMA4124, env, classElement,
                            new Object[] { classElement.getQualifiedName() });
                }
            }
        }

        protected EntityConstructorMeta getConstructorMeta(
                TypeElement classElement, EntityMeta entityMeta) {
            Map<String, EntityPropertyMeta> entityPropertyMetaMap = new HashMap<String, EntityPropertyMeta>();
            for (EntityPropertyMeta propertyMeta : entityMeta
                    .getAllPropertyMetas()) {
                entityPropertyMetaMap.put(propertyMeta.getName(), propertyMeta);
            }
            outer: for (ExecutableElement constructor : ElementFilter
                    .constructorsIn(classElement.getEnclosedElements())) {
                List<EntityPropertyMeta> entityPropertyMetaList = new ArrayList<>();
                for (VariableElement param : constructor.getParameters()) {
                    String name = param.getSimpleName().toString();
                    ParameterName parameterName = param
                            .getAnnotation(ParameterName.class);
                    if (parameterName != null) {
                        name = parameterName.value();
                    }
                    TypeMirror paramType = param.asType();
                    EntityPropertyMeta propertyMeta = entityPropertyMetaMap
                            .get(name);
                    if (propertyMeta == null) {
                        continue outer;
                    }
                    TypeMirror propertyType = propertyMeta.getType();
                    if (!TypeMirrorUtil
                            .isSameType(paramType, propertyType, env)) {
                        continue outer;
                    }
                    entityPropertyMetaList.add(propertyMeta);
                }
                if (entityPropertyMetaMap.size() == entityPropertyMetaList
                        .size()) {
                    return new EntityConstructorMeta(constructor,
                            entityPropertyMetaList);
                }
            }
            return null;
        }

    }

    protected static class AllArgsConstructorStrategy extends DefaultStrategy {

        protected final AllArgsConstructorReflection allArgsConstructorReflection;

        public AllArgsConstructorStrategy(ProcessingEnvironment env,
                EntityPropertyMetaFactory propertyMetaFactory,
                AllArgsConstructorReflection allArgsConstructorReflection) {
            super(env, propertyMetaFactory);
            assertNotNull(allArgsConstructorReflection);
            this.allArgsConstructorReflection = allArgsConstructorReflection;
        }

        @Override
        protected void validateClass(TypeElement classElement,
                EntityMeta entityMeta) {
            if (!entityMeta.isImmutable()) {
                throw new AptException(Message.DOMA4420, env, classElement,
                        allArgsConstructorReflection.getAnnotationMirror(),
                        new Object[] { classElement.getQualifiedName() });
            }
            super.validateClass(classElement, entityMeta);
        }

        @Override
        public void doConstructor(TypeElement classElement,
                EntityMeta entityMeta) {
            if (!allArgsConstructorReflection.getStaticNameValue().isEmpty()) {
                throw new AptException(Message.DOMA4421, env, classElement,
                        allArgsConstructorReflection.getAnnotationMirror(),
                        allArgsConstructorReflection.getStaticName(),
                        new Object[] { classElement.getQualifiedName() });
            }
            if (allArgsConstructorReflection.isAccessPrivate()) {
                throw new AptException(Message.DOMA4422, env, classElement,
                        allArgsConstructorReflection.getAnnotationMirror(),
                        allArgsConstructorReflection.getAccess(),
                        new Object[] { classElement.getQualifiedName() });
            }
            if (allArgsConstructorReflection.isAccessNone()) {
                throw new AptException(Message.DOMA4426, env, classElement,
                        allArgsConstructorReflection.getAnnotationMirror(),
                        allArgsConstructorReflection.getAccess(),
                        new Object[] { classElement.getQualifiedName() });
            }
        }

        @Override
        protected void validateField(TypeElement classElement,
                VariableElement fieldElement, EntityMeta entityMeta) {
            // doNothing
        }
    }

    protected static class ValueStrategy extends DefaultStrategy {

        protected final ValueReflection valueReflection;

        public ValueStrategy(ProcessingEnvironment env,
                EntityPropertyMetaFactory propertyMetaFactory,
                ValueReflection valueReflection) {
            super(env, propertyMetaFactory);
            assertNotNull(valueReflection);
            this.valueReflection = valueReflection;
        }

        @Override
        protected void validateClass(TypeElement classElement,
                EntityMeta entityMeta) {
            if (!entityMeta.isImmutable()) {
                EntityReflection entityReflection = entityMeta.getEntityReflection();
                throw new AptException(Message.DOMA4418, env, classElement,
                        entityReflection.getAnnotationMirror(),
                        entityReflection.getImmutable(),
                        new Object[] { classElement.getQualifiedName() });
            }
            super.validateClass(classElement, entityMeta);
        }

        @Override
        public void doConstructor(TypeElement classElement,
                EntityMeta entityMeta) {
            if (!valueReflection.getStaticConstructorValue().isEmpty()) {
                throw new AptException(Message.DOMA4419, env, classElement,
                        valueReflection.getAnnotationMirror(),
                        valueReflection.getStaticConstructor(),
                        new Object[] { classElement.getQualifiedName() });
            }
        }

        @Override
        protected void validateField(TypeElement classElement,
                VariableElement fieldElement, EntityMeta entityMeta) {
            // doNothing
        }
    }

}
