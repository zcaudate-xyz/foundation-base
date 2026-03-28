# spec_* model audit (2026-03-28)

This pass reviewed `src/std/lang/model/spec_*` files with a focus on consistency, typed-support readiness, and testability.

## Completed in this PR

1. **TypeScript declarations now include functions and values**
   - Updated `spec_js/ts.clj` so namespace declaration output emits `specs + functions + values`.
   - Import reference collection now includes function signatures and value types, not just spec types.

2. **Dart target now has a language grammar and xtalk helper rewrites**
   - Added base Dart grammar install (`:dart`) and helper fn mappings in `spec_xtalk/fn_dart.clj`.

3. **Go and Dart typed declaration backends added**
   - Added typed emitters that consume the normalized xtalk typed analysis for Go and Dart declarations.

4. **Strict/lossy conversion option added for Go and Dart typed emitters**
   - Both typed emitters now support `{:strict? true}` to throw when lossy mappings (union/intersection/tuple/apply) are encountered.

## Additional improvement opportunities found

### 1) `spec_rust.clj` has many placeholder docstrings (`"TODO"`)

The Rust backend has several public helper functions with no descriptive docstrings yet (`rst-typesystem`, `rst-vector`, `rst-defenum`, etc.). Replacing these with concrete behavior docs would substantially improve maintainability and onboarding.

### 2) fn mapping modules duplicate the same `add-sym` utility pattern

Files under `spec_xtalk/fn_*` repeat near-identical symbol-registration glue. A shared helper namespace for `add-sym` and common x-* wrappers would reduce drift and make adding future language backends faster.

### 3) typed declaration emitters should likely share a small common core

`spec_js/ts.clj`, `spec_go/typed.clj`, and `spec_dart/typed.clj` now each perform:
- identifier sanitization,
- named-type namespace disambiguation,
- declaration section assembly.

A shared `typed emit core` would reduce copy/paste and keep behavior aligned (especially around fallback policies for unions/intersections and maybe/nullability handling).

### 4) golden fixtures for cross-language parity

A single typed fixture namespace rendered through TS/Go/Dart emitters with snapshot assertions would help prevent regressions and ensure consistent naming/import/nullability policies across backends.

## Suggested next step order

1. Extract shared typed-emitter core utilities.
2. Add multi-backend golden fixture snapshots.
3. Replace Rust placeholder docstrings with concrete docs.
