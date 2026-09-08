package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class HttpClientResponseBodySizeMetric {

  private static final String SCOPE = "general.http.client";
  private static final String NAME = "http.client.response.body.size";

  private final DoubleHistogram instrument;
  private final boolean enabled;

  private HttpClientResponseBodySizeMetric(Meter meter, ConfigProvider configProvider) {
    DeclarativeConfigProperties config = Config.instrumentation(configProvider);
    this.enabled = Config.experimental(config, "http");
    this.instrument = meter
        .histogramBuilder(NAME)
        .setUnit("By")
        .setDescription("Size of HTTP client response bodies.")
        .build();
  }

  public static HttpClientResponseBodySizeMetric create(
      Meter meter, ConfigProvider configProvider) {
    return new HttpClientResponseBodySizeMetric(meter, configProvider);
  }

  public boolean isEnabled() {
    return enabled;
  }


  public void record(double value, Attributes attributes) {
    if (!enabled) {
      return;
    }
    instrument.record(value, attributes);
  }

}