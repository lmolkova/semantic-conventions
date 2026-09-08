package io.opentelemetry.semconv.prototype;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanBuilder;
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
import io.opentelemetry.semconv.prototype.config.DynamicConfigProvider;
import io.opentelemetry.semconv.prototype.db.DbAttributes;
import io.opentelemetry.semconv.prototype.db.DbClientOperationDurationMetric;
import io.opentelemetry.semconv.prototype.http.HttpAttributes;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.semconv.prototype.http.HttpClientActiveRequestsMetric;
import io.opentelemetry.semconv.prototype.http.HttpClientRequestExceptionEvent;
import io.opentelemetry.semconv.prototype.http.HttpClientSpan;
import io.opentelemetry.semconv.prototype.http.HttpClientTracer;
import io.opentelemetry.semconv.prototype.http.HttpServerRequestDurationMetric;
import io.opentelemetry.semconv.prototype.http.HttpServerSpan;
import io.opentelemetry.semconv.prototype.http.HttpServerTracer;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class HttpSemconvTest {

  private static final AttributeKey<List<String>> REQUEST_HEADER_FOO =
      AttributeKey.stringArrayKey("http.request.header.x-foo");
  private static final AttributeKey<List<String>> RESPONSE_HEADER_FOO =
      AttributeKey.stringArrayKey("http.response.header.x-foo");
  private static final AttributeKey<List<String>> RESPONSE_HEADER_BAR =
      AttributeKey.stringArrayKey("http.response.header.x-bar");

  private static ConfigProvider config(String yaml) {
    return () -> configProperties(yaml);
  }

  private static DeclarativeConfigProperties configProperties(String yaml) {
    DeclarativeConfigProperties root =
        DeclarativeConfiguration.toConfigProperties(
            new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
    return root.getStructured(
        "instrumentation/development", DeclarativeConfigProperties.empty());
  }

  private static ConfigProvider httpServerConfig(String properties) {
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
    ConfigProvider config =
        httpServerConfig("      server:\n        known_methods:\n          - GET\n");
    HttpServerTracer serverSpan = HttpServerTracer.create(tracer, config);

    String method = serverSpan.filterHttpRequestMethod("POST");
    serverSpan.start(
            method + " /users", "1.2.3.4", "POST", "example.com", 443L, "/users",
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
    ConfigProvider config = httpServerConfig("      server: {}\n");
    HttpServerTracer serverSpan = HttpServerTracer.create(tracer, config);

    String method = serverSpan.filterHttpRequestMethod("POST");
    assertThat(method).isEqualTo("POST");
  }

  @Test
  void configurationIsResolvedAtInitialization() {
    ConfigProvider initial =
        httpServerConfig("      server:\n        known_methods:\n          - GET\n");
    AtomicReference<DeclarativeConfigProperties> current =
        new AtomicReference<>(initial.getInstrumentationConfig());
    HttpServerTracer serverSpan =
        HttpServerTracer.create(SdkTracerProvider.builder().build().get("test"), current::get);

    current.set(
        httpServerConfig("      server:\n        known_methods:\n          - POST\n")
            .getInstrumentationConfig());

    assertThat(serverSpan.filterHttpRequestMethod("POST")).isEqualTo("_OTHER");
  }

  @Test
  void dynamicConfigUpdatesSpansMetricsAndEvents() {
    InMemorySpanExporter spanExporter = InMemorySpanExporter.create();
    InMemoryMetricReader metricReader = InMemoryMetricReader.create();
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(metricReader).build();
    InMemoryLogRecordExporter logExporter = InMemoryLogRecordExporter.create();
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(logExporter))
            .build();
    MutableConfigProvider config =
        new MutableConfigProvider(
            httpServerConfig(
                    "      server:\n"
                        + "        known_methods:\n"
                        + "          - GET\n"
                        + "      client: {}\n")
                .getInstrumentationConfig());
    HttpServerTracer serverTracer =
        HttpServerTracer.create(tracer(spanExporter), config);
    HttpClientActiveRequestsMetric metric =
        HttpClientActiveRequestsMetric.create(meterProvider.get("test"), config);
    HttpClientRequestExceptionEvent event =
        HttpClientRequestExceptionEvent.create(loggerProvider.get("test"), config);

    assertThat(serverTracer.filterHttpRequestMethod("POST")).isEqualTo("_OTHER");
    assertThat(metric.isEnabled()).isFalse();
    assertThat(event.isEnabled()).isFalse();
    metric.add(1, null, null, null, null, null);
    event.emit(Severity.WARN, new IllegalStateException("before"));
    assertThat(metricReader.collectAllMetrics()).isEmpty();
    assertThat(logExporter.getFinishedLogRecordItems()).isEmpty();

    config.set(
        httpServerConfig(
                "      semconv:\n"
                    + "        experimental: true\n"
                    + "      server:\n"
                    + "        known_methods:\n"
                    + "          - POST\n"
                    + "      client: {}\n")
            .getInstrumentationConfig());

    assertThat(serverTracer.filterHttpRequestMethod("POST")).isEqualTo("POST");
    assertThat(metric.isEnabled()).isTrue();
    assertThat(event.isEnabled()).isTrue();
    serverTracer.start(
            "POST /users", "1.2.3.4", "POST", "example.com", 443L, "/users",
            null, "https", "curl/8", name -> List.of())
        .end();
    metric.add(1, null, null, null, null, null);
    event.emit(Severity.WARN, new IllegalStateException("after"));

    assertThat(spanExporter.getFinishedSpanItems().get(0).getAttributes()
            .get(HttpAttributes.HTTP_REQUEST_METHOD))
        .isEqualTo("POST");
    assertThat(metricReader.collectAllMetrics()).hasSize(1);
    assertThat(logExporter.getFinishedLogRecordItems()).hasSize(1);
  }

  @Test
  void dynamicConfigFiltersMetricAttributes() {
    InMemoryMetricReader metricReader = InMemoryMetricReader.create();
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(metricReader).build();
    MutableConfigProvider config =
        new MutableConfigProvider(
            configProperties(
                "file_format: \"1.0-rc.1\"\n"
                    + "instrumentation/development:\n"
                    + "  general:\n"
                    + "    db:\n"
                    + "      client:\n"
                    + "        metric:\n"
                    + "          query_text: false\n"));
    DbClientOperationDurationMetric metric =
        DbClientOperationDurationMetric.create(meterProvider.get("test"), config);
    recordDbDuration(metric, "SELECT * FROM users");
    assertThat(metricReader.collectAllMetrics())
        .singleElement()
        .satisfies(
            data ->
                assertThat(data.getHistogramData().getPoints())
                    .singleElement()
                    .satisfies(
                        point ->
                            assertThat(point.getAttributes().get(DbAttributes.DB_QUERY_TEXT))
                                .isNull()));

    config.set(
        configProperties(
            "file_format: \"1.0-rc.1\"\n"
                + "instrumentation/development:\n"
                + "  general:\n"
                + "    db:\n"
                + "      client:\n"
                + "        metric:\n"
                + "          query_text: true\n"));
    recordDbDuration(metric, "SELECT * FROM users");

    assertThat(metricReader.collectAllMetrics())
        .singleElement()
        .satisfies(
            data ->
                assertThat(data.getHistogramData().getPoints())
                    .anySatisfy(
                        point ->
                            assertThat(point.getAttributes().get(DbAttributes.DB_QUERY_TEXT))
                                .isEqualTo("SELECT * FROM users")));
  }

  @Test
  void inFlightSpanKeepsItsConfigSnapshot() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    MutableConfigProvider config =
        new MutableConfigProvider(
            httpServerConfig(
                    "      client:\n"
                        + "        response_captured_headers:\n"
                        + "          - X-Foo\n")
                .getInstrumentationConfig());
    HttpClientTracer clientTracer = HttpClientTracer.create(tracer(exporter), config);
    HttpClientSpan oldSpan =
        clientTracer.start("GET", "GET", "example.com", 443L, "https://example.com/one");

    config.set(
        httpServerConfig(
                "      client:\n"
                    + "        response_captured_headers:\n"
                    + "          - X-Bar\n")
            .getInstrumentationConfig());

    Map<String, List<String>> headers =
        Map.of("X-Foo", List.of("old"), "X-Bar", List.of("new"));
    oldSpan.setResponseCapturedHeaders(headers::get);
    oldSpan.end();
    HttpClientSpan newSpan =
        clientTracer.start("GET", "GET", "example.com", 443L, "https://example.com/two");
    newSpan.setResponseCapturedHeaders(headers::get);
    newSpan.end();

    assertThat(exporter.getFinishedSpanItems().get(0).getAttributes().get(RESPONSE_HEADER_FOO))
        .containsExactly("old");
    assertThat(exporter.getFinishedSpanItems().get(0).getAttributes().get(RESPONSE_HEADER_BAR))
        .isNull();
    assertThat(exporter.getFinishedSpanItems().get(1).getAttributes().get(RESPONSE_HEADER_FOO))
        .isNull();
    assertThat(exporter.getFinishedSpanItems().get(1).getAttributes().get(RESPONSE_HEADER_BAR))
        .containsExactly("new");
  }

  @Test
  void tracerEnablementIsCheckedOnEachStart() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    Tracer delegate = tracer(exporter);
    AtomicBoolean enabled = new AtomicBoolean(true);
    Tracer dynamicTracer =
        new Tracer() {
          @Override
          public boolean isEnabled() {
            return enabled.get();
          }

          @Override
          public SpanBuilder spanBuilder(String spanName) {
            return delegate.spanBuilder(spanName);
          }
        };
    HttpClientTracer clientTracer =
        HttpClientTracer.create(
            dynamicTracer, httpServerConfig("      client: {}\n"));

    clientTracer.start("GET", "GET", "example.com", 443L, "https://example.com/one").end();
    enabled.set(false);
    HttpClientSpan disabled =
        clientTracer.start("GET", "GET", "example.com", 443L, "https://example.com/two");
    HttpClientSpan alsoDisabled =
        clientTracer.start("GET", "GET", "example.com", 443L, "https://example.com/three");
    disabled.end();

    assertThat(clientTracer.isEnabled()).isFalse();
    assertThat(alsoDisabled).isSameAs(disabled);
    assertThat(exporter.getFinishedSpanItems()).hasSize(1);
  }

  @Test
  void headersAreCapturedOnlyWhenConfiguredAndBeforeSpanStarts() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    Tracer tracer = tracer(exporter);
    ConfigProvider configured =
        httpServerConfig(
            "      server:\n        request_captured_headers:\n          - X-Foo\n");
    HttpServerTracer serverSpan = HttpServerTracer.create(tracer, configured);

    Map<String, List<String>> headers = Map.of("X-Foo", List.of("bar"), "X-Other", List.of("no"));
    serverSpan.start(
            "GET /users", "1.2.3.4", "GET", "example.com", 443L, "/users", null,
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
    ConfigProvider config = httpServerConfig("      server: {}\n");
    HttpServerTracer serverSpan = HttpServerTracer.create(tracer, config);

    Map<String, List<String>> headers = Map.of("X-Foo", List.of("bar"));
    serverSpan.start(
            "GET /users", "1.2.3.4", "GET", "example.com", 443L, "/users", null,
            "https", "curl/8", headers::get)
        .end();

    assertThat(exporter.getFinishedSpanItems().get(0).getAttributes().get(REQUEST_HEADER_FOO))
        .isNull();
  }

  @Test
  void responseHeadersAreSetAfterSpanStarts() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    HttpClientTracer clientTracer =
        HttpClientTracer.create(
            tracer(exporter),
            httpServerConfig(
                "      client:\n        response_captured_headers:\n          - X-Foo\n"));
    HttpClientSpan span =
        clientTracer.start("GET", "GET", "example.com", 443L, "https://example.com/users");

    span.setResponseCapturedHeaders(name -> List.of("bar"));
    span.end();

    assertThat(exporter.getFinishedSpanItems().get(0).getAttributes().get(RESPONSE_HEADER_FOO))
        .containsExactly("bar");
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
    ConfigProvider off = httpServerConfig("      client: {}\n");
    SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder().build();
    SdkMeterProvider meterProvider = SdkMeterProvider.builder().build();
    assertThat(
            HttpClientRequestExceptionEvent.create(loggerProvider.get("test"), off).isEnabled())
        .isFalse();

    assertThat(HttpClientActiveRequestsMetric.create(meterProvider.get("test"), off).isEnabled())
        .isFalse();

    ConfigProvider on =
        httpServerConfig("      semconv:\n        experimental: true\n      client: {}\n");
    assertThat(
            HttpClientRequestExceptionEvent.create(loggerProvider.get("test"), on).isEnabled())
        .isTrue();
    assertThat(HttpClientActiveRequestsMetric.create(meterProvider.get("test"), on).isEnabled())
        .isTrue();

    assertThat(HttpServerTracer.create(SdkTracerProvider.builder().build().get("test"), off).isEnabled())
        .isTrue();
  }

  @Test
  void developmentAttributeWithoutItsOwnToggleStillNeedsTheGate() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    Tracer tracer = tracer(exporter);
    AttributeKey<String> urlTemplate = AttributeKey.stringKey("url.template");

    HttpClientTracer offConfig =
        HttpClientTracer.create(tracer, httpServerConfig("      client: {}\n"));
    HttpClientSpan off =
        offConfig.start("GET", "GET", "example.com", 443L, "https://example.com/users/1");
    off.setUrlTemplate("/users/{id}");
    off.end();
    assertThat(exporter.getFinishedSpanItems().get(0).getAttributes().get(urlTemplate)).isNull();

    exporter.reset();
    ConfigProvider on =
        httpServerConfig("      semconv:\n        experimental: true\n      client: {}\n");
    HttpClientTracer onConfig = HttpClientTracer.create(tracer, on);
    HttpClientSpan span =
        onConfig.start("GET", "GET", "example.com", 443L, "https://example.com/users/1");
    span.setUrlTemplate("/users/{id}");
    span.end();
    assertThat(exporter.getFinishedSpanItems().get(0).getAttributes().get(urlTemplate))
        .isEqualTo("/users/{id}");
  }

  @Test
  void activeRequestsMetricIsGatedOnExperimental() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider provider = SdkMeterProvider.builder().registerMetricReader(reader).build();
    HttpClientActiveRequestsMetric off =
        HttpClientActiveRequestsMetric.create(
            provider.get("test"), httpServerConfig("      client: {}\n"));

    off.add(1, null, null, null, null, null);
    assertThat(reader.collectAllMetrics()).isEmpty();

    HttpClientActiveRequestsMetric on =
        HttpClientActiveRequestsMetric.create(
            provider.get("test"),
            httpServerConfig("      semconv:\n        experimental: true\n      client: {}\n"));
    on.add(1, null, null, null, null, null);
    assertThat(reader.collectAllMetrics())
        .singleElement()
        .satisfies(metric -> assertThat(metric.getName()).isEqualTo("http.client.active_requests"));
  }

  @Test
  void exceptionEventCarriesTheThrowable() {
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProvider provider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
            .build();
    ConfigProvider config =
        httpServerConfig("      semconv:\n        experimental: true\n      client: {}\n");

    HttpClientRequestExceptionEvent event =
        HttpClientRequestExceptionEvent.create(provider.get("test"), config);
    event.emit(Severity.WARN, new IllegalStateException("boom"));

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
    Tracer tracer = SdkTracerProvider.builder().build().get("test");
    HttpServerTracer defaults =
        HttpServerTracer.create(tracer, httpServerConfig("      server: {}\n"));
    assertThat(defaults.redactUrlQuery("sig=secret&q=1"))
        .isEqualTo("sig=REDACTED&q=1");

    HttpServerTracer overridden =
        HttpServerTracer.create(
            tracer,
            config(
                "file_format: \"1.0-rc.1\"\n"
                    + "instrumentation/development:\n"
                    + "  general:\n"
                    + "    sanitization:\n"
                    + "      url:\n"
                    + "        sensitive_query_parameters:\n"
                    + "          - token\n"));
    assertThat(overridden.redactUrlQuery("sig=secret&token=abc"))
        .isEqualTo("sig=secret&token=REDACTED");

    HttpServerTracer perSignal =
        HttpServerTracer.create(
            tracer,
            httpServerConfig(
                "      server:\n"
                    + "        sensitive_query_parameters:\n"
                    + "          - only-here\n"));
    assertThat(perSignal.redactUrlQuery("sig=secret&only-here=abc"))
        .isEqualTo("sig=secret&only-here=REDACTED");
  }

  @Test
  void servicePeerNameIsRecordedForMatchingServerAddressOnly() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    Tracer tracer = tracer(exporter);
    AttributeKey<String> servicePeerName = AttributeKey.stringKey("service.peer.name");
    ConfigProvider config =
        httpServerConfig(
            "      client:\n"
                + "        service_peer_name_mapping:\n"
                + "          - match: example.com\n"
                + "            value: shop\n");

    HttpClientTracer clientSpan = HttpClientTracer.create(tracer, config);
    clientSpan.start("GET", "GET", "example.com", 443L, "https://example.com/users/1")
        .end();
    clientSpan.start("GET", "GET", "other.com", 443L, "https://other.com/1")
        .end();

    assertThat(exporter.getFinishedSpanItems().get(0).getAttributes().get(servicePeerName))
        .isEqualTo("shop");
    assertThat(exporter.getFinishedSpanItems().get(1).getAttributes().get(servicePeerName)).isNull();
  }

  private static String bodyContent(
      Tracer tracer, InMemorySpanExporter exporter, ConfigProvider config) {
    exporter.reset();
    HttpServerTracer serverSpan = HttpServerTracer.create(tracer, config);
    HttpServerSpan span =
        serverSpan.start(
            "GET /users", "1.2.3.4", "GET", "example.com", 443L, "/users", null,
            "https", "curl/8", name -> List.of());
    span.setHttpRequestBodyContent(() -> "hello");
    span.end();
    return exporter.getFinishedSpanItems().get(0).getAttributes().get(HttpAttributes.HTTP_REQUEST_BODY_CONTENT);
  }

  private static void recordDbDuration(
      DbClientOperationDurationMetric metric, String queryText) {
    metric.record(
        1,
        null,
        null,
        null,
        null,
        queryText,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null);
  }

  private static Tracer tracer(InMemorySpanExporter exporter) {
    return SdkTracerProvider.builder()
        .addSpanProcessor(SimpleSpanProcessor.create(exporter))
        .build()
        .get("test");
  }

  private static final class MutableConfigProvider implements DynamicConfigProvider {

    private volatile DeclarativeConfigProperties config;
    private final List<Consumer<DeclarativeConfigProperties>> listeners =
        new CopyOnWriteArrayList<>();

    private MutableConfigProvider(DeclarativeConfigProperties config) {
      this.config = config;
    }

    @Override
    public DeclarativeConfigProperties getInstrumentationConfig() {
      return config;
    }

    @Override
    public void addInstrumentationConfigListener(
        Consumer<DeclarativeConfigProperties> listener) {
      listeners.add(listener);
    }

    private void set(DeclarativeConfigProperties config) {
      this.config = config;
      listeners.forEach(listener -> listener.accept(config));
    }
  }
}
