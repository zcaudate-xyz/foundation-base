# PHP xtbench Fix Plan

## Context

- CI run: https://github.com/zcaudate-xyz/foundation-base/actions/runs/28769746853/job/85300939697
- Target image: `ghcr.io/zcaudate-xyz/infra-foundation-xtbench-php:ci` (defined in `../infra-dev`)
- The PHP xtbench image is built from `../infra-dev/infra/Dockerfile_foundation_xtbench_ci` (case `php` installs `php-cli`).
- Workflow reference in `foundation-base/.github/workflows/run-xtbench.yml` now points at the dedicated PHP image.

## Baseline Failure Data

Run `lein seedgen test '[xt.lang]' :with "[php]"`.

| Commit | failed | throw | Notes |
|--------|--------|-------|-------|
| `4d5bb9e` (CI before fixes) | 34 | 337 | Mostly compile-time `No matching clause` errors |
| Local with current uncommitted PHP fixes | 141 | 239 | Compile-time errors reduced; runtime errors now dominate |

The shift from throw → failed is expected: once primitives emit valid PHP, the generated tests actually execute and surface runtime bugs.

## Root Cause

The PHP xtalk backend (`src/hara/model/annex/spec_xtalk/fn_php.clj`, `src/hara/model/annex/spec_php.clj`, `src/hara/model/annex/spec_php/rewrite.clj`) is missing emit support for many primitives used by `xt.lang`. The CI log shows two failure modes:

1. **Compile-time**: `No matching clause` / `hara.lang emit form failed` for unregistered ops.
2. **Runtime**: Generated PHP executes but returns wrong values or crashes because macros generate incorrect PHP (e.g., uninitialized arrays, wrong arity, missing helpers).

## Already Completed

Implemented in working tree (uncommitted):

1. `src/hara/model/annex/spec_xtalk/fn_php.clj`
   - `+php-for+` macros: `php-tf-for-array`, `php-tf-for-object`, `php-tf-for-iter` generating `foreach` forms.
   - `+php-lu+` added `:x-lu-create {:emit :unit :default []}`.
   - `+php-ex+` macros: `php-tf-x-ex-new`, `php-tf-x-ex-message`, `php-tf-x-ex-data`.
2. `src/hara/model/annex/spec_php.clj`
   - Registered `:foreach` as a `:block` op with raw `"foreach"` and parameter separator `" as "`.
3. `src/hara/model/annex/spec_php/rewrite.clj`
   - `rewrite-for*` now prefixes vector lhs elements with `$`.
   - Added `rewrite-foreach*` and registered `foreach` in the rewrite dispatch.

Verification: `lein test :only hara.model.annex.spec-php-test` passes.

## Remaining Work (prioritized)

### Phase 1 — Get core primitives emitting correctly

These are the highest-leverage fixes; many downstream `common-data` / `common-promise` tests depend on them.

1. **`x:ex-native?`**
   - PHP equivalent: `($err instanceof Throwable)`.
   - Add `php-tf-x-ex-native?` and register in `+php-ex+`.

2. **`x:async-run` and `x:with-delay`**
   - PHP is synchronous; implement as immediate synchronous dispatch.
   - `x:async-run`: `(call_user_func_array $thunk [])`.
   - `x:with-delay`: use `usleep($ms * 1000)` then invoke thunk, matching the existing `+php-thread+` `x:with-delay` but wiring it into the xtalk primitive set.

3. **Missing `x:arr-*` functional primitives**
   - `x:arr-clone`, `x:arr-each`, `x:arr-every`, `x:arr-some`, `x:arr-map`, `x:arr-filter`, `x:arr-foldl`, `x:arr-foldr`, `x:arr-find`.
   - Most map to built-ins (`array_map`, `array_filter`, `array_reduce`) or small loops.

4. **Missing `x:obj-*` primitives**
   - `x:obj-keys` (`array_keys`), `x:obj-vals` (`array_values`), `x:obj-pairs` (loop building `[[k v], ...]`), `x:obj-clone` (`array_merge([], $obj)`), `x:obj-assign` (`array_merge($obj, $m)`).

5. **`x:get-key` / `x:set-idx`**
   - `x:get-key`: map to `array_key_exists` / `$obj[$key]` with optional default.
   - `x:set-idx`: map to array assignment `$arr[$idx] = $val`.

### Phase 2 — Fix runtime initialization bugs

Local runs show errors such as:

```
TypeError: array_push(): Argument #1 ($array) must be of type array, null given
```

This means `(var out [])` is not reliably producing an initialized PHP array. Investigate and fix:

1. How `var` / `var*` emits array initializers in the PHP grammar.
2. Whether empty vectors `[]` are emitted as `[]` or `null`.
3. The interaction between `php-rewrite-stage` and vector literals.

### Phase 3 — Complete spec-base coverage

Remaining spec-base ops from the failure list:

- `proto:create`, `proto:method`
- `x:file-resolve`, `x:pwd`
- `.` property/index access
- `x:is-object?`, `x:not-nil?`, `x:iter-has?`, `x:iter-native?`
- `x:lu-eq`
- `x:str-substring`, `x:set-idx`
- `x:m-pow` (already aliased to `pow`; may just need `:value true` or similar)

### Phase 4 — Library-layer fallout

Once primitives are stable, the following namespaces should largely resolve because they are built on top of the primitives above:

- `xt.lang.common-data` — most arr/obj helpers.
- `xt.lang.common-iter` — iterator helpers.
- `xt.lang.common-promise` — promise internals using `for:array` and `x:ex-*`.
- `xt.lang.common-math` — `pow`, `mod`, `round`, etc.
- `xt.lang.common-string` — string helpers.
- `xt.lang.common-color` — color conversions.

### Phase 5 — Verify and push

1. Run `lein seedgen test '[xt.lang]' :with "[php]"` locally until `failed` and `throw` counts stop improving.
2. Run `lein seedgen test '[xt.event]' :with "[php]"` and `lein seedgen test '[kmi.lang]' :with "[php]"`.
3. Remove temporary files (`.tmp_test_macro.clj`, generated `test-lang/xtbench/php/*` if not intended to be committed).
4. Commit with a clear message referencing the CI run and the primitives added.
5. Push to `main` in `foundation-base`.

## Key Files

- `src/hara/model/annex/spec_xtalk/fn_php.clj` — add macros and register ops.
- `src/hara/model/annex/spec_php.clj` — grammar/template overrides.
- `src/hara/model/annex/spec_php/rewrite.clj` — local-variable `$` prefixing and special forms.
- `test/hara/model/annex/spec_php_test.clj` — add unit emit tests for new primitives.
- `.github/workflows/run-xtbench.yml` — already updated to use the PHP xtbench image.

## Reference Implementations

Comparable backends for the missing primitives:

- JavaScript: `src/hara/model/spec_xtalk/fn_js.clj`
- Python: `src/hara/model/spec_xtalk/fn_python.clj`
- Ruby: `src/hara/model/annex/spec_xtalk/fn_ruby.clj`

## Risk / Scope Note

Getting `xt.lang` fully green for PHP is a larger effort than the initial loop/exception fixes (~50+ primitives plus runtime initialization fixes). The current changes are a meaningful first slice that removes the bulk of `No matching clause` errors. The plan above breaks the remaining work into prioritized phases so each can be committed and verified independently.
