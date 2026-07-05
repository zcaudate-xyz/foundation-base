---
name: xtbench-triangulate
description: Pull a GitHub Actions xtbench run, extract failing namespaces per language/group, and triangulate whether the root cause is in the shared xt.* source test or in a language-specific spec/rewrite.
---

# xtbench Error Triangulation

Use this skill when an xtbench CI run is failing and you need to decide whether to look at the original `xt.*` test/implementation or at a language-specific spec/rewrite.

## What it does

Given a GitHub Actions run URL (or run id), it:

1. Downloads the run logs with `gh`.
2. Parses the per-language failing-function summary that `lein seedgen test` prints.
3. Maps generated `xtbench.<lang>.xt.*` namespaces back to their original `xt.*` source namespaces.
4. Groups failures by source namespace across languages.
5. Reports:
   - **Shared source namespaces** (failing in 2+ languages) → likely an issue in the original `test-lang/xt/...` test or `src-lang/xt/...` implementation.
   - **Language-specific namespaces** (failing in only one language) → likely an issue in that language's spec/rewrite (`src/hara/model/spec_<lang>.clj`, `src/hara/model/spec_<lang>/rewrite.clj`).

## Prerequisites

- `gh` CLI installed and authenticated (`gh auth status`).
- Run inside the foundation-base repo root.
- The run must be for the `Run xtbench Tests` workflow (`.github/workflows/run-xtbench.yml`).

## Quick usage

### From a run URL

```bash
lein exec scripts/xtbench_triangulate.clj https://github.com/zcaudate-xyz/foundation-base/actions/runs/28731393184
```

### From a bare run id

```bash
lein exec scripts/xtbench_triangulate.clj 28731393184
```

### From an already-downloaded log directory

If you have already extracted the zip of logs:

```bash
lein exec scripts/xtbench_triangulate.clj /path/to/extracted-logs
```

### Override the repo

```bash
lein exec scripts/xtbench_triangulate.clj 28731393184 --repo my-org/foundation-base
```

## Manual workflow (no helper)

If you cannot use the helper script, do it by hand:

1. **Get the run id** from the URL (`.../runs/<id>`).
2. **Download logs**:
   ```bash
   gh api repos/zcaudate-xyz/foundation-base/actions/runs/<id>/logs \
     --method GET -H "Accept: application/vnd.github+json" > logs.zip
   unzip logs.zip -d xtbench-logs
   ```
3. **Find the top-level job logs**: files like `0_04 - xt.db _ dart.txt`, `11_01 - xt.lang _ lua.txt`.
4. **Extract the seedgen summary**: look for the line `[seedgen] per-language failing functions:` followed by a Clojure map.
5. **Map back to source namespaces**:
   - Function symbols like `xt.lang.common-repl/notify-with-promise` → source ns `xt.lang.common-repl`.
   - Errored namespaces like `xtbench.dart.db.system.impl-sqlite-test` → source ns `xt.db.system.impl-sqlite-test`.
6. **Triangulate**: same source ns in multiple languages → original xt.* issue; unique to one language → language spec/rewrite.

## How to read the report

The helper prints a markdown report:

- **Failures by Group / Language**: raw counts of errored / failed / throw / timeout per matrix job.
- **Shared source namespaces**: these are the prime candidates for original `xt.*` problems.
- **Language-specific namespaces**: grouped by language; inspect that language's spec/rewrite files.

Each entry lists the source test file, source implementation file, and (for language-specific issues) the relevant `spec_<lang>` files.

## xtbench group reference

| CI group | Seedgen selector | Source tests under | Source impl under |
|----------|------------------|--------------------|--------------------|
| `01 - xt.lang` | `'[xt.lang]'` | `test-lang/xt/lang/*_test.clj` | `src-lang/xt/lang/*.clj` |
| `02 - xt.event` | `'[xt.event]'` | `test-lang/xt/event/*_test.clj` | `src-lang/xt/event/*.clj` |
| `03 - xt.substrate` | `'[xt.substrate]'` | `test-lang/xt/substrate/*_test.clj` | `src-lang/xt/substrate/*.clj` |
| `04 - xt.db` | `'[xt.db]'` | `test-lang/xt/db/*_test.clj` | `src-lang/xt/db/*.clj` |

## Language-specific files

For a language `<lang>` (e.g. `dart`, `ruby`, `python`, `lua`), check:

- `src/hara/model/spec_<lang>.clj`
- `src/hara/model/spec_<lang>/rewrite.clj`

Example for dart failures:

- `src/hara/model/spec_dart.clj`
- `src/hara/model/spec_dart/rewrite.clj`

## Reproducing locally

Once you have a suspect namespace, run just that language/selector locally:

```bash
# Example: xt.lang failures in dart
lein seedgen test '[xt.lang]' :with "[dart]"
```

For failures that look shared, run the same namespace in multiple languages to confirm.

## Example triangulation

Given failures:

- `xt.lang.common-protocol` throws in dart, lua, python, ruby.
- `xt.lang.spec-base` throws only in dart.

Interpretation:

- `xt.lang.common-protocol` is a **shared** issue → look at `test-lang/xt/lang/common_protocol_test.clj` and `src-lang/xt/lang/common_protocol.clj`.
- `xt.lang.spec-base` is **dart-specific** → look at `src/hara/model/spec_dart.clj` and `src/hara/model/spec_dart/rewrite.clj`.
