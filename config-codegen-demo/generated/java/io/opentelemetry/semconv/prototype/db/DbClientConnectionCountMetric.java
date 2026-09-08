package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class DbClientConnectionCountMetric {

  private static final String SCOPE = "general.db.client";
  private static final String NAME = "db.client.connection.count";

  private final LongUpDownCounter instrument;
  private final boolean enabled;

  private DbClientConnectionCountMetric(Meter meter, ConfigProvider configProvider) {
    DeclarativeConfigProperties config = Config.instrumentation(configProvider);
    this.enabled = Config.experimental(config, "db");
    this.instrument = meter
        .upDownCounterBuilder(NAME)
        .setUnit("{connection}")
        .setDescription("The number of connections that are currently in state described by the `state` attribute.")
        .build();
  }

  public static DbClientConnectionCountMetric create(
      Meter meter, ConfigProvider configProvider) {
    return new DbClientConnectionCountMetric(meter, configProvider);
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