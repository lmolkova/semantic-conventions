package io.opentelemetry.semconv.prototype;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfiguration;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.prototype.http.HttpAttributes;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.semconv.prototype.http.HttpClientActiveRequestsMetric;
import io.opentelemetry.semconv.prototype.http.HttpClientRequestExceptionEvent;
import io.opentelemetry.semconv.prototype.http.HttpClientSpan;
import io.opentelemetry.semconv.prototype.http.HttpServerRequestDurationMetric;
import io.opentelemetry.semconv.prototype.http.HttpServerSpan;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HttpSemconvTest {

  private static final AttributeKey<List<String>> REQUEST_HEADER_FOO =
      AttributeKey.stringArrayKey("http.request.header.x-foo");

  private static DeclarativeConfigProperties config(String yaml) {
    DeclarativeConfigProperties root =
        DeclarativeConfiguration.toConfigProperties(
            new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
    return root.getStructured("instrumentation/development", DeclarativeConfigProperties.empty());
  }

  private static DeclarativeConfigProperties httpServerConfig(String properties) {
    return config(
        "file_format: \"1.0-rc.1\"\n"
            + "instrumentation/development:\n"
            + "  general:\n"
            + "    http:\n"
            + properties);
  }

  @Test
  void unknownMethodIsFilteredForAttributeAndSpanName() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    Tracer tracer = tracer(exporter);
    DeclarativeConfigProperties config =
        httpServerConfig("      server:\n        known_methods:\n          - GET\n");

    String method = HttpServerSpan.filterHttpRequestMethod(config, "POST");
    HttpServerSpan.start(
            tracer, config, method + " /users", "1.2.3.4", "POST", "example.com", 443L, "/users",
            null, "https", "curl/8", name -> List.of())
        .end();

    SpanData data = exporter.getFinishedSpanItems().get(0);
    assertThat(data.getName()).isEqualTo("_OTHER /users");
    assertThat(data.getAttributes().get(HttpAttributes.HTTP_REQUEST_METHOD)).isEqualTo("_OTHER");
  }

  @Test
  void knownMethodIsPreservedByDefault() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    Tracer tracer = tracer(exporter);
    DeclarativeConfigProperties config = httpServerConfig("      server: {}\n");

    String method = HttpServerSpan.filterHttpRequestMethod(config, "POST");
    assertThat(method).isEqualTo("POST");
  }

  @Test
  void headersAreCapturedOnlyWhenConfiguredAndBeforeSpanStarts() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    Tracer tracer = tracer(exporter);
    DeclarativeConfigProperties configured =
        httpServerConfig(
            "      server:\n        request_captured_headers:\n          - X-Foo\n");

    Map<String, List<String>> headers = Map.of("X-Foo", List.of("bar"), "X-Other", List.of("no"));
    HttpServerSpan.start(
            tracer, configured, "GET /users", "1.2.3.4", "GET", "example.com", 443L, "/users", null,
            "https", "curl/8", headers::get)
        .end();

    SpanData data = exporter.getFinishedSpanItems().get(0);
    assertThat(data.getAttributes().get(REQUEST_HEADER_FOO)).containsExactly("bar");
    assertThat(data.getAttributes().asMap().keySet())
        .noneMatch(key -> key.getKey().equals("http.request.header.x-other"));
  }

  @Test
  void headersAreNotCapturedByDefault() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    Tracer tracer = tracer(exporter);
    DeclarativeConfigProperties config = httpServerConfig("      server: {}\n");

    Map<String, List<String>> headers = Map.of("X-Foo", List.of("bar"));
    HttpServerSpan.start(
            tracer, config, "GET /users", "1.2.3.4", "GET", "example.com", 443L, "/users", null,
            "https", "curl/8", headers::get)
        .end();

    assertThat(exporter.getFinishedSpanItems().get(0).getAttributes().get(REQUEST_HEADER_FOO))
        .isNull();
  }

  @Test
  void attributeWithItsOwnToggleIsGatedByThatToggleAlone() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    Tracer tracer = tracer(exporter);

    assertThat(bodyContent(tracer, exporter, httpServerConfig("      server: {}\n"))).isNull();
    assertThat(
            bodyContent(
                tracer,
                exporter,
                httpServerConfig("      server:\n        request_capture_body_content: true\n")))
        .isEqualTo("hello");
  }

  @Test
  void developmentSignalIsOffUntilExperimentalIsSet() {
    DeclarativeConfigProperties off = httpServerConfig("      client: {}\n");
    assertThat(HttpClientRequestExceptionEvent.isEnabled(off)).isFalse();

    assertThat(HttpClientActiveRequestsMetric.isEnabled(off)).isFalse();

    DeclarativeConfigProperties on =
        httpServerConfig("      semconv:\n        experimental: true\n      client: {}\n");
    assertThat(HttpClientRequestExceptionEvent.isEnabled(on)).isTrue();
    assertThat(HttpClientActiveRequestsMetric.isEnabled(on)).isTrue();

    assertThat(HttpServerSpan.isEnabled(off)).isTrue();
  }

  @Test
  void developmentAttributeWithoutItsOwnToggleStillNeedsTheGate() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    Tracer tracer = tracer(exporter);
    AttributeKey<String> urlTemplate = AttributeKey.stringKey("url.template");

    Span off =
        HttpClientSpan.start(
            tracer, httpServerConfig("      client: {}\n"), "GET", "GET", "example.com", 443L,
            "https://example.com/users/1");
    HttpClientSpan.setUrlTemplate(off, "/users/{id}", httpServerConfig("      client: {}\n"));
    off.end();
    assertThat(exporter.getFinishedSpanItems().get(0).getAttributes().get(urlTemplate)).isNull();

    exporter.reset();
    DeclarativeConfigProperties on =
        httpServerConfig("      semconv:\n        experimental: true\n      client: {}\n");
    Span span =
        HttpClientSpan.start(
            tracer, on, "GET", "GET", "example.com", 443L, "https://example.com/users/1");
    HttpClientSpan.setUrlTemplate(span, "/users/{id}", on);
    span.end();
    assertThat(exporter.getFinishedSpanItems().get(0).getAttributes().get(urlTemplate))
        .isEqualTo("/users/{id}");
  }

  @Test
  void activeRequestsMetricIsGatedOnExperimental() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider provider = SdkMeterProvider.builder().registerMetricReader(reader).build();
    LongUpDownCounter counter = HttpClientActiveRequestsMetric.create(provider.get("test"));

    HttpClientActiveRequestsMetric.add(
        counter, httpServerConfig("      client: {}\n"), 1, Attributes.empty());
    assertThat(reader.collectAllMetrics()).isEmpty();

    HttpClientActiveRequestsMetric.add(
        counter,
        httpServerConfig("      semconv:\n        experimental: true\n      client: {}\n"),
        1,
        Attributes.empty());
    assertThat(reader.collectAllMetrics()).singleElement().satisfies(
        metric -> assertThat(metric.getName()).isEqualTo("http.client.active_requests"));
  }

  @Test
  void exceptionEventCarriesTheThrowable() {
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProvider provider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
            .build();
    DeclarativeConfigProperties config =
        httpServerConfig("      semconv:\n        experimental: true\n      client: {}\n");

    HttpClientRequestExceptionEvent.emit(
        provider.get("test"), config, Severity.WARN, new IllegalStateException("boom"));

    LogRecordData record = exporter.getFinishedLogRecordItems().get(0);
    assertThat(record.getEventName()).isEqualTo("http.client.request.exception");
    assertThat(record.getAttributes().get(HttpAttributes.EXCEPTION_TYPE))
        .isEqualTo("java.lang.IllegalStateException");
    assertThat(record.getAttributes().get(HttpAttributes.EXCEPTION_MESSAGE)).isEqualTo("boom");
    assertThat(record.getAttributes().get(HttpAttributes.EXCEPTION_STACKTRACE)).isNotNull();
  }

  @Test
  void bodyContentIsTruncatedToTheConfiguredLimit() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    Tracer tracer = tracer(exporter);

    assertThat(
            bodyContent(
                tracer,
                exporter,
                httpServerConfig(
                    "      server:\n"
                        + "        request_capture_body_content: true\n"
                        + "        request_capture_body_content_max_size: 3\n")))
        .isEqualTo("hel");
  }

  @Test
  void sensitiveQueryParametersAreRedacted() {
    DeclarativeConfigProperties defaults = httpServerConfig("      server: {}\n");
    assertThat(HttpServerSpan.redactUrlQuery(defaults, "sig=secret&q=1"))
        .isEqualTo("sig=REDACTED&q=1");

    DeclarativeConfigProperties overridden =
        config(
            "file_format: \"1.0-rc.1\"\n"
                + "instrumentation/development:\n"
                + "  general:\n"
                + "    sanitization:\n"
                + "      url:\n"
                + "        sensitive_query_parameters:\n"
                + "          - token\n");
    assertThat(HttpServerSpan.redactUrlQuery(overridden, "sig=secret&token=abc"))
        .isEqualTo("sig=secret&token=REDACTED");

    DeclarativeConfigProperties perSignal =
        httpServerConfig(
            "      server:\n"
                + "        sensitive_query_parameters:\n"
                + "          - only-here\n");
    assertThat(HttpServerSpan.redactUrlQuery(perSignal, "sig=secret&only-here=abc"))
        .isEqualTo("sig=secret&only-here=REDACTED");
  }

  @Test
  void servicePeerNameIsRecordedForMatchingServerAddressOnly() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    Tracer tracer = tracer(exporter);
    AttributeKey<String> servicePeerName = AttributeKey.stringKey("service.peer.name");
    DeclarativeConfigProperties config =
        httpServerConfig(
            "      client:\n"
                + "        service_peer_name_mapping:\n"
                + "          - match: example.com\n"
                + "            value: shop\n");

    HttpClientSpan.start(
            tracer, config, "GET", "GET", "example.com", 443L, "https://example.com/users/1")
        .end();
    HttpClientSpan.start(tracer, config, "GET", "GET", "other.com", 443L, "https://other.com/1")
        .end();

    assertThat(exporter.getFinishedSpanItems().get(0).getAttributes().get(servicePeerName))
        .isEqualTo("shop");
    assertThat(exporter.getFinishedSpanItems().get(1).getAttributes().get(servicePeerName)).isNull();
  }

  private static String bodyContent(
      Tracer tracer, InMemorySpanExporter exporter, DeclarativeConfigProperties config) {
    exporter.reset();
    Span span =
        HttpServerSpan.start(
            tracer, config, "GET /users", "1.2.3.4", "GET", "example.com", 443L, "/users", null,
            "https", "curl/8", name -> List.of());
    HttpServerSpan.setHttpRequestBodyContent(span, () -> "hello", config);
    span.end();
    return exporter.getFinishedSpanItems().get(0).getAttributes().get(HttpAttributes.HTTP_REQUEST_BODY_CONTENT);
  }

  private static Tracer tracer(InMemorySpanExporter exporter) {
    return SdkTracerProvider.builder()
        .addSpanProcessor(SimpleSpanProcessor.create(exporter))
        .build()
        .get("test");
  }
}
