package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class DbClientConnectionUseTimeMetric {

  private static final String SCOPE = "general.db.client";
  private static final String NAME = "db.client.connection.use_time";

  private DbClientConnectionUseTimeMetric() {}

  public static boolean isEnabled(DeclarativeConfigProperties config) {
    return Config.experimental(config, "db");
  }


  public static DoubleHistogram create(Meter meter) {
    return meter
        .histogramBuilder(NAME)
        .setUnit("s")
        .setDescription("The time between borrowing a connection and returning it to the pool.")
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