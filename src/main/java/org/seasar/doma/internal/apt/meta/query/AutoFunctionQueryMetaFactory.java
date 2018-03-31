package org.seasar.doma.internal.apt.meta.query;

import javax.lang.model.element.ExecutableElement;
import org.seasar.doma.internal.apt.AptException;
import org.seasar.doma.internal.apt.Context;
import org.seasar.doma.internal.apt.cttype.*;
import org.seasar.doma.internal.apt.meta.parameter.*;
import org.seasar.doma.internal.apt.reflection.FunctionReflection;
import org.seasar.doma.message.Message;

/** @author taedium */
public class AutoFunctionQueryMetaFactory
    extends AutoModuleQueryMetaFactory<AutoFunctionQueryMeta> {

  public AutoFunctionQueryMetaFactory(Context ctx, ExecutableElement methodElement) {
    super(ctx, methodElement);
  }

  @Override
  public QueryMeta createQueryMeta() {
    FunctionReflection functionReflection =
        ctx.getReflections().newFunctionReflection(methodElement);
    if (functionReflection == null) {
      return null;
    }
    AutoFunctionQueryMeta queryMeta = new AutoFunctionQueryMeta(methodElement);
    queryMeta.setFunctionReflection(functionReflection);
    queryMeta.setQueryKind(QueryKind.AUTO_FUNCTION);
    doTypeParameters(queryMeta);
    doReturnType(queryMeta);
    doParameters(queryMeta);
    doThrowTypes(queryMeta);
    return queryMeta;
  }

  @Override
  protected void doReturnType(AutoFunctionQueryMeta queryMeta) {
    QueryReturnMeta returnMeta = createReturnMeta();
    queryMeta.setReturnMeta(returnMeta);
    ResultParameterMeta resultParameterMeta = createResultParameterMeta(queryMeta, returnMeta);
    queryMeta.setResultParameterMeta(resultParameterMeta);
  }

  private ResultParameterMeta createResultParameterMeta(
      final AutoFunctionQueryMeta queryMeta, final QueryReturnMeta returnMeta) {
    return returnMeta.getCtType().accept(new ReturnCtTypeVisitor(queryMeta, returnMeta), false);
  }

  /** @author nakamura-to */
  private class ReturnCtTypeVisitor
      extends SimpleCtTypeVisitor<ResultParameterMeta, Boolean, RuntimeException> {

    protected final AutoFunctionQueryMeta queryMeta;

    protected final QueryReturnMeta returnMeta;

    public ReturnCtTypeVisitor(AutoFunctionQueryMeta queryMeta, QueryReturnMeta returnMeta) {
      this.queryMeta = queryMeta;
      this.returnMeta = returnMeta;
    }

    @Override
    protected ResultParameterMeta defaultAction(CtType type, Boolean p) throws RuntimeException {
      throw new AptException(
          Message.DOMA4063, returnMeta.getMethodElement(), new Object[] {returnMeta.getType()});
    }

    @Override
    public ResultParameterMeta visitScalarCtType(ScalarCtType ctType, Boolean p)
        throws RuntimeException {
      return new ScalarSingleResultParameterMeta(ctType, p);
    }

    @Override
    public ResultParameterMeta visitIterableCtType(IterableCtType ctType, Boolean p)
        throws RuntimeException {
      if (!ctType.isList()) {
        defaultAction(ctType, p);
      }
      return ctType
          .getElementCtType()
          .accept(new IterableElementCtTypeVisitor(queryMeta, returnMeta), false);
    }

    @Override
    public ResultParameterMeta visitOptionalCtType(OptionalCtType ctType, Boolean p)
        throws RuntimeException {
      return ctType.getElementCtType().accept(this, true);
    }
  }

  /** @author nakamura-to */
  private class IterableElementCtTypeVisitor
      extends SimpleCtTypeVisitor<ResultParameterMeta, Boolean, RuntimeException> {

    protected final AutoFunctionQueryMeta queryMeta;

    protected final QueryReturnMeta returnMeta;

    public IterableElementCtTypeVisitor(
        AutoFunctionQueryMeta queryMeta, QueryReturnMeta returnMeta) {
      this.queryMeta = queryMeta;
      this.returnMeta = returnMeta;
    }

    @Override
    protected ResultParameterMeta defaultAction(CtType ctType, Boolean p) throws RuntimeException {
      throw new AptException(
          Message.DOMA4065, returnMeta.getMethodElement(), new Object[] {ctType.getTypeName()});
    }

    @Override
    public ResultParameterMeta visitScalarCtType(ScalarCtType ctType, Boolean p)
        throws RuntimeException {
      return new ScalarResultListParameterMeta(ctType, p);
    }

    @Override
    public ResultParameterMeta visitEntityCtType(EntityCtType ctType, Boolean p)
        throws RuntimeException {
      if (ctType.isAbstract()) {
        throw new AptException(
            Message.DOMA4156, returnMeta.getMethodElement(), new Object[] {ctType.getTypeName()});
      }
      return new EntityResultListParameterMeta(ctType, queryMeta.getEnsureResultMapping());
    }

    @Override
    public ResultParameterMeta visitMapCtType(MapCtType ctType, Boolean p) throws RuntimeException {
      return new MapResultListParameterMeta(ctType);
    }

    @Override
    public ResultParameterMeta visitOptionalCtType(OptionalCtType ctType, Boolean p)
        throws RuntimeException {
      return ctType.getElementCtType().accept(this, true);
    }
  }
}
