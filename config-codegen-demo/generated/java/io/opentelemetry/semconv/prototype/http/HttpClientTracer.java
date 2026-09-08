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

public final class HttpClientTracer {

  private static final String SCOPE = "general.http.client";

  private static final List<String> HTTP_REQUEST_METHOD_DEFAULT =
      List.of("CONNECT", "DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT", "TRACE");

  private final Tracer tracer;
  private volatile State state;

  private HttpClientTracer(
      Tracer tracer, ConfigProvider configProvider) {
    this.tracer = tracer;
    this.state = new State(Config.instrumentation(configProvider));
  }

  private static final class State {

    private final boolean enabled;
    private final boolean experimental;
    private final List<String> knownMethods;
    private final List<String> requestCapturedHeaders;
    private final List<String> responseCapturedHeaders;
    private final boolean requestCaptureBodyContent;
    private final boolean responseCaptureBodyContent;
    private final int requestCaptureBodyContentMaxSize;
    private final int responseCaptureBodyContentMaxSize;
    private final Map<String, String> servicePeerNameMapping;

    private State(DeclarativeConfigProperties config) {
      this.experimental = Config.experimental(config, "http");
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
      this.servicePeerNameMapping = Config.stringMap(
          Config.at(config, SCOPE).getStructuredList("service_peer_name_mapping", List.of()), "match", "value");
    }
  }

  public static HttpClientTracer create(
      Tracer tracer, ConfigProvider configProvider) {
    HttpClientTracer result =
        new HttpClientTracer(tracer, configProvider);
    Config.onInstrumentationChange(configProvider, config -> result.state = new State(config));
    return result;
  }

  public boolean isEnabled() {
    return isEnabled(state);
  }

  private boolean isEnabled(State state) {
    return state.enabled && tracer.isEnabled();
  }
  public HttpClientSpan start(
      String spanName,
      String httpRequestMethod,
      String serverAddress,
      Long serverPort,
      String urlFull) {
    State state = this.state;
    if (!isEnabled(state)) {
      return HttpClientSpan.noop();
    }
    AttributesBuilder attributes = Attributes.builder();
    if (httpRequestMethod != null) {
      attributes.put(HttpAttributes.HTTP_REQUEST_METHOD, filterHttpRequestMethod(state, httpRequestMethod));
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
    populateServicePeerNameMapping(state, attributes, serverAddress);
    Span span =
        tracer
            .spanBuilder(spanName)
            .setSpanKind(SpanKind.CLIENT)
            .setAllAttributes(attributes.build())
            .startSpan();
    return new HttpClientSpan(
        span,
        state.requestCapturedHeaders,
        state.responseCapturedHeaders,
        state.requestCaptureBodyContent,
        state.responseCaptureBodyContent,
        state.requestCaptureBodyContentMaxSize,
        state.responseCaptureBodyContentMaxSize,
        state.experimental);
  }

  public String filterHttpRequestMethod(String value) {
    return filterHttpRequestMethod(state, value);
  }

  private static String filterHttpRequestMethod(State state, String value) {
    return state.knownMethods.contains(value)
        ? value
        : "_OTHER";
  }

  public void populateServicePeerNameMapping(
      AttributesBuilder attributes,
      String serverAddress) {
    populateServicePeerNameMapping(state, attributes, serverAddress);
  }

  private static void populateServicePeerNameMapping(
      State state,
      AttributesBuilder attributes,
      String serverAddress) {
    if (serverAddress == null) {
      return;
    }
    String mapped = state.servicePeerNameMapping.get(serverAddress);
    if (mapped != null) {
      attributes.put(HttpAttributes.SERVICE_PEER_NAME, mapped);
    }
  }

}