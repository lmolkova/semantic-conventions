package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class DbClientConnectionUseTimeMetric {

  private static final String SCOPE = "general.db.client";
  private static final String NAME = "db.client.connection.use_time";

  private final DoubleHistogram instrument;
  private final boolean enabled;

  private DbClientConnectionUseTimeMetric(Meter meter, ConfigProvider configProvider) {
    DeclarativeConfigProperties config = Config.instrumentation(configProvider);
    this.enabled = Config.experimental(config, "db");
    this.instrument = meter
        .histogramBuilder(NAME)
        .setUnit("s")
        .setDescription("The time between borrowing a connection and returning it to the pool.")
        .build();
  }

  public static DbClientConnectionUseTimeMetric create(
      Meter meter, ConfigProvider configProvider) {
    return new DbClientConnectionUseTimeMetric(meter, configProvider);
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