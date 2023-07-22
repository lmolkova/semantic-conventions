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
| [`network.protocol.name`](../general/attributes.md) | string | [OSI Application Layer](https://osi-model.com/application-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `amqp`; `http`; `mqtt` | Recommended |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [1] | `3.1.1` | Recommended |
| [`network.transport`](../general/attributes.md) | string | [OSI Transport Layer](https://osi-model.com/transport-layer/) or [Inter-process Communication method](https://en.wikipedia.org/wiki/Inter-process_communication). The value SHOULD be normalized to lowercase. | `tcp`; `udp` | Recommended |
| [`network.type`](../general/attributes.md) | string | [OSI Network Layer](https://osi-model.com/network-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `ipv4`; `ipv6` | Recommended |
| [`url.full`](../url/url.md) | string | Absolute URL describing a network resource according to [RFC3986](https://www.rfc-editor.org/rfc/rfc3986) [2] | `https://www.foo.bar/search?q=OpenTelemetry#SemConv`; `//localhost` | Recommended |

**[1]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.

**[2]:** For network calls, URL usually has `scheme://host[:port][path][?query][#fragment]` format, where the fragment is not transmitted over HTTP, but if it is known, it should be included nevertheless.
`url.full` MUST NOT contain credentials passed via URL in form of `https://username:password@www.example.com/`. In such case username and password should be redacted and attribute's value should be `https://REDACTED:REDACTED@www.example.com/`.
`url.full` SHOULD capture the absolute URL when it is available (or can be reconstructed) and SHOULD NOT be validated or modified except for sanitizing purposes.

`network.transport` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `tcp` | TCP |
| `udp` | UDP |
| `pipe` | Named or anonymous pipe. See note below. |
| `unix` | Unix domain socket |

`network.type` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `ipv4` | IPv4 |
| `ipv6` | IPv6 |
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
| `dotnet.error.code` | string | General-purpose error code reported by .NET, as a starter it supports terminal statuses of .NET task https://learn.microsoft.com/en-us/dotnet/api/system.threading.tasks.taskstatus. | `Canceled`; `RanToCompletion` | Recommended |
| [`network.protocol.name`](../general/attributes.md) | string | [OSI Application Layer](https://osi-model.com/application-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `amqp`; `http`; `mqtt` | Recommended |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [1] | `3.1.1` | Recommended |
| [`network.transport`](../general/attributes.md) | string | [OSI Transport Layer](https://osi-model.com/transport-layer/) or [Inter-process Communication method](https://en.wikipedia.org/wiki/Inter-process_communication). The value SHOULD be normalized to lowercase. | `tcp`; `udp` | Recommended |
| [`network.type`](../general/attributes.md) | string | [OSI Network Layer](https://osi-model.com/network-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `ipv4`; `ipv6` | Recommended |
| `tls.version` | string | TLS protocol version | `1.3` | Recommended |
| [`url.full`](../url/url.md) | string | Absolute URL describing a network resource according to [RFC3986](https://www.rfc-editor.org/rfc/rfc3986) [2] | `https://www.foo.bar/search?q=OpenTelemetry#SemConv`; `//localhost` | Recommended |

**[1]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.

**[2]:** For network calls, URL usually has `scheme://host[:port][path][?query][#fragment]` format, where the fragment is not transmitted over HTTP, but if it is known, it should be included nevertheless.
`url.full` MUST NOT contain credentials passed via URL in form of `https://username:password@www.example.com/`. In such case username and password should be redacted and attribute's value should be `https://REDACTED:REDACTED@www.example.com/`.
`url.full` SHOULD capture the absolute URL when it is available (or can be reconstructed) and SHOULD NOT be validated or modified except for sanitizing purposes.

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

`network.type` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `ipv4` | IPv4 |
| `ipv6` | IPv6 |
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
| [`network.protocol.name`](../general/attributes.md) | string | [OSI Application Layer](https://osi-model.com/application-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `amqp`; `http`; `mqtt` | Recommended |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [1] | `3.1.1` | Recommended |
| [`network.transport`](../general/attributes.md) | string | [OSI Transport Layer](https://osi-model.com/transport-layer/) or [Inter-process Communication method](https://en.wikipedia.org/wiki/Inter-process_communication). The value SHOULD be normalized to lowercase. | `tcp`; `udp` | Recommended |
| [`network.type`](../general/attributes.md) | string | [OSI Network Layer](https://osi-model.com/network-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `ipv4`; `ipv6` | Recommended |
| [`url.full`](../url/url.md) | string | Absolute URL describing a network resource according to [RFC3986](https://www.rfc-editor.org/rfc/rfc3986) [2] | `https://www.foo.bar/search?q=OpenTelemetry#SemConv`; `//localhost` | Recommended |

**[1]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.

**[2]:** For network calls, URL usually has `scheme://host[:port][path][?query][#fragment]` format, where the fragment is not transmitted over HTTP, but if it is known, it should be included nevertheless.
`url.full` MUST NOT contain credentials passed via URL in form of `https://username:password@www.example.com/`. In such case username and password should be redacted and attribute's value should be `https://REDACTED:REDACTED@www.example.com/`.
`url.full` SHOULD capture the absolute URL when it is available (or can be reconstructed) and SHOULD NOT be validated or modified except for sanitizing purposes.

`network.transport` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `tcp` | TCP |
| `udp` | UDP |
| `pipe` | Named or anonymous pipe. See note below. |
| `unix` | Unix domain socket |

`network.type` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `ipv4` | IPv4 |
| `ipv6` | IPv6 |
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
| [`network.protocol.name`](../general/attributes.md) | string | [OSI Application Layer](https://osi-model.com/application-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `amqp`; `http`; `mqtt` | Recommended |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [1] | `3.1.1` | Recommended |
| [`network.transport`](../general/attributes.md) | string | [OSI Transport Layer](https://osi-model.com/transport-layer/) or [Inter-process Communication method](https://en.wikipedia.org/wiki/Inter-process_communication). The value SHOULD be normalized to lowercase. | `tcp`; `udp` | Recommended |
| [`network.type`](../general/attributes.md) | string | [OSI Network Layer](https://osi-model.com/network-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `ipv4`; `ipv6` | Recommended |
| [`url.full`](../url/url.md) | string | Absolute URL describing a network resource according to [RFC3986](https://www.rfc-editor.org/rfc/rfc3986) [2] | `https://www.foo.bar/search?q=OpenTelemetry#SemConv`; `//localhost` | Recommended |

**[1]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.

**[2]:** For network calls, URL usually has `scheme://host[:port][path][?query][#fragment]` format, where the fragment is not transmitted over HTTP, but if it is known, it should be included nevertheless.
`url.full` MUST NOT contain credentials passed via URL in form of `https://username:password@www.example.com/`. In such case username and password should be redacted and attribute's value should be `https://REDACTED:REDACTED@www.example.com/`.
`url.full` SHOULD capture the absolute URL when it is available (or can be reconstructed) and SHOULD NOT be validated or modified except for sanitizing purposes.

`network.transport` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `tcp` | TCP |
| `udp` | UDP |
| `pipe` | Named or anonymous pipe. See note below. |
| `unix` | Unix domain socket |

`network.type` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `ipv4` | IPv4 |
| `ipv6` | IPv6 |
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
| [`network.protocol.name`](../general/attributes.md) | string | [OSI Application Layer](https://osi-model.com/application-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `amqp`; `http`; `mqtt` | Recommended |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [1] | `3.1.1` | Recommended |
| [`network.transport`](../general/attributes.md) | string | [OSI Transport Layer](https://osi-model.com/transport-layer/) or [Inter-process Communication method](https://en.wikipedia.org/wiki/Inter-process_communication). The value SHOULD be normalized to lowercase. | `tcp`; `udp` | Recommended |
| [`network.type`](../general/attributes.md) | string | [OSI Network Layer](https://osi-model.com/network-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `ipv4`; `ipv6` | Recommended |
| [`url.full`](../url/url.md) | string | Absolute URL describing a network resource according to [RFC3986](https://www.rfc-editor.org/rfc/rfc3986) [2] | `https://www.foo.bar/search?q=OpenTelemetry#SemConv`; `//localhost` | Recommended |

**[1]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.

**[2]:** For network calls, URL usually has `scheme://host[:port][path][?query][#fragment]` format, where the fragment is not transmitted over HTTP, but if it is known, it should be included nevertheless.
`url.full` MUST NOT contain credentials passed via URL in form of `https://username:password@www.example.com/`. In such case username and password should be redacted and attribute's value should be `https://REDACTED:REDACTED@www.example.com/`.
`url.full` SHOULD capture the absolute URL when it is available (or can be reconstructed) and SHOULD NOT be validated or modified except for sanitizing purposes.

`network.transport` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `tcp` | TCP |
| `udp` | UDP |
| `pipe` | Named or anonymous pipe. See note below. |
| `unix` | Unix domain socket |

`network.type` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `ipv4` | IPv4 |
| `ipv6` | IPv6 |
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
| [`network.protocol.name`](../general/attributes.md) | string | [OSI Application Layer](https://osi-model.com/application-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `amqp`; `http`; `mqtt` | Recommended |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [1] | `3.1.1` | Recommended |
| [`network.transport`](../general/attributes.md) | string | [OSI Transport Layer](https://osi-model.com/transport-layer/) or [Inter-process Communication method](https://en.wikipedia.org/wiki/Inter-process_communication). The value SHOULD be normalized to lowercase. | `tcp`; `udp` | Recommended |
| [`network.type`](../general/attributes.md) | string | [OSI Network Layer](https://osi-model.com/network-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `ipv4`; `ipv6` | Recommended |
| [`url.full`](../url/url.md) | string | Absolute URL describing a network resource according to [RFC3986](https://www.rfc-editor.org/rfc/rfc3986) [2] | `https://www.foo.bar/search?q=OpenTelemetry#SemConv`; `//localhost` | Recommended |

**[1]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.

**[2]:** For network calls, URL usually has `scheme://host[:port][path][?query][#fragment]` format, where the fragment is not transmitted over HTTP, but if it is known, it should be included nevertheless.
`url.full` MUST NOT contain credentials passed via URL in form of `https://username:password@www.example.com/`. In such case username and password should be redacted and attribute's value should be `https://REDACTED:REDACTED@www.example.com/`.
`url.full` SHOULD capture the absolute URL when it is available (or can be reconstructed) and SHOULD NOT be validated or modified except for sanitizing purposes.

`network.transport` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `tcp` | TCP |
| `udp` | UDP |
| `pipe` | Named or anonymous pipe. See note below. |
| `unix` | Unix domain socket |

`network.type` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `ipv4` | IPv4 |
| `ipv6` | IPv6 |
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
| `dotnet.error.code` | string | General-purpose error code reported by .NET, as a starter it supports terminal statuses of .NET task https://learn.microsoft.com/en-us/dotnet/api/system.threading.tasks.taskstatus. | `Canceled`; `RanToCompletion` | Recommended |
| [`network.protocol.name`](../general/attributes.md) | string | [OSI Application Layer](https://osi-model.com/application-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `amqp`; `http`; `mqtt` | Recommended |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [1] | `3.1.1` | Recommended |
| [`network.transport`](../general/attributes.md) | string | [OSI Transport Layer](https://osi-model.com/transport-layer/) or [Inter-process Communication method](https://en.wikipedia.org/wiki/Inter-process_communication). The value SHOULD be normalized to lowercase. | `tcp`; `udp` | Recommended |
| [`network.type`](../general/attributes.md) | string | [OSI Network Layer](https://osi-model.com/network-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `ipv4`; `ipv6` | Recommended |
| [`url.full`](../url/url.md) | string | Absolute URL describing a network resource according to [RFC3986](https://www.rfc-editor.org/rfc/rfc3986) [2] | `https://www.foo.bar/search?q=OpenTelemetry#SemConv`; `//localhost` | Recommended |

**[1]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.

**[2]:** For network calls, URL usually has `scheme://host[:port][path][?query][#fragment]` format, where the fragment is not transmitted over HTTP, but if it is known, it should be included nevertheless.
`url.full` MUST NOT contain credentials passed via URL in form of `https://username:password@www.example.com/`. In such case username and password should be redacted and attribute's value should be `https://REDACTED:REDACTED@www.example.com/`.
`url.full` SHOULD capture the absolute URL when it is available (or can be reconstructed) and SHOULD NOT be validated or modified except for sanitizing purposes.

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

`network.type` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `ipv4` | IPv4 |
| `ipv6` | IPv6 |
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
| [`network.protocol.name`](../general/attributes.md) | string | [OSI Application Layer](https://osi-model.com/application-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `amqp`; `http`; `mqtt` | Recommended |
| [`network.protocol.version`](../general/attributes.md) | string | Version of the application layer protocol used. See note below. [1] | `3.1.1` | Recommended |
| [`network.transport`](../general/attributes.md) | string | [OSI Transport Layer](https://osi-model.com/transport-layer/) or [Inter-process Communication method](https://en.wikipedia.org/wiki/Inter-process_communication). The value SHOULD be normalized to lowercase. | `tcp`; `udp` | Recommended |
| [`network.type`](../general/attributes.md) | string | [OSI Network Layer](https://osi-model.com/network-layer/) or non-OSI equivalent. The value SHOULD be normalized to lowercase. | `ipv4`; `ipv6` | Recommended |
| `tls.version` | string | TLS protocol version | `1.3` | Recommended |
| [`url.full`](../url/url.md) | string | Absolute URL describing a network resource according to [RFC3986](https://www.rfc-editor.org/rfc/rfc3986) [2] | `https://www.foo.bar/search?q=OpenTelemetry#SemConv`; `//localhost` | Recommended |

**[1]:** `network.protocol.version` refers to the version of the protocol used and might be different from the protocol client's version. If the HTTP client used has a version of `0.27.2`, but sends HTTP version `1.1`, this attribute should be set to `1.1`.

**[2]:** For network calls, URL usually has `scheme://host[:port][path][?query][#fragment]` format, where the fragment is not transmitted over HTTP, but if it is known, it should be included nevertheless.
`url.full` MUST NOT contain credentials passed via URL in form of `https://username:password@www.example.com/`. In such case username and password should be redacted and attribute's value should be `https://REDACTED:REDACTED@www.example.com/`.
`url.full` SHOULD capture the absolute URL when it is available (or can be reconstructed) and SHOULD NOT be validated or modified except for sanitizing purposes.

`network.transport` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `tcp` | TCP |
| `udp` | UDP |
| `pipe` | Named or anonymous pipe. See note below. |
| `unix` | Unix domain socket |

`network.type` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `ipv4` | IPv4 |
| `ipv6` | IPv6 |
<!-- endsemconv -->


[DocumentStatus]: https://github.com/open-telemetry/opentelemetry-specification/tree/v1.22.0/specification/document-status.md