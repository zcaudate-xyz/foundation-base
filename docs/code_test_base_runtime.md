# Dynamic Variables in `code.test.base.runtime`

This file documents the dynamic variables used in the `code.test.base.runtime` namespace. These variables are used to manage the state and configuration of the testing framework.

-   **`*eval-fact*`**: A boolean that indicates whether a `fact` is currently being evaluated.
    -   **Referenced In**:
        -   `src/code/test/base/runtime.clj`
        -   `src/code/test/compile.clj`

-   **`*eval-mode*`**: A boolean that indicates whether the test framework is in evaluation mode.
    -   **Referenced In**:
        -   `src/code/test/base/runtime.clj`
        -   `src/code/test/base/process.clj`
        -   `src/code/test/base/executive.clj`
        -   `src/code/test/compile.clj`
        -   `src/code/test/manage.clj`

-   **`*eval-replace*`**: Holds a value that can be used to replace a form during evaluation.
    -   **Referenced In**:
        -   `src/code/test/base/runtime.clj`
        -   `src/code/test/base/process.clj`
        -   `src/code/test/compile/types.clj`
        -   `src/code/test/compile/snippet.clj`
        -   `test/code/test/compile/snippet_test.clj`

-   **`*eval-meta*`**: A map containing metadata about the current evaluation.
    -   **Referenced In**:
        -   `src/code/test/base/runtime.clj`
        -   `src/code/test/base/process.clj`
        -   `src/code/test/compile/types.clj`

-   **`*eval-global*`**: A map containing global settings for the current namespace.
    -   **Referenced In**:
        -   `src/code/test/base/runtime.clj`
        -   `src/code/test/compile.clj`

-   **`*eval-check*`**: The current check function being evaluated.
    -   **Referenced In**:
        -   `src/code/test/base/runtime.clj`
        -   `src/code/test/base/process.clj`
        -   `src/code/test/compile/snippet.clj`

-   **`*eval-current-ns*`**: The namespace that is currently being tested.
    -   **Referenced In**:
        -   `src/code/test/base/runtime.clj`
        -   `src/code/test/base/executive.clj`

-   **`*run-id*`**: A unique identifier (usually a UUID) for the current test run. It is used to associate results with a specific execution.
    -   **Referenced In**:
        -   `src/code/test/base/runtime.clj`
        -   `src/code/test/base/process.clj`
        -   `src/code/test/base/executive.clj`
        -   `src/code/test/compile/types.clj`

-   **`*registry*`**: An atom that holds a map of all test data, including facts, indexed by namespace.
    -   **Referenced In**:
        -   `src/code/test/base/runtime.clj`

-   **`*accumulator*`**: An atom that accumulates test results during a test run.
    -   **Referenced In**:
        -   `src/code/test/base/runtime.clj`
        -   `src/code/test/base/executive.clj`
        -   `src/code/test/base/listener.clj`

-   **`*errors*`**: A collection of any errors that occurred during a test run.
    -   **Referenced In**:
        -   `src/code/test/base/runtime.clj`
        -   `src/code/test/base/executive.clj`
        -   `src/code/test/base/listener.clj`

-   **`*settings*`**: A map of settings for the test runner, such as the test paths.
    -   **Referenced In**:
        -   `src/code/test/base/runtime.clj`
        -   `src/code/test/base/process.clj`
        -   `src/code/test/base/executive.clj`

-   **`*root*`**: The root directory of the project.
    -   **Referenced In**:
        -   `src/code/test/base/print.clj`
        -   `src/code/test/base/runtime.clj`
        -   `src/code/test/base/executive.clj`

-   **`*results*`**: The final results of a test run.
    -   **Referenced In**:
        -   `src/code/test/base/runtime.clj`
        -   `src/code/test/base/process.clj`
        -   `src/code/test/compile/snippet.clj`
