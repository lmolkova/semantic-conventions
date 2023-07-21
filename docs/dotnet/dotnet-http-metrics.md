# Semantic Conventions for HTTP-relevant metrics emitted by .NET and ASP.NET Core

**Status**: [Experimental][DocumentStatus]

This document defines semantic conventions for HTTP metrics emitted by .NET components and runtime.

**Disclaimer:** These are initial .NET metric instruments available in .NET 8 but more may be added in the future.

<!-- toc -->

- [HTTP client](#http-client)
  * [Metric: `http.client.connections.usage`](#metric-httpclientconnectionsusage)
  * [Metric: `http.client.connection.duration`](#metric-httpclientconnectionduration)
  * [Metric: `http.client.duration`](#metric-httpclientduration)
  * [Metric: `http.client.active_requests`](#metric-httpclientactive_requests)
  * [Metric: `http.client.failed_requests`](#metric-httpclientfailed_requests)
- [HTTP server](#http-server)
  * [Metric: `http.server.duration`](#metric-httpserverduration)
  * [Metric: `http.server.active_requests`](#metric-httpserveractive_requests)
  * [Metric: `http.server.unhandled_requests`](#metric-httpserverunhandled_requests)

<!-- tocstop -->

## HTTP client

All Http client metrics are reported by `System.Net.Http` meter.

### Metric: `http.client.connections.usage`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.dotnet.http.client.connections.usage(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `http.client.connections.usage` | UpDownCounter | `{connection}` | Number of outbound HTTP connections that are currently active or idle on the client [1] |

**[1]:** Corresponding `EventCounter` names are `*-connections-current-total`; Meter name is `System.Net.Http`.
<!-- endsemconv -->

<!-- semconv metric.dotnet.http.client.connections.usage -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `connection.state` | string | Connection state. | `active`; `idle` | Required |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [1] | `3.1.1` | Recommended |
| [`server.address`](../general/attributes.md) | string | Logical server hostname, matches server FQDN if available, and IP or socket address if FQDN is not known. | `example.com` | Recommended |
| [`server.port`](../general/attributes.md) | int | Logical server port number | `80`; `8080`; `443` | Recommended |
| [`server.socket.address`](../general/attributes.md) | string | Physical server IP address or Unix socket address. If set from the client, should simply use the socket's peer address, and not attempt to find any actual server IP (i.e., if set from client, this may represent some proxy server instead of the logical server). | `10.5.3.2` | Recommended: If different than `server.address`. |
| [`url.scheme`](../url/url.md) | string | The [URI scheme](https://www.rfc-editor.org/rfc/rfc3986#section-3.1) component identifying the used protocol. | `https`; `ftp`; `telnet` | Recommended |

**[1]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.

`connection.state` MUST be one of the following:

| Value  | Description |
|---|---|
| `active` | active state. |
| `idle` | idle state. |
<!-- endsemconv -->

### Metric: `http.client.connection.duration`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.dotnet.http.client.connection.duration(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `http.client.connection.duration` | Histogram | `s` | The duration of outbound HTTP connections. [1] |

**[1]:** Meter name is `System.Net.Http`.
<!-- endsemconv -->

<!-- semconv metric.dotnet.http.client.connection.duration -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [1] | `3.1.1` | Recommended |
| [`server.address`](../general/attributes.md) | string | Logical server hostname, matches server FQDN if available, and IP or socket address if FQDN is not known. | `example.com` | Recommended |
| [`server.port`](../general/attributes.md) | int | Logical server port number | `80`; `8080`; `443` | Recommended |
| [`server.socket.address`](../general/attributes.md) | string | Physical server IP address or Unix socket address. If set from the client, should simply use the socket's peer address, and not attempt to find any actual server IP (i.e., if set from client, this may represent some proxy server instead of the logical server). | `10.5.3.2` | Recommended: If different than `server.address`. |
| [`url.scheme`](../url/url.md) | string | The [URI scheme](https://www.rfc-editor.org/rfc/rfc3986#section-3.1) component identifying the used protocol. | `https`; `ftp`; `telnet` | Recommended |

**[1]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.
<!-- endsemconv -->

### Metric: `http.client.duration`

**TODO: any reason not to include server.socket.address?**

**TODO: should we add error status to http semconv ?**  - https://github.com/open-telemetry/opentelemetry-specification/issues/3243

Follows common [http.client.duration](../http/http-metrics.md#metric-httpclientduration)

### Metric: `http.client.active_requests`

**TODO: any reason not to include server.socket.address?**
**TODO: any reason we don't have this metric in generic OTEL semconv?**


**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.dotnet.http.client.active_requests(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `http.client.active_requests` | UpDownCounter | `{request}` | Number of outbound HTTP requests that have failed. [1] |

**[1]:** Corresponding `EventCounter` name is `current-requests`; Meter name is `System.Net.Http`.
<!-- endsemconv -->

<!-- semconv metric.dotnet.http.client.active_requests -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `http.request.method` | string | HTTP request method. [1] | `GET`; `POST`; `HEAD` | Required |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [2] | `3.1.1` | Recommended |
| [`server.address`](../general/attributes.md) | string | Logical server hostname, matches server FQDN if available, and IP or socket address if FQDN is not known. | `example.com` | Recommended |
| [`server.port`](../general/attributes.md) | int | Logical server port number | `80`; `8080`; `443` | Recommended |
| [`server.socket.address`](../general/attributes.md) | string | Physical server IP address or Unix socket address. If set from the client, should simply use the socket's peer address, and not attempt to find any actual server IP (i.e., if set from client, this may represent some proxy server instead of the logical server). | `10.5.3.2` | Recommended: If different than `server.address`. |
| [`url.scheme`](../url/url.md) | string | The [URI scheme](https://www.rfc-editor.org/rfc/rfc3986#section-3.1) component identifying the used protocol. | `https`; `ftp`; `telnet` | Recommended |

**[1]:** HTTP request method value SHOULD be "known" to the instrumentation.
By default, this convention defines "known" methods as the ones listed in [RFC9110](https://www.rfc-editor.org/rfc/rfc9110.html#name-methods)
and the PATCH method defined in [RFC5789](https://www.rfc-editor.org/rfc/rfc5789.html).

If the HTTP request method is not known to instrumentation, it MUST set the `http.request.method` attribute to `_OTHER` and, except if reporting a metric, MUST
set the exact method received in the request line as value of the `http.request.method_original` attribute.

If the HTTP instrumentation could end up converting valid HTTP request methods to `_OTHER`, then it MUST provide a way to override
the list of known HTTP methods. If this override is done via environment variable, then the environment variable MUST be named
OTEL_INSTRUMENTATION_HTTP_KNOWN_METHODS and support a comma-separated list of case-sensitive known HTTP methods
(this list MUST be a full override of the default known method, it is not a list of known methods in addition to the defaults).

HTTP method names are case-sensitive and `http.request.method` attribute value MUST match a known HTTP method name exactly.
Instrumentations for specific web frameworks that consider HTTP methods to be case insensitive, SHOULD populate a canonical equivalent.
Tracing instrumentations that do so, MUST also set `http.request.method_original` to the original value.

**[2]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.
<!-- endsemconv -->

### Metric: `http.client.failed_requests`

**TODO: We still need error code to make it useful and backward compatible. If we add error code, we should also add it to http.client.duration as well and then this one should be deleted.**

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.dotnet.http.client.failed_requests(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `http.client.failed_requests` | Counter | `{request}` | Number of outbound HTTP requests that have failed. [1] |

**[1]:** Corresponding `EventCounter` name is `requests-failed`; Meter name is `System.Net.Http`.
<!-- endsemconv -->

<!-- semconv metric.dotnet.http.client.failed_requests -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `http.request.method` | string | HTTP request method. [1] | `GET`; `POST`; `HEAD` | Required |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [2] | `3.1.1` | Recommended |
| [`server.address`](../general/attributes.md) | string | Logical server hostname, matches server FQDN if available, and IP or socket address if FQDN is not known. | `example.com` | Recommended |
| [`server.port`](../general/attributes.md) | int | Logical server port number | `80`; `8080`; `443` | Recommended |
| [`server.socket.address`](../general/attributes.md) | string | Physical server IP address or Unix socket address. If set from the client, should simply use the socket's peer address, and not attempt to find any actual server IP (i.e., if set from client, this may represent some proxy server instead of the logical server). | `10.5.3.2` | Recommended: If different than `server.address`. |
| [`url.scheme`](../url/url.md) | string | The [URI scheme](https://www.rfc-editor.org/rfc/rfc3986#section-3.1) component identifying the used protocol. | `https`; `ftp`; `telnet` | Recommended |

**[1]:** HTTP request method value SHOULD be "known" to the instrumentation.
By default, this convention defines "known" methods as the ones listed in [RFC9110](https://www.rfc-editor.org/rfc/rfc9110.html#name-methods)
and the PATCH method defined in [RFC5789](https://www.rfc-editor.org/rfc/rfc5789.html).

If the HTTP request method is not known to instrumentation, it MUST set the `http.request.method` attribute to `_OTHER` and, except if reporting a metric, MUST
set the exact method received in the request line as value of the `http.request.method_original` attribute.

If the HTTP instrumentation could end up converting valid HTTP request methods to `_OTHER`, then it MUST provide a way to override
the list of known HTTP methods. If this override is done via environment variable, then the environment variable MUST be named
OTEL_INSTRUMENTATION_HTTP_KNOWN_METHODS and support a comma-separated list of case-sensitive known HTTP methods
(this list MUST be a full override of the default known method, it is not a list of known methods in addition to the defaults).

HTTP method names are case-sensitive and `http.request.method` attribute value MUST match a known HTTP method name exactly.
Instrumentations for specific web frameworks that consider HTTP methods to be case insensitive, SHOULD populate a canonical equivalent.
Tracing instrumentations that do so, MUST also set `http.request.method_original` to the original value.

**[2]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.
<!-- endsemconv -->

## HTTP server

All Http server metrics are reported by `Microsoft.AspNetCore.Hosting ` meter.

### Metric: `http.server.duration`

**TODO: should we add error status to http semconv ?**  - https://github.com/open-telemetry/opentelemetry-specification/issues/3243
**TODO: Opt-in address and port?**
**TODO: method cardinality?**

Follows common [http.server.duration](../http/http-metrics.md#metric-httpserverduration)

Corresponding `EventCounter` (without dimensions) is `http-client-requests-duration`

### Metric: `http.server.active_requests`

**TODO: Opt-in address and port?**
**TODO: method cardinality?**

Follows common [http.server.active_requests](../http/http-metrics.md#metric-httpserveractive_requests)

Corresponding `EventCounter` (without dimensions) is `http-client-current-requests`

### Metric: `http.server.unhandled_requests`

**TODO:Any chance we will need to report any attributes in future?**

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.aspnet.server.unhandled_requests(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `http.server.unhandled_requests` | UpDownCounter | `{request}` | Number of requests that reached the end of the middleware pipeline without being handled by application code. [1] |

**[1]:** Meter name is `Microsoft.AspNetCore.Hosting`.
**TODO Any reason not to put it into aspnet? seems ASP.NET -specific (middleware, app code), not necessarily HTTP?**
<!-- endsemconv -->

<!-- semconv metric.aspnet.server.unhandled_requests -->
<!-- endsemconv -->

[DocumentStatus]: https://github.com/open-telemetry/opentelemetry-specification/tree/v1.22.0/specification/document-status.md