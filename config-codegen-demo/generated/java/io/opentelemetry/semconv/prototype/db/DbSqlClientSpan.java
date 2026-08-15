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

public final class DbSqlClientSpan {

  private static final String SCOPE = "general.db.client";

  private DbSqlClientSpan() {}

  public static boolean isEnabled(DeclarativeConfigProperties config) {
    return true;
  }
  public static Span start(
      Tracer tracer,
      DeclarativeConfigProperties config,
      String spanName,
      String dbQuerySummary,
      String dbQueryText,
      String serverAddress,
      Long serverPort) {
    if (!isEnabled(config)) {
      return Span.wrap(Span.current().getSpanContext());
    }
    AttributesBuilder attributes = Attributes.builder();
    if (dbQuerySummary != null) {
      attributes.put(DbAttributes.DB_QUERY_SUMMARY, dbQuerySummary);
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

  public static void populateQueryParameters(
      AttributesBuilder attributes,
      Function<String, String> lookup,
      DeclarativeConfigProperties config) {
    List<String> keys =
        Config.at(config, SCOPE).getScalarList("query_parameters", String.class, List.of());
    for (String key : keys) {
      String value = lookup.apply(key);
      if (value != null) {
        attributes.put(
            DbAttributes.DB_QUERY_PARAMETER.getAttributeKey(key),
            value);
      }
    }
  }

}