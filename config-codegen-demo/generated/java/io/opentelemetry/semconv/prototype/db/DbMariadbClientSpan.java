package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public final class DbMariadbClientSpan {

  private static final DbMariadbClientSpan NOOP =
      new DbMariadbClientSpan();

  private final Span delegate;
  private final boolean noop;
  private final List<String> queryParameters;

  private DbMariadbClientSpan() {
    this.delegate = null;
    this.noop = true;
    this.queryParameters = List.of();
  }

  DbMariadbClientSpan(
      Span delegate,
      List<String> queryParameters) {
    this.delegate = delegate;
    this.noop = false;
    this.queryParameters = queryParameters;
  }

  static DbMariadbClientSpan noop() {
    return NOOP;
  }

  public <T> DbMariadbClientSpan setAttribute(AttributeKey<T> key, T value) {
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

  public void setQueryParameters(Function<String, String> lookup) {
    if (noop) {
      return;
    }
    for (String key : queryParameters) {
      String value = lookup.apply(key);
      if (value != null) {
        delegate.setAttribute(
            DbAttributes.DB_QUERY_PARAMETER.getAttributeKey(key),
            value);
      }
    }
  }
}