package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.semconv.prototype.config.Config;

public final class HttpClientRequestExceptionEvent {

  private static final String SCOPE = "general.http.client";
  private static final String NAME = "http.client.request.exception";

  private HttpClientRequestExceptionEvent() {}

  public static boolean isEnabled(DeclarativeConfigProperties config) {
    return Config.experimental(config, "http");
  }

  public static void emit(
      Logger logger, DeclarativeConfigProperties config, Severity severity, Throwable throwable) {
    emit(logger, config, severity, throwable, Attributes.empty());
  }

  public static void emit(
      Logger logger,
      DeclarativeConfigProperties config,
      Severity severity,
      Throwable throwable,
      Attributes attributes) {
    if (!isEnabled(config)) {
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