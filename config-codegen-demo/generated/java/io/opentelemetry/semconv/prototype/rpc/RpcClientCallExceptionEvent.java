package io.opentelemetry.semconv.prototype.rpc;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.semconv.prototype.config.Config;

public final class RpcClientCallExceptionEvent {

  private static final String SCOPE = "general.rpc.client";
  private static final String NAME = "rpc.client.call.exception";

  private final Logger logger;
  private final boolean enabled;

  private RpcClientCallExceptionEvent(Logger logger, ConfigProvider configProvider) {
    DeclarativeConfigProperties config = Config.instrumentation(configProvider);
    this.logger = logger;
    this.enabled = Config.experimental(config, "rpc");
  }

  public static RpcClientCallExceptionEvent create(
      Logger logger, ConfigProvider configProvider) {
    return new RpcClientCallExceptionEvent(logger, configProvider);
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