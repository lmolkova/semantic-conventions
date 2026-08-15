package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class HttpClientRequestDurationMetric {

  private static final String SCOPE = "general.http.client";
  private static final String NAME = "http.client.request.duration";

  private HttpClientRequestDurationMetric() {}

  public static boolean isEnabled(DeclarativeConfigProperties config) {
    return true;
  }


  public static DoubleHistogram create(Meter meter) {
    return meter
        .histogramBuilder(NAME)
        .setUnit("s")
        .setDescription("Duration of HTTP client requests.")
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