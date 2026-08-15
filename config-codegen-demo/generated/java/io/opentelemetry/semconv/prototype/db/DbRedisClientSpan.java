package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.semconv.prototype.config.Config;
import io.opentelemetry.semconv.prototype.config.Redaction;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public final class DbRedisClientSpan {

  private static final String SCOPE = "general.db.client";

  private DbRedisClientSpan() {}

  public static boolean isEnabled(DeclarativeConfigProperties config) {
    return true;
  }
  public static Span start(
      Tracer tracer,
      DeclarativeConfigProperties config,
      String spanName,
      String dbNamespace,
      String dbOperationName,
      String dbQueryText,
      String serverAddress,
      Long serverPort) {
    if (!isEnabled(config)) {
      return Span.wrap(Span.current().getSpanContext());
    }
    AttributesBuilder attributes = Attributes.builder();
    if (dbNamespace != null) {
      attributes.put(DbAttributes.DB_NAMESPACE, dbNamespace);
    }
    if (dbOperationName != null) {
      attributes.put(DbAttributes.DB_OPERATION_NAME, dbOperationName);
    }
    if (dbQueryText != null) {
      attributes.put(DbAttributes.DB_QUERY_TEXT, dbQueryText);
    }
    if (serverAddress != null) {
      attributes.put(DbAttributes.SERVER_ADDRESS, serverAddress);
    }
    if (serverPort != null) {
      attributes.put(DbAttributes.SERVER_PORT, serverPort);
    }
    return tracer
        .spanBuilder(spanName)
        .setSpanKind(SpanKind.CLIENT)
        .setAllAttributes(attributes.build())
        .startSpan();
  }

}