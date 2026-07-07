# Code Tools

The `code.*` namespaces provide the development and maintenance layer for Foundation Base.

## Main areas

| Namespace | Purpose | Guide or page |
|---|---|---|
| `code.test` | Fact-based tests, arrow assertions, checkers, skips, and targeted execution | [Guide](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-doc/documentation/code/code_test.clj) |
| `code.manage` | Source analysis, scaffolding, locating, refactoring, and test management | [Guide](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-doc/documentation/code/code_manage.clj) |
| `code.query` | Structural matching, traversal, and source transformations | [Guide](https://github.com/zcaudate-xyz/foundation-base/blob/main/src-doc/documentation/code/code_query.clj) |
| `code.framework` | Source and test metadata used by other code tools | [Published page](https://zcaudate.xyz/foundation-base/code/code-framework.html) |
| `code.doc` | Static documentation parsing, linking, rendering, themes, and publishing | [Published page](https://zcaudate.xyz/foundation-base/code/code-doc.html) |
| `code.project` | Project metadata and namespace-to-file lookup | [Published page](https://zcaudate.xyz/foundation-base/code/code-project.html) |
| `code.tool` | Build, Java, Maven, and measurement utilities | [Published page](https://zcaudate.xyz/foundation-base/code/code-tool.html) |

## Testing

Foundation Base uses `code.test`, not `clojure.test`.

```clojure
(ns example.core-test
  (:require [code.test :refer [fact =>]]))

(fact "addition works"
  (+ 1 2) => 3)
```

Run a focused namespace:

```bash
lein test :only std.lib.collection-test
```

Run a namespace family:

```bash
lein test :with std.lib
```

## Documentation

Documentation source lives in:

- [`src-doc/documentation`](https://github.com/zcaudate-xyz/foundation-base/tree/main/src-doc/documentation)
- [`config/publish`](https://github.com/zcaudate-xyz/foundation-base/tree/main/config/publish)

Generate and serve documentation:

```bash
lein publish
lein serve
```

The root documentation page also provides a [Code Tools bridge](https://zcaudate.xyz/foundation-base/code-tools.html) with direct GitHub links so readers are not blocked if the full generated subsite is stale or missing.
