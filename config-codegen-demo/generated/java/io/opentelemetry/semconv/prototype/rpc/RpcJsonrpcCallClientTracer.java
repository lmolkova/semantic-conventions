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

public final class RpcJsonrpcCallClientTracer {

  private static final String SCOPE = "general.rpc.client";

  private static final List<String> RPC_METHOD_DEFAULT =
      List.of();

  private final Tracer tracer;
  private volatile State state;

  private RpcJsonrpcCallClientTracer(
      Tracer tracer, ConfigProvider configProvider) {
    this.tracer = tracer;
    this.state = new State(Config.instrumentation(configProvider));
  }

  private static final class State {

    private final boolean enabled;
    private final List<String> knownMethods;

    private State(DeclarativeConfigProperties config) {
      this.enabled = true;
      this.knownMethods = Config.stringList(
          Config.at(config, SCOPE), "known_methods", RPC_METHOD_DEFAULT);
    }
  }

  public static RpcJsonrpcCallClientTracer create(
      Tracer tracer, ConfigProvider configProvider) {
    RpcJsonrpcCallClientTracer result =
        new RpcJsonrpcCallClientTracer(tracer, configProvider);
    Config.onInstrumentationChange(configProvider, config -> result.state = new State(config));
    return result;
  }

  public boolean isEnabled() {
    return isEnabled(state);
  }

  private boolean isEnabled(State state) {
    return state.enabled && tracer.isEnabled();
  }
  public RpcJsonrpcCallClientSpan start(
      String spanName,
      String rpcMethod,
      String serverAddress,
      Long serverPort) {
    State state = this.state;
    if (!isEnabled(state)) {
      return RpcJsonrpcCallClientSpan.noop();
    }
    AttributesBuilder attributes = Attributes.builder();
    if (rpcMethod != null) {
      attributes.put(RpcAttributes.RPC_METHOD, filterRpcMethod(state, rpcMethod));
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
            .setSpanKind(SpanKind.CLIENT)
            .setAllAttributes(attributes.build())
            .startSpan();
    return new RpcJsonrpcCallClientSpan(
        span);
  }

  public String filterRpcMethod(String value) {
    return filterRpcMethod(state, value);
  }

  private static String filterRpcMethod(State state, String value) {
    return state.knownMethods.contains(value)
        ? value
        : "_OTHER";
  }

}