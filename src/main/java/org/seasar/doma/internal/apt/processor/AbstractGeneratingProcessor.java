package org.seasar.doma.internal.apt.processor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.util.Formatter;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject;
import org.seasar.doma.internal.apt.AptException;
import org.seasar.doma.internal.apt.codespec.CodeSpec;
import org.seasar.doma.internal.apt.generator.Generator;
import org.seasar.doma.internal.apt.meta.TypeElementMeta;
import org.seasar.doma.internal.apt.meta.TypeElementMetaFactory;
import org.seasar.doma.message.Message;

public abstract class AbstractGeneratingProcessor<M extends TypeElementMeta>
    extends AbstractProcessor {

  protected AbstractGeneratingProcessor(Class<? extends Annotation> supportedAnnotationType) {
    super(supportedAnnotationType);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      return true;
    }
    for (TypeElement a : annotations) {
      for (TypeElement typeElement : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(a))) {
        handleTypeElement(
            typeElement,
            (t) -> {
              TypeElementMetaFactory<M> factory = createTypeElementMetaFactory(t);
              M meta = factory.createTypeElementMeta();
              if (meta != null) {
                generate(typeElement, meta);
              }
            });
      }
    }
    return true;
  }

  protected abstract TypeElementMetaFactory<M> createTypeElementMetaFactory(
      TypeElement typeElement);

  protected void generate(TypeElement typeElement, M meta) {
    CodeSpec codeSpec = createCodeSpec(meta);
    try {
      JavaFileObject file = ctx.getResources().createSourceFile(codeSpec, typeElement);
      try (Formatter formatter = new Formatter(new BufferedWriter(file.openWriter()))) {
        Generator generator = createGenerator(meta, codeSpec, formatter);
        generator.generate();
      }
    } catch (IOException | UncheckedIOException e) {
      throw new AptException(
          Message.DOMA4079, typeElement, e, new Object[] {codeSpec.getQualifiedName(), e});
    }
  }

  protected abstract CodeSpec createCodeSpec(M meta);

  protected abstract Generator createGenerator(M meta, CodeSpec codeSpec, Formatter formatter);
}
