package after_resolution

import rego.v1

allowed_types := {
    "attribute_mapping",
    "boolean",
    "integer",
    "key_filter",
    "key_redaction",
    "string",
    "string[]",
    "toggle",
    "value_filter",
    "value_limit",
}

signals contains entry if {
    some metric in input.registry.metrics
    entry := {"name": metric.name, "signal": metric}
}

signals contains entry if {
    some span in input.registry.spans
    entry := {"name": span.type, "signal": span}
}

signals contains entry if {
    some event in input.registry.events
    entry := {"name": event.name, "signal": event}
}

config(node) := object.get(object.get(node, "annotations", {}), "config", {})

property_bindings contains binding if {
    some entry in signals
    signal_config := config(entry.signal)
    scope := object.get(signal_config, "scope", "")
    scope != ""
    some name, property in object.get(signal_config, "properties", {})
    binding := {
        "scope": scope,
        "name": name,
        "property": property,
        "source": entry.name,
    }
}

property_bindings contains binding if {
    some entry in signals
    signal_scope := object.get(config(entry.signal), "scope", "")
    signal_scope != ""
    some attribute in object.get(entry.signal, "attributes", [])
    attribute_config := config(attribute)
    some name, property in object.get(attribute_config, "properties", {})
    binding := {
        "scope": signal_scope,
        "name": name,
        "property": property,
        "source": sprintf("%s attribute %s", [entry.name, attribute.key]),
    }
}

property_bindings contains binding if {
    some entry in signals
    signal_scope := object.get(config(entry.signal), "scope", "")
    signal_scope != ""
    some attribute in object.get(entry.signal, "attributes", [])
    attribute_config := config(attribute)
    declared_scope := object.get(attribute_config, "scope", "")
    declared_scope != ""
    declared_scope != signal_scope
    some name, property in object.get(attribute_config, "properties", {})
    binding := {
        "scope": declared_scope,
        "name": name,
        "property": property,
        "source": sprintf("%s attribute %s", [entry.name, attribute.key]),
    }
}

deny contains finding if {
    some binding in property_bindings
    property_type := object.get(binding.property, "type", "")
    not property_type in allowed_types
    finding := {
        "id": "config_property_type",
        "message": sprintf("Configuration property '%s.%s' has unknown type '%s'.", [binding.scope, binding.name, property_type]),
        "level": "violation",
        "context": {},
    }
}

deny contains finding if {
    some first in property_bindings
    some second in property_bindings
    first.scope == second.scope
    first.name == second.name
    first.source < second.source
    first.property != second.property
    finding := {
        "id": "config_property_conflict",
        "message": sprintf("Configuration property '%s.%s' has conflicting declarations in '%s' and '%s'.", [first.scope, first.name, first.source, second.source]),
        "level": "violation",
        "context": {},
    }
}
