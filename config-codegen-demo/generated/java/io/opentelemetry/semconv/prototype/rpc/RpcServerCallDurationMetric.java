package io.opentelemetry.semconv.prototype.rpc;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class RpcServerCallDurationMetric {

  private static final String SCOPE = "general.rpc.server";
  private static final String NAME = "rpc.server.call.duration";

  private RpcServerCallDurationMetric() {}

  public static boolean isEnabled(DeclarativeConfigProperties config) {
    return true;
  }


  public static DoubleHistogram create(Meter meter) {
    return meter
        .histogramBuilder(NAME)
        .setUnit("s")
        .setDescription("Measures the duration of an incoming Remote Procedure Call (RPC).")
        .build();
  }

  public static void record(
      DoubleHistogram instrument,
      DeclarativeConfigProperties config,
      double value,
      Attributes attributes) {
    if (!isEnabled(config)) {
      return;
    }
    instrument.record(value, attributes);
  }

}