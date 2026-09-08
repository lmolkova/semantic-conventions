package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.semconv.prototype.config.Config;

public final class DbClientOperationExceptionEvent {

  private static final String SCOPE = "general.db.client";
  private static final String NAME = "db.client.operation.exception";

  private final Logger logger;
  private final boolean enabled;

  private DbClientOperationExceptionEvent(Logger logger, ConfigProvider configProvider) {
    DeclarativeConfigProperties config = Config.instrumentation(configProvider);
    this.logger = logger;
    this.enabled = Config.experimental(config, "db");
  }

  public static DbClientOperationExceptionEvent create(
      Logger logger, ConfigProvider configProvider) {
    return new DbClientOperationExceptionEvent(logger, configProvider);
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