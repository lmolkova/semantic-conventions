# v2 schema migration — outstanding work

The migration is implemented in the working tree: all doc snippets use
`<!-- weaver <jq> -->` markers, and `--v2` is set on the `registry-generation`,
`table-generation`, and `table-check` Makefile targets. Templates live under
`templates/registry/markdown/`. What remains is (1) splitting the change into
reviewable PRs, (2) a few blocked/deferred snippets, and (3) confirming the
intentional cosmetic diffs.

Converted so far: the common public attribute groups (server/client/…), hardware
metrics + refinements, all messaging systems (v2 spans per operation type), and
the z/OS resource entities (`entity_refinements`). No `semconv-v2-todo` markers
remain in `docs/messaging/` or `docs/resource/zos.md`.

## Rollout / PR decomposition plan

The working tree is one ~300-file change. It splits along three independent axes:
model syntax (`groups:` → `definition/2`), registry-page generation, and inline
snippet generation + markers. Key enabling facts:

- weaver reads `definition/2` natively (no-flag resolve works), and a v1
  render of a converted model is **output-neutral** for plain attribute groups
  (verified: `thread.md` diff == unconverted `code.md` diff — churn is template-,
  not model-driven).
- v2 **spans** and **entity refinements** preserve per-ref requirement levels, so
  they render correctly through the current v1 toolchain too — no freeze needed to
  land them pre-flip.
- `--v2` rejects old `<!-- semconv <id> -->` markers but silently ignores
  `<!-- semconv-v2-todo <id> -->` (proven: 6 frozen blocks coexisted with 640
  live `weaver` markers under `--v2`). That freeze keyword is the cheap "enclave"
  for anything not yet convertible — no dual-toolchain needed.

Suggested landing order (each row is one or a few PRs; Tier 1 has no dependency on
the flip and can land in any order):

| # | PR | Scope |
|---|----|-------|
| 1a | commons → v2 | `client`, `server`, `source`, `destination`, `network` — byte-identical output |
| 1b | commons → v2 | `thread`, `service`, `session`, `log`, `exceptions`, `pprof`, `profile`, `opentracing` |
| 1c | hardware → v2 | `model/hardware/*` + `policies/yaml_schema.rego` stopgap (introduces `metric_refinements`) |
| 1d | messaging → v2 | `model/messaging/*` + `docs/messaging/*` — real table diff, levels intact |
| 1e | zos → v2 | `model/zos/entity-refinements.yaml` + trimmed `common.yaml` + `docs/resource/zos.md` |
| 2a | registry pages → v2 | `registry-generation --v2` + registry templates → `docs/registry/**` (structural: v2 flattens per-group sections; drops group briefs) |
| 2b | **the flip** | `--future`→`--v2`, snippet/span/metric templates, swap all remaining `semconv`→`weaver` markers |
| 3 | content unfreeze | remaining `semconv-v2-todo` (see Deferred) once their blockers clear |

Because the working tree already flipped everything to `weaver` markers, a Tier-1
PR that must land *before* the flip needs its docs reverted to live v1
`<!-- semconv span.X -->` / `attribute_groups` markers (rendered by the v1
toolchain), then re-swapped to `weaver` at 2b. Alternatively, do 2b first and let
Tier-1 land on top — but then 2b carries all the real table diffs instead of just
the marker mechanics. Prefer Tier-1-first so each namespace's table diff is
reviewed in isolation.

## Outstanding blockers & follow-ups

### Requirement level dropped on public attribute groups (weaver dep)

weaver's resolved `AttributeGroup.attributes` is `Vec<AttributeRef(u32)>` (catalog
indices to definitions, which carry no `requirement_level`), so per-ref levels are
dropped for **public attribute groups** at resolution. `attribute_table.j2`
auto-hides the Requirement Level column when no attribute carries a level, so the
16 converted common groups render **without** that column (e.g. `log-exception`
loses its `Conditionally Required` column + conditional footnotes). Levels are
**kept in the YAML**; the column returns automatically once weaver emits per-ref
levels for public groups. (Spans, metric refinements, and entity refinements are
unaffected — they preserve levels.)

### Metric-refinement policy stopgap (rego)

`make check-policies` runs `registry check` **without `--v2`**, evaluating the
v1-translated groups. A `metric_refinement` resolves to a group with a refinement
id `{metric_name}.{ctx}` (e.g. `hw.errors.network`), which the
`metric.{metric_name}` id rule in `policies/yaml_schema.rego` rejected. Stopgap
applied: that rule now skips ids of the form `{metric_name}.{ctx}` (see the
`TODO(v2-migration)` comment). This slightly weakens typo protection for real
metric ids starting with `{metric_name}.`. Proper fix: run policies against the
v2 schema so refinements are first-class, then restore the strict rule.

### Entity refinements can't refine identity attributes (weaver issue + policy)

`entity_refinements` only accepts a `description:` list; `identity:` and a `role:`
field on a ref are rejected. Overriding an identity attribute via `description:`
silently re-roles it to descriptive (already affects z/OS `process.pid`, and all
four zos refined entities show the "no identity attributes" warning). Draft weaver
issue: `scratchpad/weaver-entity-refinement-identity.md` (not yet filed). Related
**policy decision** for our repo (not weaver): forbid `stability` overrides on
refinement refs — weaver accepts and *applies* them (base `development` →
refinement `stable`), which is almost certainly a mistake; enforce via
`policies/*.rego`.

### Deferred snippets (still frozen as `semconv-v2-todo`)

3 remain, all outside messaging/zos:

- `faas.attributes` (`docs/faas/faas-spans.md`) — attribute-group snippet not yet
  converted (decide: public group vs span vs entity refinement).
- `event.azure.resource.log`, `event.browser.web_vital` — need v2 event `body`
  support in weaver.

## Accepted v2 changes to confirm (sign-off)

Intentional diffs vs v1; all 930 attribute anchors and every enum value are
preserved (verified by set-diff). Confirm these are acceptable:

1. **Group intro briefs dropped** from attribute registry pages (per-group
   sections collapse; briefs were boilerplate).
2. **Multi-group registry pages flattened** to one "<NS> Attributes" + one
   "Deprecated <NS> Attributes" section; single alphabetical sort shifts footnote
   numbers on those pages.
3. **Entity role-less attributes** render as **Description** (was **Other**); the
   "attributes without a role" warning is replaced by a "no identity attributes"
   warning (35 entities). No attributes lost.
4. **Entity snippets** gain one trailing blank line before
   `<!-- prettier-ignore-end -->` (cosmetic; consistent with metric/span/event).
5. **Attribute links** derive from the key namespace, so they follow the new page
   names (e.g. `hw.*` → `hw.md`). Page renames: `hardware.md`→`hw.md`,
   `oracledb.md`→`oracle.md`; new fully-deprecated pages `az.md`, `net.md`,
   `pool.md`, `state.md`, `message.md`, etc. (0 non-deprecated attributes — render
   only a "Deprecated <NS>" section).
