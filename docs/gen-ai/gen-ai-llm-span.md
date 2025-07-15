# Proposal: Enhanced LLM Span Versioning Attributes

**Status**: [Development][DocumentStatus]

<!-- toc -->

- [Overview](#overview)
- [Motivation](#motivation)
- [Proposed Attributes](#proposed-attributes)
  - [Model Versioning](#model-versioning)
  - [System Prompt Versioning](#system-prompt-versioning)
- [Complete Example](#complete-example)
- [Schema Definition](#schema-definition)
- [Use Cases](#use-cases)
- [Implementation Considerations](#implementation-considerations)
- [Backward Compatibility](#backward-compatibility)
- [References](#references)

<!-- tocstop -->

## Overview

This proposal extends the OpenTelemetry semantic conventions for GenAI LLM spans to include versioning attributes for both the model and system prompt. These additions enable better tracking of model evolution and prompt changes over time.

## Motivation

Current LLM span attributes lack versioning information, which creates challenges for:
- **Model Version Tracking**: Difficulty in correlating performance changes with model updates
- **Prompt Version Management**: No standardized way to track system prompt iterations
- **A/B Testing**: Limited ability to compare different model or prompt versions
- **Debugging**: Challenges in reproducing issues without version information
- **Compliance**: Difficulty in maintaining audit trails for model and prompt changes

This proposal addresses these gaps by introducing standardized versioning attributes.

## Proposed Attributes

### Model Versioning

| Attribute | Type | Description | Examples | [Requirement Level](https://opentelemetry.io/docs/specs/semconv/general/attribute-requirement-level/) | Stability |
|---|---|---|---|---|---|
| `gen_ai.model.version` | string | The version of the model being used. | `v1.0`; `2024-02-01`; `gpt-4-0613` | `Recommended` | ![Development](https://img.shields.io/badge/-development-blue) |

**Notes:**
- This attribute captures the specific version or variant of the model specified in `gen_ai.request.model`
- Version format may vary by provider (semantic versioning, dates, or custom identifiers)
- Helps distinguish between different iterations of the same model family

### System Prompt Versioning

| Attribute | Type | Description | Examples | [Requirement Level](https://opentelemetry.io/docs/specs/semconv/general/attribute-requirement-level/) | Stability |
|---|---|---|---|---|---|
| `gen_ai.system.prompt.version` | string | The version of the system prompt template or configuration. | `v1.0`; `2024-07-15`; `customer-service-v2` | `Recommended` | ![Development](https://img.shields.io/badge/-development-blue) |

**Notes:**
- Tracks versions of system prompts used to configure model behavior
- Enables correlation between prompt changes and response quality
- Supports prompt experimentation and A/B testing scenarios

## Complete Example

Here's a comprehensive example showing the new versioning attributes alongside existing LLM span attributes:

```json
{
  "span_name": "gen_ai.chat_completions",
  "attributes": {
    "gen_ai.system": "openai",
    "gen_ai.request.model": "gpt-4",
    "gen_ai.model.version": "gpt-4-0613",
    "gen_ai.system.prompt.version": "v2.1",
    "gen_ai.request.temperature": 0.7,
    "gen_ai.request.max_tokens": 1000,
    "gen_ai.request.top_p": 0.95,
    "gen_ai.response.id": "chatcmpl-7kD3P9lFZKrjO8",
    "gen_ai.response.finish_reasons": ["stop"],
    "gen_ai.usage.input_tokens": 150,
    "gen_ai.usage.output_tokens": 250,
    "gen_ai.conversation.id": "conv-12345"
  }
}
```

## Schema Definition

The complete schema for LLM span attributes including the new versioning fields is defined in [gen-ai-llm-versioning-schema.json](gen-ai-llm-versioning-schema.json).

## Use Cases

### 1. Model Performance Tracking
- Compare response quality across different model versions
- Identify performance regressions after model updates
- Optimize model selection based on version-specific metrics

### 2. Prompt Engineering
- Track prompt iteration effectiveness
- A/B test different prompt versions
- Maintain prompt change history for compliance

### 3. Debugging and Reproducibility
- Reproduce exact conditions for issue investigation
- Correlate errors with specific model/prompt combinations
- Enable precise rollback capabilities

### 4. Cost Optimization
- Track token usage across different model versions
- Identify cost-efficient model/prompt combinations
- Monitor pricing changes across model versions

## Implementation Considerations

### Version Format Standardization
While version formats may vary by provider, consider:
- Using semantic versioning where applicable (e.g., `v1.2.3`)
- ISO 8601 dates for time-based versions (e.g., `2024-07-15`)
- Provider-specific identifiers when necessary (e.g., `gpt-4-0613`)

### Version Discovery
Instrumentations should:
- Automatically extract version information from API responses when available
- Allow manual version specification for custom deployments
- Handle missing version information gracefully

### Storage Considerations
- Version strings should be kept concise to minimize storage overhead
- Consider using consistent formats within an organization
- Document version naming conventions

## Backward Compatibility

These additions are backward compatible as they:
- Introduce new optional attributes
- Do not modify existing attribute definitions
- Follow established naming conventions

## References

- [OpenTelemetry GenAI Semantic Conventions Registry](https://opentelemetry.io/docs/specs/semconv/registry/attributes/gen-ai/)
- [OpenTelemetry Attribute Requirement Levels](https://opentelemetry.io/docs/specs/semconv/general/attribute-requirement-level/)
- [Existing LLM Span Conventions](https://github.com/open-telemetry/semantic-conventions/blob/main/docs/gen-ai/llm-spans.md)