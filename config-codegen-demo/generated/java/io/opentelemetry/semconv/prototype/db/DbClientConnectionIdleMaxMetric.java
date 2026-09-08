package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class DbClientConnectionIdleMaxMetric {

  private static final String SCOPE = "general.db.client";
  private static final String NAME = "db.client.connection.idle.max";

  private final LongUpDownCounter instrument;
  private final boolean enabled;

  private DbClientConnectionIdleMaxMetric(Meter meter, ConfigProvider configProvider) {
    DeclarativeConfigProperties config = Config.instrumentation(configProvider);
    this.enabled = Config.experimental(config, "db");
    this.instrument = meter
        .upDownCounterBuilder(NAME)
        .setUnit("{connection}")
        .setDescription("The maximum number of idle open connections allowed.")
        .build();
  }

  public static DbClientConnectionIdleMaxMetric create(
      Meter meter, ConfigProvider configProvider) {
    return new DbClientConnectionIdleMaxMetric(meter, configProvider);
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