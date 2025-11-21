## std.lib.trace: A Comprehensive Summary

The `std.lib.trace` namespace provides a powerful and flexible mechanism for tracing function calls in Clojure, primarily for debugging and introspection purposes. It allows developers to "wrap" functions with tracing logic, capturing input arguments and return values, and optionally printing them or even generating stack traces. This module is designed to be non-intrusive and easily toggleable.

**Key Features and Concepts:**

1.  **`Trace` Deftype:**
    *   A custom `deftype` (`std.lib.trace/Trace`) is used to encapsulate tracing information, including the trace type (`:basic`, `:print`, `:stack`), the fully qualified symbol of the traced function, a history of calls (an atom holding a sequence of `{:in args :out result}` maps), and the original untraced function.
    *   It implements `java.lang.Object/toString` for human-readable representation and `clojure.lang.IDeref` to easily access its internal state.

2.  **Trace Management:**
    *   `trace?`: A predicate to check if an object is a `Trace` instance.
    *   `get-trace`: Retrieves the `Trace` object associated with a var (if tracing is enabled).
    *   `make-trace`: Constructs a new `Trace` object for a given var and trace type.
    *   `has-trace?`: Checks if a var currently has tracing enabled.
    *   `apply-trace`: The core function that applies the traced function, captures input/output, and records it in the trace history.

3.  **Trace Wrapping Functions:**
    *   `wrap-basic`: Wraps a function with basic tracing, recording input and output without additional side effects.
    *   `wrap-print`: Wraps a function with tracing that prints the function call (with arguments) and its result to the console, formatted for readability.
    *   `wrap-stack`: Wraps a function with tracing that prints a stack trace when the function is called.

4.  **Trace Activation and Deactivation:**
    *   `add-raw-trace`: The internal function used to attach a trace to a var, handling different trace types and ensuring that the original function is preserved.
    *   `add-base-trace`: Enables basic tracing for a var.
    *   `add-print-trace`: Enables print tracing for a var.
    *   `add-stack-trace`: Enables stack tracing for a var.
    *   `remove-trace`: Removes tracing from a var, restoring its original function definition.

5.  **Namespace-Wide Tracing:**
    *   `trace-ns`: Applies basic tracing to all public functions within a given namespace (or the current namespace).
    *   `trace-print-ns`: Applies print tracing to all public functions within a given namespace.
    *   `untrace-ns`: Removes all traces from functions within a given namespace.

6.  **Trace Output:**
    *   `output-trace`: Retrieves and formats the most recent call and its result from a traced function's history.

**Usage and Importance:**

`std.lib.trace` is an invaluable tool for debugging complex Clojure applications, especially within the `foundation-base` project where code generation and dynamic execution are common. It allows developers to:

*   **Inspect Function Behavior:** Understand what arguments a function receives and what values it returns at each call.
*   **Pinpoint Issues:** Quickly identify the source of errors or unexpected behavior by observing the flow of data through functions.
*   **Non-Intrusive Debugging:** Add and remove tracing without modifying the original source code, making it ideal for live debugging sessions.
*   **Dynamic Analysis:** Trace functions dynamically at runtime, which is particularly useful in interactive development environments.

This module significantly enhances the developer experience by providing powerful and flexible debugging capabilities, contributing to the overall robustness and maintainability of the `foundation-base` codebase.
