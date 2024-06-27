<!--- Hugo front matter used to generate the website version of this page:
linkTitle: HTTP client and server
--->

# Semantic Conventions for HTTP client connection spans emitted by .NET

**Status**: [Experimental][DocumentStatus]

This article defines semantic conventions for HTTP client connections, DNS and TLS spans emitted by .NET.

<!-- toc -->

* [HTTP client connection](#http-connection)
* [HTTP connection setup](#http-connection-setup)
* [Socket connection setup](#socket-connection-setup)
* [DNS](#dns)
* [TLS](#tls)
* [Examples](#examples)

<!-- tocstop -->

## Overview

.NET reports spans related to HTTP connection establishment and its stages starting from .NET 9.
Application developers are encouraged to enable corresponding instrumentation in development and test environments.

Enabling connection-level details in production may result in increased telemetry consumption and reduce performance.

While such spans represent low-level details, connection lifetime is usually measured in minutes, therefore in common case
when application is under the load and connections are established for every request, the rate of connection-related spans
is expected to be much smaller than the rate of HTTP client spans.

## HTTP client connection

HTTP connection span describes the lifetime of the HTTP connection. TODO - after it's created or from the initiation time?
HTTP connection duration is also reported as [`http.client.connection.duration` metric](/dotnet/dotnet-http-metrics.md#metric-httpclientconnectionduration).

Client HTTP request spans SHOULD have links to this connection span (when it is reported).

Span name SHOULD be `http connection {address}:{server.port}`.
The `{address}` SHOULD be `server.address` when it's available and `network.peer.address` otherwise.

Span kind SHOULD be `CLIENT`

Corresponding `Activity.OperationName` is `System.Net.Http.Connections.Connection`, `ActivitySource` name - `System.Net.Http.Connections`.
Span added in .NET Core 9.

<!-- semconv dotnet.http.connection -->
<!-- NOTE: THIS TEXT IS AUTOGENERATED. DO NOT EDIT BY HAND. -->
<!-- see templates/registry/markdown/snippet.md.j2 -->
<!-- prettier-ignore-start -->
<!-- markdownlint-capture -->
<!-- markdownlint-disable -->

| Attribute  | Type | Description  | Examples  | [Requirement Level](https://opentelemetry.io/docs/specs/semconv/general/attribute-requirement-level/) | Stability |
|---|---|---|---|---|---|
| [`error.type`](/docs/attributes-registry/error.md) | string | Describes a class of error the operation ended with. [1] | `timeout`; `java.net.UnknownHostException`; `server_certificate_invalid`; `500` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| [`network.peer.address`](/docs/attributes-registry/network.md) | string | Peer address of the network connection - IP address or Unix domain socket name. | `10.1.2.80`; `/tmp/my.sock` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| [`network.protocol.version`](/docs/attributes-registry/network.md) | string | The actual version of the protocol used for network communication. [2] | `1.1`; `2` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| [`server.address`](/docs/attributes-registry/server.md) | string | Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name. [3] | `example.com`; `10.1.2.80`; `/tmp/my.sock` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| [`server.port`](/docs/attributes-registry/server.md) | int | Server port number. [4] | `80`; `8080`; `443` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| [`url.scheme`](/docs/attributes-registry/url.md) | string | The [URI scheme](https://www.rfc-editor.org/rfc/rfc3986#section-3.1) component identifying the used protocol. | `https`; `ftp`; `telnet` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |

**[1]:** The `error.type` SHOULD be predictable, and SHOULD have low cardinality.

When `error.type` is set to a type (e.g., an exception type), its
canonical class name identifying the type within the artifact SHOULD be used.

Instrumentations SHOULD document the list of errors they report.

The cardinality of `error.type` within one instrumentation library SHOULD be low.
Telemetry consumers that aggregate data from multiple instrumentation libraries and applications
should be prepared for `error.type` to have high cardinality at query time when no
additional filters are applied.

If the operation has completed successfully, instrumentations SHOULD NOT set `error.type`.

If a specific domain defines its own set of error identifiers (such as HTTP or gRPC status codes),
it's RECOMMENDED to:

* Use a domain-specific attribute
* Set `error.type` to capture all errors, regardless of whether they are defined within the domain-specific set or not.

**[2]:** If protocol version is subject to negotiation (for example using [ALPN](https://www.rfc-editor.org/rfc/rfc7301.html)), this attribute SHOULD be set to the negotiated version. If the actual protocol version is not known, this attribute SHOULD NOT be set.

**[3]:** When observed from the client side, and when communicating through an intermediary, `server.address` SHOULD represent the server address behind any intermediaries, for example proxies, if it's available.

**[4]:** When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD represent the server port behind any intermediaries, for example proxies, if it's available.



`error.type` has the following list of well-known values. If one of them applies, then the respective value MUST be used; otherwise, a custom value MAY be used.

| Value  | Description | Stability |
|---|---|---|
| `_OTHER` | A fallback error value to be used when the instrumentation doesn't define a custom value. | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |



<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->
<!-- END AUTOGENERATED TEXT -->
<!-- endsemconv -->

## HTTP connection setup

The span describes the establishment of the HTTP connection. It includes the time it takes
to resolve the DNS, establish the socket connection, and perform the TLS handshake.

It's different from `dotnet.http.connection` span, which describes the lifetime of the HTTP connection.

Client HTTP request spans SHOULD have links to this connection span (when it is reported).

Span name SHOULD be `http connection_setup {address}:{server.port}`.
The `{address}` SHOULD be `server.address` when it's available and `network.peer.address` otherwise.

Span kind SHOULD be `CLIENT`.

Corresponding `Activity.OperationName` is `System.Net.Http.Connections.ConnectionSetup`, `ActivitySource` name - `System.Net.Http.Connections` TODO - should be different than connection?
Added in .NET Core 9.

<!-- semconv dotnet.http.connection_setup -->
<!-- NOTE: THIS TEXT IS AUTOGENERATED. DO NOT EDIT BY HAND. -->
<!-- see templates/registry/markdown/snippet.md.j2 -->
<!-- prettier-ignore-start -->
<!-- markdownlint-capture -->
<!-- markdownlint-disable -->

| Attribute  | Type | Description  | Examples  | [Requirement Level](https://opentelemetry.io/docs/specs/semconv/general/attribute-requirement-level/) | Stability |
|---|---|---|---|---|---|
| [`error.type`](/docs/attributes-registry/error.md) | string | Describes a class of error the operation ended with. [1] | `timeout`; `java.net.UnknownHostException`; `server_certificate_invalid`; `500` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| [`network.peer.address`](/docs/attributes-registry/network.md) | string | Peer address of the network connection - IP address or Unix domain socket name. | `10.1.2.80`; `/tmp/my.sock` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| [`network.protocol.version`](/docs/attributes-registry/network.md) | string | The actual version of the protocol used for network communication. [2] | `1.1`; `2` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| [`server.address`](/docs/attributes-registry/server.md) | string | Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name. [3] | `example.com`; `10.1.2.80`; `/tmp/my.sock` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| [`server.port`](/docs/attributes-registry/server.md) | int | Server port number. [4] | `80`; `8080`; `443` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| [`url.scheme`](/docs/attributes-registry/url.md) | string | The [URI scheme](https://www.rfc-editor.org/rfc/rfc3986#section-3.1) component identifying the used protocol. | `https`; `ftp`; `telnet` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |

**[1]:** The `error.type` SHOULD be predictable, and SHOULD have low cardinality.

When `error.type` is set to a type (e.g., an exception type), its
canonical class name identifying the type within the artifact SHOULD be used.

Instrumentations SHOULD document the list of errors they report.

The cardinality of `error.type` within one instrumentation library SHOULD be low.
Telemetry consumers that aggregate data from multiple instrumentation libraries and applications
should be prepared for `error.type` to have high cardinality at query time when no
additional filters are applied.

If the operation has completed successfully, instrumentations SHOULD NOT set `error.type`.

If a specific domain defines its own set of error identifiers (such as HTTP or gRPC status codes),
it's RECOMMENDED to:

* Use a domain-specific attribute
* Set `error.type` to capture all errors, regardless of whether they are defined within the domain-specific set or not.

**[2]:** If protocol version is subject to negotiation (for example using [ALPN](https://www.rfc-editor.org/rfc/rfc7301.html)), this attribute SHOULD be set to the negotiated version. If the actual protocol version is not known, this attribute SHOULD NOT be set.

**[3]:** When observed from the client side, and when communicating through an intermediary, `server.address` SHOULD represent the server address behind any intermediaries, for example proxies, if it's available.

**[4]:** When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD represent the server port behind any intermediaries, for example proxies, if it's available.



`error.type` has the following list of well-known values. If one of them applies, then the respective value MUST be used; otherwise, a custom value MAY be used.

| Value  | Description | Stability |
|---|---|---|
| `_OTHER` | A fallback error value to be used when the instrumentation doesn't define a custom value. | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |



<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->
<!-- END AUTOGENERATED TEXT -->
<!-- endsemconv -->

## Socket connection setup

The span describes the establishment of the socket connection.
It's different from `dotnet.http.connection_setup` span, which covers the DNS and TLS handshake duration in addition to socket connection setup.

Span name SHOULD be `socket connection_setup {network.peer.address}:{network.peer.port}`.
Span kind SHOULD be `CLIENT`.

Corresponding `Activity.OperationName` is `System.Net.Sockets.ConnectionSetup`, `ActivitySource` name - `System.Net.Sockets`.
Added in .NET Core 9.

<!-- semconv dotnet.socket.connection_setup -->
<!-- NOTE: THIS TEXT IS AUTOGENERATED. DO NOT EDIT BY HAND. -->
<!-- see templates/registry/markdown/snippet.md.j2 -->
<!-- prettier-ignore-start -->
<!-- markdownlint-capture -->
<!-- markdownlint-disable -->

| Attribute  | Type | Description  | Examples  | [Requirement Level](https://opentelemetry.io/docs/specs/semconv/general/attribute-requirement-level/) | Stability |
|---|---|---|---|---|---|
| [`error.type`](/docs/attributes-registry/error.md) | string | Describes a class of error the operation ended with. [1] | `timeout`; `java.net.UnknownHostException`; `server_certificate_invalid`; `500` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| [`network.peer.address`](/docs/attributes-registry/network.md) | string | Peer address of the network connection - IP address or Unix domain socket name. | `10.1.2.80`; `/tmp/my.sock` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| [`network.protocol.version`](/docs/attributes-registry/network.md) | string | The actual version of the protocol used for network communication. [2] | `1.1`; `2` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| [`network.transport`](/docs/attributes-registry/network.md) | string | [OSI transport layer](https://osi-model.com/transport-layer/) or [inter-process communication method](https://wikipedia.org/wiki/Inter-process_communication). [3] | `tcp`; `udp` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| [`network.type`](/docs/attributes-registry/network.md) | string | [OSI network layer](https://osi-model.com/network-layer/) or non-OSI equivalent. [4] | `ipv4`; `ipv6` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |

**[1]:** The `error.type` SHOULD be predictable, and SHOULD have low cardinality.

When `error.type` is set to a type (e.g., an exception type), its
canonical class name identifying the type within the artifact SHOULD be used.

Instrumentations SHOULD document the list of errors they report.

The cardinality of `error.type` within one instrumentation library SHOULD be low.
Telemetry consumers that aggregate data from multiple instrumentation libraries and applications
should be prepared for `error.type` to have high cardinality at query time when no
additional filters are applied.

If the operation has completed successfully, instrumentations SHOULD NOT set `error.type`.

If a specific domain defines its own set of error identifiers (such as HTTP or gRPC status codes),
it's RECOMMENDED to:

* Use a domain-specific attribute
* Set `error.type` to capture all errors, regardless of whether they are defined within the domain-specific set or not.

**[2]:** If protocol version is subject to negotiation (for example using [ALPN](https://www.rfc-editor.org/rfc/rfc7301.html)), this attribute SHOULD be set to the negotiated version. If the actual protocol version is not known, this attribute SHOULD NOT be set.

**[3]:** The value SHOULD be normalized to lowercase.

Consider always setting the transport when setting a port number, since
a port number is ambiguous without knowing the transport. For example
different processes could be listening on TCP port 12345 and UDP port 12345.

**[4]:** The value SHOULD be normalized to lowercase.



`error.type` has the following list of well-known values. If one of them applies, then the respective value MUST be used; otherwise, a custom value MAY be used.

| Value  | Description | Stability |
|---|---|---|
| `_OTHER` | A fallback error value to be used when the instrumentation doesn't define a custom value. | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |


`network.transport` has the following list of well-known values. If one of them applies, then the respective value MUST be used; otherwise, a custom value MAY be used.

| Value  | Description | Stability |
|---|---|---|
| `pipe` | Named or anonymous pipe. | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| `tcp` | TCP | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| `udp` | UDP | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| `unix` | Unix domain socket | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |


`network.type` has the following list of well-known values. If one of them applies, then the respective value MUST be used; otherwise, a custom value MAY be used.

| Value  | Description | Stability |
|---|---|---|
| `ipv4` | IPv4 | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| `ipv6` | IPv6 | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |



<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->
<!-- END AUTOGENERATED TEXT -->
<!-- endsemconv -->

## DNS

The span describes DNS lookup duration and outcome.

DNS lookup duration is also reported by [`dns.lookup.duration` metric](/docs/dotnet/dotnet-dns-metrics.md#metric-dnslookupduration).

Span name SHOULD be `DNS {dns.question.name}`.
Span kind SHOULD be `CLIENT`

Corresponding `Activity.OperationName` is `System.Net.NameResolution.DnsLookup`, `ActivitySource` name - `System.Net.NameResolution`.
Added in .NET Core 9

<!-- semconv dotnet.dns.lookup -->
<!-- NOTE: THIS TEXT IS AUTOGENERATED. DO NOT EDIT BY HAND. -->
<!-- see templates/registry/markdown/snippet.md.j2 -->
<!-- prettier-ignore-start -->
<!-- markdownlint-capture -->
<!-- markdownlint-disable -->

| Attribute  | Type | Description  | Examples  | [Requirement Level](https://opentelemetry.io/docs/specs/semconv/general/attribute-requirement-level/) | Stability |
|---|---|---|---|---|---|
| [`dns.answer`](/docs/attributes-registry/dns.md) | string[] | The list of resolved IPv4 or IPv6 addresses. | `["10.0.0.1", "2001:0db8:85a3:0000:0000:8a2e:0370:7334"]` | `Recommended` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| [`dns.question.name`](/docs/attributes-registry/dns.md) | string | The name being queried. [1] | `www.example.com`; `opentelemetry.io` | `Recommended` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| [`error.type`](/docs/attributes-registry/error.md) | string | Describes a class of error the operation ended with. [2] | `timeout`; `java.net.UnknownHostException`; `server_certificate_invalid`; `500` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |

**[1]:** If the name field contains non-printable characters (below 32 or above 126), those characters should be represented as escaped base 10 integers (\DDD). Back slashes and quotes should be escaped. Tabs, carriage returns, and line feeds should be converted to \t, \r, and \n respectively.

**[2]:** The `error.type` SHOULD be predictable, and SHOULD have low cardinality.

When `error.type` is set to a type (e.g., an exception type), its
canonical class name identifying the type within the artifact SHOULD be used.

Instrumentations SHOULD document the list of errors they report.

The cardinality of `error.type` within one instrumentation library SHOULD be low.
Telemetry consumers that aggregate data from multiple instrumentation libraries and applications
should be prepared for `error.type` to have high cardinality at query time when no
additional filters are applied.

If the operation has completed successfully, instrumentations SHOULD NOT set `error.type`.

If a specific domain defines its own set of error identifiers (such as HTTP or gRPC status codes),
it's RECOMMENDED to:

* Use a domain-specific attribute
* Set `error.type` to capture all errors, regardless of whether they are defined within the domain-specific set or not.



`error.type` has the following list of well-known values. If one of them applies, then the respective value MUST be used; otherwise, a custom value MAY be used.

| Value  | Description | Stability |
|---|---|---|
| `_OTHER` | A fallback error value to be used when the instrumentation doesn't define a custom value. | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |



<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->
<!-- END AUTOGENERATED TEXT -->
<!-- endsemconv -->

## TLS

The span describes TLS handshake.

Span name SHOULD be `TLS {dns.question.name}`. // TODO - decide on TLS!
Span kind SHOULD be `CLIENT`

Corresponding `Activity.OperationName` is `System.Net.Security.TlsHandshake`, `ActivitySource` name - `System.Net.Security`.
Added in .NET Core 9.

<!-- semconv dotnet.tls.handshake -->
<!-- NOTE: THIS TEXT IS AUTOGENERATED. DO NOT EDIT BY HAND. -->
<!-- see templates/registry/markdown/snippet.md.j2 -->
<!-- prettier-ignore-start -->
<!-- markdownlint-capture -->
<!-- markdownlint-disable -->

| Attribute  | Type | Description  | Examples  | [Requirement Level](https://opentelemetry.io/docs/specs/semconv/general/attribute-requirement-level/) | Stability |
|---|---|---|---|---|---|
| [`error.type`](/docs/attributes-registry/error.md) | string | Describes a class of error the operation ended with. [1] | `timeout`; `java.net.UnknownHostException`; `server_certificate_invalid`; `500` | `Recommended` | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |
| [`tls.client.server_name`](/docs/attributes-registry/tls.md) | string | Also called an SNI, this tells the server which hostname to which the client is attempting to connect to. | `opentelemetry.io` | `Recommended` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| [`tls.protocol.name`](/docs/attributes-registry/tls.md) | string | Normalized lowercase protocol name parsed from original string of the negotiated [SSL/TLS protocol version](https://www.openssl.org/docs/man1.1.1/man3/SSL_get_version.html#RETURN-VALUES) | `ssl`; `tls` | `Recommended` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| [`tls.protocol.version`](/docs/attributes-registry/tls.md) | string | Numeric part of the version parsed from the original string of the negotiated [SSL/TLS protocol version](https://www.openssl.org/docs/man1.1.1/man3/SSL_get_version.html#RETURN-VALUES) | `1.2`; `3` | `Recommended` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |

**[1]:** The `error.type` SHOULD be predictable, and SHOULD have low cardinality.

When `error.type` is set to a type (e.g., an exception type), its
canonical class name identifying the type within the artifact SHOULD be used.

Instrumentations SHOULD document the list of errors they report.

The cardinality of `error.type` within one instrumentation library SHOULD be low.
Telemetry consumers that aggregate data from multiple instrumentation libraries and applications
should be prepared for `error.type` to have high cardinality at query time when no
additional filters are applied.

If the operation has completed successfully, instrumentations SHOULD NOT set `error.type`.

If a specific domain defines its own set of error identifiers (such as HTTP or gRPC status codes),
it's RECOMMENDED to:

* Use a domain-specific attribute
* Set `error.type` to capture all errors, regardless of whether they are defined within the domain-specific set or not.



`error.type` has the following list of well-known values. If one of them applies, then the respective value MUST be used; otherwise, a custom value MAY be used.

| Value  | Description | Stability |
|---|---|---|
| `_OTHER` | A fallback error value to be used when the instrumentation doesn't define a custom value. | ![Stable](https://img.shields.io/badge/-stable-lightgreen) |


`tls.protocol.name` has the following list of well-known values. If one of them applies, then the respective value MUST be used; otherwise, a custom value MAY be used.

| Value  | Description | Stability |
|---|---|---|
| `ssl` | ssl | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| `tls` | tls | ![Experimental](https://img.shields.io/badge/-experimental-blue) |



<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->
<!-- END AUTOGENERATED TEXT -->
<!-- endsemconv -->

## Examples

[DocumentStatus]: https://opentelemetry.io/docs/specs/otel/document-status