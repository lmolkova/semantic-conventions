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
| `signalr_http_transport.connection.duration` | Histogram | `s` | The duration of connections on the server. [1] |

**[1]:** **TODO: why http_transport? can we report protocol name as dimension and not a metric name?**
**TODO: we should do signalr.server**
Corresponding `EventCounter` name is `connections-duration`; Meter name is `Microsoft.AspNetCore.Http.Connections`
<!-- endsemconv -->

<!-- semconv metric.signalr.server.connection.duration(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `signalr.http_transport.status` | string | TODO [1] | `TODO` | Recommended |
| `signalr.http_transport.transport` | string | TODO [2] | `TODO` | Recommended |

**[1]:** **TODO: why not signalr.status? What's HTTP-specific about it**

**[2]:** **TODO: why not network.transport?**
<!-- endsemconv -->

## Metric: `signalr_http_transport.active_connections`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.signalr.server.active_connections(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `signalr_http_transport.active_connections` | Histogram | `s` | Number of connections that are currently active on the server. [1] |

**[1]:** **TODO: why http_transport? can we report protocol as dimension and not a metric name?**
**TODO: we should do signalr.server**
Corresponding `EventCounter` name is `current-connections`; Meter name is `Microsoft.AspNetCore.Http.Connections`
<!-- endsemconv -->

<!-- semconv metric.signalr.server.active_connections(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `signalr.http_transport.status` | string | TODO [1] | `TODO` | Recommended |

**[1]:** **TODO: why not signalr.status? What's HTTP-specific about it**
<!-- endsemconv -->


[DocumentStatus]: https://github.com/open-telemetry/opentelemetry-specification/tree/v1.22.0/specification/document-status.md