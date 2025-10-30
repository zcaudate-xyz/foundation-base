## std.lib.context: A Comprehensive Summary

The `std.lib.context` module provides a sophisticated framework for managing execution contexts and their associated runtimes within the `foundation-base` ecosystem. It introduces concepts of `Pointers`, `Registries`, and `Spaces` to abstract and control how code interacts with different execution environments. This modular design allows for flexible and dynamic switching between various runtime configurations, crucial for a multi-language transpilation and execution system.

The module is organized into three main sub-namespaces:

### `std.lib.context.pointer`

This namespace defines the `Pointer` record, which acts as a reference to a resource or function within a specific execution context. Pointers abstract away the underlying runtime details, allowing for a consistent way to interact with diverse environments.

*   **`*runtime*`**: A dynamic var that can be bound to a runtime instance, providing a default context for pointer operations.
*   **`pointer-deref [ptr]`**: Dereferences a pointer, executing its associated action within the appropriate runtime.
*   **`pointer-default [ptr]`**: Determines the default runtime for a given pointer, considering `*runtime*`, pointer metadata, and the current space's runtime.
*   **`Pointer` Deftype**: The core record representing a pointer. It holds the `context` (a keyword identifying the runtime type) and implements:
    *   `clojure.lang.IFn`: Allows pointers to be invoked directly.
    *   `std.protocol.context/IPointer`: Defines methods for accessing the pointer's context, keys, and values.
    *   `std.protocol.apply/IApplicable`: Enables the pointer to be used in an "apply" fashion, with methods for transforming input and output.
    *   `clojure.lang.IDeref`: Allows dereferencing the pointer to execute its action.
*   **`pointer? [obj]`**: A predicate to check if an object is a `Pointer`.
*   **`pointer [m]`**: Creates a new `Pointer` instance, requiring a `:context` key.
*   **`+init+`**: Initializes the module by generating helper functions (e.g., `rt-raw-eval`, `rt-init-ptr`) that delegate to the appropriate `std.protocol.context` methods, making runtime operations accessible directly through the `std.lib.context.pointer` namespace.

### `std.lib.context.registry`

This namespace manages a global registry of available context types and their associated runtime configurations. It allows for dynamic installation, uninstallation, and retrieval of different runtime environments.

*   **`+null+`**: A constant representing a null context, used as a default or fallback.
*   **`RuntimeNull` Deftype**: A record representing a null runtime. It implements `std.protocol.context/IContext` but typically throws errors for invocation, serving as a placeholder for uninitialized or unavailable runtimes.
*   **`rt-null? [obj]`**: A predicate to check if an object is a `RuntimeNull` instance.
*   **`+rt-null+`**: A pre-initialized instance of `RuntimeNull`.
*   **`res/res:spec-add`**: Registers `RuntimeNull` as a resource type.
*   **`*registry*`**: A global atom holding the map of registered contexts and their runtime configurations.
*   **`registry-list []`**: Lists the IDs of all registered contexts.
*   **`registry-install [ctx & [config]]`**: Installs a new context type with an optional configuration.
*   **`registry-uninstall [ctx]`**: Uninstalls a context type.
*   **`registry-get [ctx]`**: Retrieves the configuration for a registered context.
*   **`registry-rt-list [& [ctx]]`**: Lists all runtime types for a given context or all contexts.
*   **`registry-rt-add [ctx config]`**: Installs a new runtime type for a specific context.
*   **`registry-rt-remove [ctx key]`**: Uninstalls a runtime type from a specific context.
*   **`registry-rt [ctx & [key]]`**: Retrieves the configuration for a specific runtime type within a context.
*   **`registry-scratch [ctx]`**: Retrieves the "scratch" runtime for a registered context, typically a temporary or default runtime.
*   **`+init+`**: Initializes the registry by installing the `:null` context with a `RuntimeNull` scratch instance.

### `std.lib.context.space`

This namespace manages "spaces," which are per-namespace or per-thread collections of active contexts and their instantiated runtimes. It provides functions for setting, unsetting, starting, and stopping runtimes within a space.

*   **`*namespace*`**: A dynamic var that can be bound to a namespace, providing a default space.
*   **`space-string [sp]`**: Provides a string representation of a space.
*   **`space-context-set [sp ctx key config]`**: Sets a context's configuration within a space, resolving the runtime from the registry.
*   **`space-context-unset [sp ctx]`**: Unsets a context from a space.
*   **`space-context-get [sp ctx]`**: Retrieves the configuration of a context within a space.
*   **`space-rt-start [sp ctx]`**: Starts the runtime for a specific context within a space, instantiating it if necessary.
*   **`space-rt-stop [sp ctx]`**: Stops the runtime for a specific context within a space.
*   **`space-stop [space]`**: Stops all active runtimes within a space.
*   **`Space` Deftype**: The core record representing a space. It holds the `namespace` and an atom `state` (a map of active contexts to their runtime instances). It implements:
    *   `std.protocol.context/ISpace`: Defines methods for listing contexts, getting/setting/unsetting contexts, and managing runtime lifecycle.
    *   `std.protocol.component/IComponent`: Allows spaces to be managed as components (start/stop).
*   **`space? [obj]`**: A predicate to check if an object is a `Space`.
*   **`space-create [m]`**: Creates a new `Space` instance.
*   **`res/res:spec-add`**: Registers `Space` as a resource type.
*   **`space [& [namespace]]`**: Retrieves or creates a `Space` for a given namespace (or the current one).
*   **`space-resolve [obj]`**: Resolves a space from various inputs (e.g., `nil`, symbol, namespace object).
*   **`protocol-tmpl [opts]`**: A helper function to construct template functions for generating protocol implementations, used to create `space:` prefixed functions that delegate to `ISpace` protocol methods.
*   **`space:rt-current [& [namespace ctx]]`**: Gets the current active runtime for a given context within a space, falling back to scratch or null runtimes.

**Overall Importance:**

The `std.lib.context` module is fundamental to the `foundation-base` project's ability to manage diverse execution environments. It provides:

*   **Abstraction of Runtimes:** `Pointers` and `Spaces` abstract away the complexities of interacting with different language runtimes, offering a unified API.
*   **Dynamic Context Management:** The `Registry` allows for dynamic registration and retrieval of various runtime configurations, enabling the system to support multiple target languages and execution strategies.
*   **Component-Based Lifecycle:** Integration with `std.protocol.component` ensures that runtimes and spaces can be managed with a consistent lifecycle (start, stop, kill).
*   **Extensibility:** The protocol-driven design allows for easy extension with new runtime types, pointer behaviors, and context management strategies.
*   **Modular Architecture:** By separating concerns into pointers, registry, and spaces, the module promotes a clean and maintainable architecture for handling complex execution environments.

This module is crucial for `foundation-base`'s core mission of transpiling Clojure to other languages and managing their live execution, as it provides the necessary infrastructure to seamlessly switch between and interact with these different language contexts.
