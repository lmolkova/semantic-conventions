package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class HttpClientConnectionDurationMetric {

  private static final String SCOPE = "general.http.client";
  private static final String NAME = "http.client.connection.duration";

  private final DoubleHistogram instrument;
  private volatile State state;

  private HttpClientConnectionDurationMetric(Meter meter, ConfigProvider configProvider) {
    this.state = new State(Config.instrumentation(configProvider));
    this.instrument = meter
        .histogramBuilder(NAME)
        .setUnit("s")
        .setDescription("The duration of the successfully established outbound HTTP connections.")
        .build();
  }

  private static final class State {

    private final boolean enabled;

    private State(DeclarativeConfigProperties config) {
      this.enabled = Config.experimental(config, "http");
    }
  }

  public static HttpClientConnectionDurationMetric create(
      Meter meter, ConfigProvider configProvider) {
    HttpClientConnectionDurationMetric result =
        new HttpClientConnectionDurationMetric(meter, configProvider);
    Config.onInstrumentationChange(configProvider, config -> result.state = new State(config));
    return result;
  }

  public boolean isEnabled() {
    return state.enabled;
  }


  public void record(double value, Attributes attributes) {
    if (!state.enabled) {
      return;
    }
    instrument.record(value, attributes);
  }

}