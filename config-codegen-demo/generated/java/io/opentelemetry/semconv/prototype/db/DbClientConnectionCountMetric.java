package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class DbClientConnectionCountMetric {

  private static final String SCOPE = "general.db.client";
  private static final String NAME = "db.client.connection.count";

  private final LongUpDownCounter instrument;
  private volatile State state;

  private DbClientConnectionCountMetric(Meter meter, ConfigProvider configProvider) {
    this.state = new State(Config.instrumentation(configProvider));
    this.instrument = meter
        .upDownCounterBuilder(NAME)
        .setUnit("{connection}")
        .setDescription("The number of connections that are currently in state described by the `state` attribute.")
        .build();
  }

  private static final class State {

    private final boolean enabled;

    private State(DeclarativeConfigProperties config) {
      this.enabled = Config.experimental(config, "db");
    }
  }

  public static DbClientConnectionCountMetric create(
      Meter meter, ConfigProvider configProvider) {
    DbClientConnectionCountMetric result =
        new DbClientConnectionCountMetric(meter, configProvider);
    Config.onInstrumentationChange(configProvider, config -> result.state = new State(config));
    return result;
  }

  public boolean isEnabled() {
    return state.enabled;
  }

  public void add(
      long value,
      String dbClientConnectionPoolName,
      String dbClientConnectionState) {
    State state = this.state;
    if (!state.enabled) {
      return;
    }
    AttributesBuilder attributes = Attributes.builder();
    if (dbClientConnectionPoolName != null) {
      attributes.put(DbAttributes.DB_CLIENT_CONNECTION_POOL_NAME, dbClientConnectionPoolName);
    }
    if (dbClientConnectionState != null) {
      attributes.put(DbAttributes.DB_CLIENT_CONNECTION_STATE, dbClientConnectionState);
    }
    instrument.add(value, attributes.build());
  }
}