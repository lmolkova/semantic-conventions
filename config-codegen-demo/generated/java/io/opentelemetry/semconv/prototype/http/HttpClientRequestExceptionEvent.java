package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.semconv.prototype.config.Config;

public final class HttpClientRequestExceptionEvent {

  private static final String SCOPE = "general.http.client";
  private static final String NAME = "http.client.request.exception";

  private final Logger logger;
  private final boolean enabled;

  private HttpClientRequestExceptionEvent(Logger logger, ConfigProvider configProvider) {
    DeclarativeConfigProperties config = Config.instrumentation(configProvider);
    this.logger = logger;
    this.enabled = Config.experimental(config, "http");
  }

  public static HttpClientRequestExceptionEvent create(
      Logger logger, ConfigProvider configProvider) {
    return new HttpClientRequestExceptionEvent(logger, configProvider);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void emit(Severity severity, Throwable throwable) {
    emit(severity, throwable, Attributes.empty());
  }

  public void emit(
      Severity severity,
      Throwable throwable,
      Attributes attributes) {
    if (!enabled) {
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