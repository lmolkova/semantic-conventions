package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class HttpClientOpenConnectionsMetric {

  private static final String SCOPE = "general.http.client";
  private static final String NAME = "http.client.open_connections";

  private HttpClientOpenConnectionsMetric() {}

  public static boolean isEnabled(DeclarativeConfigProperties config) {
    return Config.experimental(config, "http");
  }


  public static LongUpDownCounter create(Meter meter) {
    return meter
        .upDownCounterBuilder(NAME)
        .setUnit("{connection}")
        .setDescription("Number of outbound HTTP connections that are currently active or idle on the client.")
        .build();
  }

  public static void add(
      LongUpDownCounter instrument,
      DeclarativeConfigProperties config,
      long value,
      Attributes attributes) {
    if (!isEnabled(config)) {
      return;
    }
    instrument.add(value, attributes);
  }

}