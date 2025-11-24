# MCP Client Guidelines for `code.ai.server`

This document provides guidelines for Model Context Protocol (MCP) clients interfacing with `code.ai.server`. It details the available tools, their expected inputs, and usage examples.

## Overview

The `code.ai.server` exposes a set of tools to interact with the Code AI infrastructure, including:
- Basic utilities (`echo`, `ping`)
- Standard Language (`std.lang`) transpilation and introspection
- Code management tasks (`code.manage`)
- Code healing (`code.heal`)
- Test execution (`code.test`)
- Documentation management (`code.doc`)

All tools follow the MCP tool schema. Many tools expect arguments encoded as EDN strings to support complex Clojure data structures.

## Available Tools

### 1. Basic Tools

#### `echo`
Echoes the input text back to the client. Useful for verifying connectivity.

*   **Input Schema:**
    ```json
    {
      "text": "string"
    }
    ```
*   **Example Usage:**
    *   **Input:** `{"text": "Hello MCP"}`
    *   **Output:** `{"content": [{"type": "text", "text": "Hello MCP"}], "isError": false}`

#### `ping`
Returns a simple "ping" response.

*   **Input Schema:**
    ```json
    {
      "text": "string" // content is ignored
    }
    ```
*   **Example Usage:**
    *   **Input:** `{"text": "any"}`
    *   **Output:** `{"content": [{"type": "text", "text": "ping"}], "isError": false}`

---

### 2. Standard Language (`std.lang`) Tools

These tools allow interaction with the `std.lang` multi-language transpiler.

#### `std-lang-list`
Lists all available languages in the `std.lang` ecosystem.

*   **Input Schema:** `{}`
*   **Example Usage:**
    *   **Input:** `{}`
    *   **Output:** `(:lua :r :js :glsl :redis :bash :c :xtalk :python :rust)`

#### `std-lang-modules`
Lists available modules for a specific language.

*   **Input Schema:**
    ```json
    {
      "lang": "string" // e.g. "js", "lua"
    }
    ```
*   **Example Usage:**
    *   **Input:** `{"lang": "lua"}`
    *   **Output:** `nil` (or list of modules if available)

#### `lang-emit-as`
Transpiles Clojure DSL code into a target language.

*   **Input Schema:**
    ```json
    {
      "type": "string", // Target language (e.g., "js", "python", "lua")
      "code": "string"  // Clojure code to transpile
    }
    ```
*   **Example Usage:**
    *   **Input:** `{"type": "js", "code": "(+ 1 2)"}`
    *   **Output:** `1 + 2`

---

### 3. Code Management (`code.manage`)

Executes maintenance and analysis tasks defined in `code.manage`.

#### `code-manage`
Runs a specific task from `code.manage`.

*   **Input Schema:**
    ```json
    {
      "task": "string", // Task name (e.g., "vars", "analyse", "clean")
      "args": "string"  // EDN string representing the arguments vector
    }
    ```
*   **Important Note:** The `args` parameter must be an EDN string of a **vector** containing the arguments. Symbols should be passed as symbols (e.g., `[code.core]`), not strings (e.g., `["code.core"]`), unless the task specifically expects strings.

*   **Example Usage (List Vars):**
    *   **Task:** `vars`
    *   **Args:** `"[code.ai.server.tool.basic]"` (EDN string for vector containing one symbol)
    *   **Input:**
        ```json
        {
          "task": "vars",
          "args": "[code.ai.server.tool.basic]"
        }
        ```
    *   **Output:** List of vars in the namespace.

---

### 4. Code Healing (`code.heal`)

#### `code-heal`
Heals code in a namespace (e.g., fixing missing imports).

*   **Input Schema:**
    ```json
    {
      "ns": "string",    // Namespace to heal
      "params": "string" // EDN string of optional parameters
    }
    ```
*   **Example Usage:**
    *   **Input:** `{"ns": "code.ai.server.tool.basic", "params": "{}"}`
    *   **Output:** `{:changed [], :updated false, :verified true, :path "..."}`

---

### 5. Code Testing (`code.test`)

#### `code-test`
Runs tests using the `code.test` framework.

*   **Input Schema:**
    ```json
    {
      "target": "string", // Test target (e.g., "test", "test:unit")
      "args": "string"    // EDN string of arguments map (e.g., "{:only ...}")
    }
    ```
*   **Example Usage:**
    *   **Input:**
        ```json
        {
          "target": "test",
          "args": "{:only code.ai.server.tool.basic}"
        }
        ```
    *   **Output:** (Test runner output string)

---

### 6. Documentation (`code.doc`)

Tools to manage documentation sites.

#### `code-doc-init`
Initializes a documentation template.

*   **Input Schema:**
    ```json
    {
      "site": "string",   // EDN string of site key (e.g. ":hara")
      "params": "string"  // EDN string of params
    }
    ```

#### `code-doc-deploy`
Deploys the documentation template.

*   **Input Schema:**
    ```json
    {
      "site": "string",
      "params": "string"
    }
    ```

#### `code-doc-publish`
Publishes the documentation.

*   **Input Schema:**
    ```json
    {
      "site": "string",
      "params": "string"
    }
    ```

## Error Handling

*   Tools return an `isError` flag in the response.
*   If `isError` is true, the `content` usually contains error details or stack traces.
*   Ensure EDN strings are well-formed; `read-string` failures will cause tool execution errors.
