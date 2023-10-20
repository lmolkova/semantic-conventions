<!--- Hugo front matter used to generate the website version of this page:
linkTitle: Azure SDK
--->

# Semantic conventions for Azure SDK spans

**Status**: [Experimental][DocumentStatus]

This document describes tracing semantic conventions adopted by Azure SDK. Instrumentations live in Azure SDK repos and are shipped along with Azure SDK artifacts.

- [Java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core/azure-core-tracing-opentelemetry)
- [JavaScript](https://github.com/Azure/azure-sdk-for-js/tree/main/sdk/core/core-tracing)
- [Python](https://github.com/Azure/azure-sdk-for-python/tree/main/sdk/core/azure-core-tracing-opentelemetry)
- [.NET](https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/core/Azure.Core/samples/Diagnostics.md#activitysource-support)
- [Go](https://github.com/Azure/azure-sdk-for-go/blob/main/sdk/tracing/azotel)
- [C++](https://github.com/Azure/azure-sdk-for-cpp/tree/main/sdk/core/azure-core-tracing-opentelemetry)

Azure SDK produces spans for public API calls and nested HTTP client spans. AMQP transport-level calls are not traced.

## Versioning

Azure SDKs follow OpenTelemetry semantic conventions when applicable, but adopt new versions of conventions at their own pace. Telemetry consumers MAY use [SchemaUrl](https://opentelemetry.io/docs/specs/otel/schemas/#schema-url) to detect which version of semantic conventions are emitted by Azure SDKs.

## Public API calls

Azure SDK SHOULD create a span for each call to service methods, that is, public APIs that involve communication with Azure services.

- Spans representing public API calls SHOULD have names following `client.method` pattern and are language-specific. In case OpenTelemetry defines semantic convention for span name (for example, in messaging or database conventions), standard OpenTelemetry name SHOULD be used instead.
- For HTTP-based SDKs, public API spans SHOULD have `INTERNAL` kind.

See [Messaging](#messaging-sdks) section below and [CosmosDB conventions](/docs/database/cosmosdb.md) for non-HTTP semantics.

API-level spans produced by Azure SDK have the following attributes:

<!-- semconv azure.sdk.api(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `az.namespace` | string | [Namespace](https://docs.microsoft.com/azure/azure-resource-manager/management/azure-services-resource-providers) of Azure service request is made against. [1] | `Microsoft.Storage`; `Microsoft.KeyVault`; `Microsoft.ServiceBus` | Required |
| `az.schema_url` | string | OpenTelemetry Schema URL including schema version. Only 1.23.0 is supported. | `https://opentelemetry.io/schemas/1.23.0` | Conditionally Required: [2] |
| `error.type` | string | Describes a class of error the operation ended with. [3] | `java.net.UnknownHostException`; `System.Threading.Tasks.OperationCanceledException`; `azure.core.exceptions.ServiceRequestError` | Recommended |

**[1]:** This SHOULD be set as an instrumentation scope attribute when creating a `Tracer` as long as OpenTelemetry in a given language allows to do so.

**[2]:** if and only if OpenTelemetry in a given language doesn't provide a standard way to set schema_url (i.e. .NET)

**[3]:** The `error.type` SHOULD be predictable and SHOULD have low cardinality.
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

`error.type` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `_OTHER` | A fallback error value to be used when the instrumentation doesn't define a custom value. |
<!-- endsemconv -->

## HTTP Client

Azure SDK implements a valid subset of stable part of [OpenTelemetry HTTP spans conventions](/docs/http/http-spans.md) and create a span per HTTP call (attempt).

<!-- semconv azure.sdk.http -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `az.client_request_id` | string | Value of the [x-ms-client-request-id] header (or other request-id header, depending on the service) sent by the client. | `eb178587-c05a-418c-a695-ae9466c5303c` | Conditionally Required: only if present. |
| `az.namespace` | string | [Namespace](https://docs.microsoft.com/azure/azure-resource-manager/management/azure-services-resource-providers) of Azure service request is made against. [1] | `Microsoft.Storage`; `Microsoft.KeyVault`; `Microsoft.ServiceBus` | Required |
| `az.schema_url` | string | OpenTelemetry Schema URL including schema version. Only 1.23.0 is supported. | `https://opentelemetry.io/schemas/1.23.0` | Conditionally Required: [2] |
| `az.service_request_id` | string | Value of the [x-ms-request-id]  header (or other request-id header, depending on the service) sent by the server in response. | `3f828ae5-ecb9-40ab-88d9-db0420af30c6` | Conditionally Required: if and only if one was received |
| `error.type` | string | Describes a class of error the operation ended with. [3] | `timeout`; `java.net.UnknownHostException`; `server_certificate_invalid`; `500` | Conditionally Required: If request has ended with an error. |
| [`http.request.method`](../attributes-registry/http.md) | string | HTTP request method. [4] | `GET`; `POST`; `HEAD` | Required |
| [`http.request.resend_count`](../attributes-registry/http.md) | int | The ordinal number of request resending attempt (for any reason, including redirects). [5] | `3` | Recommended |
| [`http.response.status_code`](../attributes-registry/http.md) | int | [HTTP response status code](https://tools.ietf.org/html/rfc7231#section-6). | `200` | Conditionally Required: If and only if one was received/sent. |
| [`server.address`](../general/attributes.md) | string | Host identifier of the ["URI origin"](https://www.rfc-editor.org/rfc/rfc9110.html#name-uri-origin) HTTP request is sent to. [6] | `example.com`; `10.1.2.80`; `/tmp/my.sock` | Required |
| [`server.port`](../general/attributes.md) | int | Port identifier of the ["URI origin"](https://www.rfc-editor.org/rfc/rfc9110.html#name-uri-origin) HTTP request is sent to. [7] | `80`; `8080`; `443` | Conditionally Required: [8] |
| [`url.full`](../attributes-registry/url.md) | string | Absolute URL describing a network resource according to [RFC3986](https://www.rfc-editor.org/rfc/rfc3986) [9] | `https://www.foo.bar/search?q=OpenTelemetry#SemConv`; `//localhost` | Recommended |
| `user_agent.original` | string | Value of the [HTTP User-Agent](https://www.rfc-editor.org/rfc/rfc9110.html#field.user-agent) header sent by the client. | `CERN-LineMode/2.15 libwww/2.17b3` | Recommended |

**[1]:** This SHOULD be set as an instrumentation scope attribute when creating a `Tracer` as long as OpenTelemetry in a given language allows to do so.

**[2]:** if and only if OpenTelemetry in a given language doesn't provide a standard way to set schema_url (i.e. .NET)

**[3]:** If the request fails with an error before response status code was sent or received,
`error.type` SHOULD be set to exception type (its fully-qualified class name, if applicable)
or a component-specific low cardinality error identifier.

If response status code was sent or received and status indicates an error according to [HTTP span status definition](/docs/http/http-spans.md),
`error.type` SHOULD be set to the status code number (represented as a string), an exception type (if thrown) or a component-specific error identifier.

The `error.type` value SHOULD be predictable and SHOULD have low cardinality.
Instrumentations SHOULD document the list of errors they report.

The cardinality of `error.type` within one instrumentation library SHOULD be low, but
telemetry consumers that aggregate data from multiple instrumentation libraries and applications
should be prepared for `error.type` to have high cardinality at query time, when no
additional filters are applied.

If the request has completed successfully, instrumentations SHOULD NOT set `error.type`.

**[4]:** Azure SDKs support "known" methods as the ones listed in [RFC9110](https://www.rfc-editor.org/rfc/rfc9110.html#name-methods) and the PATCH method defined in [RFC5789](https://www.rfc-editor.org/rfc/rfc5789.html)

**[5]:** The resend count SHOULD be updated each time an HTTP request gets resent by the client, regardless of what was the cause of the resending (e.g. redirection, authorization failure, 503 Server Unavailable, network issues, or any other).

**[6]:** Describes host component of Azure service endpoint.

**[7]:** Describes port component of Azure service endpoint.

**[8]:** If not default (`80` for `http` scheme, `443` for `https`).

**[9]:** For network calls, URL usually has `scheme://host[:port][path][?query][#fragment]` format, where the fragment is not transmitted over HTTP, but if it is known, it should be included nevertheless.
`url.full` MUST NOT contain credentials passed via URL in form of `https://username:password@www.example.com/`. In such case username and password should be redacted and attribute's value should be `https://REDACTED:REDACTED@www.example.com/`.
`url.full` SHOULD capture the absolute URL when it is available (or can be reconstructed) and SHOULD NOT be validated or modified except for sanitizing purposes.
<!-- endsemconv -->

Instrumentation supports [W3C Trace context](https://w3c.github.io/trace-context/) propagation and Azure legacy correlation protocols. Propagator configuration is not supported.

## Messaging SDKs

Messaging span semantics apply to Azure Event Hubs and Service Bus SDKs and follow [OpenTelemetry Messaging spans conventions v1.22.0](https://github.com/open-telemetry/semantic-conventions/blob/v1.22.0/docs/messaging/messaging-spans.md).

Azure SDK will update messaging semantic conventions as messaging specification evolves.

Messaging SDKs produce three kinds of spans:

- `PRODUCER` - describes message creation and associates unique context with the message to trace them when they are sent in batches.
- `CLIENT` - describes message (or batch) publishing.
  - It has links pointing to each message being sent.
- `CONSUMER` - describes message (or batch) processing.
  - It is created when user leverages handler APIs that wrap message or batch processing.
  - Processing span has links to each message being processed (when context is present).

### Messaging attributes

<!-- semconv azure.sdk.messaging -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `az.namespace` | string | [Namespace](https://docs.microsoft.com/azure/azure-resource-manager/management/azure-services-resource-providers) of Azure service request is made against. [1] | `Microsoft.Storage`; `Microsoft.KeyVault`; `Microsoft.ServiceBus` | Required |
| `az.schema_url` | string | OpenTelemetry Schema URL including schema version. Only 1.23.0 is supported. | `https://opentelemetry.io/schemas/1.23.0` | Conditionally Required: [2] |
| [`messaging.batch.message_count`](../messaging/messaging-spans.md) | int | The number of messages sent, received, or processed in the scope of the batching operation. [3] | `0`; `1`; `2` | Conditionally Required: [4] |
| `messaging.destination.name` | string | The message destination name [5] | `MyQueue`; `MyTopic` | Recommended |
| `messaging.message.id` | string | A value used by the messaging system as an identifier for the message, represented as a string. | `452a7c7c7c7048c2f887f61572b18fc2` | Recommended |
| [`messaging.operation`](../messaging/messaging-spans.md) | string | A string identifying the kind of messaging operation as defined in the [Operation names](#operation-names) section above. [6] | `publish` | Required |
| [`messaging.system`](../messaging/messaging-spans.md) | string | A string identifying the messaging system. | `eventhubs`; `servicebus` | Required |
| [`server.address`](../general/attributes.md) | string | Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name. [7] | `example.com`; `10.1.2.80`; `/tmp/my.sock` | Recommended |
| [`server.port`](../general/attributes.md) | int | Server port number. [8] | `80`; `8080`; `443` | Recommended |

**[1]:** This SHOULD be set as an instrumentation scope attribute when creating a `Tracer` as long as OpenTelemetry in a given language allows to do so.

**[2]:** if and only if OpenTelemetry in a given language doesn't provide a standard way to set schema_url (i.e. .NET)

**[3]:** Instrumentations SHOULD NOT set `messaging.batch.message_count` on spans that operate with a single message. When a messaging client library supports both batch and single-message API for the same operation, instrumentations SHOULD use `messaging.batch.message_count` for batching APIs and SHOULD NOT use it for single-message APIs.

**[4]:** If the span describes an operation on a batch of messages.

**[5]:** Destination name SHOULD uniquely identify a specific queue, topic or other entity within the broker. If
the broker doesn't have such notion, the destination name SHOULD uniquely identify the broker.

**[6]:** If a custom value is used, it MUST be of low cardinality.

**[7]:** When observed from the client side, and when communicating through an intermediary, `server.address` SHOULD represent the server address behind any intermediaries, for example proxies, if it's available.

**[8]:** When observed from the client side, and when communicating through an intermediary, `server.port` SHOULD represent the server port behind any intermediaries, for example proxies, if it's available.

Following attributes MUST be provided **at span creation time** (when provided at all), so they can be considered for sampling decisions:

* `messaging.destination.name`
* [`messaging.system`](../messaging/messaging-spans.md)
<!-- endsemconv -->
