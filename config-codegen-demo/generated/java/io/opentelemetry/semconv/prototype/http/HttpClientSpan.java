package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.semconv.prototype.config.Config;
import io.opentelemetry.semconv.prototype.config.Redaction;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public final class HttpClientSpan {

  private static final String SCOPE = "general.http.client";

  private static final List<String> HTTP_REQUEST_METHOD_DEFAULT =
      List.of("CONNECT", "DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT", "TRACE");

  private HttpClientSpan() {}

  public static boolean isEnabled(DeclarativeConfigProperties config) {
    return true;
  }
  public static Span start(
      Tracer tracer,
      DeclarativeConfigProperties config,
      String spanName,
      String httpRequestMethod,
      String serverAddress,
      Long serverPort,
      String urlFull) {
    if (!isEnabled(config)) {
      return Span.wrap(Span.current().getSpanContext());
    }
    AttributesBuilder attributes = Attributes.builder();
    if (httpRequestMethod != null) {
      attributes.put(HttpAttributes.HTTP_REQUEST_METHOD, filterHttpRequestMethod(config, httpRequestMethod));
    }
    if (serverAddress != null) {
      attributes.put(HttpAttributes.SERVER_ADDRESS, serverAddress);
    }
    if (serverPort != null) {
      attributes.put(HttpAttributes.SERVER_PORT, serverPort);
    }
    if (urlFull != null) {
      attributes.put(HttpAttributes.URL_FULL, urlFull);
    }
    populateServicePeerNameMapping(attributes, config, serverAddress);
    return tracer
        .spanBuilder(spanName)
        .setSpanKind(SpanKind.CLIENT)
        .setAllAttributes(attributes.build())
        .startSpan();
  }

  public static String filterHttpRequestMethod(
      DeclarativeConfigProperties config, String value) {
    List<String> allowed =
        Config.at(config, SCOPE)
            .getScalarList("known_methods", String.class, HTTP_REQUEST_METHOD_DEFAULT);
    return allowed.contains(value) ? value : "_OTHER";
  }

  public static void populateRequestCapturedHeaders(
      AttributesBuilder attributes,
      Function<String, List<String>> lookup,
      DeclarativeConfigProperties config) {
    List<String> keys =
        Config.at(config, SCOPE).getScalarList("request_captured_headers", String.class, List.of());
    for (String key : keys) {
      List<String> value = lookup.apply(key);
      if (value != null && !value.isEmpty()) {
        attributes.put(
            HttpAttributes.HTTP_REQUEST_HEADER.getAttributeKey(key.toLowerCase(Locale.ROOT)),
            value);
      }
    }
  }

  public static void populateResponseCapturedHeaders(
      AttributesBuilder attributes,
      Function<String, List<String>> lookup,
      DeclarativeConfigProperties config) {
    List<String> keys =
        Config.at(config, SCOPE).getScalarList("response_captured_headers", String.class, List.of());
    for (String key : keys) {
      List<String> value = lookup.apply(key);
      if (value != null && !value.isEmpty()) {
        attributes.put(
            HttpAttributes.HTTP_RESPONSE_HEADER.getAttributeKey(key.toLowerCase(Locale.ROOT)),
            value);
      }
    }
  }

  public static void setHttpRequestBodyContent(
      Span span, Supplier<String> value, DeclarativeConfigProperties config) {
    if (!Config.at(config, SCOPE).getBoolean("request_capture_body_content", false)) {
      return;
    }
    String resolved = value.get();
    if (resolved != null) {
      resolved = truncateHttpRequestBodyContent(config, resolved);
      span.setAttribute(HttpAttributes.HTTP_REQUEST_BODY_CONTENT, resolved);
    }
  }

  public static void setHttpResponseBodyContent(
      Span span, Supplier<String> value, DeclarativeConfigProperties config) {
    if (!Config.at(config, SCOPE).getBoolean("response_capture_body_content", false)) {
      return;
    }
    String resolved = value.get();
    if (resolved != null) {
      resolved = truncateHttpResponseBodyContent(config, resolved);
      span.setAttribute(HttpAttributes.HTTP_RESPONSE_BODY_CONTENT, resolved);
    }
  }

  public static String truncateHttpRequestBodyContent(
      DeclarativeConfigProperties config, String value) {
    int limit = Config.at(config, SCOPE).getInt("request_capture_body_content_max_size", -1);
    if (limit < 0 || value == null || value.length() <= limit) {
      return value;
    }
    return value.substring(0, limit);
  }

  public static String truncateHttpResponseBodyContent(
      DeclarativeConfigProperties config, String value) {
    int limit = Config.at(config, SCOPE).getInt("response_capture_body_content_max_size", -1);
    if (limit < 0 || value == null || value.length() <= limit) {
      return value;
    }
    return value.substring(0, limit);
  }

  public static void populateServicePeerNameMapping(
      AttributesBuilder attributes,
      DeclarativeConfigProperties config,
      String serverAddress) {
    if (serverAddress == null) {
      return;
    }
    for (DeclarativeConfigProperties entry :
        Config.at(config, SCOPE).getStructuredList("service_peer_name_mapping", List.of())) {
      if (serverAddress.equals(entry.getString("match"))) {
        attributes.put(
            HttpAttributes.SERVICE_PEER_NAME, entry.getString("value"));
        return;
      }
    }
  }

  public static void setUrlTemplate(
      Span span, String value, DeclarativeConfigProperties config) {
    if (!Config.experimental(config, "http")) {
      return;
    }
    if (value != null) {
      span.setAttribute(HttpAttributes.URL_TEMPLATE, value);
    }
  }

}