package io.opentelemetry.semconv.prototype.db;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.semconv.AttributeKeyTemplate;
import java.util.List;

public final class DbAttributes {

  private DbAttributes() {}

  public static final AttributeKey<String> AZURE_CLIENT_ID =
      AttributeKey.stringKey("azure.client.id");

  public static final AttributeKey<String> AZURE_COSMOSDB_CONNECTION_MODE =
      AttributeKey.stringKey("azure.cosmosdb.connection.mode");

  public static final AttributeKey<String> AZURE_COSMOSDB_CONSISTENCY_LEVEL =
      AttributeKey.stringKey("azure.cosmosdb.consistency.level");

  public static final AttributeKey<List<String>> AZURE_COSMOSDB_OPERATION_CONTACTED_REGIONS =
      AttributeKey.stringArrayKey("azure.cosmosdb.operation.contacted_regions");

  public static final AttributeKey<Double> AZURE_COSMOSDB_OPERATION_REQUEST_CHARGE =
      AttributeKey.doubleKey("azure.cosmosdb.operation.request_charge");

  public static final AttributeKey<Long> AZURE_COSMOSDB_REQUEST_BODY_SIZE =
      AttributeKey.longKey("azure.cosmosdb.request.body.size");

  public static final AttributeKey<Long> AZURE_COSMOSDB_RESPONSE_SUB_STATUS_CODE =
      AttributeKey.longKey("azure.cosmosdb.response.sub_status_code");

  public static final AttributeKey<String> AZURE_RESOURCE_PROVIDER_NAMESPACE =
      AttributeKey.stringKey("azure.resource_provider.namespace");

  public static final AttributeKey<String> CASSANDRA_CONSISTENCY_LEVEL =
      AttributeKey.stringKey("cassandra.consistency.level");

  public static final AttributeKey<String> CASSANDRA_COORDINATOR_DC =
      AttributeKey.stringKey("cassandra.coordinator.dc");

  public static final AttributeKey<String> CASSANDRA_COORDINATOR_ID =
      AttributeKey.stringKey("cassandra.coordinator.id");

  public static final AttributeKey<Long> CASSANDRA_PAGE_SIZE =
      AttributeKey.longKey("cassandra.page.size");

  public static final AttributeKey<Boolean> CASSANDRA_QUERY_IDEMPOTENT =
      AttributeKey.booleanKey("cassandra.query.idempotent");

  public static final AttributeKey<Long> CASSANDRA_SPECULATIVE_EXECUTION_COUNT =
      AttributeKey.longKey("cassandra.speculative_execution.count");

  public static final AttributeKey<String> DB_CLIENT_CONNECTION_POOL_NAME =
      AttributeKey.stringKey("db.client.connection.pool.name");

  public static final AttributeKey<String> DB_CLIENT_CONNECTION_STATE =
      AttributeKey.stringKey("db.client.connection.state");

  public static final AttributeKey<String> DB_COLLECTION_NAME =
      AttributeKey.stringKey("db.collection.name");

  public static final AttributeKey<String> DB_NAMESPACE =
      AttributeKey.stringKey("db.namespace");

  public static final AttributeKey<Long> DB_OPERATION_BATCH_SIZE =
      AttributeKey.longKey("db.operation.batch.size");

  public static final AttributeKey<String> DB_OPERATION_NAME =
      AttributeKey.stringKey("db.operation.name");

  public static final AttributeKeyTemplate<String> DB_OPERATION_PARAMETER =
      AttributeKeyTemplate.stringKeyTemplate("db.operation.parameter");

  public static final AttributeKeyTemplate<String> DB_QUERY_PARAMETER =
      AttributeKeyTemplate.stringKeyTemplate("db.query.parameter");

  public static final AttributeKey<String> DB_QUERY_SUMMARY =
      AttributeKey.stringKey("db.query.summary");

  public static final AttributeKey<String> DB_QUERY_TEXT =
      AttributeKey.stringKey("db.query.text");

  public static final AttributeKey<Long> DB_RESPONSE_RETURNED_ROWS =
      AttributeKey.longKey("db.response.returned_rows");

  public static final AttributeKey<String> DB_RESPONSE_STATUS_CODE =
      AttributeKey.stringKey("db.response.status_code");

  public static final AttributeKey<String> DB_STORED_PROCEDURE_NAME =
      AttributeKey.stringKey("db.stored_procedure.name");

  public static final AttributeKey<String> DB_SYSTEM_NAME =
      AttributeKey.stringKey("db.system.name");

  public static final AttributeKey<String> ELASTICSEARCH_NODE_NAME =
      AttributeKey.stringKey("elasticsearch.node.name");

  public static final AttributeKey<String> ERROR_TYPE =
      AttributeKey.stringKey("error.type");

  public static final AttributeKey<String> EXCEPTION_MESSAGE =
      AttributeKey.stringKey("exception.message");

  public static final AttributeKey<String> EXCEPTION_STACKTRACE =
      AttributeKey.stringKey("exception.stacktrace");

  public static final AttributeKey<String> EXCEPTION_TYPE =
      AttributeKey.stringKey("exception.type");

  public static final AttributeKey<String> HTTP_REQUEST_METHOD =
      AttributeKey.stringKey("http.request.method");

  public static final AttributeKey<String> NETWORK_PEER_ADDRESS =
      AttributeKey.stringKey("network.peer.address");

  public static final AttributeKey<Long> NETWORK_PEER_PORT =
      AttributeKey.longKey("network.peer.port");

  public static final AttributeKey<String> ORACLE_DB_DOMAIN =
      AttributeKey.stringKey("oracle.db.domain");

  public static final AttributeKey<String> ORACLE_DB_INSTANCE_NAME =
      AttributeKey.stringKey("oracle.db.instance.name");

  public static final AttributeKey<String> ORACLE_DB_NAME =
      AttributeKey.stringKey("oracle.db.name");

  public static final AttributeKey<String> ORACLE_DB_PDB =
      AttributeKey.stringKey("oracle.db.pdb");

  public static final AttributeKey<String> ORACLE_DB_SERVICE =
      AttributeKey.stringKey("oracle.db.service");

  public static final AttributeKey<String> SERVER_ADDRESS =
      AttributeKey.stringKey("server.address");

  public static final AttributeKey<Long> SERVER_PORT =
      AttributeKey.longKey("server.port");

  public static final AttributeKey<String> URL_FULL =
      AttributeKey.stringKey("url.full");

  public static final AttributeKey<String> USER_AGENT_ORIGINAL =
      AttributeKey.stringKey("user_agent.original");
}