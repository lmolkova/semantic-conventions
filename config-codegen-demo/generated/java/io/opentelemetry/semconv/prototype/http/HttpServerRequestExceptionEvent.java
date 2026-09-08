package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.semconv.prototype.config.Config;

public final class HttpServerRequestExceptionEvent {

  private static final String SCOPE = "general.http.server";
  private static final String NAME = "http.server.request.exception";

  private final Logger logger;
  private volatile State state;

  private HttpServerRequestExceptionEvent(Logger logger, ConfigProvider configProvider) {
    this.logger = logger;
    this.state = new State(Config.instrumentation(configProvider));
  }

  private static final class State {

    private final boolean enabled;

    private State(DeclarativeConfigProperties config) {
      this.enabled = Config.experimental(config, "http");
    }
  }

  public static HttpServerRequestExceptionEvent create(
      Logger logger, ConfigProvider configProvider) {
    HttpServerRequestExceptionEvent result =
        new HttpServerRequestExceptionEvent(logger, configProvider);
    Config.onInstrumentationChange(configProvider, config -> result.state = new State(config));
    return result;
  }

  public boolean isEnabled(Severity severity) {
    return isEnabled(state, severity);
  }

  private boolean isEnabled(State state, Severity severity) {
    return state.enabled && logger.isEnabled(severity);
  }

  public void emit(Severity severity, Throwable throwable) {
    emit(severity, throwable, Attributes.empty());
  }

  public void emit(
      Severity severity,
      Throwable throwable,
      Attributes attributes) {
    State state = this.state;
    if (!isEnabled(state, severity)) {
      return;
    }
    ExtendedLogRecordBuilder builder = (ExtendedLogRecordBuilder) logger.logRecordBuilder();
    builder
        .setEventName(NAME)
        .setSeverity(severity)
        .setException(throwable)
        .setAllAttributes(attributes)
        .emit();
  }
}