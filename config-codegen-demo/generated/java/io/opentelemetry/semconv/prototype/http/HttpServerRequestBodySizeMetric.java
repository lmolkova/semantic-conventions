package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class HttpServerRequestBodySizeMetric {

  private static final String SCOPE = "general.http.server";
  private static final String NAME = "http.server.request.body.size";

  private final DoubleHistogram instrument;
  private final boolean enabled;

  private HttpServerRequestBodySizeMetric(Meter meter, ConfigProvider configProvider) {
    DeclarativeConfigProperties config = Config.instrumentation(configProvider);
    this.enabled = Config.experimental(config, "http");
    this.instrument = meter
        .histogramBuilder(NAME)
        .setUnit("By")
        .setDescription("Size of HTTP server request bodies.")
        .build();
  }

  public static HttpServerRequestBodySizeMetric create(
      Meter meter, ConfigProvider configProvider) {
    return new HttpServerRequestBodySizeMetric(meter, configProvider);
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