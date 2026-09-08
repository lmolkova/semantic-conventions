package io.opentelemetry.semconv.prototype.demo;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.semconv.prototype.http.HttpAttributes;
import io.opentelemetry.semconv.prototype.http.HttpClientActiveRequestsMetric;
import io.opentelemetry.semconv.prototype.http.HttpClientRequestDurationMetric;
import io.opentelemetry.semconv.prototype.http.HttpClientRequestExceptionEvent;
import io.opentelemetry.semconv.prototype.http.HttpClientSpan;
import io.opentelemetry.semconv.prototype.http.HttpClientTracer;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Instruments a JDK {@link HttpClient} with the generated helpers.
 *
 * <p>The generated helpers resolve the relevant {@link ConfigProvider} properties in this class's
 * constructor.
 */
public final class HttpClientInstrumentation {

  private final HttpClient client;
  private final HttpClientTracer clientTracer;
  private final HttpClientRequestDurationMetric duration;
  private final HttpClientActiveRequestsMetric activeRequests;
  private final HttpClientRequestExceptionEvent requestException;

  public HttpClientInstrumentation(
      HttpClient client,
      Tracer tracer,
      Meter meter,
      Logger logger,
      ConfigProvider configProvider) {
    this.client = client;
    this.clientTracer = HttpClientTracer.create(tracer, configProvider);
    this.duration = HttpClientRequestDurationMetric.create(meter, configProvider);
    this.activeRequests = HttpClientActiveRequestsMetric.create(meter, configProvider);
    this.requestException = HttpClientRequestExceptionEvent.create(logger, configProvider);
  }

  public HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
    return send(request, null);
  }

  /** {@code urlTemplate} is the low cardinality route the application used, when it knows it. */
  public HttpResponse<String> send(HttpRequest request, String urlTemplate)
      throws IOException, InterruptedException {
    String method = clientTracer.filterHttpRequestMethod(request.method());

    HttpClientSpan span =
        clientTracer.start(
            method,
            request.method(),
            request.uri().getHost(),
            port(request),
            request.uri().toString());

    // `start` also records `service.peer.name` for the `server.address` it was given, when
    // `service_peer_name_mapping` configures one.

    span.setRequestCapturedHeaders(name -> request.headers().allValues(name));

    span.setUrlTemplate(urlTemplate);

    AttributesBuilder metricAttributes = Attributes.builder();
    metricAttributes.put(HttpAttributes.HTTP_REQUEST_METHOD, method);
    metricAttributes.put(HttpAttributes.SERVER_ADDRESS, request.uri().getHost());

    Attributes inFlight =
        Attributes.builder()
            .put(HttpAttributes.HTTP_REQUEST_METHOD, method)
            .put(HttpAttributes.SERVER_ADDRESS, request.uri().getHost())
            .put(HttpAttributes.URL_SCHEME, request.uri().getScheme())
            .build();
    activeRequests.add(1, inFlight);

    long startNanos = System.nanoTime();
    try (Scope ignored = span.makeCurrent()) {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      span.setResponseCapturedHeaders(name -> response.headers().allValues(name));
      span.setHttpResponseBodyContent(response::body);

      span.setAttribute(HttpAttributes.HTTP_RESPONSE_STATUS_CODE, (long) response.statusCode());
      metricAttributes.put(HttpAttributes.HTTP_RESPONSE_STATUS_CODE, (long) response.statusCode());
      return response;
    } catch (IOException | InterruptedException | RuntimeException e) {
      span.setAttribute(HttpAttributes.ERROR_TYPE, e.getClass().getName());
      metricAttributes.put(HttpAttributes.ERROR_TYPE, e.getClass().getName());
      requestException.emit(Severity.WARN, e);
      throw e;
    } finally {
      activeRequests.add(-1, inFlight);
      span.end();
      duration.record(
          (System.nanoTime() - startNanos) / 1_000_000_000.0,
          metricAttributes.build());
    }
  }

  private static Long port(HttpRequest request) {
    int port = request.uri().getPort();
    return port < 0 ? null : (long) port;
  }
}
