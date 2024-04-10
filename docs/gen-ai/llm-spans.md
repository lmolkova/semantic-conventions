<!--- Hugo front matter used to generate the website version of this page:
linkTitle: LLM requests
--->

# Semantic Conventions for LLM requests

**Status**: [Experimental][DocumentStatus]

<!-- Re-generate TOC with `markdown-toc --no-first-h1 -i` -->

- [Configuration](#configuration)
- [LLM Request attributes](#llm-request-attributes)
- [Events](#events)
  - [System message](#system-message)
  - [User message](#user-message)
  - [Assistant message in prompt](#assistant-message-in-prompt)
  - [Tool message](#tool-message)
  - [Assistant response](#assistant-response)

<!-- tocstop -->

A request to an LLM is modeled as a span in a trace.

**Span kind:** MUST always be `CLIENT`.

The **span name** SHOULD be set to a low cardinality value describing an operation made to an LLM.
For example, the API name such as [Create chat completion](https://platform.openai.com/docs/api-reference/chat/create) could be represented as `ChatCompletions gpt-4` to include the API and the LLM.

## Configuration

Instrumentations for LLMs MAY capture prompts and completions.
Instrumentations that support it, MUST offer the ability to turn off capture of prompt and completion contents. This is for three primary reasons:

1. Data privacy concerns. End users of LLM applications may input sensitive information or personally identifiable information (PII) that they do not wish to be sent to a telemetry backend.
2. Data size concerns. Although there is no specified limit to sizes, there are practical limitations in programming languages and telemetry systems. Some LLMs allow for extremely large context windows that end users may take full advantage of.
3. Performance concerns. Sending large amounts of data to a telemetry backend may cause performance issues for the application.

By default, these configurations SHOULD NOT capture prompts and completion contents.

## LLM Request attributes

These attributes track input data and metadata for a request to an LLM. Each attribute represents a concept that is common to most LLMs.

<!-- semconv gen_ai.request -->
| Attribute  | Type | Description  | Examples  | [Requirement Level](https://opentelemetry.io/docs/specs/semconv/general/attribute-requirement-level/) | Stability |
|---|---|---|---|---|---|
| [`gen_ai.request.model`](../attributes-registry/llm.md) | string | The name of the LLM a request is being made to. [1] | `gpt-4` | `Required` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| [`gen_ai.system`](../attributes-registry/llm.md) | string | The name of the LLM foundation model vendor. [2] | `openai` | `Required` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| [`gen_ai.request.max_tokens`](../attributes-registry/llm.md) | int | The maximum number of tokens the LLM generates for a request. | `100` | `Recommended` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| [`gen_ai.request.temperature`](../attributes-registry/llm.md) | double | The temperature setting for the LLM request. | `0.0` | `Recommended` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| [`gen_ai.request.top_p`](../attributes-registry/llm.md) | double | The top_p sampling setting for the LLM request. | `1.0` | `Recommended` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| [`gen_ai.response.id`](../attributes-registry/llm.md) | string | The unique identifier for the completion. | `chatcmpl-123` | `Recommended` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| [`gen_ai.response.model`](../attributes-registry/llm.md) | string | The name of the LLM a response was generated from. [3] | `gpt-4-0613` | `Recommended` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| [`gen_ai.usage.completion_tokens`](../attributes-registry/llm.md) | int | The number of tokens used in the LLM response (completion). | `180` | `Recommended` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| [`gen_ai.usage.prompt_tokens`](../attributes-registry/llm.md) | int | The number of tokens used in the LLM prompt. | `100` | `Recommended` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |

**[1]:** The name of the LLM a request is being made to. If the LLM is supplied by a vendor, then the value must be the exact name of the model requested. If the LLM is a fine-tuned custom model, the value should have a more specific name than the base model that's been fine-tuned.

**[2]:** If not using a vendor-supplied model, provide a custom friendly name, such as a name of the company or project. If the instrumetnation reports any attributes specific to a custom model, the value provided in the `gen_ai.system` SHOULD match the custom attribute namespace segment. For example, if `gen_ai.system` is set to `the_best_llm`, custom attributes should be added in the `gen_ai.the_best_llm.*` namespace. If none of above options apply, the instrumentation should set `_OTHER`.

**[3]:** If available. The name of the LLM serving a response. If the LLM is supplied by a vendor, then the value must be the exact name of the model actually used. If the LLM is a fine-tuned custom model, the value should have a more specific name than the base model that's been fine-tuned.
<!-- endsemconv -->

## Events

In the lifetime of an LLM span, an event for each message application sends to GenAI and receives in response from it MAY be created, depending on the configuration of the instrumentation.
The generic events applicable to multiple GenAI models follow `gen_ai.{role}.message` naming pattern.
Gen AI vendor-specific instrumentations SHOULD follow `gen_ai.{gen_ai.system}.{role}.message` pattern to record events that apply to their system only.

It's RECOMMENDED to use [Event API](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/logs/event-api.md) to record Gen AI events once it's implemented in corresponding language.
If, however Event API is not supported yet, events SHOULD be recorded as span events. The payload SHOULD be provided with `event.body` attribute as a JSON string.

The event payload describes message content sent to or received from Gen AI and depends on specific messages described in the following sections.

Instrumentations for individual Gen AI systems MAY add system specific fields into corresponding events payload.
It's RECOMMENDED to document them in system-specific extensions.
Telemetry consumers SHOULD expect to receive unknown payload fields.

### System message

The event name MUST be `gen_ai.system.message`.

This event describes the instructions passed to the Gen AI system inside the prompt.

| Body Field | Type | Description | Examples | Requirement Level |
|---|---|---|---|---|
| `role` | string | The role of the messages author | `system` | `Required` |
| `content` | string | The contents of the system message. | `You're a friendly bot that helps use OpenTelemetry.` | `Required` |
| `name` | string | An optional name for the participant. | `bot` | `Recommended: if available` |

Example of event serialized into `event.data` attribute:

```json
{"role":"system","content":"You're a friendly bot that helps use OpenTelemetry.","name":"bot"}
```

### User message

The event name MUST be `gen_ai.user.message`.

This event describes the prompt message specified by the user.

| Body Field | Type | Description | Examples | Requirement Level |
|---|---|---|---|---|
| `role` | string | The role of the messages author | `user` | `Required` |
| `content` | string | The contents of the user message. | `What telemetry is reported by OpenAI instrumentations?` | `Required` |
| `name` | string | An optional name for the participant. | `alice` | `Recommended: if available` |

Examples of serialized event payload that can be passed in `event.data` attribute:

```json
{"role":"user","content":"What telemetry is reported by OpenAI instrumentations?"}
```

### Assistant message in prompt

The event name MUST be `gen_ai.assistant.message`.

This event describes the assistant message when it's passed in the prompt.

| Body Field | Type | Description | Examples | Requirement Level |
|---|---|---|---|---|
| `role` | string | The role of the messages author | `assistant` | `Required` |
| `content` | string | The contents of the assistant message. | `Spans, events, metrics that follow Gen AI semantic conventions.` | `Conditionally Required: if available` |
| `tool_calls` | object | The tool calls generated by the model, such as function calls. | `get_link_to_otel_semconv` | `Conditionally Required: if available` |

<!-- TODO we need to describe tool_calls structure here, but we can't do it yet.-->

Examples of serialized event payload that can be passed in `event.data` attribute:

```json
{"role":"assistant","content":"Spans, events, metrics that follow Gen AI semantic conventions."}
```

```json
{"role":"assistant","tool_calls":[{"id":"call_hHM72v9f1JprJBStycQC4Svz","function":{"name":"get_link_to_otel_semconv","arguments":"{\"gen_ai_system\": \"OpenAI\"}"},"type":"function"}]}
```

### Tool message

The event name MUST be `gen_ai.tool.message`.

This event describes the tool or function response message.

| Body Field | Type | Description | Examples | Requirement Level |
|---|---|---|---|---|
| `role` | string | The role of the messages author | `tool`, `function` | `Required` |
| `content` | string | The contents of the tool message. | `OpenAI Semantic conventions are available at opentelemetry.io` | `Required` |
| `tool_call_id` | string | Tool call that this message is responding to. | `call_BC9hyMlI7if1ZMIH8l1R26Lo` | `Required` |

Examples of serialized event payload that can be passed in `event.data` attribute:

```json
{"role":"tool","content":"OpenAI Semantic conventions are available at opentelemetry.io","tool_call_id":"call_BC9hyMlI7if1ZMIH8l1R26Lo"}
```

### Assistant response

This event describes the Gen AI response message.
If Gen AI model returns multiple messages (aka choices), each of the message SHOULD be recorded as an individual event.

<!-- semconv gen_ai.response.message -->
The event name MUST be `gen_ai.response.message`.

| Attribute  | Type | Description  | Examples  | [Requirement Level](https://opentelemetry.io/docs/specs/semconv/general/attribute-requirement-level/) | Stability |
|---|---|---|---|---|---|
| [`gen_ai.response.finish_reason`](../attributes-registry/llm.md) | string | The reason the model stopped generating tokens. | `stop`; `content_filter`; `tool_calls` | `Recommended` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
<!-- endsemconv -->


When response is streamed, instrumentations that report response content with events MUST reconstruct and report the full message amd MUST NOT report individual chunks as events.
Response event payload has the following fields:

| Body Field | Type | Description | Examples | Requirement Level |
|---|---|---|---|---|
| `finish_reason` | string | The reason the model stopped generating tokens. | `stop`, `tool_calls`, `content_filter` | `Required` |
| `content_filter_results` | object | The content filter results. | `{"protected_material_text":{"detected":true,"filtered":true}}` | `Conditionally Required: if finish_reason is `content_filter` |
| `index` | int | The index of the choice in the list of choices. | `1` | `Recommended: if not 0` |
| `message.role` | string | The role of the messages author | `assistant` | `Conditionally Required: if available` |
| `message.content` | string | The contents of the assistant message. | `Spans, events, metrics that follow Gen AI semantic conventions.` | `Conditionally Required: if available` |
| `message.tool_calls` | object | The tool calls generated by the model, such as function calls. | `get_link_to_otel_semconv` | `Conditionally Required: if available` |

<!-- TODO we need to describe tool_calls and content_filter_results structure here, but we can't do it yet.-->

Examples of serialized event payload that can be passed in `event.data` attribute:

```json
{"index":0,"finish_reason":"stop","message":{"role":"assistant","content":"The OpenAI semantic conventions are available at opentelemetry.io"}}
```

or

```json
{"index":0,"finish_reason":"content_filter","content_filter_results":{"protected_material_text":{"detected":true,"filtered":true}}}
```

[DocumentStatus]: https://github.com/open-telemetry/opentelemetry-specification/tree/v1.31.0/specification/document-status.md
