# Semantic Conventions for Kestrel web server metrics

**Status**: [Experimental][DocumentStatus]

This document defines semantic conventions for Kestrel web server.

<!-- toc -->

- [Metric: `kestrel.active_connections`](#metric-kestrelactive_connections)
- [Metric: `kestrel.connection.duration`](#metric-kestrelconnectionduration)
- [Metric: `kestrel.rejected_connections`](#metric-kestrelrejected_connections)
- [Metric: `kestrel.queued_connections`](#metric-kestrelqueued_connections)
- [Metric: `kestrel.queued_requests`](#metric-kestrelqueued_requests)
- [Metric: `kestrel.upgraded_connections`](#metric-kestrelupgraded_connections)
- [Metric: `kestrel.tls_handshake.duration`](#metric-kestreltls_handshakeduration)
- [Metric: `kestrel.active_tls_handshakes`](#metric-kestrelactive_tls_handshakes)

<!-- tocstop -->

## Metric: `kestrel.active_connections`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.kestrel.active_connections(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `kestrel.active_connections` | UpDownCounter | `{connection}` | Number of connections that are currently active on the server. [1] |

**[1]:** Corresponding `EventCounter` name is `current-connections`; Meter name is `Microsoft.AspNetCore.Server.Kestrel`
<!-- endsemconv -->

<!-- semconv metric.kestrel.active_connections(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `kestrel.endpoint` | string | TODO [1] | `TODO` | Recommended |

**[1]:** **TODO: why not server.**? What's KEstrel-specific about it**
<!-- endsemconv -->

## Metric: `kestrel.connection.duration`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.kestrel.connection.duration(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `kestrel.connection.duration` | Histogram | `s` | The duration of connections on the server. [1] |

**[1]:** Meter name is `Microsoft.AspNetCore.Server.Kestrel`
<!-- endsemconv -->

<!-- semconv metric.kestrel.connection.duration(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `exception.type` | string | The type of the exception (its fully-qualified class name, if applicable). The dynamic type of the exception should be preferred over the static type in languages that support it. [1] | `java.net.ConnectException`; `OSError` | Recommended |
| `kestrel.endpoint` | string | TODO [2] | `TODO` | Recommended |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [3] | `3.1.1` | Recommended |
| `tls.protocol` | string | TODO [4] | `TODO` | Recommended |
| [`url.scheme`](../url/url.md) | string | The [URI scheme](https://www.rfc-editor.org/rfc/rfc3986#section-3.1) component identifying the used protocol. | `https`; `ftp`; `telnet` | Recommended |

**[1]:** **TODO: Let's define common attribute for error reason/status**

**[2]:** **TODO: why not server.**? What's KEstrel-specific about it**

**[3]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.

**[4]:** **TODO: why not network.protocol.***?
<!-- endsemconv -->

## Metric: `kestrel.rejected_connections`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.kestrel.rejected_connections(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `kestrel.rejected_connections` | Counter | `{connection}` | Number of connections rejected by the server. [1] |

**[1]:** Connections are rejected when the currently active count exceeds the value configured with MaxConcurrentConnections.
Meter name is `Microsoft.AspNetCore.Server.Kestrel`

**TODO: do we ever will have other rejection reasons**
<!-- endsemconv -->

<!-- semconv metric.kestrel.rejected_connections(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `kestrel.endpoint` | string | TODO [1] | `TODO` | Recommended |

**[1]:** **TODO: why not server.**? What's KEstrel-specific about it**
<!-- endsemconv -->

## Metric: `kestrel.queued_connections`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.kestrel.queued_connections(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `kestrel.queued_connections` | UpDownCounter | `{connection}` | Number of connections that are currently queued and are waiting to start. [1] |

**[1]:** Meter name is `Microsoft.AspNetCore.Server.Kestrel`
<!-- endsemconv -->

<!-- semconv metric.kestrel.queued_connections(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `kestrel.endpoint` | string | TODO [1] | `TODO` | Recommended |

**[1]:** **TODO: why not server.**? What's KEstrel-specific about it**
<!-- endsemconv -->

## Metric: `kestrel.queued_requests`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.kestrel.queued_requests(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `kestrel.queued_requests` | UpDownCounter | `{connection}` | Number of HTTP requests on multiplexed connections (HTTP/2 and HTTP/3) that are currently queued and are waiting to start. [1] |

**[1]:** Meter name is `Microsoft.AspNetCore.Server.Kestrel`
<!-- endsemconv -->

<!-- semconv metric.kestrel.queued_requests(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `kestrel.endpoint` | string | TODO [1] | `TODO` | Recommended |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [2] | `3.1.1` | Recommended |
| [`url.scheme`](../url/url.md) | string | The [URI scheme](https://www.rfc-editor.org/rfc/rfc3986#section-3.1) component identifying the used protocol. | `https`; `ftp`; `telnet` | Recommended |

**[1]:** **TODO: why not server.**? What's KEstrel-specific about it**

**[2]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.
<!-- endsemconv -->

## Metric: `kestrel.upgraded_connections`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.kestrel.upgraded_connections(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `kestrel.upgraded_connections` | UpDownCounter | `{connection}` | Number of connections that are currently upgraded (WebSockets). . [1] |

**[1]:** The number only tracks HTTP/1.1 connections. **TODO: can we add protocol info in dimensions so we can extend it to other versions/protocols? **
Meter name is `Microsoft.AspNetCore.Server.Kestrel`
<!-- endsemconv -->

<!-- semconv metric.kestrel.upgraded_connections(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `kestrel.endpoint` | string | TODO [1] | `TODO` | Recommended |

**[1]:** **TODO: why not server.**? What's KEstrel-specific about it**
<!-- endsemconv -->

## Metric: `kestrel.tls_handshake.duration`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.kestrel.tls_handshake.duration(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `kestrel.tls_handshake.duration` | Histogram | `s` | The duration of TLS handshakes on the server. [1] |

**[1]:** Meter name is `Microsoft.AspNetCore.Server.Kestrel`
<!-- endsemconv -->

<!-- semconv metric.kestrel.tls_handshake.duration(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `exception.type` | string | The type of the exception (its fully-qualified class name, if applicable). The dynamic type of the exception should be preferred over the static type in languages that support it. [1] | `java.net.ConnectException`; `OSError` | Recommended |
| `kestrel.endpoint` | string | TODO [2] | `TODO` | Recommended |
| `tls.protocol` | string | TODO [3] | `TODO` | Recommended |

**[1]:** **TODO: Let's define common attribute for error reason/status**

**[2]:** **TODO: why not server.**? What's KEstrel-specific about it**

**[3]:** **TODO: why not network.protocol.***?
<!-- endsemconv -->

## Metric: `kestrel.active_tls_handshakes`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.kestrel.active_tls_handshakes(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `active_tls_handshakes` | UpDownCounter | `{handshake}` | Number of TLS handshakes that are currently in progress on the server. [1] |

**[1]:** Meter name is `Microsoft.AspNetCore.Server.Kestrel`
<!-- endsemconv -->

<!-- semconv metric.kestrel.active_tls_handshakes(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `kestrel.endpoint` | string | TODO [1] | `TODO` | Recommended |

**[1]:** **TODO: why not server.**? What's KEstrel-specific about it**
<!-- endsemconv -->


[DocumentStatus]: https://github.com/open-telemetry/opentelemetry-specification/tree/v1.22.0/specification/document-status.md