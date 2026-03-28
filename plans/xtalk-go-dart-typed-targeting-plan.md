# xtalk typed expansion plan: Go and Dart

## Context

`xtalk` already has a typed layer (`defspec.xt`, parser/analysis/checking, and a TypeScript declaration emitter). The main opportunity is to reuse the type graph for **language-specific declaration output** and then optionally guide code generation/interop.

## Current capabilities (what we can leverage)

1. **Typed AST/registry and namespace analysis already exist**
   - `std.lang.typed.xtalk` exposes registration and analysis APIs (`analyze-namespace`, `analyze-and-register!`, registry lookups).
   - Parsed analysis includes `:specs`, `:functions`, `:macros`, and `:values` in a normalized type form.

2. **Type normalization already supports a useful common subset**
   - Primitive, named, maybe, union, intersection, tuple, array, dict, record, function, and generic application are all represented in the typed model.
   - This is enough to map to both Go and Dart with a few language-specific constraints.

3. **A working declaration backend exists (TypeScript)**
   - `std.lang.model.spec-js.ts` already demonstrates:
     - import/reference collection,
     - identifier sanitization,
     - type rendering,
     - namespace-level declaration generation.
   - This file is the best template for additional target backends.

4. **Go runtime/codegen already exists for xtalk language model**
   - `std.lang.model.spec-go` and `std.lang.model.spec-xtalk.fn-go` already provide syntax/runtime mappings for xtalk -> Go code generation.
   - Typed output can piggyback on this existing target instead of creating a net-new language pipeline.

## Recommended strategy

Use a **declaration-first approach** for both Go and Dart, then add optional runtime/codegen typing improvements.

### Phase 1: Shared typed declaration core (high ROI)

Create a small common module (e.g. `std.lang.model.spec-xtalk.typed.emit`) that encapsulates:

- namespace traversal and dependency grouping,
- symbol sanitization hooks,
- emitted declaration ordering,
- optional handling policies for unsupported type constructs.

Keep per-language files focused on rendering.

Why first: this minimizes duplicated logic currently embedded in `spec_js/ts.clj` and makes Go/Dart additions straightforward.

### Phase 2: Add Go typed declaration backend

Add a backend file (suggestion: `src/std/lang/model/spec_go/typed.clj`) implementing:

- `emit-go-type` for the normalized xtalk type tree,
- `emit-spec-declaration` as `type ...` aliases/structs/interfaces,
- `emit-function-signature` for function specs,
- `emit-namespace-declarations` as the public entrypoint.

Suggested type mapping policy:

- `:xt/str` -> `string`
- `:xt/bool` -> `bool`
- `:xt/int` -> `int`
- `:xt/num` -> `float64` (or configurable)
- `:xt/nil` -> `nil` only in pointer/interface contexts
- `:xt/any` / `:xt/unknown` -> `any` (Go 1.18+)
- `:xt/obj` -> `map[string]any`
- `:xt/fn` -> `func(...any) any`
- `[:xt/maybe T]` -> pointer form when possible (`*T`), else `interface{}`/`any` fallback
- `[:xt/record ...]` -> `struct`
- `[:xt/dict K V]` -> `map[K]V` (restrict `K` to comparable primitives)
- union/intersection -> fallback strategy (`any`) with generated comments for traceability.

### Phase 3: Add Dart typed declaration backend

Add backend file (suggestion: `src/std/lang/model/spec_dart/typed.clj`) implementing:

- `emit-dart-type` for normalized types,
- typedef/class generation,
- nullable type support (`T?`) for maybe,
- import generation across namespaces.

Suggested mapping policy:

- `:xt/str` -> `String`
- `:xt/bool` -> `bool`
- `:xt/int` -> `int`
- `:xt/num` -> `double`
- `:xt/nil` -> `Null`
- `:xt/any` / `:xt/unknown` -> `dynamic` (or `Object?`, configurable)
- `:xt/obj` -> `Map<String, Object?>`
- arrays -> `List<T>`
- dict -> `Map<K, V>` (prefer `String` keys where possible)
- records -> generated class with final fields + constructor
- fn -> `typedef`
- union/intersection -> sealed-class strategy (later), `Object?` fallback initially.

### Phase 4: Integrate with existing workflows

- Add stable public fns in an obvious namespace:
  - `emit-go-namespace-declarations`
  - `emit-dart-namespace-declarations`
- Optionally wire into `lein manage`/publish tasks after API settles.
- Add fixture-driven tests mirroring `spec_xtalk_typed_test` style.

## Important design choices

1. **Keep language backends lossy-but-explicit at first**
   Some xtalk type features (especially unions/intersections) do not map perfectly to Go/Dart without advanced wrappers. Emit safe fallback types plus comments instead of blocking generation.

2. **Separate declaration generation from runtime transpilation**
   Use typed specs for generated declarations immediately; do not couple this to changes in expression-level xtalk codegen until declarations are stable.

3. **Treat function specs as first-class output**
   The current TS emitter has helper functions for function/value declarations but only emits specs in the final namespace output path. Expanding declaration output to include functions/values should be part of the shared core.

4. **Add backend options early**
   Configuration options should include primitive mappings, strictness for unknown/union handling, naming/casing mode, and nullability policy.

## Minimal implementation sequence

1. Refactor reusable logic out of `spec_js/ts.clj` into a typed emit helper namespace.
2. Keep TS output identical (golden test).
3. Add Go emitter + tests on existing fixture namespace.
4. Add Dart emitter + tests on same fixture.
5. Add one mixed fixture exercising unions, optional fields, and cross-namespace references.

## Risks and mitigations

- **Risk:** Go/Dart mismatch for unions/intersections.
  - **Mitigation:** explicit fallback policy with comments + strict mode that throws when lossy mappings occur.

- **Risk:** named type collisions across namespaces.
  - **Mitigation:** keep current ns-prefixed sanitization pattern used by TS emitter.

- **Risk:** declaration drift from runtime semantics.
  - **Mitigation:** fixture tests that assert both typed analysis and emitted declarations for the same symbols.

## Practical recommendation

If the goal is “target more languages quickly,” start with **typed declaration generation** for Go and Dart first, because this is mostly additive and reuses existing typed infrastructure immediately. After that, incrementally introduce type-aware runtime/codegen rules where language backends benefit most (Go first, since an xtalk Go backend already exists).
