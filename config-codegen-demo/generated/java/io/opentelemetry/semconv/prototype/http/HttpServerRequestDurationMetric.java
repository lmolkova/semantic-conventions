package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class HttpServerRequestDurationMetric {

  private static final String SCOPE = "general.http.server";
  private static final String NAME = "http.server.request.duration";

  private HttpServerRequestDurationMetric() {}

  public static boolean isEnabled(DeclarativeConfigProperties config) {
    return true;
  }


  public static DoubleHistogram create(Meter meter) {
    return meter
        .histogramBuilder(NAME)
        .setUnit("s")
        .setDescription("Duration of HTTP server requests.")
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