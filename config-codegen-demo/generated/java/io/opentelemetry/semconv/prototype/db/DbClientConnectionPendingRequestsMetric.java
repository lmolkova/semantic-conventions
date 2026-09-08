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
  private volatile State state;

  private DbClientConnectionPendingRequestsMetric(Meter meter, ConfigProvider configProvider) {
    this.state = new State(Config.instrumentation(configProvider));
    this.instrument = meter
        .upDownCounterBuilder(NAME)
        .setUnit("{request}")
        .setDescription("The number of current pending requests for an open connection.")
        .build();
  }

  private static final class State {

    private final boolean enabled;

    private State(DeclarativeConfigProperties config) {
      this.enabled = Config.experimental(config, "db");
    }
  }

  public static DbClientConnectionPendingRequestsMetric create(
      Meter meter, ConfigProvider configProvider) {
    DbClientConnectionPendingRequestsMetric result =
        new DbClientConnectionPendingRequestsMetric(meter, configProvider);
    Config.onInstrumentationChange(configProvider, config -> result.state = new State(config));
    return result;
  }

  public boolean isEnabled() {
    return state.enabled;
  }


  public void add(long value, Attributes attributes) {
    if (!state.enabled) {
      return;
    }
    instrument.add(value, attributes);
  }

}