# `std.lang.base.script` Summary

The `std.lang.base.script*` namespaces provide a high-level interface for interacting with the `foundation-base` language ecosystem. They tie together the `grammar`, `emit`, `book`, and `runtime` components to provide a seamless experience for defining, compiling, and executing code in different languages.

**Core Concepts:**

*   **Script:** A "script" is a self-contained unit of code that can be executed in a specific language runtime.
*   **`script` macro:** The `script` macro is the main entry point for defining a script. It takes a language keyword, a module name, and a configuration map as arguments.
*   **Runtime Management:** The `std.lang.base.script-control` namespace provides functions for managing the lifecycle of language runtimes, including `script-rt-get`, `script-rt-stop`, and `script-rt-restart`.
*   **Annex:** An "annex" (`std.lang.base.script-annex`) is a way to extend an existing language with new functionality. It allows you to define new macros and functions that can be used in the extended language.

**How `std.lang.base.script` Ties Everything Together:**

The `script` macro is the glue that holds the `foundation-base` language ecosystem together. When you use the `script` macro, it performs the following steps:

1.  **Module Definition:** It defines a new module in the `book` for the specified language. This module contains the code and metadata for the script.
2.  **Runtime Initialization:** It gets or creates a runtime for the specified language using `script-rt-get`.
3.  **Macro and Highlight Interning:** It interns the macros and highlight symbols from the language's grammar into the current namespace, making them available for use in the script.
4.  **Code Execution:** The code within the `script` macro is then executed in the context of the specified language runtime.

**The `!` Macro:**

The `!` macro provides a convenient way to switch between different language runtimes within the same namespace. This is especially useful for testing and for writing polyglot scripts.

```clojure
(require '[std.lang.base.script :as script])

(script/script :lua my-lua-module)
(script/script+ [:py :python] {})

(!:lua (+ 1 2))
;; => 3

(!:py (+ 1 2))
;; => 3
```

In this example, the `script` macro is used to set up the `:lua` runtime, and the `script+` macro is used to set up the `:python` runtime as an annex. The `!` macro is then used to execute code in each of these runtimes.

**Summary:**

The `std.lang.base.script*` namespaces provide a powerful and flexible way to work with multiple languages in the `foundation-base` ecosystem. By abstracting away the details of runtime management and code compilation, they allow developers to focus on writing code in the language of their choice. The `script` and `!` macros, in particular, provide a seamless and intuitive way to work with polyglot code.
