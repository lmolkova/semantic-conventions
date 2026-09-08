package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public final class HttpClientSpan {

  private static final HttpClientSpan NOOP =
      new HttpClientSpan();

  private final Span delegate;
  private final boolean noop;
  private final List<String> requestCapturedHeaders;
  private final List<String> responseCapturedHeaders;
  private final boolean requestCaptureBodyContent;
  private final boolean responseCaptureBodyContent;
  private final int requestCaptureBodyContentMaxSize;
  private final int responseCaptureBodyContentMaxSize;
  private final boolean experimental;

  private HttpClientSpan() {
    this.delegate = null;
    this.noop = true;
    this.requestCapturedHeaders = List.of();
    this.responseCapturedHeaders = List.of();
    this.requestCaptureBodyContent = false;
    this.responseCaptureBodyContent = false;
    this.requestCaptureBodyContentMaxSize = -1;
    this.responseCaptureBodyContentMaxSize = -1;
    this.experimental = false;
  }

  HttpClientSpan(
      Span delegate,
      List<String> requestCapturedHeaders,
      List<String> responseCapturedHeaders,
      boolean requestCaptureBodyContent,
      boolean responseCaptureBodyContent,
      int requestCaptureBodyContentMaxSize,
      int responseCaptureBodyContentMaxSize,
      boolean experimental) {
    this.delegate = delegate;
    this.noop = false;
    this.requestCapturedHeaders = requestCapturedHeaders;
    this.responseCapturedHeaders = responseCapturedHeaders;
    this.requestCaptureBodyContent = requestCaptureBodyContent;
    this.responseCaptureBodyContent = responseCaptureBodyContent;
    this.requestCaptureBodyContentMaxSize = requestCaptureBodyContentMaxSize;
    this.responseCaptureBodyContentMaxSize = responseCaptureBodyContentMaxSize;
    this.experimental = experimental;
  }

  static HttpClientSpan noop() {
    return NOOP;
  }

  public <T> HttpClientSpan setAttribute(AttributeKey<T> key, T value) {
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

  public void setRequestCapturedHeaders(Function<String, List<String>> lookup) {
    if (noop) {
      return;
    }
    for (String key : requestCapturedHeaders) {
      List<String> value = lookup.apply(key);
      if (value != null && !value.isEmpty()) {
        delegate.setAttribute(
            HttpAttributes.HTTP_REQUEST_HEADER.getAttributeKey(key.toLowerCase(Locale.ROOT)),
            value);
      }
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

  public void setUrlTemplate(String value) {
    if (noop) {
      return;
    }
    if (!experimental) {
      return;
    }
    if (value != null) {
      delegate.setAttribute(HttpAttributes.URL_TEMPLATE, value);
    }
  }
}