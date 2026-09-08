package io.opentelemetry.semconv.prototype.rpc;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public final class RpcCallServerSpan {

  private static final RpcCallServerSpan NOOP =
      new RpcCallServerSpan();

  private final Span delegate;
  private final boolean noop;

  private RpcCallServerSpan() {
    this.delegate = null;
    this.noop = true;
  }

  RpcCallServerSpan(
      Span delegate) {
    this.delegate = delegate;
    this.noop = false;
  }

  static RpcCallServerSpan noop() {
    return NOOP;
  }

  public <T> RpcCallServerSpan setAttribute(AttributeKey<T> key, T value) {
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