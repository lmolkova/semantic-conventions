package io.opentelemetry.semconv.prototype.rpc;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public final class RpcJsonrpcCallServerSpan {

  private static final RpcJsonrpcCallServerSpan NOOP =
      new RpcJsonrpcCallServerSpan();

  private final Span delegate;
  private final boolean noop;

  private RpcJsonrpcCallServerSpan() {
    this.delegate = null;
    this.noop = true;
  }

  RpcJsonrpcCallServerSpan(
      Span delegate) {
    this.delegate = delegate;
    this.noop = false;
  }

  static RpcJsonrpcCallServerSpan noop() {
    return NOOP;
  }

  public <T> RpcJsonrpcCallServerSpan setAttribute(AttributeKey<T> key, T value) {
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