<!--- Hugo front matter used to generate the website version of this page:
linkTitle: Container
--->

# Semantic Conventions for Container Metrics

**Status**: [Experimental][DocumentStatus]

## Container Metrics

### Metric: `container.cpu.time`

This metric is [opt-in][MetricOptIn].

<!-- semconv metric.container.cpu.time(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    | Stability |
| -------- | --------------- | ----------- | -------------- | --------- |
| `container.cpu.time` | Counter | `s` | Total CPU time consumed [1] | ![Experimental](https://img.shields.io/badge/-experimental-blue) |

**[1]:** Total CPU time consumed by the specific container on all available CPU cores
<!-- endsemconv -->

<!-- semconv metric.container.cpu.time(full) -->
| Attribute  | Type | Description  | Examples  | [Requirement Level](https://opentelemetry.io/docs/specs/semconv/general/attribute-requirement-level/) | Stability |
|---|---|---|---|---|---|
| [`container.cpu.state`](/docs/attributes-registry/container.md) | string | The CPU state for this data point. A container SHOULD be characterized _either_ by data points with no `state` labels, _or only_ data points with `state` labels. | `user`; `kernel` | `Opt-In` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |

`container.cpu.state` has the following list of well-known values. If one of them applies, then the respective value MUST be used; otherwise, a custom value MAY be used.

| Value  | Description | Stability |
|---|---|---|
| `user` | When tasks of the cgroup are in user mode (Linux). When all container processes are in user mode (Windows). | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| `system` | When CPU is used by the system (host OS) | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| `kernel` | When tasks of the cgroup are in kernel mode (Linux). When all container processes are in kernel mode (Windows). | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
<!-- endsemconv -->

### Metric: `container.memory.usage`

This metric is [opt-in][MetricOptIn].

<!-- semconv metric.container.memory.usage(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    | Stability |
| -------- | --------------- | ----------- | -------------- | --------- |
| `container.memory.usage` | Counter | `By` | Memory usage of the container. [1] | ![Experimental](https://img.shields.io/badge/-experimental-blue) |

**[1]:** Memory usage of the container.
<!-- endsemconv -->

<!-- semconv metric.container.memory.usage(full) -->
<!-- endsemconv -->

### Metric: `container.disk.io`

This metric is [opt-in][MetricOptIn].

<!-- semconv metric.container.disk.io(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    | Stability |
| -------- | --------------- | ----------- | -------------- | --------- |
| `container.disk.io` | Counter | `By` | Disk bytes for the container. [1] | ![Experimental](https://img.shields.io/badge/-experimental-blue) |

**[1]:** The total number of bytes read/written successfully (aggregated from all disks).
<!-- endsemconv -->

<!-- semconv metric.container.disk.io(full) -->
| Attribute  | Type | Description  | Examples  | [Requirement Level](https://opentelemetry.io/docs/specs/semconv/general/attribute-requirement-level/) | Stability |
|---|---|---|---|---|---|
| [`disk.io.direction`](/docs/attributes-registry/disk.md) | string | The disk IO operation direction. | `read` | `Recommended` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| [`system.device`](/docs/attributes-registry/system.md) | string | The device identifier | `(identifier)` | `Recommended` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |

`disk.io.direction` has the following list of well-known values. If one of them applies, then the respective value MUST be used; otherwise, a custom value MAY be used.

| Value  | Description | Stability |
|---|---|---|
| `read` | read | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| `write` | write | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
<!-- endsemconv -->

### Metric: `container.network.io`

This metric is [opt-in][MetricOptIn].

<!-- semconv metric.container.network.io(metric_table) -->
| Name     | Instrument Type | Unit (UCUM) | Description    | Stability |
| -------- | --------------- | ----------- | -------------- | --------- |
| `container.network.io` | Counter | `By` | Network bytes for the container. [1] | ![Experimental](https://img.shields.io/badge/-experimental-blue) |

**[1]:** The number of bytes sent/received on all network interfaces by the container.
<!-- endsemconv -->

<!-- semconv metric.container.network.io(full) -->
| Attribute  | Type | Description  | Examples  | [Requirement Level](https://opentelemetry.io/docs/specs/semconv/general/attribute-requirement-level/) | Stability |
|---|---|---|---|---|---|
| [`network.io.direction`](/docs/attributes-registry/network.md) | string | The network IO operation direction. | `transmit` | `Recommended` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| [`system.device`](/docs/attributes-registry/system.md) | string | The device identifier | `(identifier)` | `Recommended` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |

`network.io.direction` has the following list of well-known values. If one of them applies, then the respective value MUST be used; otherwise, a custom value MAY be used.

| Value  | Description | Stability |
|---|---|---|
| `transmit` | transmit | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| `receive` | receive | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
<!-- endsemconv -->

[DocumentStatus]: https://github.com/open-telemetry/opentelemetry-specification/tree/v1.22.0/specification/document-status.md
[MetricOptIn]: https://github.com/open-telemetry/opentelemetry-specification/tree/v1.31.0/specification/metrics/metric-requirement-level.md#opt-in
