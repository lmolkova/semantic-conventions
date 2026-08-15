package io.opentelemetry.semconv.prototype.demo;

import com.sun.net.httpserver.HttpServer;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfiguration;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

/** Sends one HTTP request through {@link HttpClientInstrumentation} with `demo-config.yaml`. */
public final class Demo {

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
      HttpClientInstrumentation instrumentation =
          new HttpClientInstrumentation(
              HttpClient.newHttpClient(),
              sdk.getTracer("demo"),
              sdk.getMeter("demo"),
              sdk.getLogsBridge().get("demo"),
              instrumentationConfig());

      instrumentation.send(
          HttpRequest.newBuilder(
                  URI.create("http://localhost:" + server.getAddress().getPort() + "/users"))
              .header("X-Request-Id", "abc123")
              .build());
    } finally {
      server.stop(0);
    }
  }

  /**
   * The instrumentation reads the `.instrumentation/development` subtree. It is loaded as untyped
   * properties because the SDK's model of that subtree does not know the properties this prototype
   * adds - those exist only once the generated schema lands in opentelemetry-configuration.
   */
  private static DeclarativeConfigProperties instrumentationConfig() throws Exception {
    try (InputStream yaml = Demo.class.getResourceAsStream("/demo-config.yaml")) {
      return DeclarativeConfiguration.toConfigProperties(yaml)
          .getStructured("instrumentation/development", DeclarativeConfigProperties.empty());
    }
  }
}
