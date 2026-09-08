package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public final class HttpServerSpan {

  private static final HttpServerSpan NOOP =
      new HttpServerSpan();

  private final Span delegate;
  private final boolean noop;
  private final List<String> responseCapturedHeaders;
  private final boolean requestCaptureBodyContent;
  private final boolean responseCaptureBodyContent;
  private final int requestCaptureBodyContentMaxSize;
  private final int responseCaptureBodyContentMaxSize;

  private HttpServerSpan() {
    this.delegate = null;
    this.noop = true;
    this.responseCapturedHeaders = List.of();
    this.requestCaptureBodyContent = false;
    this.responseCaptureBodyContent = false;
    this.requestCaptureBodyContentMaxSize = -1;
    this.responseCaptureBodyContentMaxSize = -1;
  }

  HttpServerSpan(
      Span delegate,
      List<String> responseCapturedHeaders,
      boolean requestCaptureBodyContent,
      boolean responseCaptureBodyContent,
      int requestCaptureBodyContentMaxSize,
      int responseCaptureBodyContentMaxSize) {
    this.delegate = delegate;
    this.noop = false;
    this.responseCapturedHeaders = responseCapturedHeaders;
    this.requestCaptureBodyContent = requestCaptureBodyContent;
    this.responseCaptureBodyContent = responseCaptureBodyContent;
    this.requestCaptureBodyContentMaxSize = requestCaptureBodyContentMaxSize;
    this.responseCaptureBodyContentMaxSize = responseCaptureBodyContentMaxSize;
  }

  static HttpServerSpan noop() {
    return NOOP;
  }

  public <T> HttpServerSpan setAttribute(AttributeKey<T> key, T value) {
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

  public void setResponseCapturedHeaders(Function<String, List<String>> lookup) {
    if (noop) {
      return;
    }
    for (String key : responseCapturedHeaders) {
      List<String> value = lookup.apply(key);
      if (value != null && !value.isEmpty()) {
        delegate.setAttribute(
            HttpAttributes.HTTP_RESPONSE_HEADER.getAttributeKey(key.toLowerCase(Locale.ROOT)),
            value);
      }
    }
  }

  public void setHttpRequestBodyContent(Supplier<String> value) {
    if (noop) {
      return;
    }
    if (!requestCaptureBodyContent) {
      return;
    }
    String resolved = value.get();
    if (resolved != null) {
      resolved = truncateHttpRequestBodyContent(resolved);
      delegate.setAttribute(HttpAttributes.HTTP_REQUEST_BODY_CONTENT, resolved);
    }
  }

  public void setHttpResponseBodyContent(Supplier<String> value) {
    if (noop) {
      return;
    }
    if (!responseCaptureBodyContent) {
      return;
    }
    String resolved = value.get();
    if (resolved != null) {
      resolved = truncateHttpResponseBodyContent(resolved);
      delegate.setAttribute(HttpAttributes.HTTP_RESPONSE_BODY_CONTENT, resolved);
    }
  }

  private String truncateHttpRequestBodyContent(String value) {
    int limit = requestCaptureBodyContentMaxSize;
    if (limit < 0 || value == null || value.length() <= limit) {
      return value;
    }
    return value.substring(0, limit);
  }

  private String truncateHttpResponseBodyContent(String value) {
    int limit = responseCaptureBodyContentMaxSize;
    if (limit < 0 || value == null || value.length() <= limit) {
      return value;
    }
    return value.substring(0, limit);
  }
}