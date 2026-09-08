package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class HttpClientActiveRequestsMetric {

  private static final String SCOPE = "general.http.client";
  private static final String NAME = "http.client.active_requests";

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

    private State(DeclarativeConfigProperties config) {
      this.enabled = Config.experimental(config, "http");
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


  public void add(long value, Attributes attributes) {
    if (!state.enabled) {
      return;
    }
    instrument.add(value, attributes);
  }

}