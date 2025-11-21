# Agent Main Workflow

This document outlines the main workflow for an AI agent performing software development tasks within the `foundation-base` ecosystem. The workflow is designed to leverage the powerful code analysis, manipulation, and execution libraries available in the project.

## 1. Understand the Request

The first step is to parse and understand the user's request. This involves identifying the user's intent (e.g., add a feature, fix a bug, refactor code), the relevant files or modules, and any constraints.

## 2. Code and Documentation Search

To gather context, the agent should use the `code.query` library to search for relevant code and documentation.

*   **Tool:** `code.query`
*   **Key Functions:**
    *   `code.query.match/p-pattern`: To search for code structures that match a specific pattern.
    *   `code.query.match/p-form`: To find forms with a specific function call.
    *   `code.query.walk/matchwalk`: To traverse the code and find all occurrences of a pattern.

**Example:** To find all functions related to "user authentication", the agent could search for patterns like `(defn authenticate-user ...)` or forms that call `login`.

## 3. Analysis and Planning

Based on the search results, the agent should analyze the existing codebase and formulate a plan for implementing the requested changes. This may involve:

*   Identifying the files that need to be modified.
*   Determining the scope of the changes.
*   Planning the implementation steps, including any new functions or components that need to be created.

The `code.query.walk` and `code.query.traverse` functions can be used to analyze the Abstract Syntax Tree (AST) of the code and understand its structure in detail.

## 4. Implementation

The agent can implement the changes using a combination of `code.query` for code transformation and standard file I/O.

*   **For complex code transformations:** Use `code.query.walk/matchwalk` or `code.query.traverse/traverse` to programmatically modify the code's AST and then write the changes back to the file.
*   **For simpler changes:** Use standard file read/write operations.
*   **For multi-language tasks:** Leverage `xt.lang` to call functions and manipulate code across different languages (e.g., Clojure, Javascript, Lua).

## 5. Testing

Testing is a crucial part of the workflow. The agent must write and run tests to verify that the changes are correct and do not introduce any regressions.

### Test Format

The project uses a custom testing framework based on `code.test`. Tests are organized in `fact` blocks.

*   **File Naming:** Test files must follow the `*_test.clj` naming convention.
*   **`fact` Macro:** Each test case is defined within a `(fact ...)` block.
*   **Assertions:** Assertions are made using the `=>` operator.
*   **Metadata:** Each `fact` should be annotated with `^{:refer ...}` metadata to link it to the function being tested.

### Example Test

```clojure
(ns my-awesome-feature-test
  (:use code.test)
  (:require [my-awesome-feature :as awesome]))

^{:refer my-awesome-feature/do-something-cool :added "1.0"}
(fact "checks if do-something-cool returns the correct value"
  (awesome/do-something-cool) => "cool-value")
```

### Running Tests

The agent should run all relevant tests after making changes. The exact command for running tests will depend on the project's setup, but it is typically a command like `lein test` or a similar command provided by the project's build tool.

## 6. Task Execution and Reporting

The entire workflow can be encapsulated within a `std.task`. This allows for:

*   **Structured Execution:** Defining the workflow as a series of steps.
*   **Logging and Reporting:** Using `std.task.bulk` to provide detailed reports on the outcome of the task, including successes, failures, and warnings.
*   **Parallelism:** Running parts of the workflow in parallel (e.g., running tests for different modules simultaneously).

By following this workflow, the agent can perform complex software development tasks in a structured, reliable, and efficient manner, making full use of the powerful tools provided by the `foundation-base` project.
