package io.opentelemetry.semconv.prototype.rpc;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class RpcClientCallDurationMetric {

  private static final String SCOPE = "general.rpc.client";
  private static final String NAME = "rpc.client.call.duration";

  private RpcClientCallDurationMetric() {}

  public static boolean isEnabled(DeclarativeConfigProperties config) {
    return true;
  }


  public static DoubleHistogram create(Meter meter) {
    return meter
        .histogramBuilder(NAME)
        .setUnit("s")
        .setDescription("Measures the duration of an outgoing Remote Procedure Call (RPC).")
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