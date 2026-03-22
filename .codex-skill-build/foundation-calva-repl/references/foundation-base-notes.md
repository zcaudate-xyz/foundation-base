# Foundation Base Notes

Use this reference for repo-specific behavior that differs from stock Clojure projects.

## Test Framework

- `lein test` is aliased to `run -m code.test`.
- Tests use `(fact ...)` and `=>`, not `deftest` and `is`.
- Prefer narrow runs:
  - `./lein test :only code.manage-test`
  - `./lein test :only std.lang-test`
  - `./lein test :only rt.postgres.grammar.typed-analyze-test`
  - `./lein test :with rt.postgres.grammar`
- Avoid assuming standard `clojure.test` fixtures or reporting.

## `code.manage`

- The public entry points live in `src/code/manage.clj`.
- Many tasks are safe to probe in the REPL because they return data.
- For transforms or imports, prefer `{:write false}` until the result shape is understood.
- If using the repository MCP server instead of direct REPL calls, `code-manage` expects EDN strings for `target` and `options`.

Useful REPL examples:

```clojure
(require '[code.manage :as manage])
(manage/analyse 'code.manage {:return :summary})
(manage/vars '#{code.manage} {:return :summary})
(manage/import '[code.manage.unit] {:write false})
```

## `std.lang`

- The public surface lives in `src/std/lang.clj`.
- Use live probes for emitters and library state instead of reading only macro-heavy internals.
- Prefer `l/emit-script` for statement-shaped forms and `l/emit-as` for expression-shaped probes.
- Useful introspection entry points include `l/default-library`, `l/get-book`, `l/get-snapshot`, and `l/lib:module`.

Useful REPL examples:

```clojure
(require '[std.lang :as l])
(l/emit-as :lua '[(:= a 1)])
(keys (l/get-snapshot (l/default-library)))
```

## `rt.postgres`

- The main public entry point is `src/rt/postgres.clj`.
- Much of the subsystem is grammar, analysis, and code generation. Prefer probing pure analysis functions before touching compile paths or runtime integration.
- Existing tests already use scratch modules such as `rt.postgres.script.test.scratch-v2`; reuse those patterns instead of inventing new fixtures.
- Helper reset functions exist in `rt.postgres`, including `purge-postgres` and `purge-scratch`, when cached library state gets in the way.

Useful REPL examples:

```clojure
(require '[rt.postgres :as pg])
(require '[rt.postgres.grammar.typed-parse :as parse])
(require '[rt.postgres.grammar.typed-analyze :as analyze])
(-> 'rt.postgres.script.test.scratch-v2
    parse/analyze-namespace
    parse/register-types!)
```

## Practical Defaults

- Use `./lein` from the repo root when invoking commands from shell automation.
- Keep validation tight: REPL probe first, one targeted test namespace second.
- When a change spans emitters plus tests, validate the emitter output before running the test namespace so failures stay local and legible.
