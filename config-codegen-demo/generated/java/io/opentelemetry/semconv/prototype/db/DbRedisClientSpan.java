package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public final class DbRedisClientSpan {

  private static final DbRedisClientSpan NOOP =
      new DbRedisClientSpan();

  private final Span delegate;
  private final boolean noop;

  private DbRedisClientSpan() {
    this.delegate = null;
    this.noop = true;
  }

  DbRedisClientSpan(
      Span delegate) {
    this.delegate = delegate;
    this.noop = false;
  }

  static DbRedisClientSpan noop() {
    return NOOP;
  }

  public <T> DbRedisClientSpan setAttribute(AttributeKey<T> key, T value) {
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