package io.opentelemetry.semconv.prototype.rpc;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.semconv.prototype.config.Config;

public final class RpcServerCallExceptionEvent {

  private static final String SCOPE = "general.rpc.server";
  private static final String NAME = "rpc.server.call.exception";

  private final Logger logger;
  private volatile State state;

  private RpcServerCallExceptionEvent(Logger logger, ConfigProvider configProvider) {
    this.logger = logger;
    this.state = new State(Config.instrumentation(configProvider));
  }

  private static final class State {

    private final boolean enabled;

    private State(DeclarativeConfigProperties config) {
      this.enabled = Config.experimental(config, "rpc");
    }
  }

  public static RpcServerCallExceptionEvent create(
      Logger logger, ConfigProvider configProvider) {
    RpcServerCallExceptionEvent result =
        new RpcServerCallExceptionEvent(logger, configProvider);
    Config.onInstrumentationChange(configProvider, config -> result.state = new State(config));
    return result;
  }

  public boolean isEnabled() {
    return state.enabled;
  }

  public void emit(Severity severity, Throwable throwable) {
    emit(severity, throwable, Attributes.empty());
  }

  public void emit(
      Severity severity,
      Throwable throwable,
      Attributes attributes) {
    if (!state.enabled) {
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