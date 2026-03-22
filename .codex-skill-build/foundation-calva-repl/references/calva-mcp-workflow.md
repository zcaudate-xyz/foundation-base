# Calva MCP Workflow

Use this reference when the skill needs concrete Calva MCP patterns.

## Session Discovery

Start with `clojure_list_sessions`.

- If a `clj` session exists for `foundation-base`, use it.
- If no session exists, do not fake REPL evidence. Read code, explain the gap, and validate with shell commands instead.

## Minimal Probe Pattern

1. Require the namespace in `user`.
2. Evaluate the smallest expression that exposes the behavior.
3. Move to the target namespace only when that reduces noise.

Example setup in `user`:

```clojure
(require 'rt.postgres.grammar.typed-analyze :reload)
(require 'rt.postgres.grammar.typed-parse :reload)
```

Example targeted probe:

```clojure
(let [analysis (-> 'rt.postgres.script.test.scratch-v2
                   rt.postgres.grammar.typed-parse/analyze-namespace
                   rt.postgres.grammar.typed-parse/register-types!)
      fn-def   (some #(when (= "insert-entry" (:name %)) %)
                     (:functions analysis))]
  (rt.postgres.grammar.typed-analyze/infer-report fn-def))
```

## Tool Selection

- Use `clojure_evaluate_code` for live evaluation.
- Use `clojure_symbol_info` when you need arglists, doc, or source location for a project var.
- Use `clojure_repl_output_log` when code starts runtimes, background work, or prints diagnostics.
- Use `clojuredocs_info` only for core functions or macros.

## Namespace Discipline

- Evaluate the `ns` form in `user` or require the namespace before calling into it.
- Prefer fully qualified vars in probes when moving between namespaces would add confusion.
- Reload aggressively when changing macros, grammar tables, or emitters.

## Editing Discipline

Calva's structural editing tools are the safest path for Clojure changes in this repo.

- Replace or insert one top-level form at a time.
- Work from the bottom of the file upward if multiple forms change.
- Re-run the exact proving probe after each meaningful edit, not just at the end.
