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

public final class RpcDubboCallClientTracer {

  private static final String SCOPE = "general.rpc.client";

  private final Tracer tracer;
  private volatile State state;

  private RpcDubboCallClientTracer(
      Tracer tracer, ConfigProvider configProvider) {
    this.tracer = tracer;
    this.state = new State(Config.instrumentation(configProvider));
  }

  private static final class State {

    private final boolean enabled;
    private final List<String> requestCapturedMetadata;
    private final List<String> responseCapturedMetadata;

    private State(DeclarativeConfigProperties config) {
      this.enabled = true;
      this.requestCapturedMetadata = Config.stringList(
          Config.at(config, SCOPE), "request_captured_metadata", List.of());
      this.responseCapturedMetadata = Config.stringList(
          Config.at(config, SCOPE), "response_captured_metadata", List.of());
    }
  }

  public static RpcDubboCallClientTracer create(
      Tracer tracer, ConfigProvider configProvider) {
    RpcDubboCallClientTracer result =
        new RpcDubboCallClientTracer(tracer, configProvider);
    Config.onInstrumentationChange(configProvider, config -> result.state = new State(config));
    return result;
  }

  public boolean isEnabled() {
    return isEnabled(state);
  }

  private boolean isEnabled(State state) {
    return state.enabled && tracer.isEnabled();
  }
  public RpcDubboCallClientSpan start(
      String spanName,
      String rpcMethod,
      String serverAddress,
      Long serverPort) {
    State state = this.state;
    if (!isEnabled(state)) {
      return RpcDubboCallClientSpan.noop();
    }
    AttributesBuilder attributes = Attributes.builder();
    if (rpcMethod != null) {
      attributes.put(RpcAttributes.RPC_METHOD, rpcMethod);
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
    return new RpcDubboCallClientSpan(
        span,
        state.requestCapturedMetadata,
        state.responseCapturedMetadata);
  }

}