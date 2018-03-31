package org.seasar.doma.internal.apt.meta.query;

import javax.lang.model.element.ExecutableElement;
import org.seasar.doma.internal.apt.AptException;
import org.seasar.doma.internal.apt.Context;
import org.seasar.doma.internal.apt.reflection.ProcedureReflection;
import org.seasar.doma.message.Message;

/** @author taedium */
public class AutoProcedureQueryMetaFactory
    extends AutoModuleQueryMetaFactory<AutoProcedureQueryMeta> {

  public AutoProcedureQueryMetaFactory(Context ctx, ExecutableElement methodElement) {
    super(ctx, methodElement);
  }

  @Override
  public QueryMeta createQueryMeta() {
    ProcedureReflection procedureReflection =
        ctx.getReflections().newProcedureReflection(methodElement);
    if (procedureReflection == null) {
      return null;
    }
    AutoProcedureQueryMeta queryMeta = new AutoProcedureQueryMeta(methodElement);
    queryMeta.setQueryKind(QueryKind.AUTO_PROCEDURE);
    queryMeta.setProcedureReflection(procedureReflection);
    doTypeParameters(queryMeta);
    doReturnType(queryMeta);
    doParameters(queryMeta);
    doThrowTypes(queryMeta);
    return queryMeta;
  }

  @Override
  protected void doReturnType(AutoProcedureQueryMeta queryMeta) {
    QueryReturnMeta resultMeta = createReturnMeta();
    if (!resultMeta.isPrimitiveVoid()) {
      throw new AptException(Message.DOMA4064, resultMeta.getMethodElement());
    }
    queryMeta.setReturnMeta(resultMeta);
  }
}
