package org.seasar.doma.internal.apt.meta.query;

import static org.seasar.doma.internal.util.AssertionUtil.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.seasar.doma.Transient;
import org.seasar.doma.internal.apt.Context;

/**
 * @author taedium
 * 
 */
public class EntityPropertyNameCollector {

    private final Context ctx;

    public EntityPropertyNameCollector(Context ctx) {
        assertNotNull(ctx);
        this.ctx = ctx;
    }

    public Set<String> collect(TypeMirror entityType) {
        Set<String> names = new HashSet<>();
        collectNames(entityType, names);
        return names;
    }

    private void collectNames(TypeMirror type, Set<String> names) {
        for (TypeElement t = ctx.getTypes().toTypeElement(type); t != null
                && t.asType().getKind() != TypeKind.NONE; t = ctx.getTypes()
                        .toTypeElement(t.getSuperclass())) {
            for (VariableElement field : ElementFilter.fieldsIn(t.getEnclosedElements())) {
                if (isPersistent(field)) {
                    names.add(field.getSimpleName().toString());
                }
            }
        }
    }

    private boolean isPersistent(VariableElement field) {
        return field.getAnnotation(Transient.class) == null
                && !field.getModifiers().contains(Modifier.STATIC);
    }
}
