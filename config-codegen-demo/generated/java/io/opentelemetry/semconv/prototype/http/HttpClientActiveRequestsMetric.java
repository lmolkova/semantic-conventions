package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;
import java.util.List;

public final class HttpClientActiveRequestsMetric {

  private static final String SCOPE = "general.http.client";
  private static final String NAME = "http.client.active_requests";

  private static final List<String> HTTP_REQUEST_METHOD_DEFAULT =
      List.of("CONNECT", "DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT", "TRACE");

  private final LongUpDownCounter instrument;
  private volatile State state;

  private HttpClientActiveRequestsMetric(Meter meter, ConfigProvider configProvider) {
    this.state = new State(Config.instrumentation(configProvider));
    this.instrument = meter
        .upDownCounterBuilder(NAME)
        .setUnit("{request}")
        .setDescription("Number of active HTTP requests.")
        .build();
  }

  private static final class State {

    private final boolean enabled;
    private final boolean experimental;
    private final List<String> knownMethods;

    private State(DeclarativeConfigProperties config) {
      this.enabled = Config.experimental(config, "http");
      this.experimental = Config.experimental(config, "http");
      this.knownMethods = Config.stringList(
          Config.at(config, SCOPE), "known_methods", HTTP_REQUEST_METHOD_DEFAULT);
    }
  }

  public static HttpClientActiveRequestsMetric create(
      Meter meter, ConfigProvider configProvider) {
    HttpClientActiveRequestsMetric result =
        new HttpClientActiveRequestsMetric(meter, configProvider);
    Config.onInstrumentationChange(configProvider, config -> result.state = new State(config));
    return result;
  }

  public boolean isEnabled() {
    return state.enabled;
  }

  public void add(
      long value,
      String httpRequestMethod,
      String serverAddress,
      Long serverPort,
      String urlScheme,
      String urlTemplate) {
    State state = this.state;
    if (!state.enabled) {
      return;
    }
    AttributesBuilder attributes = Attributes.builder();
    if (httpRequestMethod != null) {
      attributes.put(
          HttpAttributes.HTTP_REQUEST_METHOD,
          state.knownMethods.contains(httpRequestMethod)
              ? httpRequestMethod
              : "_OTHER");
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
    if (state.experimental && urlTemplate != null) {
      attributes.put(HttpAttributes.URL_TEMPLATE, urlTemplate);
    }
    instrument.add(value, attributes.build());
  }
}