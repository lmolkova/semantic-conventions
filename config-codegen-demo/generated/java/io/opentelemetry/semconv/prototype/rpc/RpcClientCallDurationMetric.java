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
  private final boolean enabled;

  private RpcClientCallDurationMetric(Meter meter, ConfigProvider configProvider) {
    DeclarativeConfigProperties config = Config.instrumentation(configProvider);
    this.enabled = true;
    this.instrument = meter
        .histogramBuilder(NAME)
        .setUnit("s")
        .setDescription("Measures the duration of an outgoing Remote Procedure Call (RPC).")
        .build();
  }

  public static RpcClientCallDurationMetric create(
      Meter meter, ConfigProvider configProvider) {
    return new RpcClientCallDurationMetric(meter, configProvider);
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