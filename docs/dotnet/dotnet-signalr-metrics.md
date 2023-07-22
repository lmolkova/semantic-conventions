# Semantic Conventions for SignalR metrics emitted by ASP.NET Core

**Status**: [Experimental][DocumentStatus]

This document defines semantic conventions for SignalR metrics emitted by .NET components and runtime.

**Disclaimer:** These are initial .NET metric instruments available in .NET 8 but more may be added in the future.

<!-- toc -->

- [Metric: `signalr.http.server.connection.duration`](#metric-signalrhttpserverconnectionduration)
- [Metric: `signalr.http.server.active_connections`](#metric-signalrhttpserveractive_connections)

<!-- tocstop -->

## Metric: `signalr.http.server.connection.duration`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.signalr.http.server.connection.duration(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `signalr.http.server.connection.duration` | Histogram | `s` | The duration of the HTTP connections on the server. [1] |

**[1]:** Only reported for HTTP SignalR transport.
Corresponding `EventCounter` name is `connections-duration`; Meter name is `Microsoft.AspNetCore.Http.Connections`
<!-- endsemconv -->

<!-- semconv metric.signalr.http.server.connection.duration(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `signalr.transport` | string | SignalR transport - https://github.com/dotnet/aspnetcore/blob/main/src/SignalR/docs/specs/TransportProtocols.md [1] | `ServerSentEvents` | Conditionally Required: if HTTP SignalR transport is used |
| `dotnet.error.code` | string | General-purpose error code reported by .NET, as a starter it supports terminal states of .NET task https://learn.microsoft.com/en-us/dotnet/api/system.threading.tasks.taskstatus. | `Canceled`; `RanToCompletion` | Recommended |
| `http.response.status_code` | int | [HTTP response status code](https://tools.ietf.org/html/rfc7231#section-6). | `200` | Conditionally Required: If and only if one was received/sent. |
| [`network.protocol.name`](../general/attributes.md) | string | [OSI Application Layer](https://osi-model.com/application-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `http`; `websockets` | Recommended: if not default (`http`) |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [2] | `3.1.1` | Recommended |
| [`network.transport`](../general/attributes.md) | string | [OSI Transport Layer](https://osi-model.com/transport-layer/) or [Inter-process Communication method](https://en.wikipedia.org/wiki/Inter-process_communication). The value SHOULD be normalized to lowercase. | `tcp`; `udp` | Recommended |

**[1]:** websockets are recoded in `network.protocol.name` attribute and `signalr.transport` is not set.

**[2]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.

`signalr.transport` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `ServerSentEvents` | ServerSentEvents |
| `LongPolling` | LongPolling |

`dotnet.error.code` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `RanToCompletion` | No error |
| `Canceled` | Canceled |
| `Faulted` | Faulted [1] |

**[1]:** When error code is set to `other`, it's recommended accompany this attribute with a domain-specific error code when it's known, such as `http.request.error` or `http.response.status_code` for HTTP errors.

`network.transport` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `tcp` | TCP |
| `udp` | UDP |
| `pipe` | Named or anonymous pipe. See note below. |
| `unix` | Unix domain socket |
<!-- endsemconv -->

## Metric: `signalr.http.server.active_connections`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.signalr.http.server.active_connections(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `signalr.http.server.active_connections` | Histogram | `s` | Number of connections that are currently active on the server. [1] |

**[1]:** Only reported for HTTP SignalR transport.
Corresponding `EventCounter` name is `current-connections`; Meter name is `Microsoft.AspNetCore.Http.Connections`
<!-- endsemconv -->

<!-- semconv metric.signalr.http.server.active_connections(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `signalr.transport` | string | SignalR transport - https://github.com/dotnet/aspnetcore/blob/main/src/SignalR/docs/specs/TransportProtocols.md [1] | `ServerSentEvents` | Conditionally Required: if HTTP SignalR transport is used |
| [`network.protocol.name`](../general/attributes.md) | string | [OSI Application Layer](https://osi-model.com/application-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `http`; `websockets` | Recommended: if not default (`http`) |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [2] | `3.1.1` | Recommended |
| [`network.transport`](../general/attributes.md) | string | [OSI Transport Layer](https://osi-model.com/transport-layer/) or [Inter-process Communication method](https://en.wikipedia.org/wiki/Inter-process_communication). The value SHOULD be normalized to lowercase. | `tcp`; `udp` | Recommended |

**[1]:** websockets are recoded in `network.protocol.name` attribute and `signalr.transport` is not set.

**[2]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.

`signalr.transport` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `ServerSentEvents` | ServerSentEvents |
| `LongPolling` | LongPolling |

`network.transport` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `tcp` | TCP |
| `udp` | UDP |
| `pipe` | Named or anonymous pipe. See note below. |
| `unix` | Unix domain socket |
<!-- endsemconv -->


[DocumentStatus]: https://github.com/open-telemetry/opentelemetry-specification/tree/v1.22.0/specification/document-status.md