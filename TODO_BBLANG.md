# Babashka Port for std.lang

## Overview
The goal is to provide a `bb.lang` namespace that contains the core logic of `std.lang` without the heavy Java interop and `std.lib` custom metaprogramming, allowing it to run within Babashka. We have started duplicating `std.lang` logic into `src/bb/lang/` and its required helper namespaces into `src/bb/lib/` and `src/bb/string/`.

## Remaining Work

1. **Untangle `definvoke` usage**
   `std.lib.invoke` heavily utilizes macros and Java interoperability (e.g. creating records that implement `clojure.lang.IFn`). Babashka does not support this.
   - We must go through all `bb.*` files and replace `(definvoke my-func ...)` with standard `(defn ...)` or native `(clojure.core/memoize ...)`.
   - `path-separator` in `bb.string.coerce` has already been converted. Look out for others across `bb.lang.base.*`.

2. **Clean up `bb.lib` and `bb.string`**
   - Several `bb.lib.*` files (like `bb.lib.memoize` and `bb.lib.impl`) still contain code intended to compile JVM byte-code or interact with deeply embedded `std.lib` concepts. These need to be rewritten to their simplest possible Babashka-compatible Clojure forms.
   - Replace complex record-based implementations (like `Memoize`) with simpler atoms or native functions.
   - Fix unresolved symbols and missing methods that stem from stripping Java imports out (like missing `StringBuffer` which needs to be replaced with `StringBuilder`).

3. **Get `bb.lang.base.emit` compiling**
   - Run `bb -cp src -e "(require 'bb.lang)"` iteratively. This will surface `definvoke` or `defrecord` errors one-by-one.
   - Once `bb.lang` loads cleanly in Babashka, test its fundamental execution capabilities.

4. **Tests**
   - We have not copied `test/std/lang` to `test/bb/lang` yet. Once the source compiles in Babashka, copy the test directories.
   - Refactor tests to utilize `clojure.test` since the custom `code.test` framework (`fact`, `=>`) relies on the full `std.lib` stack and macro ecosystem which will likely not compile in Babashka.
