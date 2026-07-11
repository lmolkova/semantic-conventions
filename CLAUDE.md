# OpenTelemetry Semantic Conventions

Definitions of names and values for telemetry (attributes, metrics, spans, events, resources) shared across the OpenTelemetry ecosystem.

## The one rule that matters

**YAML is the source of truth; Markdown tables are generated.** Never hand-edit generated tables.

- `model/{namespace}/*.yaml` — the definitions you edit (`registry.yaml`, `spans.yaml`, `metrics.yaml`, `events.yaml`, `resources.yaml`, `deprecated/`).
- `docs/{namespace}/*.md` — human docs. Prose is hand-written; the tables between `<!-- semconv <id> -->` / `<!-- endsemconv -->` markers are generated from the YAML.
- After editing YAML, run `make generate-all` to regenerate tables, then commit YAML + Markdown together.

Attributes live only in `model/{namespace}/registry.yaml` inside an `attribute_group` whose `id` starts with `registry.`. Signals (spans/metrics/events/resources) reference those attributes.

## Workflow for a change

1. Edit the YAML under `model/`.
2. `make generate-all` — regenerates registry + tables of contents + area tables.
3. `make check` — runs lint, spell, link, TOC, and policy/compatibility checks.
4. Add a changelog entry: `make chlog-new`, fill in the file, `make chlog-validate`.
   - `component` MUST be a folder name under `model/` (e.g. `http`, `db`).
   - Skip only for editorial/tooling/chore changes.

## Constraints

- Changes to existing conventions must respect [stability guarantees](CONTRIBUTING.md) and pass the backward-compat policies in `policies/compatibility.rego`.
- Follow naming / requirement-level rules in `docs/general/`.
- Links to the spec repo must point to a tag, not `main`.
- New Markdown files need Hugo frontmatter with `linkTitle`.

## Prerequisites

`npm install`; Docker or Podman (used to run the `weaver` and `opa` containers); on macOS `gsed` (`brew bundle`).

## Key references

- [CONTRIBUTING.md](CONTRIBUTING.md) — full contributor guide.
- [docs/how-to-write-conventions/README.md](docs/how-to-write-conventions/README.md) — defining a new area.
- YAML syntax: https://github.com/open-telemetry/weaver/blob/main/schemas/semconv-syntax.md
- `make help` (or read the [Makefile](Makefile)) for all targets.
