# Agent Instructions for Foundation Base

This document provides essential context for AI agents working with this codebase.

## Testing Framework

**IMPORTANT:** This project uses a custom testing framework (`code.test`), NOT the standard `clojure.test`.

### How Tests Work

- The `lein test` command is aliased in `project.clj` to run `code.test` (via `:aliases {"test" ["run" "-m" "code.test"]}`)
- This is NOT the standard Leiningen test runner - it's a custom test framework with different behavior
- Tests are defined using `(fact ...)` macros and `=>` assertions, not `deftest` and `is`

### Running Tests

```bash
# Run all tests (Warning: ~930 tests, may take several minutes)
lein test

# Run a specific test namespace (RECOMMENDED)
lein test :only code.doc-test
lein test :only std.lib.collection-test

# Run tests matching a namespace pattern
lein test :with code.doc
lein test :with std.lib
```

### Test Syntax

Tests use the `fact` macro and `=>` arrow assertions:

```clojure
^{:refer my.namespace/function-name :added "3.0"}
(fact "description of what this tests"
  (function-name arg1 arg2)
  => expected-result)
```

## code.doc - Documentation System

`code.doc` is the documentation generation system. It generates HTML documentation from Clojure source files.

### Architecture

```
config/publish.edn          # Main config, lists sites
config/publish/std.lib.edn  # Site config (pages, theme, output)
src-doc/documentation/*.clj # Documentation source files
public/                     # Generated HTML output
```

### Configuration Files

**Main Config** (`config/publish.edn`):
```clojure
{:template {...}
 :snippets "config/snippets"
 :sites {:core    [:include "config/publish/foundation.core.edn"]
         :std.lib [:include "config/publish/std.lib.edn"]}}
```

**Site Config** (`config/publish/std.lib.edn`):
```clojure
{:theme  "bolton"    ; Theme name (bolton/stark)
 :output "public"    ; Output directory
 :pages  {page-key   ; Page identifier
          {:input    "src-doc/documentation/file.clj"
           :title    "Page Title"
           :subtitle "Description"}}}
```

### Documentation Source Syntax

Documentation files are Clojure files in `src-doc/documentation/`:

```clojure
(ns documentation.my-page
  (:use code.test))

;; Chapter/Section headers
[[:chapter {:title "Introduction"}]]
[[:section {:title "Subsection"}]]

;; Markdown paragraphs (just strings)
"This is **markdown** text."

;; API documentation - auto-generated from namespace
[[:api {:namespace "std.lib.collection"}]]

;; API with filters
[[:api {:namespace "std.lib.collection"
        :only ["map-keys" "map-vals"]
        :exclude ["internal-fn"]}]]

;; Code examples (using fact macro)
(fact "map-keys example"
  (map-keys inc {0 :a 1 :b})
  => {1 :a 2 :b})

;; Non-running code examples
(comment
  (this-wont-run-but-will-be-displayed))

;; Reference source code
[[:reference {:refer "std.lib.collection/map-keys"}]]

;; Reference tests
[[:reference {:refer "std.lib.collection/map-keys" :mode :test}]]
```

### Generating Documentation

```bash
# Generate all docs for a site
lein exec -ep "(use 'code.doc) (publish '[std.lib] {:write true})"

# Generate specific page
lein exec -ep "(use 'code.doc) (publish '[std.lib/my-page] {:write true})"

# Generate without writing (dry run)
lein exec -ep "(use 'code.doc) (publish '[std.lib])"
```

Note: The page key uses the format `[site-key/page-key]` where page-key matches
the key defined in the site config (with hyphens instead of underscores).

### Available Elements

| Element | Purpose |
|---------|---------|
| `[[:chapter {:title "..."}]]` | Top-level section |
| `[[:section {:title "..."}]]` | Subsection |
| `[[:subsection {:title "..."}]]` | Lower-level section |
| `[[:api {:namespace "..."}]]` | Auto-generate API docs |
| `[[:reference {:refer "ns/fn"}]]` | Include source code |
| `[[:image {:src "..." :title "..."}]]` | Embed image |
| `[[:file {:src "..."}]]` | Include another file |
| `[[:code {:lang "python"} "..."]]` | Code in other languages |

### Page Key Mapping

In config: `:my-page` → In command: `[site/my-page]`

Example: `std-lib-collection` key in config → `[std.lib/std-lib-collection]` in command.

## Key Project Conventions

### Namespaces

- `std.*` - Standard libraries (core functionality)
- `code.*` - Development tools (test, manage, doc, framework)
- `rt.*` - Language runtimes

### Common Commands

```bash
# Start REPL
lein repl

# Run tests (custom framework)
lein test

# Run management tasks
lein manage

# Publish documentation (code.doc)
lein publish

# Install to local maven repo
lein install
```

### Project Structure

- `src/` - Main source code
- `test/` - Test files (mirror src structure)
- `src-doc/` - Documentation site source
- `public/` - Generated documentation output
- `config/` - Configuration files

## Important Notes

1. **Do not assume standard Clojure test conventions** - This project predates or diverges from common patterns
2. **The test framework modifies files** - Some tests (like `grep-replace`) can modify source files; the framework shows diffs but doesn't write unless explicitly told to
3. **Large codebase** - 930+ tests across many namespaces; prefer targeted test runs
