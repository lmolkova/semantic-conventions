package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class DbClientConnectionIdleMinMetric {

  private static final String SCOPE = "general.db.client";
  private static final String NAME = "db.client.connection.idle.min";

  private final LongUpDownCounter instrument;
  private final boolean enabled;

  private DbClientConnectionIdleMinMetric(Meter meter, ConfigProvider configProvider) {
    DeclarativeConfigProperties config = Config.instrumentation(configProvider);
    this.enabled = Config.experimental(config, "db");
    this.instrument = meter
        .upDownCounterBuilder(NAME)
        .setUnit("{connection}")
        .setDescription("The minimum number of idle open connections allowed.")
        .build();
  }

  public static DbClientConnectionIdleMinMetric create(
      Meter meter, ConfigProvider configProvider) {
    return new DbClientConnectionIdleMinMetric(meter, configProvider);
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