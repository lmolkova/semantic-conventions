package io.opentelemetry.semconv.prototype.demo;

import com.sun.net.httpserver.HttpServer;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfiguration;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

/** Demonstrates a live configuration update without recreating the instrumentation. */
public final class Demo {

  private static final String INITIAL_CONFIG =
      """
      file_format: "1.0-rc.1"
      instrumentation/development:
        general:
          http:
            client:
              known_methods: [GET, POST]
              request_captured_headers: [X-Request-Id]
              service_peer_name_mapping:
                - match: localhost
                  value: shopping-cart
      """;

  private static final String UPDATED_CONFIG =
      """
      file_format: "1.0-rc.1"
      instrumentation/development:
        general:
          http:
            semconv:
              experimental: true
            client:
              known_methods: [POST]
              request_captured_headers: [X-Config-Version]
              service_peer_name_mapping:
                - match: localhost
                  value: updated-shopping-cart
      """;

  private Demo() {}

  public static void main(String[] args) throws Exception {
    OpenTelemetrySdk sdk = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();

    HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    server.createContext(
        "/users",
        exchange -> {
          byte[] body = "ok".getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(200, body.length);
          try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
          }
        });
    server.start();

    try {
      ReloadableConfigProvider configProvider =
          new ReloadableConfigProvider(loadConfig(INITIAL_CONFIG));
      HttpClientInstrumentation instrumentation =
          new HttpClientInstrumentation(
              HttpClient.newHttpClient(),
              sdk.getTracer("demo"),
              sdk.getMeter("demo"),
              sdk.getLogsBridge().get("demo"),
              configProvider);

      instrumentation.send(
          HttpRequest.newBuilder(
                  URI.create("http://localhost:" + server.getAddress().getPort() + "/users"))
              .header("X-Request-Id", "abc123")
              .build());

      configProvider.update(loadConfig(UPDATED_CONFIG));

      instrumentation.send(
          HttpRequest.newBuilder(
                  URI.create("http://localhost:" + server.getAddress().getPort() + "/users"))
              .header("X-Request-Id", "not-captured")
              .header("X-Config-Version", "updated")
              .build());

      try {
        instrumentation.send(
            HttpRequest.newBuilder(URI.create("http://localhost:1/unreachable")).build());
      } catch (java.io.IOException expected) {
        System.out.println("Expected request failure: " + expected.getClass().getSimpleName());
      }
    } finally {
      server.stop(0);
      sdk.close();
    }
  }

  /**
   * Temporary bridge until the Java SDK is generated from the schema produced by this prototype.
   * The checked-in merged schema is used for validation only and does not change the configuration
   * model baked into the SDK dependency.
   */
  private static DeclarativeConfigProperties loadConfig(String yaml) {
    return DeclarativeConfiguration.toConfigProperties(
            new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)))
        .getStructured("instrumentation/development", DeclarativeConfigProperties.empty());
  }
}
