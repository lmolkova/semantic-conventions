package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;
import java.util.List;

public final class HttpServerResponseBodySizeMetric {

  private static final String SCOPE = "general.http.server";
  private static final String NAME = "http.server.response.body.size";

  private static final List<String> HTTP_REQUEST_METHOD_DEFAULT =
      List.of("CONNECT", "DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT", "TRACE");

  private final DoubleHistogram instrument;
  private volatile State state;

  private HttpServerResponseBodySizeMetric(Meter meter, ConfigProvider configProvider) {
    this.state = new State(Config.instrumentation(configProvider));
    this.instrument = meter
        .histogramBuilder(NAME)
        .setUnit("By")
        .setDescription("Size of HTTP server response bodies.")
        .build();
  }

  private static final class State {

    private final boolean enabled;
    private final List<String> knownMethods;

    private State(DeclarativeConfigProperties config) {
      this.enabled = Config.experimental(config, "http");
      this.knownMethods = Config.stringList(
          Config.at(config, SCOPE), "known_methods", HTTP_REQUEST_METHOD_DEFAULT);
    }
  }

  public static HttpServerResponseBodySizeMetric create(
      Meter meter, ConfigProvider configProvider) {
    HttpServerResponseBodySizeMetric result =
        new HttpServerResponseBodySizeMetric(meter, configProvider);
    Config.onInstrumentationChange(configProvider, config -> result.state = new State(config));
    return result;
  }

  public boolean isEnabled() {
    return state.enabled;
  }

  public void record(
      long value,
      String errorType,
      String httpRequestMethod,
      Long httpResponseStatusCode,
      String httpRoute,
      String networkProtocolName,
      String networkProtocolVersion,
      String serverAddress,
      Long serverPort,
      String urlScheme,
      String userAgentSyntheticType) {
    State state = this.state;
    if (!state.enabled) {
      return;
    }
    AttributesBuilder attributes = Attributes.builder();
    if (errorType != null) {
      attributes.put(HttpAttributes.ERROR_TYPE, errorType);
    }
    if (httpRequestMethod != null) {
      attributes.put(
          HttpAttributes.HTTP_REQUEST_METHOD,
          state.knownMethods.contains(httpRequestMethod)
              ? httpRequestMethod
              : "_OTHER");
    }
    if (httpResponseStatusCode != null) {
      attributes.put(HttpAttributes.HTTP_RESPONSE_STATUS_CODE, httpResponseStatusCode);
    }
    if (httpRoute != null) {
      attributes.put(HttpAttributes.HTTP_ROUTE, httpRoute);
    }
    if (networkProtocolName != null) {
      attributes.put(HttpAttributes.NETWORK_PROTOCOL_NAME, networkProtocolName);
    }
    if (networkProtocolVersion != null) {
      attributes.put(HttpAttributes.NETWORK_PROTOCOL_VERSION, networkProtocolVersion);
    }
    if (serverAddress != null) {
      attributes.put(HttpAttributes.SERVER_ADDRESS, serverAddress);
    }
    if (serverPort != null) {
      attributes.put(HttpAttributes.SERVER_PORT, serverPort);
    }
    if (urlScheme != null) {
      attributes.put(HttpAttributes.URL_SCHEME, urlScheme);
    }
    if (userAgentSyntheticType != null) {
      attributes.put(HttpAttributes.USER_AGENT_SYNTHETIC_TYPE, userAgentSyntheticType);
    }
    instrument.record(value, attributes.build());
  }
}