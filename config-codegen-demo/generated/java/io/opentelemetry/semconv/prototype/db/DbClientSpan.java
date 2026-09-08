package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public final class DbClientSpan {

  private static final DbClientSpan NOOP =
      new DbClientSpan();

  private final Span delegate;
  private final boolean noop;
  private final List<String> queryParameters;
  private final boolean experimental;

  private DbClientSpan() {
    this.delegate = null;
    this.noop = true;
    this.queryParameters = List.of();
    this.experimental = false;
  }

  DbClientSpan(
      Span delegate,
      List<String> queryParameters,
      boolean experimental) {
    this.delegate = delegate;
    this.noop = false;
    this.queryParameters = queryParameters;
    this.experimental = experimental;
  }

  static DbClientSpan noop() {
    return NOOP;
  }

  public <T> DbClientSpan setAttribute(AttributeKey<T> key, T value) {
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

  public void setDbResponseReturnedRows(Long value) {
    if (noop) {
      return;
    }
    if (!experimental) {
      return;
    }
    if (value != null) {
      delegate.setAttribute(DbAttributes.DB_RESPONSE_RETURNED_ROWS, value);
    }
  }
}