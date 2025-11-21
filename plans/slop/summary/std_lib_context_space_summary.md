## std.lib.context.space: A Comprehensive Summary

The `std.lib.context.space` namespace provides a crucial layer for managing execution environments (called "spaces") within the `foundation-base` ecosystem. A space is essentially a container for a set of active runtimes, typically associated with a Clojure namespace. This module allows for dynamic configuration, starting, stopping, and querying of runtimes within a specific space, enabling isolated and flexible execution contexts for different parts of an application or for different languages.

### Core Concepts:

*   **Space:** A `Space` is a record that holds an atom (`:state`) containing the configurations and instances of various runtimes. Each space is typically associated with a Clojure namespace, providing a localized context for runtime management. It implements `std.protocol.context/ISpace` and `std.protocol.component/IComponent`.
*   **Runtime Configuration:** Within a space, each context (e.g., `:lua`, `:js`) can have multiple runtime configurations (e.g., `:default`, `:dev-db`). These configurations are stored in the space's `:state` atom.
*   **`ISpace` Protocol:** Defines methods for managing runtimes within a space:
    *   `-context-list`: Lists all contexts configured in the space.
    *   `-rt-get`: Retrieves a runtime instance for a given context.
    *   `-rt-active`: Lists all active runtime contexts.
    *   `-rt-started?`, `-rt-stopped?`: Checks the status of a runtime.
*   **`IComponent` Protocol:** Allows a space itself to be started and stopped, which in turn manages the lifecycle of its contained runtimes.

### Key Functions:

*   **`space-string`**:
    *   **Purpose:** Provides a string representation of a `Space` object, including its associated namespace and active runtimes.
*   **`space-context-set`**:
    *   **Purpose:** Configures a specific runtime within a context in the space. It merges the provided configuration with the registry's default for that runtime.
    *   **Usage:** `(space-context-set my-space :postgres :default {:config {:dbname "test"}})`
*   **`space-context-unset`**:
    *   **Purpose:** Removes a context and its associated runtime configuration from the space.
    *   **Usage:** `(space-context-unset my-space :postgres)`
*   **`space-context-get`**:
    *   **Purpose:** Retrieves the configuration for a specific context within the space.
    *   **Usage:** `(space-context-get my-space :postgres)`
*   **`space-rt-start`**:
    *   **Purpose:** Starts a runtime for a given context within the space. If the runtime is not yet instantiated, it creates and starts it using `std.lib.resource`.
    *   **Usage:** `(space-rt-start my-space :lua)`
*   **`space-rt-stop`**:
    *   **Purpose:** Stops and tears down a runtime for a given context within the space.
    *   **Usage:** `(space-rt-stop my-space :lua)`
*   **`space-stop`**:
    *   **Purpose:** Stops all active runtimes within the space.
    *   **Usage:** `(space-stop my-space)`
*   **`Space` (defimpl record)**:
    *   **Purpose:** The concrete record type for a space. It holds the namespace and the `state` atom.
*   **`space?`**:
    *   **Purpose:** Checks if an object is an instance of a `Space`.
    *   **Usage:** `(space? some-object)`
*   **`space-create`**:
    *   **Purpose:** Creates a new `Space` record.
    *   **Usage:** `(space-create {:namespace 'my-app.core})`
*   **`space`**:
    *   **Purpose:** Retrieves the `Space` instance associated with the current or a specified namespace. It uses `std.lib.resource` to manage space instances.
    *   **Usage:** `(space)`, `(space 'my-app.core)`
*   **`space-resolve`**:
    *   **Purpose:** Resolves a space object from various inputs (e.g., `nil`, symbol, `Namespace` object, or an existing `Space` instance).
*   **`protocol-tmpl`**:
    *   **Purpose:** A helper function to generate functions that delegate to the `ISpace` protocol methods, making them accessible directly from the `std.lib.context.space` namespace (e.g., `space:rt-get`).
*   **`space:rt-current`**:
    *   **Purpose:** Retrieves the currently active runtime for a given context within the current space. It falls back to a scratch runtime from the registry or a `RuntimeNull` if no specific runtime is found.
    *   **Usage:** `(space:rt-current :lua)`

### Usage Pattern:

The `std.lib.context.space` module is vital for:
*   **Multi-language Development:** Providing isolated execution environments for different languages within the same application.
*   **Modular Design:** Encapsulating runtime configurations and instances within specific namespaces, promoting modularity.
*   **Testing:** Setting up and tearing down specific runtime environments for tests without affecting other parts of the application.
*   **Dynamic Configuration:** Allowing runtime configurations to be changed or updated dynamically.

By providing a clear and programmatic way to manage execution spaces and their runtimes, `std.lib.context.space` is a cornerstone of the `foundation-base` project's ability to manage complex, polyglot applications.