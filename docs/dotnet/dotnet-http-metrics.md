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

<!-- semconv metric.dotnet.http.client.connections.usage(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `dotnet.connection.state` | string | Connection state. | `active`; `idle` | Required |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [1] | `3.1.1` | Recommended |
| [`server.address`](../general/attributes.md) | string | Logical server hostname, matches server FQDN if available, and IP or socket address if FQDN is not known. | `example.com` | Recommended |
| [`server.port`](../general/attributes.md) | int | Logical server port number | `80`; `8080`; `443` | Recommended |
| [`server.socket.address`](../general/attributes.md) | string | Physical server IP address or Unix socket address. If set from the client, should simply use the socket's peer address, and not attempt to find any actual server IP (i.e., if set from client, this may represent some proxy server instead of the logical server). | `10.5.3.2` | Recommended: If different than `server.address`. |
| [`url.scheme`](../url/url.md) | string | The [URI scheme](https://www.rfc-editor.org/rfc/rfc3986#section-3.1) component identifying the used protocol. | `https`; `ftp`; `telnet` | Recommended |

**[1]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.

`dotnet.connection.state` MUST be one of the following:

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

<!-- semconv metric.dotnet.http.client.connection.duration(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `dotnet.error.code` | string | General-purpose error code reported by .NET, as a starter it supports terminal statuses of .NET task https://learn.microsoft.com/en-us/dotnet/api/system.threading.tasks.taskstatus. | `Canceled`; `RanToCompletion` | Recommended |
| `dotnet.http.request.error` | string | HTTP Request error as defined inhttps://github.com/dotnet/runtime/blob/c430570a01c103bc7f117be573f37d8ce8a129b8/src/libraries/System.Net.Http/src/System/Net/Http/HttpRequestError.cs | `extended_connection_not_supported`; `http_protocol` | Recommended |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [1] | `3.1.1` | Recommended |
| [`server.address`](../general/attributes.md) | string | Logical server hostname, matches server FQDN if available, and IP or socket address if FQDN is not known. | `example.com` | Recommended |
| [`server.port`](../general/attributes.md) | int | Logical server port number | `80`; `8080`; `443` | Recommended |
| [`server.socket.address`](../general/attributes.md) | string | Physical server IP address or Unix socket address. If set from the client, should simply use the socket's peer address, and not attempt to find any actual server IP (i.e., if set from client, this may represent some proxy server instead of the logical server). | `10.5.3.2` | Recommended: If different than `server.address`. |
| [`url.scheme`](../url/url.md) | string | The [URI scheme](https://www.rfc-editor.org/rfc/rfc3986#section-3.1) component identifying the used protocol. | `https`; `ftp`; `telnet` | Recommended |

**[1]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.

`dotnet.error.code` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `RanToCompletion` | No error |
| `Canceled` | Canceled |
| `Faulted` | Faulted [1] |

**[1]:** When error code is set to `other`, it's recommended accompany this attribute with a domain-specific error code when it's known, such as `http.request.error` or `http.response.status_code` for HTTP errors.

`dotnet.http.request.error` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `Unknown` | Unknown error |
| `NameResolutionError` | NameResolutionError |
| `ConnectionError` | ConnectionError |
| `SecureConnectionError` | SecureConnectionError |
| `HttpProtocolError` | HttpProtocolError |
| `ExtendedConnectNotSupported` | ExtendedConnectNotSupported |
<!-- endsemconv -->

### Metric: `http.client.duration`

Follows common [http.client.duration](../http/http-metrics.md#metric-httpclientduration), but adds non-code errors

<!-- semconv metric.dotnet.http.client.duration(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `http.client.duration` | Histogram | `s` | Measures the duration of outbound HTTP requests. |
<!-- endsemconv -->

<!-- semconv metric.dotnet.http.client.duration(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `dotnet.error.code` | string | General-purpose error code reported by .NET, as a starter it supports terminal statuses of .NET task https://learn.microsoft.com/en-us/dotnet/api/system.threading.tasks.taskstatus. | `Canceled`; `RanToCompletion` | Recommended |
| `dotnet.http.request.error` | string | HTTP Request error as defined inhttps://github.com/dotnet/runtime/blob/c430570a01c103bc7f117be573f37d8ce8a129b8/src/libraries/System.Net.Http/src/System/Net/Http/HttpRequestError.cs | `extended_connection_not_supported`; `http_protocol` | Recommended |
| `http.request.method` | string | HTTP request method. [1] | `GET`; `POST`; `HEAD` | Required |
| `http.response.status_code` | int | [HTTP response status code](https://tools.ietf.org/html/rfc7231#section-6). | `200` | Conditionally Required: If and only if one was received/sent. |
| [`network.protocol.name`](../general/attributes.md) | string | [OSI Application Layer](https://osi-model.com/application-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `amqp`; `http`; `mqtt` | Recommended |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [2] | `3.1.1` | Recommended |
| [`server.address`](../general/attributes.md) | string | Host identifier of the ["URI origin"](https://www.rfc-editor.org/rfc/rfc9110.html#name-uri-origin) HTTP request is sent to. [3] | `example.com` | Required |
| [`server.port`](../general/attributes.md) | int | Port identifier of the ["URI origin"](https://www.rfc-editor.org/rfc/rfc9110.html#name-uri-origin) HTTP request is sent to. [4] | `80`; `8080`; `443` | Conditionally Required: [5] |
| [`server.socket.address`](../general/attributes.md) | string | Physical server IP address or Unix socket address. If set from the client, should simply use the socket's peer address, and not attempt to find any actual server IP (i.e., if set from client, this may represent some proxy server instead of the logical server). | `10.5.3.2` | Recommended: If different than `server.address`. |

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

**[3]:** Determined by using the first of the following that applies

- Host identifier of the [request target](https://www.rfc-editor.org/rfc/rfc9110.html#target.resource)
  if it's sent in absolute-form
- Host identifier of the `Host` header

SHOULD NOT be set if capturing it would require an extra DNS lookup.

**[4]:** When [request target](https://www.rfc-editor.org/rfc/rfc9110.html#target.resource) is absolute URI, `server.port` MUST match URI port identifier, otherwise it MUST match `Host` header port identifier.

**[5]:** If not default (`80` for `http` scheme, `443` for `https`).

`dotnet.error.code` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `RanToCompletion` | No error |
| `Canceled` | Canceled |
| `Faulted` | Faulted [1] |

**[1]:** When error code is set to `other`, it's recommended accompany this attribute with a domain-specific error code when it's known, such as `http.request.error` or `http.response.status_code` for HTTP errors.

`dotnet.http.request.error` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `Unknown` | Unknown error |
| `NameResolutionError` | NameResolutionError |
| `ConnectionError` | ConnectionError |
| `SecureConnectionError` | SecureConnectionError |
| `HttpProtocolError` | HttpProtocolError |
| `ExtendedConnectNotSupported` | ExtendedConnectNotSupported |

`http.request.method` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `CONNECT` | CONNECT method. |
| `DELETE` | DELETE method. |
| `GET` | GET method. |
| `HEAD` | HEAD method. |
| `OPTIONS` | OPTIONS method. |
| `PATCH` | PATCH method. |
| `POST` | POST method. |
| `PUT` | PUT method. |
| `TRACE` | TRACE method. |
| `_OTHER` | Any HTTP method that the instrumentation has no prior knowledge of. |
<!-- endsemconv -->

### Metric: `http.client.active_requests`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.dotnet.http.client.active_requests(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `http.client.active_requests` | UpDownCounter | `{request}` | Number of outbound HTTP requests that have failed. [1] |

**[1]:** Corresponding `EventCounter` name is `current-requests`; Meter name is `System.Net.Http`.
<!-- endsemconv -->

<!-- semconv metric.dotnet.http.client.active_requests(full) -->
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

`http.request.method` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `CONNECT` | CONNECT method. |
| `DELETE` | DELETE method. |
| `GET` | GET method. |
| `HEAD` | HEAD method. |
| `OPTIONS` | OPTIONS method. |
| `PATCH` | PATCH method. |
| `POST` | POST method. |
| `PUT` | PUT method. |
| `TRACE` | TRACE method. |
| `_OTHER` | Any HTTP method that the instrumentation has no prior knowledge of. |
<!-- endsemconv -->

## HTTP server

All Http server metrics are reported by `Microsoft.AspNetCore.Hosting ` meter.

### Metric: `http.server.duration`

**TODO: Opt-in address and port?**
**TODO: method cardinality?**

Follows common [http.server.duration](../http/http-metrics.md#metric-httpserverduration)

<!-- semconv metric.dotnet.http.server.duration(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `http.server.duration` | Histogram | `s` | Measures the duration of inbound HTTP requests. |
<!-- endsemconv -->

<!-- semconv metric.dotnet.http.server.duration(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `dotnet.error.code` | string | General-purpose error code reported by .NET, as a starter it supports terminal statuses of .NET task https://learn.microsoft.com/en-us/dotnet/api/system.threading.tasks.taskstatus. | `Canceled`; `RanToCompletion` | Recommended |
| `dotnet.http.request.error` | string | HTTP Request error as defined inhttps://github.com/dotnet/runtime/blob/c430570a01c103bc7f117be573f37d8ce8a129b8/src/libraries/System.Net.Http/src/System/Net/Http/HttpRequestError.cs | `extended_connection_not_supported`; `http_protocol` | Recommended |
| `http.request.method` | string | HTTP request method. [1] | `GET`; `POST`; `HEAD` | Required |
| `http.response.status_code` | int | [HTTP response status code](https://tools.ietf.org/html/rfc7231#section-6). | `200` | Conditionally Required: If and only if one was received/sent. |
| `http.route` | string | The matched route (path template in the format used by the respective server framework). See note below [2] | `/users/:userID?`; `{controller}/{action}/{id?}` | Conditionally Required: If and only if it's available |
| [`network.protocol.name`](../general/attributes.md) | string | [OSI Application Layer](https://osi-model.com/application-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `amqp`; `http`; `mqtt` | Recommended |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [3] | `3.1.1` | Recommended |
| [`server.address`](../general/attributes.md) | string | Name of the local HTTP server that received the request. [4] | `example.com` | Opt-In |
| [`server.port`](../general/attributes.md) | int | Port of the local HTTP server that received the request. [5] | `80`; `8080`; `443` | Opt-In |
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

**[2]:** MUST NOT be populated when this is not supported by the HTTP server framework as the route attribute should have low-cardinality and the URI path can NOT substitute it.
SHOULD include the [application root](/docs/http/http-spans.md#http-server-definitions) if there is one.

**[3]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.

**[4]:** Determined by using the first of the following that applies

- The [primary server name](/docs/http/http-spans.md#http-server-definitions) of the matched virtual host. MUST only
  include host identifier.
- Host identifier of the [request target](https://www.rfc-editor.org/rfc/rfc9110.html#target.resource)
  if it's sent in absolute-form.
- Host identifier of the `Host` header

SHOULD NOT be set if only IP address is available and capturing name would require a reverse DNS lookup.

**[5]:** Determined by using the first of the following that applies

- Port identifier of the [primary server host](/docs/http/http-spans.md#http-server-definitions) of the matched virtual host.
- Port identifier of the [request target](https://www.rfc-editor.org/rfc/rfc9110.html#target.resource)
  if it's sent in absolute-form.
- Port identifier of the `Host` header

`dotnet.error.code` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `RanToCompletion` | No error |
| `Canceled` | Canceled |
| `Faulted` | Faulted [1] |

**[1]:** When error code is set to `other`, it's recommended accompany this attribute with a domain-specific error code when it's known, such as `http.request.error` or `http.response.status_code` for HTTP errors.

`dotnet.http.request.error` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `Unknown` | Unknown error |
| `NameResolutionError` | NameResolutionError |
| `ConnectionError` | ConnectionError |
| `SecureConnectionError` | SecureConnectionError |
| `HttpProtocolError` | HttpProtocolError |
| `ExtendedConnectNotSupported` | ExtendedConnectNotSupported |

`http.request.method` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `CONNECT` | CONNECT method. |
| `DELETE` | DELETE method. |
| `GET` | GET method. |
| `HEAD` | HEAD method. |
| `OPTIONS` | OPTIONS method. |
| `PATCH` | PATCH method. |
| `POST` | POST method. |
| `PUT` | PUT method. |
| `TRACE` | TRACE method. |
| `_OTHER` | Any HTTP method that the instrumentation has no prior knowledge of. |
<!-- endsemconv -->

### Metric: `http.server.active_requests`

**TODO: Opt-in address and port?**
**TODO: method cardinality?**

Follows common [`http.server.active_requests``](../http/http-metrics.md#metric-httpserveractive_requests)

<!-- semconv metric.http.server.active_requests(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `http.server.active_requests` | UpDownCounter | `{request}` | Measures the number of concurrent HTTP requests that are currently in-flight. |
<!-- endsemconv -->

<!-- semconv metric.http.server.active_requests(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `http.request.method` | string | HTTP request method. [1] | `GET`; `POST`; `HEAD` | Required |
| [`server.address`](../general/attributes.md) | string | Name of the local HTTP server that received the request. [2] | `example.com` | Opt-In |
| [`server.port`](../general/attributes.md) | int | Port of the local HTTP server that received the request. [3] | `80`; `8080`; `443` | Opt-In |
| [`url.scheme`](../url/url.md) | string | The [URI scheme](https://www.rfc-editor.org/rfc/rfc3986#section-3.1) component identifying the used protocol. | `http`; `https` | Required |

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

**[2]:** Determined by using the first of the following that applies

- The [primary server name](/docs/http/http-spans.md#http-server-definitions) of the matched virtual host. MUST only
  include host identifier.
- Host identifier of the [request target](https://www.rfc-editor.org/rfc/rfc9110.html#target.resource)
  if it's sent in absolute-form.
- Host identifier of the `Host` header

SHOULD NOT be set if only IP address is available and capturing name would require a reverse DNS lookup.

**[3]:** Determined by using the first of the following that applies

- Port identifier of the [primary server host](/docs/http/http-spans.md#http-server-definitions) of the matched virtual host.
- Port identifier of the [request target](https://www.rfc-editor.org/rfc/rfc9110.html#target.resource)
  if it's sent in absolute-form.
- Port identifier of the `Host` header

`http.request.method` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `CONNECT` | CONNECT method. |
| `DELETE` | DELETE method. |
| `GET` | GET method. |
| `HEAD` | HEAD method. |
| `OPTIONS` | OPTIONS method. |
| `PATCH` | PATCH method. |
| `POST` | POST method. |
| `PUT` | PUT method. |
| `TRACE` | TRACE method. |
| `_OTHER` | Any HTTP method that the instrumentation has no prior knowledge of. |
<!-- endsemconv -->

### Metric: `http.server.unhandled_requests`

**TODO:Any chance we will need to report any attributes in future?**

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.aspnet.server.unhandled_requests(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `aspnet.unhandled_requests` | UpDownCounter | `{request}` | Number of requests that reached the end of the middleware pipeline without being handled by application code. [1] |

**[1]:** Meter name is `Microsoft.AspNetCore.Hosting`.**
<!-- endsemconv -->

<!-- semconv metric.aspnet.server.unhandled_requests(full) -->
<!-- endsemconv -->

[DocumentStatus]: https://github.com/open-telemetry/opentelemetry-specification/tree/v1.22.0/specification/document-status.md