# Gemini Interaction Guidelines for `foundation-base`

This document outlines the best practices and workflow for Gemini when interacting with the `foundation-base` codebase. The primary goal is to ensure that all changes are made in a systematic, context-aware, and test-oriented manner.

## Core Mandates

1.  **Adhere to Project Conventions:** All code modifications must adhere to the existing conventions of the `foundation-base` project. This includes coding style, naming conventions, and architectural patterns.

2.  **Leverage Existing Libraries:** The agent must prioritize using the project's own libraries (`std.lib`, `code.query`, `std.task`, `xt.lang`) for all tasks, as outlined in the workflow below.

3.  **Test-Driven Development:** All new features, bug fixes, or refactorings must be accompanied by tests. The agent must follow the project's testing conventions.

4.  **Clarity and Communication:** The agent should provide clear and concise explanations of its plan and the changes it has made. When in doubt, it should ask for clarification from the user.

## Primary Workflow

The agent will follow the workflow defined in `agents/main_workflow.md`:

1.  **Understand the Request:** Analyze the user's request to determine the intent, scope, and requirements.

2.  **Code and Documentation Search:** Use `code.query` to find relevant code and documentation within the project.

3.  **Analysis and Planning:** Analyze the search results to create a detailed plan for implementing the changes.

4.  **Implementation:** Modify the code using `code.query` for transformations and `xt.lang` for multi-language tasks.

5.  **Testing:** Write and run tests using the `code.test` framework to verify the changes.

6.  **Task Execution and Reporting:** Wrap the entire process in a `std.task` to ensure structured execution and clear reporting.

## Tool Usage

*   **`code.query`:** This is the primary tool for all code analysis and manipulation tasks. The agent should use it to search, match, and transform code.

*   **`std.task`:** All significant operations should be wrapped in a `std.task` to provide a structured and repeatable process.

*   **`xt.lang`:** For tasks that involve multiple languages, `xt.lang` should be used to manage the cross-language interactions.

*   **`code.test`:** All tests must be written using the `code.test` framework, following the conventions outlined in `agents/main_workflow.md`.

*   **`clj-eval`:** This tool should be used to evaluate Clojure code snippets to verify their behavior or to get information about the current state of the system.

By adhering to these guidelines, Gemini can act as a powerful and effective assistant for the development of the `foundation-base` project.
