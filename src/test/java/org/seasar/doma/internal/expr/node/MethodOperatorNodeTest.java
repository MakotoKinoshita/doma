package org.seasar.doma.internal.expr.node;

import junit.framework.TestCase;
import org.seasar.doma.internal.expr.EvaluationResult;
import org.seasar.doma.internal.expr.ExpressionEvaluator;
import org.seasar.doma.internal.expr.Value;

/** @author taedium */
public class MethodOperatorNodeTest extends TestCase {

  protected ExpressionLocation location = new ExpressionLocation("", 0);

  public void testOneParameter() throws Exception {
    MethodOperatorNode node = new MethodOperatorNode(location, ".equals", "equals");
    node.setTargetObjectNode(new VariableNode(location, "hoge"));
    CommaOperatorNode commaOperatorNode = new CommaOperatorNode(location, ",");
    commaOperatorNode.addNode(new LiteralNode(location, "\"aaa\"", "aaa", String.class));
    node.setParametersNode(commaOperatorNode);
    ExpressionEvaluator evaluator = new ExpressionEvaluator();
    evaluator.add("hoge", new Value(String.class, "aaa"));
    EvaluationResult evaluationResult = evaluator.evaluate(node);
    assertTrue(evaluationResult.getBooleanValue());
  }
}
