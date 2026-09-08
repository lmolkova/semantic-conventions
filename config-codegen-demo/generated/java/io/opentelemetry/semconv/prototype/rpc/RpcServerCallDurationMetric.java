package io.opentelemetry.semconv.prototype.rpc;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class RpcServerCallDurationMetric {

  private static final String SCOPE = "general.rpc.server";
  private static final String NAME = "rpc.server.call.duration";

  private final DoubleHistogram instrument;
  private volatile State state;

  private RpcServerCallDurationMetric(Meter meter, ConfigProvider configProvider) {
    this.state = new State(Config.instrumentation(configProvider));
    this.instrument = meter
        .histogramBuilder(NAME)
        .setUnit("s")
        .setDescription("Measures the duration of an incoming Remote Procedure Call (RPC).")
        .build();
  }

  private static final class State {

    private final boolean enabled;

    private State(DeclarativeConfigProperties config) {
      this.enabled = true;
    }
  }

  public static RpcServerCallDurationMetric create(
      Meter meter, ConfigProvider configProvider) {
    RpcServerCallDurationMetric result =
        new RpcServerCallDurationMetric(meter, configProvider);
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