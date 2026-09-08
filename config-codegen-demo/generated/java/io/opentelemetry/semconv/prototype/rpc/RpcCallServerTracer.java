package io.opentelemetry.semconv.prototype.rpc;

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

public final class RpcCallServerTracer {

  private static final String SCOPE = "general.rpc.server";

  private final Tracer tracer;
  private volatile State state;

  private RpcCallServerTracer(
      Tracer tracer, ConfigProvider configProvider) {
    this.tracer = tracer;
    this.state = new State(Config.instrumentation(configProvider));
  }

  private static final class State {

    private final boolean enabled;

    private State(DeclarativeConfigProperties config) {
      this.enabled = true;
    }
  }

  public static RpcCallServerTracer create(
      Tracer tracer, ConfigProvider configProvider) {
    RpcCallServerTracer result =
        new RpcCallServerTracer(tracer, configProvider);
    Config.onInstrumentationChange(configProvider, config -> result.state = new State(config));
    return result;
  }

  public boolean isEnabled() {
    return isEnabled(state);
  }

  private boolean isEnabled(State state) {
    return state.enabled && tracer.isEnabled();
  }
  public RpcCallServerSpan start(
      String spanName,
      String rpcMethod,
      String rpcSystemName,
      String serverAddress,
      Long serverPort) {
    State state = this.state;
    if (!isEnabled(state)) {
      return RpcCallServerSpan.noop();
    }
    AttributesBuilder attributes = Attributes.builder();
    if (rpcMethod != null) {
      attributes.put(RpcAttributes.RPC_METHOD, rpcMethod);
    }
    if (rpcSystemName != null) {
      attributes.put(RpcAttributes.RPC_SYSTEM_NAME, rpcSystemName);
    }
    if (serverAddress != null) {
      attributes.put(RpcAttributes.SERVER_ADDRESS, serverAddress);
    }
    if (serverPort != null) {
      attributes.put(RpcAttributes.SERVER_PORT, serverPort);
    }
    Span span =
        tracer
            .spanBuilder(spanName)
            .setSpanKind(SpanKind.SERVER)
            .setAllAttributes(attributes.build())
            .startSpan();
    return new RpcCallServerSpan(
        span);
  }

}