# Test Directory Summary

This document summarizes the findings from exploring the `test/` directory of the `foundation-base` project.

## 1. Directory Structure

The `test/` directory mirrors the structure of the `src/` directory, adhering to the standard Clojure convention where `test/x/y/z_test.clj` tests `src/x/y/z.clj`.

### Top-level Subdirectories
*   `cases/`: Likely test cases or data.
*   `code/`: Tests for `src/code/` (project management, dev tools, documentation, etc.).
*   `js/`, `lua/`, `python/`: Likely tests for language-specific implementations or DSL outputs.
*   `rt/`: Tests for "runtimes" (e.g., basic, Graal, Nginx, Postgres, Redis, Solidity).
*   `std/`: Tests for the standard library (`src/std/`). This appears to be the core utility testing area.
    *   `std/lib/`: Extensive collection of tests for utility libraries (collection, future, string, etc.).
    *   `std/lang/`: Tests for the DSL and language generation.
*   `web3/`: Tests for Web3/Solidity related functionality.
*   `xt/`: Tests for cross-talk or extended utilities.

## 2. Testing Framework

The project uses a custom or specialized testing framework `code.test`, which appears to wrap or extend standard testing capabilities.

### Key Characteristics
*   **Namespace:** `(ns ... (:use code.test) ...)` is the standard header.
*   **Fact Macro:** Tests are defined using the `fact` macro.
*   **Assertions:** The `=>` arrow syntax is used for assertions (e.g., `(expr) => expected`).
*   **Metadata:**
    *   `^{:refer <namespace>/<function> :added "<version>"}`: **Mandatory** metadata linking the test to the specific function it verifies. This facilitates traceability and potentially coverage analysis.
    *   `:setup` / `:teardown`: Used within the metadata for per-fact setup/teardown.
    *   `^:hidden`: Hides the output or details in certain reports (likely documentation generation).
*   **Global Setup**: `fact:global` is used for namespace-level setup/teardown (e.g., starting/stopping servers or runtimes).

## 3. Test Patterns Observed

### 3.1. Standard Library Tests (`std/lib/*`)
*   **Granularity:** Tests are highly granular, often mapping 1:1 with functions.
*   **Coverage:** Extensive coverage of utility functions (e.g., `collection_test.clj` tests `hash-map?`, `map-keys`, `merge-nested`).
*   **Style:** Functional style, focusing on input-output verification.

### 3.2. Project/Tooling Tests (`code/*`)
*   Tests project inspection, file lookup, and dev tools.
*   Mocking/Context: Uses `reset!` on dynamic vars (e.g., `*lookup*`) in `:setup` to isolate tests.

### 3.3. Multi-Language/DSL Tests (`std/lang/*`, `web3/*`)
*   **`l/script`**: Uses `std.lang` macros (`l/script`, `l/script-`) to define test contexts for specific target languages (e.g., Solidity, JS).
*   **Runtime Integration**: Tests interact with actual runtimes.
    *   `web3` tests start/stop a Ganache server using `fact:global`.
    *   `s/rt:deploy` is used to deploy contracts before running tests.
    *   `s/with:measure` suggests performance or gas usage tracking.

### 3.4. Runtime Tests (`rt/*`)
*   Tests specific runtime implementations (e.g., `rt.basic`, `rt.solidity`).
*   Verifies connection, execution, and cleanup logic.

## 4. Notable Findings

*   **Consistency:** The codebase strictly follows the naming convention (`ns-test` mirrors `ns`) and metadata linking (`:refer`).
*   **Integration Testing:** There is significant infrastructure for integration testing (starting runtimes like Ganache), which might explain why `lein test` can be slow or flaky in restricted environments.
*   **Custom Framework**: The reliance on `code.test` means standard `clojure.test` patterns (like `deftest`, `is`) are replaced by `fact` and `=>`.

## 5. Directives for Jules (Updated)

*   **When writing tests**:
    *   Always use `(:use code.test)`.
    *   Always include `^{:refer ...}` metadata.
    *   Use `fact` instead of `deftest`.
    *   Use `=>` for assertions.
*   **When debugging**:
    *   Look for `fact:global` to understand environmental dependencies.
    *   Be aware of `l/script` blocks if testing cross-platform code.
