package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.semconv.prototype.config.Config;
import io.opentelemetry.semconv.prototype.config.Redaction;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class HttpServerTracer {

  private static final String SCOPE = "general.http.server";

  private static final List<String> HTTP_REQUEST_METHOD_DEFAULT =
      List.of("CONNECT", "DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT", "TRACE");

  private static final List<String> URL_QUERY_SENSITIVE_DEFAULT =
      List.of("X-Amz-Signature", "X-Amz-Credential", "X-Amz-Security-Token", "AWSAccessKeyId", "Signature", "sig", "X-Goog-Signature");

  private final Tracer tracer;
  private volatile State state;

  private HttpServerTracer(
      Tracer tracer, ConfigProvider configProvider) {
    this.tracer = tracer;
    this.state = new State(Config.instrumentation(configProvider));
  }

  private static final class State {

    private final boolean enabled;
    private final List<String> knownMethods;
    private final List<String> requestCapturedHeaders;
    private final List<String> responseCapturedHeaders;
    private final boolean requestCaptureBodyContent;
    private final boolean responseCaptureBodyContent;
    private final int requestCaptureBodyContentMaxSize;
    private final int responseCaptureBodyContentMaxSize;
    private final List<String> sensitiveQueryParameters;

    private State(DeclarativeConfigProperties config) {
      this.enabled = true;
      this.knownMethods = Config.stringList(
          Config.at(config, SCOPE), "known_methods", HTTP_REQUEST_METHOD_DEFAULT);
      this.requestCapturedHeaders = Config.stringList(
          Config.at(config, SCOPE), "request_captured_headers", List.of());
      this.responseCapturedHeaders = Config.stringList(
          Config.at(config, SCOPE), "response_captured_headers", List.of());
      this.requestCaptureBodyContent = Config.at(config, SCOPE).getBoolean(
          "request_capture_body_content", false);
      this.responseCaptureBodyContent = Config.at(config, SCOPE).getBoolean(
          "response_capture_body_content", false);
      this.requestCaptureBodyContentMaxSize = Config.at(config, SCOPE).getInt("request_capture_body_content_max_size", -1);
      this.responseCaptureBodyContentMaxSize = Config.at(config, SCOPE).getInt("response_capture_body_content_max_size", -1);
      this.sensitiveQueryParameters = Config.stringList(
          Config.resolve(config, "sensitive_query_parameters", SCOPE, "general.sanitization.url"), "sensitive_query_parameters", URL_QUERY_SENSITIVE_DEFAULT);
    }
  }

  public static HttpServerTracer create(
      Tracer tracer, ConfigProvider configProvider) {
    HttpServerTracer result =
        new HttpServerTracer(tracer, configProvider);
    Config.onInstrumentationChange(configProvider, config -> result.state = new State(config));
    return result;
  }

  public boolean isEnabled() {
    return isEnabled(state);
  }

  private boolean isEnabled(State state) {
    return state.enabled && tracer.isEnabled();
  }
  public HttpServerSpan start(
      String spanName,
      String clientAddress,
      String httpRequestMethod,
      String serverAddress,
      Long serverPort,
      String urlPath,
      String urlQuery,
      String urlScheme,
      String userAgentOriginal,
      Function<String, List<String>> requestCapturedHeaders) {
    State state = this.state;
    if (!isEnabled(state)) {
      return HttpServerSpan.noop();
    }
    AttributesBuilder attributes = Attributes.builder();
    if (clientAddress != null) {
      attributes.put(HttpAttributes.CLIENT_ADDRESS, clientAddress);
    }
    if (httpRequestMethod != null) {
      attributes.put(HttpAttributes.HTTP_REQUEST_METHOD, filterHttpRequestMethod(state, httpRequestMethod));
    }
    if (serverAddress != null) {
      attributes.put(HttpAttributes.SERVER_ADDRESS, serverAddress);
    }
    if (serverPort != null) {
      attributes.put(HttpAttributes.SERVER_PORT, serverPort);
    }
    if (urlPath != null) {
      attributes.put(HttpAttributes.URL_PATH, urlPath);
    }
    if (urlQuery != null) {
      attributes.put(HttpAttributes.URL_QUERY, urlQuery);
    }
    if (urlScheme != null) {
      attributes.put(HttpAttributes.URL_SCHEME, urlScheme);
    }
    if (userAgentOriginal != null) {
      attributes.put(HttpAttributes.USER_AGENT_ORIGINAL, userAgentOriginal);
    }
    populateRequestCapturedHeaders(state, attributes, requestCapturedHeaders);
    Span span =
        tracer
            .spanBuilder(spanName)
            .setSpanKind(SpanKind.SERVER)
            .setAllAttributes(attributes.build())
            .startSpan();
    return new HttpServerSpan(
        span,
        state.responseCapturedHeaders,
        state.requestCaptureBodyContent,
        state.responseCaptureBodyContent,
        state.requestCaptureBodyContentMaxSize,
        state.responseCaptureBodyContentMaxSize);
  }

  public String filterHttpRequestMethod(String value) {
    return filterHttpRequestMethod(state, value);
  }

  private static String filterHttpRequestMethod(State state, String value) {
    return state.knownMethods.contains(value)
        ? value
        : "_OTHER";
  }

  private static void populateRequestCapturedHeaders(
      State state,
      AttributesBuilder attributes,
      Function<String, List<String>> lookup) {
    for (String key : state.requestCapturedHeaders) {
      List<String> value = lookup.apply(key);
      if (value != null && !value.isEmpty()) {
        attributes.put(
            HttpAttributes.HTTP_REQUEST_HEADER.getAttributeKey(key.toLowerCase(Locale.ROOT)),
            value);
      }
    }
  }

  public String redactUrlQuery(String value) {
    return Redaction.redactQueryParameters(
        value, state.sensitiveQueryParameters, "REDACTED");
  }

}