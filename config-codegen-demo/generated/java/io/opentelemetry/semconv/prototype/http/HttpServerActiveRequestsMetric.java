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
  private final boolean enabled;

  private HttpServerActiveRequestsMetric(Meter meter, ConfigProvider configProvider) {
    DeclarativeConfigProperties config = Config.instrumentation(configProvider);
    this.enabled = Config.experimental(config, "http");
    this.instrument = meter
        .upDownCounterBuilder(NAME)
        .setUnit("{request}")
        .setDescription("Number of active HTTP server requests.")
        .build();
  }

  public static HttpServerActiveRequestsMetric create(
      Meter meter, ConfigProvider configProvider) {
    return new HttpServerActiveRequestsMetric(meter, configProvider);
  }

  public boolean isEnabled() {
    return enabled;
  }


  public void add(long value, Attributes attributes) {
    if (!enabled) {
      return;
    }
    instrument.add(value, attributes);
  }

}