## Testing Style Guide for `foundation-base`

This guide outlines the conventions and best practices for writing tests in the `foundation-base` project, based on the existing test suite.

### 1. File and Namespace Naming

*   **Test Files:** Test files should mirror the namespace of the code they are testing, with `-test` appended to the file name.
    *   *Example:* The tests for `std.lib.collection` are in `test/std/lib/collection_test.clj`.

*   **Test Namespaces:** Test namespaces should follow the same pattern as the file names, with `_test` appended to the namespace.
    *   *Example:* `std.lib.collection-test`

### 2. The `code.test` Framework

All tests in the `foundation-base` project are written using the `code.test` framework. This framework provides a set of macros and functions for defining, running, and asserting the behavior of your code.

#### 2.1. Test Structure

*   **`fact` Macro:** The primary macro for defining tests is `fact`. Each `fact` should test a single, specific piece of functionality.

*   **`^{:refer ...}` Metadata: The Cornerstone of Testability**

    It is **mandatory** for every `fact` to have `^{:refer ...}` metadata. This metadata links the test directly to the function it is testing. This is a crucial feature of the `code.test` framework for several reasons:

    *   **Traceability:** It provides a clear and explicit link between a test and the code it is testing, making it easy to understand what each test is for.
    *   **Code Coverage:** It allows for accurate tracking of test coverage, ensuring that all public functions are tested.
    *   **Maintainability:** When a function is changed or refactored, it is easy to find the corresponding tests and update them accordingly.

    ```clojure
    (ns std.lib.collection-test
      (:use code.test)
      (:require [std.lib.collection :as c]))
    
    ^{:refer std.lib.collection/map-keys :added "3.0"}
    (fact "changes the keys of a map"
      (c/map-keys inc {0 :a 1 :b 2 :c})
      => {1 :a, 2 :b, 3 :c})
    ```

#### 2.2. Scaffolding

*   **`fact:global`:** Use `fact:global` to define setup and teardown logic that should run before and after all tests in a namespace. This is the primary mechanism for scaffolding test environments.

*   **Fact-level Scaffolding:** For more granular control, you can use the `:setup` and `:teardown` keys within the `fact` metadata to define setup and teardown logic for a single fact.

    ```clojure
    ^{:refer std.lib.collection/map-keys :added "3.0"
      :setup [(println "Setting up fact")]
      :teardown [(println "Tearing down fact")]}
    (fact "demonstrates fact-level setup and teardown"
      (+ 1 1) => 2)
    ```

*   **Component Management:** `fact:global` is also used to manage test components, such as starting and stopping servers, clients, and other resources.

*   **Reusing Scaffolding:** The `:setup [(fact:global :setup)]` pattern is used to inherit and extend the setup logic from a more general `fact:global` definition. This is particularly useful for creating complex scaffolding setups.

#### 2.3. Assertions

*   **`=>` Operator:** The `=>` operator is used for assertions. The left side is the expression to be tested, and the right side is the expected result.
*   **`throws`:** To assert that an expression throws an exception, use `=> (throws)`.
*   **Checkers:** The `code.test.checker.common` namespace provides a set of checkers for more complex assertions.

```clojure
(fact "demonstrates various assertions"
  (+ 1 1) => 2

  (some? nil) => false?

  (throw (Exception. "error")) => (throws))
```

#### 2.4. Advanced Features

*   **`fact:let`:** Allows for running a test with different bindings, making it easy to test a function with various inputs.

```clojure
(fact:template "a template for testing addition"
  (fact:let [[a b c] [1 2 3]]
    (+ a b) => c))
```

*   **`fact:derive`:**  Creates a new test that inherits the setup and teardown logic from another test.

*   **`fact:template`:**  Defines a test that can be used as a template for other tests.

*   **`fact:check`:**  A macro for property-based testing, allowing you to test a function with a range of generated inputs.

*   **`fact:bench`:**  A macro for running micro-benchmarks on your code.

#### 2.5. Running Tests

*   **`executive/run-namespace`:** Runs all tests in a given namespace.
*   **`executive/run-current`:** Runs all tests in the current namespace.

#### 2.6. Debugging Tests

*   **`print/print-failure`:** Prints a detailed report of a test failure.
*   **`print/print-thrown`:** Prints a detailed report of an exception that was thrown during a test.

### 3. Cross-Platform Testing with `std.lang`

The `foundation-base` project uses a custom DSL provided by `std.lang` for writing code that can be compiled to multiple platforms (e.g., JavaScript, Lua). This DSL is also used for writing cross-platform tests.

*   **`l/script-`:** Use the `l/script-` macro to define platform-specific code and dependencies for your tests. You can have multiple `l/script-` blocks in a single test file, one for each platform you want to test against.

*   **`!.<platform>`:** Use the `!.<platform>` macro to execute a form on a specific platform. This allows you to write a single test that runs the same code on the JVM, in JavaScript, and in Lua, and asserts that the results are the same.

```clojure
(ns xt.lang.base-lib-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]]})

(fact "identity function works on all platforms"
  (!.js (k/identity 1))
  => 1

  (!.lua (k/identity 1))
  => 1)
```

### 4. Best Practices

*   **Always use `^{:refer ...}`:** Every `fact` must have `^{:refer ...}` metadata to link it to the function it is testing.
*   **Keep tests small and focused:** Each `fact` should test a single, specific piece of functionality.
*   **Use descriptive names for tests:** The description of a `fact` should clearly explain what the test is doing.
*   **Write tests for all new code:** All new features, bug fixes, and refactorings should be accompanied by tests.
*   **Run tests often:** Run your tests frequently to catch regressions early.

By following these conventions, you will ensure that your tests are consistent with the rest of the project, easy to read, and easy to maintain.
