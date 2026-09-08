package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.semconv.prototype.config.Config;

public final class DbClientOperationDurationMetric {

  private static final String SCOPE = "general.db.client";
  private static final String NAME = "db.client.operation.duration";

  private final DoubleHistogram instrument;
  private volatile State state;

  private DbClientOperationDurationMetric(Meter meter, ConfigProvider configProvider) {
    this.state = new State(Config.instrumentation(configProvider));
    this.instrument = meter
        .histogramBuilder(NAME)
        .setUnit("s")
        .setDescription("Duration of database client operations.")
        .build();
  }

  private static final class State {

    private final boolean enabled;
    private final boolean metricQueryText;

    private State(DeclarativeConfigProperties config) {
      this.enabled = true;
      this.metricQueryText = Config.resolve(config, "metric_query_text", SCOPE, "general.db.client")
          .getBoolean("metric_query_text", false);
    }
  }

  public static DbClientOperationDurationMetric create(
      Meter meter, ConfigProvider configProvider) {
    DbClientOperationDurationMetric result =
        new DbClientOperationDurationMetric(meter, configProvider);
    Config.onInstrumentationChange(configProvider, config -> result.state = new State(config));
    return result;
  }

  public boolean isEnabled() {
    return state.enabled;
  }

  public void record(
      double value,
      String dbCollectionName,
      String dbNamespace,
      String dbOperationName,
      String dbQuerySummary,
      String dbQueryText,
      String dbResponseStatusCode,
      String dbStoredProcedureName,
      String dbSystemName,
      String errorType,
      String networkPeerAddress,
      Long networkPeerPort,
      String serverAddress,
      Long serverPort) {
    State state = this.state;
    if (!state.enabled) {
      return;
    }
    AttributesBuilder attributes = Attributes.builder();
    if (dbCollectionName != null) {
      attributes.put(DbAttributes.DB_COLLECTION_NAME, dbCollectionName);
    }
    if (dbNamespace != null) {
      attributes.put(DbAttributes.DB_NAMESPACE, dbNamespace);
    }
    if (dbOperationName != null) {
      attributes.put(DbAttributes.DB_OPERATION_NAME, dbOperationName);
    }
    if (dbQuerySummary != null) {
      attributes.put(DbAttributes.DB_QUERY_SUMMARY, dbQuerySummary);
    }
    if (state.metricQueryText && dbQueryText != null) {
      attributes.put(DbAttributes.DB_QUERY_TEXT, dbQueryText);
    }
    if (dbResponseStatusCode != null) {
      attributes.put(DbAttributes.DB_RESPONSE_STATUS_CODE, dbResponseStatusCode);
    }
    if (dbStoredProcedureName != null) {
      attributes.put(DbAttributes.DB_STORED_PROCEDURE_NAME, dbStoredProcedureName);
    }
    if (dbSystemName != null) {
      attributes.put(DbAttributes.DB_SYSTEM_NAME, dbSystemName);
    }
    if (errorType != null) {
      attributes.put(DbAttributes.ERROR_TYPE, errorType);
    }
    if (networkPeerAddress != null) {
      attributes.put(DbAttributes.NETWORK_PEER_ADDRESS, networkPeerAddress);
    }
    if (networkPeerPort != null) {
      attributes.put(DbAttributes.NETWORK_PEER_PORT, networkPeerPort);
    }
    if (serverAddress != null) {
      attributes.put(DbAttributes.SERVER_ADDRESS, serverAddress);
    }
    if (serverPort != null) {
      attributes.put(DbAttributes.SERVER_PORT, serverPort);
    }
    instrument.record(value, attributes.build());
  }
}