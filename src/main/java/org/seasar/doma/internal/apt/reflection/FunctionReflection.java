package org.seasar.doma.internal.apt.reflection;

import static org.seasar.doma.internal.util.AssertionUtil.assertNotNull;
import static org.seasar.doma.internal.util.AssertionUtil.assertNotNullValue;

import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import org.seasar.doma.MapKeyNamingType;
import org.seasar.doma.internal.apt.AptIllegalStateException;
import org.seasar.doma.internal.apt.util.AnnotationValueUtil;
import org.seasar.doma.jdbc.SqlLogType;

public class FunctionReflection extends AbstractReflection {

  public static final String ENSURE_RESULT_MAPPING = "ensureResultMapping";

  public static final String SQL_LOG = "sqlLog";

  public static final String MAP_KEY_NAMING = "mapKeyNaming";

  public static final String QUERY_TIMEOUT = "queryTimeout";

  public static final String QUOTE = "quote";

  public static final String NAME = "name";

  public static final String SCHEMA = "schema";

  public static final String CATALOG = "catalog";

  private final String defaultName;

  private final AnnotationValue catalog;

  private final AnnotationValue schema;

  private final AnnotationValue name;

  private final AnnotationValue quote;

  private final AnnotationValue queryTimeout;

  private final AnnotationValue mapKeyNaming;

  private final AnnotationValue ensureResultMapping;

  private final AnnotationValue sqlLog;

  FunctionReflection(
      AnnotationMirror annotationMirror, String defaultName, Map<String, AnnotationValue> values) {
    super(annotationMirror);
    assertNotNull(defaultName, values);

    this.defaultName = defaultName;

    this.catalog = assertNotNullValue(values, CATALOG);
    this.schema = assertNotNullValue(values, SCHEMA);
    this.name = assertNotNullValue(values, NAME);
    this.quote = assertNotNullValue(values, QUOTE);
    this.queryTimeout = assertNotNullValue(values, QUERY_TIMEOUT);
    this.mapKeyNaming = assertNotNullValue(values, MAP_KEY_NAMING);
    this.ensureResultMapping = assertNotNullValue(values, ENSURE_RESULT_MAPPING);
    this.sqlLog = assertNotNullValue(values, SQL_LOG);
  }

  public AnnotationValue getQueryTimeout() {
    return queryTimeout;
  }

  public AnnotationValue getMapKeyNaming() {
    return mapKeyNaming;
  }

  public AnnotationValue getSqlLog() {
    return sqlLog;
  }

  public String getCatalogValue() {
    String value = AnnotationValueUtil.toString(catalog);
    if (value == null) {
      throw new AptIllegalStateException(CATALOG);
    }
    return value;
  }

  public String getSchemaValue() {
    String value = AnnotationValueUtil.toString(schema);
    if (value == null) {
      throw new AptIllegalStateException(SCHEMA);
    }
    return value;
  }

  public String getNameValue() {
    String value = AnnotationValueUtil.toString(name);
    if (value == null || value.isEmpty()) {
      return defaultName;
    }
    return value;
  }

  public boolean getQuoteValue() {
    Boolean value = AnnotationValueUtil.toBoolean(quote);
    if (value == null) {
      throw new AptIllegalStateException(QUOTE);
    }
    return value;
  }

  public int getQueryTimeoutValue() {
    Integer value = AnnotationValueUtil.toInteger(queryTimeout);
    if (value == null) {
      throw new AptIllegalStateException(QUERY_TIMEOUT);
    }
    return value;
  }

  public MapKeyNamingType getMapKeyNamingValue() {
    VariableElement enumConstant = AnnotationValueUtil.toEnumConstant(mapKeyNaming);
    if (enumConstant == null) {
      throw new AptIllegalStateException(MAP_KEY_NAMING);
    }
    return MapKeyNamingType.valueOf(enumConstant.getSimpleName().toString());
  }

  public boolean getEnsureResultMappingValue() {
    Boolean value = AnnotationValueUtil.toBoolean(ensureResultMapping);
    if (value == null) {
      throw new AptIllegalStateException(ENSURE_RESULT_MAPPING);
    }
    return value;
  }

  public SqlLogType getSqlLogValue() {
    VariableElement enumConstant = AnnotationValueUtil.toEnumConstant(sqlLog);
    if (enumConstant == null) {
      throw new AptIllegalStateException(SQL_LOG);
    }
    return SqlLogType.valueOf(enumConstant.getSimpleName().toString());
  }
}
