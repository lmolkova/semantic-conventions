# Config-driven codegen prototype

A prototype for describing declarative configuration in the semantic conventions model in a way
that machines can act on.

[PR 4008](https://github.com/open-telemetry/semantic-conventions/pull/4008) proposes recording the
declarative configuration *schema* on semconv attributes so that
[opentelemetry-configuration](https://github.com/open-telemetry/opentelemetry-configuration) can
compile its JSON schema from the model. That answers what config properties exist. It does not say
what a property does to telemetry, so instrumentation authors still translate prose into code by
hand.

This prototype adds that missing piece and shows three artifacts falling out of one annotation:

1. the `Experimental*Instrumentation` schema in opentelemetry-configuration,
2. Configuration tables in the generated semconv docs,
3. Fully generated Java instrumentation helpers that read config at runtime.

HTTP, database and RPC are annotated. The annotations are freeform (`annotations.config`) so no
weaver schema change is needed yet, but we probably should make it strongly typed
top-level property.

## The annotation

A config property declares a **semantic type** from a closed set. The JSON schema is derived from
it, not hand written. A closed set is what makes the schema, the docs and the code all generatable.

| type | meaning | Java |
| --- | --- | --- |
| `value_filter` | config supplies the allow-list of accepted values, anything else becomes `fallback_value` | `filterHttpRequestMethod(config, value)` |
| `key_filter` | config supplies which keys of a template attribute to record | `populateRequestCapturedHeaders(attributes, lookup, config)` |
| `toggle` | config gates an attribute or a whole signal | `setHttpRequestBodyContent(...)`, `isEnabled(config)` |
| `value_limit` | config supplies a maximum size, longer values are truncated | `truncateHttpRequestBodyContent(config, value)` |
| `key_redaction` | config supplies which keys inside the value are replaced with `fallback_value` | `redactUrlQuery(config, value)` |
| `attribute_mapping` | config supplies values of one attribute to match and the attribute to record on a match | `populateServicePeerNameMapping(attributes, config, serverAddress)` |
| `semconv_experimental` | opting in to development features | the experimental gate |

A knob that no semantic type fits declares a plain type instead - `boolean`, `string`, `integer` or
`string[]`, with an optional `default`. It reaches the schema and the docs and only loses code
generation, so an unmodelled knob is never inexpressible or forced into the wrong role.
`sanitize_query_text` on `db.query.text` is one: it changes the value, not whether it is recorded.

```yaml
- id: http.request.method
  annotations:
    config:
      properties:
        known_methods:
          type: value_filter
          fallback_value: _OTHER
          env_var: OTEL_INSTRUMENTATION_HTTP_KNOWN_METHODS
          description: Override the default list of known HTTP methods.
```

Defaults are derived rather than declared. A `value_filter` defaults to the attribute's stable enum
members, and a `toggle` the requirement level of what it gates: `false` for `opt_in`, `true`
otherwise.

## Placement and naming

A property sits on the attribute definition when it applies wherever the attribute is used, and on
an attribute reference when it applies to one signal only.

`semconv_experimental` is the exception: its scope is the domain's semconv config, not the
attribute, so it is never declared on a definition. Declared on a signal group it gates the whole
signal, and declared on an attribute reference it gates that attribute, such as `url.template` in
the scope of the HTTP spans and metrics.

Nothing is inferred. A gate appears in generated code only where a config property asks for one, so
an attribute carrying its own toggle is gated by that toggle alone.

Note that a reference's `annotations` replace the definition's rather than merging with them.
This is a feature gap, tracked in [weaver#1705](https://github.com/open-telemetry/weaver/issues/1705).

A property id is just its name. Where it lands is decided by placement: every signal declares its
config scope, and a property resolves under it. An attribute's config block may declare its own
scope, which is how a property shared across domains, such as `url.query` sanitization, keeps one
home. Both scopes work at runtime: the signal's scope is read first and the declared one is the
fallback, so a shared property can still be overridden for a single signal.

A declared scope also keeps a property that governs one signal kind out of the others.
`db.query.text` is opt-in on the database metrics and recommended on the spans, so its toggle
declares `db.client.metric` and never reads as if it governed spans.

```yaml
  - id: span.http.client
    type: span
    annotations:
      config:
        scope: http.client
```

So `span.http.client` and `metric.http.client.request.duration` both put their properties under
`.instrumentation/development.general.http.client`. The parent `ExperimentalHttpInstrumentation`
type is derived from the declared scopes rather than declared separately.

A scope is not one per signal. All fourteen database span groups declare `db.client`, one per
database system, because a user configuring database instrumentation is not configuring Redis and
PostgreSQL separately.

This makes placement the scoping tool, which matters more than it sounds. `known_methods` sits on
the `http.request.method` reference in the shared HTTP attribute group. Putting it on the
definition would hand a `general.db.elasticsearch.known_methods` property to Elasticsearch spans,
which use the same attribute.

The attribute registry pages list these properties too, since that is where a reader looks up an
attribute. They leave the scope open unless the attribute declares one, because an attribute used by
several signals has no single scope.

`semconv_experimental` is declared where a development convention is an addition to an established
one: on a development stability signal alongside stable siblings, such as the HTTP and database
development metrics, and on a development stability attribute of an otherwise stable signal, such
as `url.template` on the HTTP client metrics.

Development stability alone does not call for it. The Connect RPC and JSON-RPC spans are the entire
convention for those systems rather than an addition, so gating them would leave the instrumentation
silent.

## What is generated

`generated/instrumentation-<domain>.yaml` is the `$defs` block that opentelemetry-configuration
holds by hand today. For HTTP every property name that exists there is reproduced; the additions are
`request_capture_body_content` and `response_capture_body_content`, and the inconsistent `minItems`
is dropped because the semantic type already says what the array is for.

`generated/java/` holds one class per signal and one attribute key class per domain, built on
`DeclarativeConfigProperties` and `AttributesBuilder` from opentelemetry-java. The hand written
runtime files are `Config`, which resolves a scope, and `Redaction`, which rewrites a query string.

A span helper takes exactly the sampling relevant attributes and short circuits into a non recording
but still propagating span, so a disabled signal does not break trace propagation:

```java
// auto-generated
public static Span start(
    Tracer tracer, DeclarativeConfigProperties config, String spanName,
    String httpRequestMethod, ...,
    Function<String, List<String>> requestCapturedHeaders) {
  if (!isEnabled(config)) {
    return Span.wrap(Span.current().getSpanContext());
  }
  ...
  attributes.put(HTTP_REQUEST_METHOD, filterHttpRequestMethod(config, httpRequestMethod));
  populateRequestCapturedHeaders(attributes, requestCapturedHeaders, config);
```

Captured headers are sampling relevant on the server, so the server helper takes a header lookup and
records them before the span exists. Which keys to record comes from config, not from the caller:

```java
List<String> keys =
    Config.at(config, SCOPE).getScalarList("request_captured_headers", String.class, List.of());
for (String key : keys) {
  ...
  attributes.put(HTTP_REQUEST_HEADER.getAttributeKey(key.toLowerCase(Locale.ROOT)), value);
```

`url.template` is a development attribute on stable HTTP signals, so its setter carries the
experimental gate and a development metric's `isEnabled` is that gate:

```java
public static void setUrlTemplate(Span span, String value, DeclarativeConfigProperties config) {
  if (!Config.experimental(config, "http")) {
    return;
  }
```

A signal's own toggle is its `isEnabled`, off by default for an `opt_in` signal and an off-switch
for any other. Gate appears only where a config property
asks for one.

An event declaring the `exception.*` attributes gets a helper taking a `Throwable`, which calls
`ExtendedLogRecordBuilder.setException` rather than asking the caller to unpack it.

The Configuration tables in the HTTP, database and RPC docs come from the same annotations.

## Generating code and running the demo

```shell
make config-codegen-demo
```

This regenerates both artifacts, runs the tests and runs the demo.

[`HttpClientInstrumentation`](src/main/java/io/opentelemetry/semconv/prototype/demo/HttpClientInstrumentation.java)
instruments a real JDK `HttpClient` call with the generated helpers.
[`Demo`](src/main/java/io/opentelemetry/semconv/prototype/demo/Demo.java) sends one request through
it with [`demo-config.yaml`](src/main/resources/demo-config.yaml) and prints the span, and

`demo-config.yaml` configures a `service_peer_name_mapping`, so the request records an attribute the
instrumentation never passes:

```
GET {http.request.header.x-request-id=[abc123], http.response.status_code=200,
     server.address=localhost, service.peer.name=shopping-cart, http.request.method=GET, ...}
```

## Open questions

- Resolution already falls back from the narrow scope to the wider one a property declares. The
  open question is declaration time: should a property be able to declare several scopes, or should
  scopes union when a signal is refined?

- Config is repetitive. A property is declared at each place it applies, so one knob that governs
  several attributes or signals is copied around and only merges because the names match. We should
  find a way to share the same config section without duplicating it.

- `semconv_experimental` is a flat stand-in for
  `.instrumentation/development.general.<domain>.semconv.experimental`, which is the `experimental`
  property of the shared `ExperimentalSemconvConfig` type. Today both generators and the docs macro
  special case that one name to rewrite the path. What they should do instead is model the config
  types a property can point into, so that a property can name a shared type and the nesting falls
  out. The same mechanism would cover `version` and `dual_emit`, which are not modelled at all yet.
