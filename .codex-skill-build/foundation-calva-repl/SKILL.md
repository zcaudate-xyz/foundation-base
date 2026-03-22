---
name: foundation-calva-repl
description: Drive REPL-first Clojure work in foundation-base through Calva MCP. Use when Codex needs to inspect, debug, refactor, or implement changes in this repository by probing a live REPL, editing Clojure forms structurally, and validating with the project's custom `code.test` runner or narrow repo commands. Especially relevant for work in `code.manage`, `std.lang`, `rt.postgres`, and nearby namespaces where live evaluation is the fastest way to remove uncertainty.
---

# Foundation Calva Repl

Use Calva MCP as the primary instrument for understanding and validating behavior in `foundation-base`. Prefer the smallest live probe that proves the next decision.

## Core Workflow

1. Call `clojure_list_sessions` before assuming a REPL exists.
2. If a session is available, use the active `clj` session. If not, say so and fall back to static reading plus narrow shell validation.
3. Read only enough code to form a hypothesis, then test that hypothesis with `clojure_evaluate_code`.
4. Use `clojure_symbol_info` for project vars and `clojuredocs_info` only for Clojure core symbols.
5. Use `clojure_repl_output_log` when runtime output or startup failures matter.
6. Edit only after you can name the exact function or form to change and the REPL probe that justified it.
7. For Clojure edits, use the structural Calva tools one top-level form at a time.
8. Re-run the proving REPL probe after editing, then run the narrowest repo validator that covers the changed behavior.

## REPL Probing Rules

- Start in `user` for one-off `require` and setup work.
- Evaluate in the target namespace when calling vars from the file under change.
- If a namespace is not loaded yet, require it explicitly instead of assuming editor state:
  ```clojure
  (require 'rt.postgres.grammar.typed-analyze :reload)
  ```
- Keep probes data-oriented. Prefer returning maps, vectors, emitted strings, or reduced summaries over printing entire runtime objects.
- When a probe is expensive or mutating, create a smaller pure sub-probe first.

Read [references/calva-mcp-workflow.md](references/calva-mcp-workflow.md) for concrete tool usage patterns.

## Validation Choices

- Use `./lein test :only ...` for a single namespace.
- Use `./lein test :with ...` when a subsystem is spread across related namespaces.
- Avoid `./lein test` unless the task truly spans the full suite.
- Prefer REPL evaluation before shell test execution when the code under change exposes a pure function or emitter.
- For `code.manage` transforms, keep `:write false` until the output shape is understood.

## Repo Priorities

- Treat `code.test` as the test authority, not `clojure.test`.
- Treat `std.lang` and `rt.postgres` as emitter/grammar systems: confirm emitted data or analyzed forms in the REPL before editing surrounding plumbing.
- Treat `code.manage` tasks as data-returning operations first and file-writing tools second.

Read [references/foundation-base-notes.md](references/foundation-base-notes.md) when working in `code.manage`, `std.lang`, `rt.postgres`, or when choosing a validator.
