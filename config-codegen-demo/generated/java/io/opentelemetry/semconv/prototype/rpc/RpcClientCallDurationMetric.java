package io.opentelemetry.semconv.prototype.rpc;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class RpcClientCallDurationMetric {

  private static final String SCOPE = "general.rpc.client";
  private static final String NAME = "rpc.client.call.duration";

  private final DoubleHistogram instrument;
  private volatile State state;

  private RpcClientCallDurationMetric(Meter meter, ConfigProvider configProvider) {
    this.state = new State(Config.instrumentation(configProvider));
    this.instrument = meter
        .histogramBuilder(NAME)
        .setUnit("s")
        .setDescription("Measures the duration of an outgoing Remote Procedure Call (RPC).")
        .build();
  }

  private static final class State {

    private final boolean enabled;

    private State(DeclarativeConfigProperties config) {
      this.enabled = true;
    }
  }

  public static RpcClientCallDurationMetric create(
      Meter meter, ConfigProvider configProvider) {
    RpcClientCallDurationMetric result =
        new RpcClientCallDurationMetric(meter, configProvider);
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