package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class DbClientConnectionCreateTimeMetric {

  private static final String SCOPE = "general.db.client";
  private static final String NAME = "db.client.connection.create_time";

  private final DoubleHistogram instrument;
  private volatile State state;

  private DbClientConnectionCreateTimeMetric(Meter meter, ConfigProvider configProvider) {
    this.state = new State(Config.instrumentation(configProvider));
    this.instrument = meter
        .histogramBuilder(NAME)
        .setUnit("s")
        .setDescription("The time it took to create a new connection.")
        .build();
  }

  private static final class State {

    private final boolean enabled;

    private State(DeclarativeConfigProperties config) {
      this.enabled = Config.experimental(config, "db");
    }
  }

  public static DbClientConnectionCreateTimeMetric create(
      Meter meter, ConfigProvider configProvider) {
    DbClientConnectionCreateTimeMetric result =
        new DbClientConnectionCreateTimeMetric(meter, configProvider);
    Config.onInstrumentationChange(configProvider, config -> result.state = new State(config));
    return result;
  }

  public boolean isEnabled() {
    return state.enabled;
  }

  public void record(
      double value,
      String dbClientConnectionPoolName) {
    State state = this.state;
    if (!state.enabled) {
      return;
    }
    AttributesBuilder attributes = Attributes.builder();
    if (dbClientConnectionPoolName != null) {
      attributes.put(DbAttributes.DB_CLIENT_CONNECTION_POOL_NAME, dbClientConnectionPoolName);
    }
    instrument.record(value, attributes.build());
  }
}