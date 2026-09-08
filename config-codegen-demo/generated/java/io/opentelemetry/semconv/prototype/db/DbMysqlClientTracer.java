package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.semconv.prototype.config.Config;
import io.opentelemetry.semconv.prototype.config.Redaction;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class DbMysqlClientTracer {

  private static final String SCOPE = "general.db.client";

  private final Tracer tracer;
  private volatile State state;

  private DbMysqlClientTracer(
      Tracer tracer, ConfigProvider configProvider) {
    this.tracer = tracer;
    this.state = new State(Config.instrumentation(configProvider));
  }

  private static final class State {

    private final boolean enabled;
    private final List<String> queryParameters;

    private State(DeclarativeConfigProperties config) {
      this.enabled = true;
      this.queryParameters = Config.stringList(
          Config.at(config, SCOPE), "query_parameters", List.of());
    }
  }

  public static DbMysqlClientTracer create(
      Tracer tracer, ConfigProvider configProvider) {
    DbMysqlClientTracer result =
        new DbMysqlClientTracer(tracer, configProvider);
    Config.onInstrumentationChange(configProvider, config -> result.state = new State(config));
    return result;
  }

  public boolean isEnabled() {
    return isEnabled(state);
  }

  private boolean isEnabled(State state) {
    return state.enabled && tracer.isEnabled();
  }
  public DbMysqlClientSpan start(
      String spanName,
      String dbNamespace,
      String dbQuerySummary,
      String dbQueryText,
      String serverAddress,
      Long serverPort) {
    State state = this.state;
    if (!isEnabled(state)) {
      return DbMysqlClientSpan.noop();
    }
    AttributesBuilder attributes = Attributes.builder();
    if (dbNamespace != null) {
      attributes.put(DbAttributes.DB_NAMESPACE, dbNamespace);
    }
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
    Span span =
        tracer
            .spanBuilder(spanName)
            .setSpanKind(SpanKind.CLIENT)
            .setAllAttributes(attributes.build())
            .startSpan();
    return new DbMysqlClientSpan(
        span,
        state.queryParameters);
  }

}