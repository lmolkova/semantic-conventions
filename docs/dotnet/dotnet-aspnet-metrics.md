# Semantic Conventions for ASP.NEt Core specific metrics

**Status**: [Experimental][DocumentStatus]

This document defines semantic conventions for ASP.NET Core metrics, not specific to HTTP

**Disclaimer:** These are initial .NET metric instruments available in .NET 8 but more may be added in the future.

<!-- toc -->

- [Routing](#routing)
  * [Metric: `aspnet.routing.matches`](#metric-aspnetroutingmatches)
- [Exception metrics](#exception-metrics)
  * [Metric: `aspnet.diagnostics_handler.exceptions`](#metric-aspnetdiagnostics_handlerexceptions)
- [Rate-limiting](#rate-limiting)
  * [Metric: `aspnet.rate_limiting.active_request_leases`](#metric-aspnetrate_limitingactive_request_leases)
  * [Metric: `aspnet.rate_limiting.request_lease.duration`](#metric-aspnetrate_limitingrequest_leaseduration)
  * [Metric: `aspnet.rate_limiting.queued_requests`](#metric-aspnetrate_limitingqueued_requests)
  * [Metric: `aspnet.rate_limiting.queued_requests.duration`](#metric-aspnetrate_limitingqueued_requestsduration)
  * [Metric: `aspnet.rate_limiting.requests`](#metric-aspnetrate_limitingrequests)

<!-- tocstop -->

## Routing

All routing metrics are reported by `Microsoft.AspNetCore.Routing` meter.

### Metric: `aspnet.routing.matches`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.aspnet.routing.matches(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `aspnet.routing.successful_matches` | Counter | `{match}` | Number of requests that successfully matched to an endpoint. [1] |

**[1]:** An unmatched request may be handled by later middleware, such as the static files or authentication middleware. Meter name is `Microsoft.AspNetCore.Routing`.
<!-- endsemconv -->

<!-- semconv metric.aspnet.routing.matches(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `aspnet.match_status` | string | Match result - success or failure | `success`; `failure` | Required |
| `aspnet.routing.fallback` | string | TODO | `TODO` | Required |
| `http.route` | string | The matched route (path template in the format used by the respective server framework). See note below [1] | `/users/:userID?`; `{controller}/{action}/{id?}` | Required |

**[1]:** MUST NOT be populated when this is not supported by the HTTP server framework as the route attribute should have low-cardinality and the URI path can NOT substitute it.
SHOULD include the [application root](/docs/http/http-spans.md#http-server-definitions) if there is one.

`aspnet.match_status` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `success` | matched |
| `failure` | failure |
<!-- endsemconv -->

## Exception metrics

Metrics reported by `Microsoft.AspNetCore.Diagnostics` meter.

### Metric: `aspnet.diagnostics_handler.exceptions`

**Status**: [Experimental][DocumentStatus]

This metric is required.
<!-- semconv metric.aspnet.diagnostics_handler.exceptions(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `aspnet.diagnostics_handler.exceptions` | Counter | `{exception}` | Number of exceptions caught by exception handling middleware. [1] |

**[1]:** Meter name is `Microsoft.AspNetCore.Diagnostics`
<!-- endsemconv -->

<!-- semconv metric.aspnet.diagnostics_handler.exceptions(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `aspnet.handler` | string | TODO | `TODO` | Required |
| `dotnet.error.code` | string | General-purpose error code reported by .NET, as a starter it supports terminal statuses of .NET task https://learn.microsoft.com/en-us/dotnet/api/system.threading.tasks.taskstatus. | `Canceled`; `RanToCompletion` | Recommended |
| `exception.type` | string | The type of the exception (its fully-qualified class name, if applicable). The dynamic type of the exception should be preferred over the static type in languages that support it. | `java.net.ConnectException`; `OSError` | Recommended |

`dotnet.error.code` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `RanToCompletion` | No error |
| `Canceled` | Canceled |
| `Faulted` | Faulted [1] |

**[1]:** When error code is set to `other`, it's recommended accompany this attribute with a domain-specific error code when it's known, such as `http.request.error` or `http.response.status_code` for HTTP errors.
<!-- endsemconv -->

## Rate-limiting

All rate-limiting metrics are reported by `Microsoft.AspNetCore.RateLimiting` meter.

### Metric: `aspnet.rate_limiting.active_request_leases`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.aspnet.rate_limiting.active_request_leases(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `aspnet.rate_limiting.active_request_leases` | UpDownCounter | `{request}` | Number of requests that are currently active on the server that hold a rate limiting lease. [1] |

**[1]:** Meter name is `Microsoft.AspNetCore.RateLimiting`
<!-- endsemconv -->

<!-- semconv metric.aspnet.rate_limiting.active_request_leases(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `aspnet.rate_limiting.policy` | string | TODO | `TODO` | Required |
<!-- endsemconv -->

### Metric: `aspnet.rate_limiting.request_lease.duration`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.aspnet.rate_limiting.request_lease.duration(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `aspnet.rate_limiting.request_lease.duration` | Histogram | `s` | The duration of rate limiting lease held by requests on the server. [1] |

**[1]:** Meter name is `Microsoft.AspNetCore.RateLimiting`
**TODO do we need attributes?, can we explain what it means better**`
<!-- endsemconv -->

<!-- semconv metric.aspnet.rate_limiting.request_lease.duration(full) -->
<!-- endsemconv -->

### Metric: `aspnet.rate_limiting.queued_requests`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.aspnet.rate_limiting.queued_requests(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `aspnet.rate_limiting.queued_requests` | UpDownCounter | `{request}` | Number of requests that are currently queued, waiting to acquire a rate limiting lease. [1] |

**[1]:** Meter name is `Microsoft.AspNetCore.RateLimiting`
<!-- endsemconv -->

<!-- semconv metric.aspnet.rate_limiting.queued_requests(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `aspnet.rate_limiting.policy` | string | TODO | `TODO` | Required |
<!-- endsemconv -->

### Metric: `aspnet.rate_limiting.queued_requests.duration`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.aspnet.rate_limiting.queued_request.duration(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `aspnet.rate_limiting.queued_request.duration` | Histogram | `s` | The duration of request in a queue, waiting to acquire a rate limiting lease. [1] |

**[1]:** Meter name is `Microsoft.AspNetCore.RateLimiting`

**TODO: I don't really understand what this duration is, can we improve name, brief or description to explain? **
<!-- endsemconv -->

<!-- semconv metric.aspnet.rate_limiting.queued_request.duration(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `aspnet.rate_limiting.policy` | string | TODO | `TODO` | Required |
<!-- endsemconv -->

### Metric: `aspnet.rate_limiting.requests`

**Status**: [Experimental][DocumentStatus]

This metric is required.

<!-- semconv metric.aspnet.rate_limiting.requests(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `aspnet.rate_limiting.requests` | Counter | `{request}` | Number of requests that tried to acquire a rate limiting lease. [1] |

**[1]:** Requests could be rejected by global or endpoint rate limiting policies. Or the request could be cancelled while waiting for the lease.

Meter name is `Microsoft.AspNetCore.RateLimiting`
<!-- endsemconv -->

<!-- semconv metric.aspnet.rate_limiting.requests(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `aspnet.rate_limiting.policy` | string | TODO | `TODO` | Required |
| `aspnet.rate_limiting.result` | string | Rate-limiting result, shows whether lease was acquired or contains rejection reason | `acquired`; `rejected_reason1` | Recommended |

`aspnet.rate_limiting.result` has the following list of well-known values. If one of them applies, then the respective value MUST be used, otherwise a custom value MAY be used.

| Value  | Description |
|---|---|
| `acquired` | lease acquired |
| `rejected_reason1` | TODO |
<!-- endsemconv -->


[DocumentStatus]: https://github.com/open-telemetry/opentelemetry-specification/tree/v1.22.0/specification/document-status.md