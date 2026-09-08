package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public final class DbMongodbClientSpan {

  private static final DbMongodbClientSpan NOOP =
      new DbMongodbClientSpan();

  private final Span delegate;
  private final boolean noop;

  private DbMongodbClientSpan() {
    this.delegate = null;
    this.noop = true;
  }

  DbMongodbClientSpan(
      Span delegate) {
    this.delegate = delegate;
    this.noop = false;
  }

  static DbMongodbClientSpan noop() {
    return NOOP;
  }

  public <T> DbMongodbClientSpan setAttribute(AttributeKey<T> key, T value) {
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