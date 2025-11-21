# `std.lang.base.runtime` Summary

The `std.lang.base.runtime*` and `std.lang.base.impl*` namespaces, along with `rt.basic`, are responsible for defining, managing, and interacting with language runtimes in the `foundation-base` ecosystem. A runtime is an environment where code can be executed. The system is designed to be extensible, allowing new runtimes to be defined and integrated.

**Core Concepts:**

*   **Runtime:** A runtime is a component that provides an execution environment for a specific language. It implements the `std.protocol.context/IContext` and `std.protocol.component/IComponent` protocols, which define the interface for interacting with the runtime.
*   **`defimpl`:** The `defimpl` macro from `std.lib.impl` is the primary tool for creating new runtime types. It simplifies the process of defining a `defrecord` that implements one or more protocols.
*   **Runtime Proxy:** A runtime proxy (`std.lang.base.runtime-proxy`) is a runtime that forwards calls to another runtime. This is useful for creating aliases or for providing a different interface to an existing runtime.
*   **Book:** A `book` (`std.lang.base.book`) is a data structure that contains all the code and metadata for a specific language. Each runtime is associated with a book.
*   **Pointer:** A `pointer` (`std.lang.base.pointer`) is a reference to a piece of code in a book. Runtimes use pointers to execute code.
*   **Lifecycle:** Runtimes have a lifecycle that is managed by the `std.protocol.component/IComponent` protocol. This includes `start`, `stop`, and `kill` functions.

**Runtime Generation and Customization:**

The `foundation-base` ecosystem provides a flexible way to define and customize runtimes.

**1. Defining a Runtime with `defimpl`:**

A new runtime is typically defined using the `defimpl` macro. This macro takes a name, a list of fields, and a set of protocol implementations.

```clojure
(defimpl MyRuntime [field1 field2]
  :protocols [std.protocol.context/IContext
              :body {-raw-eval (fn [this string]
                                 ;; implementation for evaluating a raw string
                                 )}
              std.protocol.component/IComponent
              :body {-start (fn [this]
                              ;; implementation for starting the runtime
                              this)
                     -stop (fn [this]
                             ;; implementation for stopping the runtime
                             this)}])
```

*   **`IContext` Protocol:** This protocol defines the core interface for interacting with a runtime. Key functions include:
    *   `-raw-eval`: Evaluates a raw string of code in the runtime's context.
    *   `-invoke-ptr`: Invokes a function pointer with specified arguments.
    *   `-deref-ptr`: Dereferences a pointer to get its value.
    *   `-init-ptr`: Initializes a pointer in the runtime.

*   **`IComponent` Protocol:** This protocol defines the component lifecycle for the runtime. Key functions include:
    *   `-start`: Starts the runtime, preparing it for execution.
    *   `-stop`: Stops the runtime, releasing any resources.

**2. The `RuntimeDefault` Record:**

The `std.lang.base.runtime` namespace defines a `RuntimeDefault` record using `defimpl`. This record provides a default implementation for the `IContext` and `IComponent` protocols.

*   It serves as a base for many of the language-specific runtimes in `rt.basic`.
*   It includes logic for proxying calls to another runtime via the `redirect` field.
*   The `default-*` functions in `std.lang.base.runtime` provide the actual implementations for the protocol functions. For example, `default-invoke-ptr` handles the logic for invoking a function pointer.

**3. Customization:**

*   **Extending `RuntimeDefault`:** The easiest way to create a new runtime is to extend `RuntimeDefault` and override the functions that need to be customized.
*   **Creating a New Runtime from Scratch:** For more advanced use cases, you can create a new runtime from scratch by implementing the `IContext` and `IComponent` protocols yourself.
*   **Runtime Proxies:** The `std.lang.base.runtime-proxy` namespace allows you to create a proxy for an existing runtime. This is useful for adding functionality or for creating a different interface to a runtime.

**Example: A Simple Runtime Definition**

The following example from `rt.basic.type-basic` shows how a basic runtime is defined using `defimpl`:

```clojure
(defimpl RuntimeBasic [id lang]
  :protocols [protocol.context/IContext
              :body {-raw-eval eval-string}
              protocol.component/IComponent
              :body {-start start-basic
                     -stop stop-basic}])
```

This defines a `RuntimeBasic` record that implements the `IContext` and `IComponent` protocols. The `-raw-eval` function is implemented by `eval-string`, and the `-start` and `-stop` functions are implemented by `start-basic` and `stop-basic`, respectively.

By using `defimpl` and the provided protocols, the `foundation-base` ecosystem makes it easy to create new runtimes and to extend the functionality of existing ones.