package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class HttpServerRequestBodySizeMetric {

  private static final String SCOPE = "general.http.server";
  private static final String NAME = "http.server.request.body.size";

  private HttpServerRequestBodySizeMetric() {}

  public static boolean isEnabled(DeclarativeConfigProperties config) {
    return Config.experimental(config, "http");
  }


  public static DoubleHistogram create(Meter meter) {
    return meter
        .histogramBuilder(NAME)
        .setUnit("By")
        .setDescription("Size of HTTP server request bodies.")
        .build();
  }

  public static void record(
      DoubleHistogram instrument,
      DeclarativeConfigProperties config,
      double value,
      Attributes attributes) {
    if (!isEnabled(config)) {
      return;
    }
    instrument.record(value, attributes);
  }

}