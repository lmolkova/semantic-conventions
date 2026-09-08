package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class DbClientConnectionPendingRequestsMetric {

  private static final String SCOPE = "general.db.client";
  private static final String NAME = "db.client.connection.pending_requests";

  private final LongUpDownCounter instrument;
  private final boolean enabled;

  private DbClientConnectionPendingRequestsMetric(Meter meter, ConfigProvider configProvider) {
    DeclarativeConfigProperties config = Config.instrumentation(configProvider);
    this.enabled = Config.experimental(config, "db");
    this.instrument = meter
        .upDownCounterBuilder(NAME)
        .setUnit("{request}")
        .setDescription("The number of current pending requests for an open connection.")
        .build();
  }

  public static DbClientConnectionPendingRequestsMetric create(
      Meter meter, ConfigProvider configProvider) {
    return new DbClientConnectionPendingRequestsMetric(meter, configProvider);
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