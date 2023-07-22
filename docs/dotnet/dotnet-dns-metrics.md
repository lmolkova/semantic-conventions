# Semantic Conventions for DNS metrics emitted by .NET

**Status**: [Experimental][DocumentStatus]

This document defines semantic conventions for DNS metrics emitted by .NET.

**Disclaimer:** These are initial .NET metric instruments available in .NET 8 but more may be added in the future.

<!-- toc -->

- [DNS metrics](#dns-metrics)
  * [Metric: `dns.lookups.duration`](#metric-dnslookupsduration)

<!-- tocstop -->

## DNS metrics

### Metric: `dns.lookups.duration`

**Status**: [Experimental][DocumentStatus]

This metric is required.

This metric SHOULD be specified with
[`ExplicitBucketBoundaries`](https://github.com/open-telemetry/opentelemetry-specification/tree/v1.22.0/specification/metrics/api.md#instrument-advice)
of **`[TODO]`**.

<!-- semconv metric.dotnet.dns.lookup.duration(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    |
| -------- | --------------- | ----------- | -------------- |
| `dns.lookup.duration` | Histogram | `s` | Measures the time take to perform a DNS lookup. [1] |

**[1]:** Meter name is `System.Net.NameResolution`.
<!-- endsemconv -->

<!-- semconv metric.dotnet.dns.lookup.duration(full) -->
| Attribute  | Type | Description  | Examples  | Requirement Level |
|---|---|---|---|---|
| `dns.question.name` | string | The name being queried. [1] | `www.example.com`; `dot.net` | Required |

**[1]:** The name being queried.

If the name field contains non-printable
characters (below 32 or above 126), those characters should be represented
as escaped base 10 integers (\DDD). Back slashes and quotes should be escaped.
Tabs, carriage returns, and line feeds should be converted to \t, \r, and
\n respectively.
<!-- endsemconv -->


[DocumentStatus]: https://github.com/open-telemetry/opentelemetry-specification/tree/v1.22.0/specification/document-status.md
