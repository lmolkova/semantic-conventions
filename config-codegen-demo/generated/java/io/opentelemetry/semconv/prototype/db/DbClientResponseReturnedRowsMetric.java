package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class DbClientResponseReturnedRowsMetric {

  private static final String SCOPE = "general.db.client";
  private static final String NAME = "db.client.response.returned_rows";

  private final DoubleHistogram instrument;
  private volatile State state;

  private DbClientResponseReturnedRowsMetric(Meter meter, ConfigProvider configProvider) {
    this.state = new State(Config.instrumentation(configProvider));
    this.instrument = meter
        .histogramBuilder(NAME)
        .setUnit("{row}")
        .setDescription("The actual number of records returned by the database operation.")
        .build();
  }

  private static final class State {

    private final boolean enabled;

    private State(DeclarativeConfigProperties config) {
      this.enabled = Config.experimental(config, "db");
    }
  }

  public static DbClientResponseReturnedRowsMetric create(
      Meter meter, ConfigProvider configProvider) {
    DbClientResponseReturnedRowsMetric result =
        new DbClientResponseReturnedRowsMetric(meter, configProvider);
    Config.onInstrumentationChange(configProvider, config -> result.state = new State(config));
    return result;
  }

  public boolean isEnabled() {
    return state.enabled;
  }


  public void record(double value, Attributes attributes) {
    if (!state.enabled) {
      return;
    }
    instrument.record(value, attributes);
  }

}