package after_resolution
import rego.v1

attribute_usages := { attr |
    group := [g | g := input.groups[_]; g.type != "attribute_group"][_];
    attr := group.attributes[_]
}

attr_registry_violation(description, group_id, attr_name) = violation if {
    violation := {
        "id": description,
        "type": "semconv_attribute",
        "category": "attribute_registry_checks",
        "group": group_id,
        "attr": attr_name,
    }
}

# TODO
deny contains attr_registry_violation(description, group.id, name) if {

    group := input.groups[_]
    startswith(group.id, "registry.")

    attr := group.attributes[_]
    not attr.deprecated
    name := attr.name

    usages := [usage.name |
        usage := attribute_usages[_]
        usage.name == name
    ]

    count(usages) == 0
    description := sprintf("Attribute '%s' defined in the group '%s' is not used.", [name, group.id])
}
