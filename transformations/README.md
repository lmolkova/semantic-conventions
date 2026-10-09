# Semantic convention transformations

<!-- disable markdownlint requirement for tables to be aligned -->
<!-- markdownlint-disable-file MD060 -->

> [!WARNING]
> Experimental. The file format below is a draft ("transform/2.0"). Nothing
> consumes these files yet, apart from a query-rewrite prototype that was used
> to validate them.

This folder holds machine-readable versions of the
[migration guides](/docs/non-normative/) and of the
[gRPC compatibility guide](/docs/non-normative/compatibility/grpc.md).
Each file is a **transformation set**: a list of
[OTEP #4738 Telemetry Policy](https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/4738-telemetry-policy.md)
policies plus a header that ties them to a source and a target schema version.

A transformation set can be applied in two ways. The policies are the same in both:

- **Data mode.** Convert telemetry records (OTLP metrics, spans, logs, resources),
  for example in a Collector processor.
- **Schema mode.** Apply the policies to the definitions in a resolved schema
  instead. Query rewriters, dashboard and alert migration tools, and validators
  use this mode. It never touches stored data: queries written against the
  target version are rewritten to also read data in the source shape.

Both modes MUST agree: converting the data and then running a query against the
target gives the same result as running the rewritten query on the original data.

- [Files](#files)
- [Syntax](#syntax)
  - [File structure](#file-structure)
  - [Matchers](#matchers)
  - [Keep](#keep)
  - [Transform](#transform)
  - [Entities](#entities)
  - [Evaluation](#evaluation)
  - [Safety](#safety)
  - [What can't be expressed](#what-cant-be-expressed)
- [Conventions used in these files](#conventions-used-in-these-files)
- [What isn't covered](#what-isnt-covered)

## Files

Each guide has one file per direction. The reverse direction is written on its own, not inverted:
nothing can undo a `remove`, and a change that can be bridged one way often can't be bridged the other way.

| Area | File | Source → target | Guide |
|---|---|---|---|
| Code attributes | [`code/1.29.0_to_1.33.0.yaml`](code/1.29.0_to_1.33.0.yaml) | 1.29.0 → 1.33.0 | [code-attrs-migration](/docs/non-normative/code-attrs-migration.md) |
| | [`code/1.33.0_to_1.29.0.yaml`](code/1.33.0_to_1.29.0.yaml) | 1.33.0 → 1.29.0 | |
| Database | [`db/1.24.0_to_1.33.0.yaml`](db/1.24.0_to_1.33.0.yaml) | 1.24.0 → 1.33.0 | [db-migration](/docs/non-normative/db-migration.md) |
| | [`db/1.33.0_to_1.24.0.yaml`](db/1.33.0_to_1.24.0.yaml) | 1.33.0 → 1.24.0 | |
| HTTP | [`http/1.20.0_to_1.23.1.yaml`](http/1.20.0_to_1.23.1.yaml) | 1.20.0 → 1.23.1 | [http-migration](/docs/non-normative/http-migration.md) |
| | [`http/1.23.1_to_1.20.0.yaml`](http/1.23.1_to_1.20.0.yaml) | 1.23.1 → 1.20.0 | |
| Kubernetes | [`k8s/collector-1.18.0_to_1.44.0.yaml`](k8s/collector-1.18.0_to_1.44.0.yaml) | Collector (1.18.0) → 1.44.0 | [k8s-migration](/docs/non-normative/k8s-migration.md) |
| | [`k8s/1.44.0_to_collector-1.18.0.yaml`](k8s/1.44.0_to_collector-1.18.0.yaml) | 1.44.0 → Collector (1.18.0) | |
| RPC | [`rpc/1.37.0_to_1.45.0.yaml`](rpc/1.37.0_to_1.45.0.yaml) | 1.37.0 → "1.45.0" | [rpc-migration](/docs/non-normative/rpc-migration.md) |
| | [`rpc/1.45.0_to_1.37.0.yaml`](rpc/1.45.0_to_1.37.0.yaml) | "1.45.0" → 1.37.0 | |
| gRPC (native) | [`grpc/grpc-42.42.0_to_1.45.0.yaml`](grpc/grpc-42.42.0_to_1.45.0.yaml) | gRPC A66/A72 → "1.45.0" | [compatibility/grpc](/docs/non-normative/compatibility/grpc.md) |
| | [`grpc/1.45.0_to_grpc-42.42.0.yaml`](grpc/1.45.0_to_grpc-42.42.0.yaml) | "1.45.0" → gRPC A66/A72 | |
| GenAI | [`gen-ai/1.41.0_to_gen-ai-dev-1.42.0-dev.yaml`](gen-ai/1.41.0_to_gen-ai-dev-1.42.0-dev.yaml) | 1.41.0 → gen-ai-dev 1.42.0-dev | [GenAI migration](https://github.com/lmolkova/semantic-conventions-genai/blob/1780533536101fee409056c8ff24ed99ab861f0a/docs/gen-ai/non-normative/migration.md) |
| | [`gen-ai/gen-ai-dev-1.42.0-dev_to_1.41.0.yaml`](gen-ai/gen-ai-dev-1.42.0-dev_to_1.41.0.yaml) | gen-ai-dev 1.42.0-dev → 1.41.0 | |

## Syntax

The rule syntax and evaluation model come from
[OTEP #4738](https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/4738-telemetry-policy.md).
This section summarizes them and lists what transformation sets add.
MUST, MUST NOT, SHOULD and MAY follow RFC 2119.

### File structure

```yaml
file_format: transform/2.0
source_schema_url: https://opentelemetry.io/schemas/1.20.0   # required
target_schema_url: https://opentelemetry.io/schemas/1.23.1   # required, must differ

policies:
  - id: http-metric-server-duration          # required, unique in the set
    name: http.server.duration becomes http.server.request.duration   # required
    description: Optional. Why, and caveats.
    metric:                                  # exactly one target
      match:
        - {metric_field: name, exact: http.server.duration}
        - {datapoint_attribute: [http.method], exists: true}
      transform:
        scale: [{metric_field: unit, from: ms, to: s}]
        rename: [{metric_field: name, to: http.server.request.duration}]
```

`policies` is a list of OTEP #4738 `Policy` objects: `id` and `name` (required),
`description`, `enabled` and `labels` (optional), and exactly one target:

| Target | Defined by | Applies to |
|---|---|---|
| `metric` | [OTEP #4738](https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/4738-telemetry-policy.md) | Metrics |
| `trace` | [OTEP #4738](https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/4738-telemetry-policy.md) | Spans |
| `log` | [OTEP #4738](https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/4738-telemetry-policy.md) | Log records and events. Events match on `log_field: event_name`. |
| `entity` | transformation sets | Resources (resource attributes) |

A change that affects several signals needs one policy per signal. For example,
an attribute renamed on spans and on metrics needs a `trace` policy and a
`metric` policy. Signal targets only touch the signal's own name, fields and
attributes; resource attributes change only through `entity` policies.

One set covers the whole migration from source to target, with no chaining. If a
migration spans several releases, the set holds the changes of all of them,
including names that only existed in an intermediate release. A set MAY be split
across several files with identical headers; the policy lists are concatenated
and duplicate ids are an error. Parsers MUST reject unknown top-level keys.

### Matchers

Matchers are [OTEP #4738](https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/4738-telemetry-policy.md)'s, unchanged.
`match` is a non-empty list, and all entries must hold. Each entry names one field and one operator:

```yaml
match:
  - {metric_field: name, exact: gen_ai.client.token.usage}
  - {datapoint_attribute: [gen_ai.token.type], exact: input}
  - {span_kind: client}
  - {trace_field: name, starts_with: "Sent."}
  - {span_attribute: [db.system], regex: "^(postgresql|oracle)$", negate: true}
  - {log_field: event_name, exact: gen_ai.client.inference.operation.details}
```

- Operators: `exact`, `regex` (RE2, unanchored), `exists` (`true`/`false`), `starts_with`, `ends_with`, `contains`.
- Modifiers: `negate`, `case_insensitive`.
- `negate` on an absent attribute matches. For example,
  `{ span_attribute: [gen_ai.operation.name], exact: retrieval, negate: true }`
  holds for spans without `gen_ai.operation.name`.

| Target | Fields |
|---|---|
| `metric` | `metric_field` (`name`, `unit`, ...), `metric_type`, `datapoint_attribute` |
| `trace` | `trace_field` (`name`, ...), `span_kind`, `span_status`, `span_attribute`, `event_name`, `event_attribute` |
| `log` | `log_field` (including `event_name`), `log_attribute` |
| `entity` | `entity_field` (`type`), `resource_attribute` |

Span type isn't on OTLP, so `trace` policies can't match on it.

### Keep

The keep stage is [OTEP #4738](https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/4738-telemetry-policy.md)'s,
restricted to dropping everything: `keep: false` (or `keep: none` on logs). It
means "this data has no equivalent in the target". Sampling values MUST NOT
appear: a migration either converts data or doesn't. A dropping policy MUST NOT
have a `transform`.

The files in this folder don't drop anything. Data without an equivalent is
left as-is, because its names don't collide with anything in the target.

### Transform

The transform shape is [OTEP #4738](https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/4738-telemetry-policy.md)'s `LogTransform`:
one list per operation, each entry naming one field. OTEP #4738 defines it for
logs. Transformation sets use the same shape for every target and add `convert`,
`scale` and `set_role`.

```yaml
transform:
  remove: [{span_attribute: [net.sock.family]}]
  redact: [{span_attribute: [db.statement], replacement: "[REDACTED]"}]
  convert: [{span_attribute: [gen_ai.request.top_k], type: int}]    # int | double | bool | string
  scale: [{metric_field: unit, from: ms, to: s}]                 # metric only
  rename: [{span_attribute: [http.method], to: http.request.method, upsert: false},
             {metric_field: name, to: http.server.request.duration}]
  add: [{span_attribute: [rpc.system.name], value: grpc, upsert: false}]
  set_role: [{resource_attribute: [k8s.pod.uid], role: identifying}] # entity only
```

Operations run in a fixed order, whatever order they're written in.
Every field reference uses the **source** name, except `to` and the field set by `add`.

| Order | Op | Defined by | Meaning |
|---|---|---|---|
| 1 | `remove` | [OTEP #4738](https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/4738-telemetry-policy.md) | Removes the field. For metrics, points that end up with identical attributes are aggregated (exact for sums and histograms). Sets SHOULD NOT remove gauge attributes. |
| 2 | `redact` | [OTEP #4738](https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/4738-telemetry-policy.md) | Replaces the value with `replacement` (default `[REDACTED]`). |
| 3 | `convert` | transformation sets | Changes the value's type. A value that doesn't convert is removed. |
| 4 | `scale` | transformation sets | Metrics only. Changes the unit from `from` to `to` (UCUM). Multiplies values, sums, min/max and explicit bucket bounds by the factor. |
| 5 | `rename` | [OTEP #4738](https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/4738-telemetry-policy.md) | Moves the value to `to`. If `to` already has a value, `upsert: true` overwrites it and `upsert: false` (default) keeps it. Either way the old key goes away. `metric_field: name` and `log_field: event_name` rename the signal itself. |
| 6 | `add` | [OTEP #4738](https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/4738-telemetry-policy.md) | Sets the field to `value`, honoring `upsert` (default `false`). |
| 7 | `set_role` | transformation sets | Entities only. Moves a resource attribute between the entity's identifying and descriptive attributes. |

### Entities

```yaml
- id: k8s-replicationcontroller-name
  name: k8s.replication_controller.name becomes k8s.replicationcontroller.name
  entity:
    match: [{resource_attribute: [k8s.replication_controller.name], exists: true}]
    transform:
      rename: [{resource_attribute: [k8s.replication_controller.name], to: k8s.replicationcontroller.name}]
```

`entity_field: type` reads the type from the resource's entity refs. A policy that
only matches on resource attributes applies to every resource. A renamed key keeps
its role (identifying or descriptive); an added key is descriptive unless `set_role` says otherwise.

### Evaluation

Policies are independent, as in [OTEP #4738](https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/4738-telemetry-policy.md).
No policy runs before another, and no policy sees another policy's output. For each record:

1. Find every enabled policy whose matchers hold on the **input** record.
2. If any of them drops the record, it's removed.
3. Otherwise, collect the operations of all matching policies and run them by operation type, in the order above.

Commutativity, idempotency and determinism are MUSTs. Where OTEP #4738 settles two
policies writing the same field by policy-id order, transformation sets forbid
the case instead: two policies that can match the same record MUST NOT apply the
same operation to the same field with different parameters. Different operations
on the same field are fine, because the fixed order settles them. For example,
a `remove` and a `rename` of the same key: `remove` runs first, so the rename finds nothing.

This makes two patterns safe, and the files use both:

```yaml
# Value map, emulated for one value: the general policy renames the key,
# the value policy overwrites the value (add runs after rename).
- id: rpc-span-system-rename
  name: rpc.system renamed to rpc.system.name
  trace:
    match: [{span_attribute: [rpc.system], exists: true}]
    transform:
      rename: [{span_attribute: [rpc.system], to: rpc.system.name}]
- id: rpc-span-system-value-dubbo
  name: rpc.system apache_dubbo becomes rpc.system.name dubbo
  trace:
    match: [{span_attribute: [rpc.system], exact: apache_dubbo}]
    transform:
      add: [{span_attribute: [rpc.system.name], value: dubbo, upsert: true}]
```

Data mode fails open, as in OTEP #4738: a policy that can't be parsed or applied
is skipped, and the telemetry passes through. Schema mode fails closed: a query
rewriter that can't translate a policy MUST reject the queries that touch the
policy's signals, naming the policy and the reason, rather than return wrong results.

### Safety

Every policy MUST leave data that's already in the target shape unchanged.

That's what makes a set safe to apply to all telemetry, old or new, with or
without a schema URL: target-shaped data passes through untouched. In practice a
policy matches on something only the source has: a source-only metric name or
attribute, or a value the target never uses.

Some changes would have to touch target-shaped data, so they can't be policies:

- **A meaning change under the same name**, for example `server.address` on RPC spans.
- **A name the target reuses for something else**, for example `v8js.memory.heap.limit` in 1.42.
- **A unit change under the same name**, for example `http.server.duration`, in seconds since 1.21.

### What can't be expressed

These changes need operations that don't exist yet. The files leave them out and
list them in their header comments:

| Needed operation | Examples |
|---|---|
| Value map (exact, with a reverse) | `db.system` values, gRPC status codes. Emulated today with one policy per value. |
| Concatenate / split | `rpc.service` + `rpc.method`, `http.target` → `url.path` + `url.query`, `grpc.target` ↔ `server.address`/`server.port` |
| Derive | `error.type` from a status code or an exception |
| Templated keys | `k8s.pod.label.<key>`, `http.request.header.<key>` |
| Span name rewrite, span status and other span fields | DB span names, gRPC `Sent.`/`Recv.` prefixes, gRPC span status |
| Value ↔ attribute | K8s phase and condition metrics |
| Instrument change, fan-out | GenAI `gen_ai.client.token.usage` → usage counters, K8s gauge → up-down counter |
| Moves between signals | exception span events → log events |

## Conventions used in these files

- **Schema URLs.**
  - `https://opentelemetry.io/schemas/1.45.0` is a placeholder for the first
    release that contains the stable RPC conventions (`rpc.status_code`,
    `rpc.request.header.*`). They're on `main` but not released yet.
  - `https://grpc.io/schemas/42.42.0` is invented. gRPC publishes conventions
    ([A66](https://github.com/grpc/proposal/blob/master/A66-otel-stats.md),
    [A72](https://github.com/grpc/proposal/blob/master/A72-open-telemetry-tracing.md)), not a schema URL.
  - The Kubernetes "old" side is what the Collector emits; it stamps
    `https://opentelemetry.io/schemas/1.18.0`.
  - `https://opentelemetry.io/schemas/gen-ai-dev/1.42.0-dev` is the manifest URL of
    the GenAI conventions at the pinned commit. No schema file is published for it.
- **Intermediate releases.** Where producers shipped names in an intermediate release
  (HTTP 1.21 `server.socket.*`, DB 1.26 `db.client.connections.*`, RPC 1.39–1.44
  `rpc.response.status_code`), the set also covers them, as long as that doesn't conflict
  with the target.
- **HTTP 1.21 durations** were already in seconds under the old names, so only
  series that carry 1.20-only attributes are scaled.
- **HTTP/2 protocol version.** The 1.23.1 → 1.20.0 set assumes 1.20 producers
  reported HTTP/2 as `"2.0"`, as in the 1.20 examples.
- **Dual emission.** A producer that emits both shapes is counted twice. Sets don't deduplicate.

## What isn't covered

**Impossible** means no rule could ever be correct (the data was never captured,
the mapping is ambiguous, or the meaning changed under the same name).
**New op** means the source has the information but today's operations can't express it.
**High** marks items that block a golden signal: duration, error rate or throughput.

| Area | Direction | Not covered |
|---|---|---|
| Code | both | Impossible: `code.function.name` from 1.30 producers (short name under the same key); splitting `code.function.name` back. New op: concatenating `code.namespace` + `code.function`. |
| DB | 1.24 → 1.33 | Impossible: **`db.client.operation.duration` (High, new metric)**; PostgreSQL schema / Oracle service parts of `db.namespace`; new attributes. New op: **`error.type` (High)**, `db.namespace` for SQL Server instances, span names, `db.query.summary`, Elasticsearch path parts. |
| DB | 1.33 → 1.24 | Impossible: removed attributes (no source data). New op: splitting qualified `db.namespace` values, span names. |
| HTTP | 1.20 → 1.23.1 | Impossible: header keys with `_`; values derived from `X-Forwarded-*`; defaulted ports. New op: **`error.type` (High)**, `http.target` split, `_OTHER` span names, `client.address` fallback, default `server.port`. |
| HTTP | 1.23.1 → 1.20 | Impossible: original method on metrics (`_OTHER`); `net.host.name` on the server metric (opt-in in stable); `net.sock.*` on the client metric. New op: `http.target` concat, header keys, span names, default ports. |
| K8s | both | Impossible: `.current` limits and requests; pod CPU utilization (denominator changed); rounded `cpu` quota; namespace phase "unknown". New op: phase and condition value ↔ attribute, labels and annotations, quota CPU millicores, hugepage and storage-class keys, minor page faults, volume attributes, gauge → up-down counter. |
| RPC | 1.37 → 1.45 | Impossible: `server.address` / `server.port` (new meaning); response header vs trailer. New op: **`rpc.service` + `rpc.method` (High)**, **`error.type` (High)**, metadata keys, exception events, JSON-RPC error message. |
| RPC | 1.45 → 1.37 | Impossible: `server.address` meaning; header/trailer key collisions; removed attributes. New op: **splitting `rpc.method` (High)**, metadata keys, integer gRPC status, exception events. |
| gRPC | gRPC → OTel | New op: `server.address`/`server.port` from `grpc.target` (part is impossible: `xds:`, `unix:` targets); span name without `Sent.`/`Recv.`; `rpc.method` and `rpc.status_code` on spans; span status. Impossible: `error.type` for failures without a status code. Attempt metrics and spans have no equivalent and are left as-is. |
| gRPC | OTel → gRPC | New op: `grpc.target` from `server.address`/`server.port`; `Sent.`/`Recv.` span names; span status and status description. Impossible: attempt metrics and spans (no source data). |
| GenAI | 1.41 → dev | Impossible: **`gen_ai.invoke_agent.duration`, `gen_ai.execute_tool.duration`, `gen_ai.invoke_workflow.duration` (High, new metrics)**; cache, reasoning and per-invocation call metrics; billed vs consumed tokens; new attributes. New op: **token usage counters from the `gen_ai.client.token.usage` histogram (High)**, text-only `gen_ai.system_instructions`, MCP span status, event severity. |
| GenAI | dev → 1.41 | Impossible: `gen_ai.provider.name`, `gen_ai.agent.id`, `gen_ai.agent.version` on internal agent spans; `finish_reasons` `error` padding. New op: `finish_reason` back-fill, cache tokens on internal agent spans, skill span names, MCP span status. |

**GenAI #521.** The GenAI migration guide describes #521: inference metrics move to
`gen_ai.client.inference.*`, and the span type becomes `gen_ai.client.inference`.
The model at the pinned commit doesn't have it yet, so the files contain no #521
policies: applied to the pinned target, they would move target-shaped data. Once
#521 lands, add these policies (and the same renames back in the reverse file):

```yaml
- id: genai-521-inference-duration
  name: Inference operations move to gen_ai.client.inference.duration
  description: >
    Inference calls with a custom gen_ai.operation.name can't be told apart
    from other operations, so they stay on gen_ai.client.operation.duration.
  metric:
    match:
      - {metric_field: name, exact: gen_ai.client.operation.duration}
      - {datapoint_attribute: [gen_ai.operation.name], regex: "^(chat|generate_content|text_completion)$"}
    transform:
      rename: [{metric_field: name, to: gen_ai.client.inference.duration}]
- id: genai-521-ttfc
  name: time_to_first_chunk renamed
  metric:
    match: [{metric_field: name, exact: gen_ai.client.operation.time_to_first_chunk}]
    transform:
      rename: [{metric_field: name, to: gen_ai.client.inference.time_to_first_chunk}]
- id: genai-521-tpoc
  name: time_per_output_chunk renamed
  metric:
    match: [{metric_field: name, exact: gen_ai.client.operation.time_per_output_chunk}]
    transform:
      rename: [{metric_field: name, to: gen_ai.client.inference.time_per_output_chunk}]
```
