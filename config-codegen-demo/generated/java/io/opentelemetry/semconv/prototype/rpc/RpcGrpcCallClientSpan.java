package io.opentelemetry.semconv.prototype.rpc;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public final class RpcGrpcCallClientSpan {

  private static final RpcGrpcCallClientSpan NOOP =
      new RpcGrpcCallClientSpan();

  private final Span delegate;
  private final boolean noop;
  private final List<String> requestCapturedMetadata;
  private final List<String> responseCapturedMetadata;

  private RpcGrpcCallClientSpan() {
    this.delegate = null;
    this.noop = true;
    this.requestCapturedMetadata = List.of();
    this.responseCapturedMetadata = List.of();
  }

  RpcGrpcCallClientSpan(
      Span delegate,
      List<String> requestCapturedMetadata,
      List<String> responseCapturedMetadata) {
    this.delegate = delegate;
    this.noop = false;
    this.requestCapturedMetadata = requestCapturedMetadata;
    this.responseCapturedMetadata = responseCapturedMetadata;
  }

  static RpcGrpcCallClientSpan noop() {
    return NOOP;
  }

  public <T> RpcGrpcCallClientSpan setAttribute(AttributeKey<T> key, T value) {
    if (!noop) {
      delegate.setAttribute(key, value);
    }
    return this;
  }

  public Scope makeCurrent() {
    return noop ? Scope.noop() : delegate.makeCurrent();
  }

  public void end() {
    if (!noop) {
      delegate.end();
    }
  }

  public void setRequestCapturedMetadata(Function<String, List<String>> lookup) {
    if (noop) {
      return;
    }
    for (String key : requestCapturedMetadata) {
      List<String> value = lookup.apply(key);
      if (value != null && !value.isEmpty()) {
        delegate.setAttribute(
            RpcAttributes.RPC_REQUEST_METADATA.getAttributeKey(key.toLowerCase(Locale.ROOT)),
            value);
      }
    }
  }

  public void setResponseCapturedMetadata(Function<String, List<String>> lookup) {
    if (noop) {
      return;
    }
    for (String key : responseCapturedMetadata) {
      List<String> value = lookup.apply(key);
      if (value != null && !value.isEmpty()) {
        delegate.setAttribute(
            RpcAttributes.RPC_RESPONSE_METADATA.getAttributeKey(key.toLowerCase(Locale.ROOT)),
            value);
      }
    }
  }
}