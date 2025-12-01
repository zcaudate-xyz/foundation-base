# Learnings

## `code.test` Framework

- **Fact Structure**: Tests are defined using `fact`. Metadata controls execution.
- **Component Injection**: Components like `|rt|` are NOT injected via `:use` or `:let` in the way standard `clojure.test` fixtures might work.
- **Global Components**: `fact:global` defines components that can be accessed in tests.
- **Broken `:use`/`:let`**: The `:use` and `:let` keys in `fact` metadata are stripped out by `fact-thunk` in `src/code/test/compile.clj` and are not functional for binding in the test body.
- **Setup/Teardown**: The correct way to handle per-test setup/teardown is using `:setup` and `:teardown` metadata keys, which can call helper functions.
- **Global State**: `fact:global` can define `:setup` and `:teardown` for the entire namespace, but per-fact isolation requires explicit `:setup`/`:teardown` on the fact itself if not using the global ones.

## `std.scheduler.spawn`

- **Runtime Management**: Spawns are managed within a runtime atom.
- **Testing Spawns**: Tests often require a runtime `|rt|` to be initialized.
- **Clear Operation**: The `clear` function needed to be updated to also remove spawns from the `:running` map, not just `:programs` and `:past`.

## `std.lang` Tests

- **Macro Expansion**: `defstruct.rs` and other macros in `std.lang.model.spec-rust` work correctly in isolation, suggesting previous "Syntax error macroexpanding" issues might have been context-dependent or transient.
- **R Integration**: `std.lang.model.spec-r-test` uses a guard `std.lang.spec.r/CANARY`. If this guard fails (returns false), the tests in that fact are skipped or fail with "Guard failed".
