package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public final class DbElasticsearchClientSpan {

  private static final DbElasticsearchClientSpan NOOP =
      new DbElasticsearchClientSpan();

  private final Span delegate;
  private final boolean noop;
  private final List<String> operationParameters;

  private DbElasticsearchClientSpan() {
    this.delegate = null;
    this.noop = true;
    this.operationParameters = List.of();
  }

  DbElasticsearchClientSpan(
      Span delegate,
      List<String> operationParameters) {
    this.delegate = delegate;
    this.noop = false;
    this.operationParameters = operationParameters;
  }

  static DbElasticsearchClientSpan noop() {
    return NOOP;
  }

  public <T> DbElasticsearchClientSpan setAttribute(AttributeKey<T> key, T value) {
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

  public void setOperationParameters(Function<String, String> lookup) {
    if (noop) {
      return;
    }
    for (String key : operationParameters) {
      String value = lookup.apply(key);
      if (value != null) {
        delegate.setAttribute(
            DbAttributes.DB_OPERATION_PARAMETER.getAttributeKey(key),
            value);
      }
    }
  }
}