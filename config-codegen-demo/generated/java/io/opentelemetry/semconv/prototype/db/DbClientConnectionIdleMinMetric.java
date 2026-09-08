package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class DbClientConnectionIdleMinMetric {

  private static final String SCOPE = "general.db.client";
  private static final String NAME = "db.client.connection.idle.min";

  private final LongUpDownCounter instrument;
  private volatile State state;

  private DbClientConnectionIdleMinMetric(Meter meter, ConfigProvider configProvider) {
    this.state = new State(Config.instrumentation(configProvider));
    this.instrument = meter
        .upDownCounterBuilder(NAME)
        .setUnit("{connection}")
        .setDescription("The minimum number of idle open connections allowed.")
        .build();
  }

  private static final class State {

    private final boolean enabled;

    private State(DeclarativeConfigProperties config) {
      this.enabled = Config.experimental(config, "db");
    }
  }

  public static DbClientConnectionIdleMinMetric create(
      Meter meter, ConfigProvider configProvider) {
    DbClientConnectionIdleMinMetric result =
        new DbClientConnectionIdleMinMetric(meter, configProvider);
    Config.onInstrumentationChange(configProvider, config -> result.state = new State(config));
    return result;
  }

  public boolean isEnabled() {
    return state.enabled;
  }

  public void add(
      long value,
      String dbClientConnectionPoolName) {
    State state = this.state;
    if (!state.enabled) {
      return;
    }
    AttributesBuilder attributes = Attributes.builder();
    if (dbClientConnectionPoolName != null) {
      attributes.put(DbAttributes.DB_CLIENT_CONNECTION_POOL_NAME, dbClientConnectionPoolName);
    }
    instrument.add(value, attributes.build());
  }
}