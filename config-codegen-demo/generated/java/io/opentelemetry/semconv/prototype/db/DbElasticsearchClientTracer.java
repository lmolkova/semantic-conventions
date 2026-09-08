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

public final class DbElasticsearchClientTracer {

  private static final String SCOPE = "general.db.client";

  private final Tracer tracer;
  private volatile State state;

  private DbElasticsearchClientTracer(
      Tracer tracer, ConfigProvider configProvider) {
    this.tracer = tracer;
    this.state = new State(Config.instrumentation(configProvider));
  }

  private static final class State {

    private final boolean enabled;
    private final List<String> operationParameters;

    private State(DeclarativeConfigProperties config) {
      this.enabled = true;
      this.operationParameters = Config.stringList(
          Config.at(config, SCOPE), "operation_parameters", List.of());
    }
  }

  public static DbElasticsearchClientTracer create(
      Tracer tracer, ConfigProvider configProvider) {
    DbElasticsearchClientTracer result =
        new DbElasticsearchClientTracer(tracer, configProvider);
    Config.onInstrumentationChange(configProvider, config -> result.state = new State(config));
    return result;
  }

  public boolean isEnabled() {
    return isEnabled(state);
  }

  private boolean isEnabled(State state) {
    return state.enabled && tracer.isEnabled();
  }
  public DbElasticsearchClientSpan start(
      String spanName,
      String dbCollectionName,
      String dbNamespace,
      String dbOperationName,
      String dbQueryText,
      String httpRequestMethod,
      String serverAddress,
      Long serverPort,
      String urlFull) {
    State state = this.state;
    if (!isEnabled(state)) {
      return DbElasticsearchClientSpan.noop();
    }
    AttributesBuilder attributes = Attributes.builder();
    if (dbCollectionName != null) {
      attributes.put(DbAttributes.DB_COLLECTION_NAME, dbCollectionName);
    }
    if (dbNamespace != null) {
      attributes.put(DbAttributes.DB_NAMESPACE, dbNamespace);
    }
    if (dbOperationName != null) {
      attributes.put(DbAttributes.DB_OPERATION_NAME, dbOperationName);
    }
    if (dbQueryText != null) {
      attributes.put(DbAttributes.DB_QUERY_TEXT, dbQueryText);
    }
    if (httpRequestMethod != null) {
      attributes.put(DbAttributes.HTTP_REQUEST_METHOD, httpRequestMethod);
    }
    if (serverAddress != null) {
      attributes.put(DbAttributes.SERVER_ADDRESS, serverAddress);
    }
    if (serverPort != null) {
      attributes.put(DbAttributes.SERVER_PORT, serverPort);
    }
    if (urlFull != null) {
      attributes.put(DbAttributes.URL_FULL, urlFull);
    }
    Span span =
        tracer
            .spanBuilder(spanName)
            .setSpanKind(SpanKind.CLIENT)
            .setAllAttributes(attributes.build())
            .startSpan();
    return new DbElasticsearchClientSpan(
        span,
        state.operationParameters);
  }

}