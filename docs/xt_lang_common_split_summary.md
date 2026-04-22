# xt.lang `common_*` / xtalk Area Split Summary

This note captures the second pass of the xtalk split.

The first pass extracted reusable `xt.lang.common-*` modules out of the old
`xt.lang.base-lib` surface. The second pass reorganizes
`std.lang.base.grammar-xtalk-system` so the public xtalk grammar profiles now
match the implementation areas that `std.lang.base.grammar-xtalk` already uses
internally.

## Goal

- Keep the low-level `grammar-xtalk` implementation split detailed.
- Expose a simpler `grammar-xtalk-system` profile model.
- Let a new language, especially `:c`, adopt only the `common` xtalk subset
  first.
- Keep xtalk inventory, scanning, and support reporting organized around the
  five high-level implementation areas.

## First Pass Recap

The earlier split moved reusable helpers out of `xt.lang.base-lib` into smaller
modules such as:

- `xt.lang.common-base`
- `xt.lang.common-lib`
- `xt.lang.common-data`
- `xt.lang.common-math`
- `xt.lang.common-string`

That extraction is still the substrate for hard-linked xtalk helpers like:

- `xt.lang.common-base/identity`
- `xt.lang.common-data/obj-keys`
- `xt.lang.common-string/pad-left`
- `xt.lang.common-string/starts-with?`

That same extraction can keep growing in-place:

- `xt.lang.common-lib` for smaller helper and step combinators
- `xt.lang.common-data` for non-functional data helpers such as nested equality
  and diff operations

## Final `grammar-xtalk` Shape

`std.lang.base.grammar-xtalk` is now split into five implementation areas.

### 1. Common

Internal groups:

- `+xt-common-basic+`
- `+xt-common-index+`
- `+xt-common-number+`
- `+xt-common-nil+`
- `+xt-common-primitives+`
- `+xt-common-object+`
- `+xt-common-array+`
- `+xt-common-print+`
- `+xt-common-string+`
- `+xt-common-math+`

This is the portable substrate that a language like `:c` can implement first.

### 2. Functional

Internal groups:

- `+xt-functional-base+`
- `+xt-functional-invoke+`
- `+xt-functional-return+`
- `+xt-functional-array+`
- `+xt-functional-future+`
- `+xt-functional-iter+`

### 3. Language Specific

Internal groups:

- `+xt-lang-lu+`
- `+xt-lang-global+`
- `+xt-lang-proto+`
- `+xt-lang-bit+`
- `+xt-lang-throw+`
- `+xt-lang-unpack+`
- `+xt-lang-random+`
- `+xt-lang-time+`

### 4. `std.lang` Link Specific

Internal groups:

- `+xt-notify-socket+`
- `+xt-notify-http+`
- `+xt-network-socket+`
- `+xt-network-ws+`
- `+xt-network-debug-client-basic+`
- `+xt-network-debug-client-ws+`
- `+xt-network-server-basic+`
- `+xt-network-server-ws+`

### 5. Runtime Specific

Internal groups:

- `+xt-runtime-cache+`
- `+xt-runtime-thread+`
- `+xt-runtime-shell+`
- `+xt-runtime-file+`
- `+xt-runtime-b64+`
- `+xt-runtime-uri+`
- `+xt-runtime-js+`

## `grammar-xtalk-system` Reorg

The system layer now exposes one public xtalk profile per implementation area:

- `:xtalk-common`
- `:xtalk-functional`
- `:xtalk-language-specific`
- `:xtalk-std-lang-link-specific`
- `:xtalk-runtime-specific`

This is the key change.

The detailed split still lives in `std.lang.base.grammar-xtalk`, but
`std.lang.base.grammar-xtalk-system` now flattens those detailed groups into the
five public grammar profiles above.

## Why This Helps

### For new languages

A new language can start with:

- `:xtalk-common`

and only add the others when ready:

- `:xtalk-functional`
- `:xtalk-language-specific`
- `:xtalk-std-lang-link-specific`
- `:xtalk-runtime-specific`

This is especially useful for `:c`, where the initial implementation target is
the portable common subset rather than the full xtalk surface.

### For grammar organization

The public xtalk categories now line up with the implementation story:

- portable/common
- functional helpers
- language-specific behavior
- `std.lang` link adapters
- runtime adapters

That keeps:

- `grammar/build`
- xtalk scan metadata
- support matrix reporting
- generated inventory categories

aligned around the same five-part classification.

## Migration Notes

The old fine-grained public profile names such as:

- `:xtalk-common-object`
- `:xtalk-functional-array`
- `:xtalk-lang-lu`
- `:xtalk-runtime-js`

are no longer the public `grammar-xtalk-system` profile boundary.

They remain useful as internal implementation buckets inside
`std.lang.base.grammar-xtalk`, but the system-facing grammar profile model is
now area-based.

## Recommended Usage

### Minimal portable xtalk

Use:

- `:xtalk-common`

### Full xtalk language layer

Use:

- `:xtalk-common`
- `:xtalk-functional`
- `:xtalk-language-specific`

### Full xtalk runtime/link stack

Add:

- `:xtalk-std-lang-link-specific`
- `:xtalk-runtime-specific`

## Summary

The split is now two-level:

1. `xt.lang.common-*` provides the reusable hard-link substrate.
2. `grammar-xtalk` keeps detailed internal buckets.
3. `grammar-xtalk-system` exposes five public area profiles.

That gives us a simpler capability model for language bring-up while keeping the
implementation itself well-factored.
