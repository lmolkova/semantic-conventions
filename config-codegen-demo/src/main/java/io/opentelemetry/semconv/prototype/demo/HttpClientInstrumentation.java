package io.opentelemetry.semconv.prototype.demo;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.semconv.prototype.http.HttpAttributes;
import io.opentelemetry.semconv.prototype.http.HttpClientActiveRequestsMetric;
import io.opentelemetry.semconv.prototype.http.HttpClientRequestDurationMetric;
import io.opentelemetry.semconv.prototype.http.HttpClientRequestExceptionEvent;
import io.opentelemetry.semconv.prototype.http.HttpClientSpan;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Instruments a JDK {@link HttpClient} with the generated helpers.
 *
 * <p>{@code config} is the {@code .instrumentation/development} subtree of a declarative
 * configuration file - see `demo-config.yaml` and {@link Demo}.
 */
public final class HttpClientInstrumentation {

  private final HttpClient client;
  private final Tracer tracer;
  private final Logger logger;
  private final DeclarativeConfigProperties config;
  private final DoubleHistogram duration;
  private final LongUpDownCounter activeRequests;

  public HttpClientInstrumentation(
      HttpClient client,
      Tracer tracer,
      Meter meter,
      Logger logger,
      DeclarativeConfigProperties config) {
    this.client = client;
    this.tracer = tracer;
    this.logger = logger;
    this.config = config;
    this.duration = HttpClientRequestDurationMetric.create(meter);
    this.activeRequests = HttpClientActiveRequestsMetric.create(meter);
  }

  public HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
    return send(request, null);
  }

  /** {@code urlTemplate} is the low cardinality route the application used, when it knows it. */
  public HttpResponse<String> send(HttpRequest request, String urlTemplate)
      throws IOException, InterruptedException {
    String method = HttpClientSpan.filterHttpRequestMethod(config, request.method());

    Span span =
        HttpClientSpan.start(
            tracer,
            config,
            method,
            request.method(),
            request.uri().getHost(),
            port(request),
            request.uri().toString());

    // `start` also records `service.peer.name` for the `server.address` it was given, when
    // `service_peer_name_mapping` configures one.

    AttributesBuilder requestAttributes = Attributes.builder();
    HttpClientSpan.populateRequestCapturedHeaders(
        requestAttributes, name -> request.headers().allValues(name), config);
    span.setAllAttributes(requestAttributes.build());

    HttpClientSpan.setUrlTemplate(span, urlTemplate, config);

    AttributesBuilder metricAttributes = Attributes.builder();
    metricAttributes.put(HttpAttributes.HTTP_REQUEST_METHOD, method);
    metricAttributes.put(HttpAttributes.SERVER_ADDRESS, request.uri().getHost());

    Attributes inFlight =
        Attributes.builder()
            .put(HttpAttributes.HTTP_REQUEST_METHOD, method)
            .put(HttpAttributes.SERVER_ADDRESS, request.uri().getHost())
            .put(HttpAttributes.URL_SCHEME, request.uri().getScheme())
            .build();
    HttpClientActiveRequestsMetric.add(activeRequests, config, 1, inFlight);

    long startNanos = System.nanoTime();
    try (Scope ignored = span.makeCurrent()) {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      AttributesBuilder responseAttributes = Attributes.builder();
      HttpClientSpan.populateResponseCapturedHeaders(
          responseAttributes, name -> response.headers().allValues(name), config);
      span.setAllAttributes(responseAttributes.build());
      HttpClientSpan.setHttpResponseBodyContent(span, response::body, config);

      span.setAttribute(HttpAttributes.HTTP_RESPONSE_STATUS_CODE, (long) response.statusCode());
      metricAttributes.put(HttpAttributes.HTTP_RESPONSE_STATUS_CODE, (long) response.statusCode());
      return response;
    } catch (IOException | InterruptedException | RuntimeException e) {
      span.setAttribute(HttpAttributes.ERROR_TYPE, e.getClass().getName());
      metricAttributes.put(HttpAttributes.ERROR_TYPE, e.getClass().getName());
      HttpClientRequestExceptionEvent.emit(logger, config, Severity.WARN, e);
      throw e;
    } finally {
      HttpClientActiveRequestsMetric.add(activeRequests, config, -1, inFlight);
      span.end();
      HttpClientRequestDurationMetric.record(
          duration,
          config,
          (System.nanoTime() - startNanos) / 1_000_000_000.0,
          metricAttributes.build());
    }
  }

  private static Long port(HttpRequest request) {
    int port = request.uri().getPort();
    return port < 0 ? null : (long) port;
  }
}
