package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class HttpServerActiveRequestsMetric {

  private static final String SCOPE = "general.http.server";
  private static final String NAME = "http.server.active_requests";

  private final LongUpDownCounter instrument;
  private volatile State state;

  private HttpServerActiveRequestsMetric(Meter meter, ConfigProvider configProvider) {
    this.state = new State(Config.instrumentation(configProvider));
    this.instrument = meter
        .upDownCounterBuilder(NAME)
        .setUnit("{request}")
        .setDescription("Number of active HTTP server requests.")
        .build();
  }

  private static final class State {

    private final boolean enabled;

    private State(DeclarativeConfigProperties config) {
      this.enabled = Config.experimental(config, "http");
    }
  }

  public static HttpServerActiveRequestsMetric create(
      Meter meter, ConfigProvider configProvider) {
    HttpServerActiveRequestsMetric result =
        new HttpServerActiveRequestsMetric(meter, configProvider);
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