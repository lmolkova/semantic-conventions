package io.opentelemetry.semconv.prototype.rpc;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public final class RpcJsonrpcCallClientSpan {

  private static final RpcJsonrpcCallClientSpan NOOP =
      new RpcJsonrpcCallClientSpan();

  private final Span delegate;
  private final boolean noop;

  private RpcJsonrpcCallClientSpan() {
    this.delegate = null;
    this.noop = true;
  }

  RpcJsonrpcCallClientSpan(
      Span delegate) {
    this.delegate = delegate;
    this.noop = false;
  }

  static RpcJsonrpcCallClientSpan noop() {
    return NOOP;
  }

  public <T> RpcJsonrpcCallClientSpan setAttribute(AttributeKey<T> key, T value) {
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
}