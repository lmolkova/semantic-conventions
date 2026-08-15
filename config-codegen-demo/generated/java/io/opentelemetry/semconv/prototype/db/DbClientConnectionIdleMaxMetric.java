package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class DbClientConnectionIdleMaxMetric {

  private static final String SCOPE = "general.db.client";
  private static final String NAME = "db.client.connection.idle.max";

  private DbClientConnectionIdleMaxMetric() {}

  public static boolean isEnabled(DeclarativeConfigProperties config) {
    return Config.experimental(config, "db");
  }


  public static LongUpDownCounter create(Meter meter) {
    return meter
        .upDownCounterBuilder(NAME)
        .setUnit("{connection}")
        .setDescription("The maximum number of idle open connections allowed.")
        .build();
  }

  public static void add(
      LongUpDownCounter instrument,
      DeclarativeConfigProperties config,
      long value,
      Attributes attributes) {
    if (!isEnabled(config)) {
      return;
    }
    instrument.add(value, attributes);
  }

}