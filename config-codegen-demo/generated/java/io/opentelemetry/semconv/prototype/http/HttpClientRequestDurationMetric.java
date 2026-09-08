package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class HttpClientRequestDurationMetric {

  private static final String SCOPE = "general.http.client";
  private static final String NAME = "http.client.request.duration";

  private final DoubleHistogram instrument;
  private final boolean enabled;

  private HttpClientRequestDurationMetric(Meter meter, ConfigProvider configProvider) {
    DeclarativeConfigProperties config = Config.instrumentation(configProvider);
    this.enabled = true;
    this.instrument = meter
        .histogramBuilder(NAME)
        .setUnit("s")
        .setDescription("Duration of HTTP client requests.")
        .build();
  }

  public static HttpClientRequestDurationMetric create(
      Meter meter, ConfigProvider configProvider) {
    return new HttpClientRequestDurationMetric(meter, configProvider);
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