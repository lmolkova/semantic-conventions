# Proposal: Enhanced Execute Tool Span Attributes

**Status**: [Development][DocumentStatus]

<!-- toc -->

- [Spans](#spans)
  - [Execute tool span](#execute-tool-span)
- [Proposed Attributes](#proposed-attributes)
  - [Tool Metadata](#tool-metadata)
  - [Tool Input Payload](#tool-input-payload)
  - [Tool Output](#tool-output)
- [Complete Example](#complete-example)
- [Schema Definitions](#schema-definitions)
- [Use Cases](#use-cases-for-utilizing-the-information-in-the-execute-tool-span)
- [Implementation Considerations](#implementation-considerations)
  - [Storage and Performance](#storage-and-performance)
  - [Privacy and Security](#privacy-and-security)
- [Backward Compatibility](#backward-compatibility)
- [References](#references)

<!-- tocstop -->

## Spans

### Execute tool span

The execute tool span captures the execution of a tool or function by a GenAI agent. This span type is essential for understanding tool interactions, debugging agent behavior, and evaluating tool effectiveness.

## Overview

This proposal extends the OpenTelemetry semantic conventions for GenAI agent execute tool spans to include comprehensive tool metadata, input payload, and output information. These additions enable better observability, debugging, and evaluation of agent tool usage.

## Motivation

Current execute tool spans lack critical information which can power  needed for:
- White-box-observability on tool calling
- Tool selection effectiveness evaluation
- Tool execution performance analysis
- Error handling capability assessment
- Input-output correlation analysis

This proposal addresses these gaps by adding structured attributes for tool metadata, inputs, and outputs.

## Proposed Attributes

### Tool Metadata

Additional attributes to capture tool identification and versioning:

| Attribute | Type | Description | Examples | [Requirement Level](https://opentelemetry.io/docs/specs/semconv/general/attribute-requirement-level/) | Stability |
|---|---|---|---|---|---|
| `gen_ai.tool.version` | string | Tool schema or interface version. | `1.0.0`; `2.1.3` | `Recommended` | ![Development](https://img.shields.io/badge/-development-blue) |
| `gen_ai.role` | string | Typically "tool" to denote the actor in the conversation. | `tool` | `Recommended` | ![Development](https://img.shields.io/badge/-development-blue) |
| `gen_ai.tool.type` | string | Type of tool invoked. [1] | `function`; `search`; `api` | `Required` | ![Development](https://img.shields.io/badge/-development-blue) |

**[1] `gen_ai.tool.type`:** MUST be one of the following:
- `function` - Stateless functions following JSON schemas for logic, lookups, or transformations (e.g., Function Calling, Azure Functions)
- `search` - Information retrieval from indexed data sources, documents, or web content (e.g., Azure AI Search, Bing Search, File Search, SharePoint)
- `api` - External service integrations via RESTful APIs or protocol-based communications (e.g., OpenAPI Spec Tool, Model Context Protocol)
- `file_lookup` - Direct file access and content extraction from user-uploaded documents (e.g., PDFs, Word, Excel files)
- `code_execution` - Sandboxed code execution environments for calculations and data analysis (e.g., Code Interpreter with Python)
- `custom` - Enterprise-specific or proprietary tools not fitting standard categories (e.g., Microsoft Fabric for structured data queries)

### Tool Input Payload

Structured attributes for capturing tool invocation details:

| Attribute | Type | Description | Examples | [Requirement Level](https://opentelemetry.io/docs/specs/semconv/general/attribute-requirement-level/) | Stability |
|---|---|---|---|---|---|
| `gen_ai.tool.input.tool_call.name` | string | Function/tool name being invoked. | `get_weather`; `search_files` | `Required` | ![Development](https://img.shields.io/badge/-development-blue) |
| `gen_ai.tool.input.tool_call.arguments` | JSON Object | Input schema and runtime values used. [2] | See structure below | `Required` | ![Development](https://img.shields.io/badge/-development-blue) |

**[2] `gen_ai.tool.input.tool_call.arguments`:** MUST follow the structure defined in [gen-ai-tool-input.json](gen-ai-tool-input.json):
- `parameters_schema`: List of all expected parameters (used for docs, validation, traceability)
- `runtime_arguments`: Actual input values passed during tool execution

Example:
```json
{
  "parameters_schema": [
    {
      "name": "location",
      "type": "string",
      "description": "City name for weather lookup",
      "required": true
    }
  ],
  "runtime_arguments": {
    "location": "Bali"
  }
}
```

### Tool Output

Attributes for capturing tool execution results:

| Attribute | Type | Description | Examples | [Requirement Level](https://opentelemetry.io/docs/specs/semconv/general/attribute-requirement-level/) | Stability |
|---|---|---|---|---|---|
| `gen_ai.tool.message.content` | string | The tool's response/output. [3] | `{"temperature": 25, "condition": "sunny"}`; `"File not found"` | `Required` | ![Development](https://img.shields.io/badge/-development-blue) |
| `gen_ai.tool.message.content.type` | string | Content type of the tool's output. [4] | `text`; `json`; `table` | `Required` | ![Development](https://img.shields.io/badge/-development-blue) |

**[3] `gen_ai.tool.message.content`:** The format depends on `content.type`:
- For `text` type: Plain text string containing the tool's response
- For `json` type: Valid JSON string that can be parsed into an object or array
- For `table` type: JSON-stringified array of objects (recommended), Markdown table, or CSV-formatted string
- For `html` type: Valid HTML string containing structured markup
- For `image` type: Base64-encoded image string
- For `image_url` type: URL string pointing to an externally hosted image
- For `chart` type: Either base64-encoded chart image or code/data structure to render the chart


**[4] `gen_ai.tool.message.content.type`:** MUST be one of the following:
- `text`
- `json`
- `table`
- `html`
- `image` (base64-encoded)
- `image_url` (external URL)
- `chart` (could imply embedded base64 or code to render chart)

## Complete Example

Here's a complete example showing all the proposed attributes for an execute tool span:

```json
{
  "gen_ai.tool.version": "v1.0",
  "gen_ai.role": "tool",
  "gen_ai.tool.type": "function",
  "gen_ai.tool.input.tool_call.name": "get_weather",
  "gen_ai.tool.input.tool_call.arguments": {
    "parameters_schema": [
      {
        "name": "location",
        "type": "string",
        "description": "City name for weather lookup",
        "required": true
      }
    ],
    "runtime_arguments": {
      "location": "Bali"
    }
  },
  "gen_ai.tool.message.content": "The weather in Bali is sunny and 25°C.",
  "gen_ai.tool.message.content.type": "text"
}
```

## Schema Definitions

The complete schema for execute tool span attributes, including both tool input and output structures, is defined in [gen-ai-execute-tool-new-attributes.json](gen-ai-execute-tool-new-attributes.json).


## Use Cases for utilizing the information in the `execute-tool` span

These enhanced attributes enable several scenarios:

### 1. Tool Selection Accuracy
- Analyze if the correct tool was selected based on the user query
- Compare `gen_ai.tool.type` and `gen_ai.tool.input.tool_call.name` against expected tool for the query

### 2. Parameter Correctness
- Validate that runtime arguments match expected parameters
- Check if required parameters are provided
- Ensure parameter types and values are appropriate

### 3. Output Quality Assessment
- Evaluate the relevance and accuracy of tool outputs
- Assess if the output format matches the expected `content.type`
- Measure the completeness of the response

### 4. Error Handling
- Track tool execution failures through error attributes
- Analyze error patterns across different tool types
- Evaluate retry strategies and fallback mechanisms

## Implementation Considerations

### Storage and Performance

When handling tool inputs and outputs, particularly large content in `gen_ai.tool.message.content` (e.g., base64 images, extensive JSON data), instrumentations should follow established patterns for content storage:

1. **Recording content on attributes** - Suitable for pre-production environments where telemetry volume is manageable and full content visibility aids debugging.

2. **External storage with references** - Recommended for production environments to manage telemetry size and handle sensitive data by storing only content references in spans.

3. **Additional considerations**:
   - Implement sampling strategies for high-volume tool calls
   - Use content truncation when partial content suffices
   - Leverage span events for streaming or progressive outputs

For detailed implementation guidance, refer to:
- [Recording content patterns](https://github.com/open-telemetry/semantic-conventions/blob/main/docs/gen-ai/gen-ai-spans.md#recording-content-on-attributes)
- [External storage patterns](https://github.com/open-telemetry/semantic-conventions/blob/main/docs/gen-ai/gen-ai-spans.md#uploading-content-to-external-storage)
- [PR #2179](https://github.com/open-telemetry/semantic-conventions/pull/2179/files#diff-cfecef30f6384caa7756376e4c61dbd946bb5c96cb3cdea19543b0773061e4fc) for additional context on content handling conventions


### Privacy and Security
- Tool inputs and outputs may contain sensitive information
- Implement appropriate redaction or encryption mechanisms
- Consider compliance requirements for data retention

## Backward Compatibility

This proposal extends existing attributes and does not modify current ones, ensuring backward compatibility with existing instrumentations.

## References

- [OpenTelemetry GenAI Agent Spans](https://github.com/open-telemetry/semantic-conventions/blob/main/docs/gen-ai/gen-ai-agent-spans.md)
- [OpenTelemetry GenAI Spans](https://github.com/open-telemetry/semantic-conventions/blob/main/docs/gen-ai/gen-ai-spans.md)
