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
  private final boolean enabled;

  private HttpClientActiveRequestsMetric(Meter meter, ConfigProvider configProvider) {
    DeclarativeConfigProperties config = Config.instrumentation(configProvider);
    this.enabled = Config.experimental(config, "http");
    this.instrument = meter
        .upDownCounterBuilder(NAME)
        .setUnit("{request}")
        .setDescription("Number of active HTTP requests.")
        .build();
  }

  public static HttpClientActiveRequestsMetric create(
      Meter meter, ConfigProvider configProvider) {
    return new HttpClientActiveRequestsMetric(meter, configProvider);
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