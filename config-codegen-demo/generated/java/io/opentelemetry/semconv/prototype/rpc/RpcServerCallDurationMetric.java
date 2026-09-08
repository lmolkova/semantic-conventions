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
  private final boolean enabled;

  private RpcServerCallDurationMetric(Meter meter, ConfigProvider configProvider) {
    DeclarativeConfigProperties config = Config.instrumentation(configProvider);
    this.enabled = true;
    this.instrument = meter
        .histogramBuilder(NAME)
        .setUnit("s")
        .setDescription("Measures the duration of an incoming Remote Procedure Call (RPC).")
        .build();
  }

  public static RpcServerCallDurationMetric create(
      Meter meter, ConfigProvider configProvider) {
    return new RpcServerCallDurationMetric(meter, configProvider);
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