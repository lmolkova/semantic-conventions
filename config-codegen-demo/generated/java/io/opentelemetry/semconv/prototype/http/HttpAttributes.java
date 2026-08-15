package io.opentelemetry.semconv.prototype.http;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.semconv.AttributeKeyTemplate;
import java.util.List;

public final class HttpAttributes {

  private HttpAttributes() {}

  public static final AttributeKey<String> CLIENT_ADDRESS =
      AttributeKey.stringKey("client.address");

  public static final AttributeKey<Long> CLIENT_PORT =
      AttributeKey.longKey("client.port");

  public static final AttributeKey<String> ERROR_TYPE =
      AttributeKey.stringKey("error.type");

  public static final AttributeKey<String> EXCEPTION_MESSAGE =
      AttributeKey.stringKey("exception.message");

  public static final AttributeKey<String> EXCEPTION_STACKTRACE =
      AttributeKey.stringKey("exception.stacktrace");

  public static final AttributeKey<String> EXCEPTION_TYPE =
      AttributeKey.stringKey("exception.type");

  public static final AttributeKey<String> HTTP_CONNECTION_STATE =
      AttributeKey.stringKey("http.connection.state");

  public static final AttributeKey<String> HTTP_REQUEST_BODY_CONTENT =
      AttributeKey.stringKey("http.request.body.content");

  public static final AttributeKey<Long> HTTP_REQUEST_BODY_SIZE =
      AttributeKey.longKey("http.request.body.size");

  public static final AttributeKeyTemplate<List<String>> HTTP_REQUEST_HEADER =
      AttributeKeyTemplate.stringArrayKeyTemplate("http.request.header");

  public static final AttributeKey<String> HTTP_REQUEST_METHOD =
      AttributeKey.stringKey("http.request.method");

  public static final AttributeKey<String> HTTP_REQUEST_METHOD_ORIGINAL =
      AttributeKey.stringKey("http.request.method_original");

  public static final AttributeKey<Long> HTTP_REQUEST_RESEND_COUNT =
      AttributeKey.longKey("http.request.resend_count");

  public static final AttributeKey<Long> HTTP_REQUEST_SIZE =
      AttributeKey.longKey("http.request.size");

  public static final AttributeKey<String> HTTP_RESPONSE_BODY_CONTENT =
      AttributeKey.stringKey("http.response.body.content");

  public static final AttributeKey<Long> HTTP_RESPONSE_BODY_SIZE =
      AttributeKey.longKey("http.response.body.size");

  public static final AttributeKeyTemplate<List<String>> HTTP_RESPONSE_HEADER =
      AttributeKeyTemplate.stringArrayKeyTemplate("http.response.header");

  public static final AttributeKey<Long> HTTP_RESPONSE_SIZE =
      AttributeKey.longKey("http.response.size");

  public static final AttributeKey<Long> HTTP_RESPONSE_STATUS_CODE =
      AttributeKey.longKey("http.response.status_code");

  public static final AttributeKey<String> HTTP_ROUTE =
      AttributeKey.stringKey("http.route");

  public static final AttributeKey<String> NETWORK_LOCAL_ADDRESS =
      AttributeKey.stringKey("network.local.address");

  public static final AttributeKey<Long> NETWORK_LOCAL_PORT =
      AttributeKey.longKey("network.local.port");

  public static final AttributeKey<String> NETWORK_PEER_ADDRESS =
      AttributeKey.stringKey("network.peer.address");

  public static final AttributeKey<Long> NETWORK_PEER_PORT =
      AttributeKey.longKey("network.peer.port");

  public static final AttributeKey<String> NETWORK_PROTOCOL_NAME =
      AttributeKey.stringKey("network.protocol.name");

  public static final AttributeKey<String> NETWORK_PROTOCOL_VERSION =
      AttributeKey.stringKey("network.protocol.version");

  public static final AttributeKey<String> NETWORK_TRANSPORT =
      AttributeKey.stringKey("network.transport");

  public static final AttributeKey<String> SERVER_ADDRESS =
      AttributeKey.stringKey("server.address");

  public static final AttributeKey<Long> SERVER_PORT =
      AttributeKey.longKey("server.port");

  public static final AttributeKey<String> SERVICE_PEER_NAME =
      AttributeKey.stringKey("service.peer.name");

  public static final AttributeKey<String> URL_FULL =
      AttributeKey.stringKey("url.full");

  public static final AttributeKey<String> URL_PATH =
      AttributeKey.stringKey("url.path");

  public static final AttributeKey<String> URL_QUERY =
      AttributeKey.stringKey("url.query");

  public static final AttributeKey<String> URL_SCHEME =
      AttributeKey.stringKey("url.scheme");

  public static final AttributeKey<String> URL_TEMPLATE =
      AttributeKey.stringKey("url.template");

  public static final AttributeKey<String> USER_AGENT_ORIGINAL =
      AttributeKey.stringKey("user_agent.original");

  public static final AttributeKey<String> USER_AGENT_SYNTHETIC_TYPE =
      AttributeKey.stringKey("user_agent.synthetic.type");
}