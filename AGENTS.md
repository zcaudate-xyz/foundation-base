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
