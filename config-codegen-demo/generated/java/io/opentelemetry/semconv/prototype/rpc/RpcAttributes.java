package io.opentelemetry.semconv.prototype.rpc;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.semconv.AttributeKeyTemplate;
import java.util.List;

public final class RpcAttributes {

  private RpcAttributes() {}

  public static final AttributeKey<String> ERROR_TYPE =
      AttributeKey.stringKey("error.type");

  public static final AttributeKey<String> EXCEPTION_MESSAGE =
      AttributeKey.stringKey("exception.message");

  public static final AttributeKey<String> EXCEPTION_STACKTRACE =
      AttributeKey.stringKey("exception.stacktrace");

  public static final AttributeKey<String> EXCEPTION_TYPE =
      AttributeKey.stringKey("exception.type");

  public static final AttributeKey<String> JSONRPC_PROTOCOL_VERSION =
      AttributeKey.stringKey("jsonrpc.protocol.version");

  public static final AttributeKey<String> JSONRPC_REQUEST_ID =
      AttributeKey.stringKey("jsonrpc.request.id");

  public static final AttributeKey<String> NETWORK_PEER_ADDRESS =
      AttributeKey.stringKey("network.peer.address");

  public static final AttributeKey<Long> NETWORK_PEER_PORT =
      AttributeKey.longKey("network.peer.port");

  public static final AttributeKey<String> RPC_METHOD =
      AttributeKey.stringKey("rpc.method");

  public static final AttributeKey<String> RPC_METHOD_ORIGINAL =
      AttributeKey.stringKey("rpc.method_original");

  public static final AttributeKeyTemplate<List<String>> RPC_REQUEST_METADATA =
      AttributeKeyTemplate.stringArrayKeyTemplate("rpc.request.metadata");

  public static final AttributeKeyTemplate<List<String>> RPC_RESPONSE_METADATA =
      AttributeKeyTemplate.stringArrayKeyTemplate("rpc.response.metadata");

  public static final AttributeKey<String> RPC_STATUS_CODE =
      AttributeKey.stringKey("rpc.status_code");

  public static final AttributeKey<String> RPC_SYSTEM_NAME =
      AttributeKey.stringKey("rpc.system.name");

  public static final AttributeKey<String> SERVER_ADDRESS =
      AttributeKey.stringKey("server.address");

  public static final AttributeKey<Long> SERVER_PORT =
      AttributeKey.longKey("server.port");
}