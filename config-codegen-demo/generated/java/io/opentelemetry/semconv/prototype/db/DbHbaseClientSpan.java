package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public final class DbHbaseClientSpan {

  private static final DbHbaseClientSpan NOOP =
      new DbHbaseClientSpan();

  private final Span delegate;
  private final boolean noop;

  private DbHbaseClientSpan() {
    this.delegate = null;
    this.noop = true;
  }

  DbHbaseClientSpan(
      Span delegate) {
    this.delegate = delegate;
    this.noop = false;
  }

  static DbHbaseClientSpan noop() {
    return NOOP;
  }

  public <T> DbHbaseClientSpan setAttribute(AttributeKey<T> key, T value) {
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