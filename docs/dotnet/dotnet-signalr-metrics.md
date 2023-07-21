# Semantic Conventions for SignalR metrics emitted by ASP.NET Core

**Status**: [Experimental][DocumentStatus]

This document defines semantic conventions for SignalR metrics emitted by .NET components and runtime.

**Disclaimer:** These are initial .NET metric instruments available in .NET 8 but more may be added in the future.

<!-- toc -->

- [Metric: `signalr_http_transport.connection.duration`](#metric-signalr_http_transportconnectionduration)
- [Metric: `signalr_http_transport.active_connections`](#metric-signalr_http_transportactive_connections)

<!-- tocstop -->

## Metric: `signalr_http_transport.connection.duration`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.signalr.server.connection.duration(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `signalr.server.connection.duration` | Histogram | `s` | The duration of connections on the server. [1] |

**[1]:** Corresponding `EventCounter` name is `connections-duration`; Meter name is `Microsoft.AspNetCore.Http.Connections`
<!-- endsemconv -->

<!-- semconv metric.signalr.server.connection.duration(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `dotnet.error.code` | string | General-purpose error code reported by .NET, as a started it supports terminal states of .NET task https://learn.microsoft.com/en-us/dotnet/api/system.threading.tasks.taskstatus. | `Canceled`; `RanToCompletion` | Recommended |
| [`network.protocol.name`](../general/attributes.md) | string | [OSI Application Layer](https://osi-model.com/application-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `amqp`; `http`; `mqtt` | Recommended |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [1] | `3.1.1` | Recommended |
| [`network.transport`](../general/attributes.md) | string | [OSI Transport Layer](https://osi-model.com/transport-layer/) or [Inter-process Communication method](https://en.wikipedia.org/wiki/Inter-process_communication). The value SHOULD be normalized to lowercase. | `tcp`; `udp` | Recommended |

**[1]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.

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

## Metric: `signalr_http_transport.active_connections`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.signalr.server.active_connections(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `signalr.server.active_connections` | Histogram | `s` | Number of connections that are currently active on the server. [1] |

**[1]:** Corresponding `EventCounter` name is `current-connections`; Meter name is `Microsoft.AspNetCore.Http.Connections`
<!-- endsemconv -->

<!-- semconv metric.signalr.server.active_connections(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| [`network.protocol.name`](../general/attributes.md) | string | [OSI Application Layer](https://osi-model.com/application-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `amqp`; `http`; `mqtt` | Recommended |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [1] | `3.1.1` | Recommended |
| [`network.transport`](../general/attributes.md) | string | [OSI Transport Layer](https://osi-model.com/transport-layer/) or [Inter-process Communication method](https://en.wikipedia.org/wiki/Inter-process_communication). The value SHOULD be normalized to lowercase. | `tcp`; `udp` | Recommended |

**[1]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.

`network.transport` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `tcp` | TCP |
| `udp` | UDP |
| `pipe` | Named or anonymous pipe. See note below. |
| `unix` | Unix domain socket |
<!-- endsemconv -->


[DocumentStatus]: https://github.com/open-telemetry/opentelemetry-specification/tree/v1.22.0/specification/document-status.md