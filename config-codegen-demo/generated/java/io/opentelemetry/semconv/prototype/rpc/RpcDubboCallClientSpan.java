package io.opentelemetry.semconv.prototype.rpc;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.semconv.prototype.config.Config;
import io.opentelemetry.semconv.prototype.config.Redaction;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public final class RpcDubboCallClientSpan {

  private static final String SCOPE = "general.rpc.client";

  private RpcDubboCallClientSpan() {}

  public static boolean isEnabled(DeclarativeConfigProperties config) {
    return true;
  }
  public static Span start(
      Tracer tracer,
      DeclarativeConfigProperties config,
      String spanName,
      String rpcMethod,
      String serverAddress,
      Long serverPort) {
    if (!isEnabled(config)) {
      return Span.wrap(Span.current().getSpanContext());
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
    return tracer
        .spanBuilder(spanName)
        .setSpanKind(SpanKind.CLIENT)
        .setAllAttributes(attributes.build())
        .startSpan();
  }

  public static void populateRequestCapturedMetadata(
      AttributesBuilder attributes,
      Function<String, List<String>> lookup,
      DeclarativeConfigProperties config) {
    List<String> keys =
        Config.at(config, SCOPE).getScalarList("request_captured_metadata", String.class, List.of());
    for (String key : keys) {
      List<String> value = lookup.apply(key);
      if (value != null && !value.isEmpty()) {
        attributes.put(
            RpcAttributes.RPC_REQUEST_METADATA.getAttributeKey(key.toLowerCase(Locale.ROOT)),
            value);
      }
    }
  }

  public static void populateResponseCapturedMetadata(
      AttributesBuilder attributes,
      Function<String, List<String>> lookup,
      DeclarativeConfigProperties config) {
    List<String> keys =
        Config.at(config, SCOPE).getScalarList("response_captured_metadata", String.class, List.of());
    for (String key : keys) {
      List<String> value = lookup.apply(key);
      if (value != null && !value.isEmpty()) {
        attributes.put(
            RpcAttributes.RPC_RESPONSE_METADATA.getAttributeKey(key.toLowerCase(Locale.ROOT)),
            value);
      }
    }
  }

}