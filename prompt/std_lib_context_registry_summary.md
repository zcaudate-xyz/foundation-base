## std.lib.context.registry: A Comprehensive Summary

The `std.lib.context.registry` namespace provides a centralized system for managing and registering different "contexts" and their associated "runtimes." In the `foundation-base` ecosystem, a context typically represents a language or execution environment, and a runtime is a specific instance or configuration of that environment. This registry allows for dynamic installation, retrieval, and management of these contexts and runtimes, facilitating a pluggable and extensible architecture.

### Core Concepts:

*   **Context:** A logical grouping for a set of runtimes, often corresponding to a specific language (e.g., `:lua`, `:js`, `:postgres`). Each context has a unique keyword identifier.
*   **Runtime:** An instance or configuration of an execution environment within a context. Runtimes implement the `std.protocol.context/IContext` protocol, defining how code is evaluated, pointers are handled, etc.
*   **Registry (`*registry*`):** A global atom that stores a nested map of all registered contexts and their runtimes.
*   **`RuntimeNull`:** A default, "null" runtime implementation that serves as a placeholder or a base for contexts that don't yet have a fully defined runtime. It implements `IContext` but typically throws errors for actual execution attempts.

### Key Functions:

*   **`rt-null-string`, `RuntimeNull`, `+rt-null+`, `rt-null?`**:
    *   Define and manage the `RuntimeNull` record, which is a basic implementation of `std.protocol.context/IContext` that essentially does nothing or throws errors for operations. It's used as a default or placeholder.
*   **`registry-list`**:
    *   **Purpose:** Lists the keyword identifiers of all currently registered contexts.
    *   **Usage:** `(registry-list)`
*   **`registry-install`**:
    *   **Purpose:** Installs a new context type into the registry. It initializes the context with a default `RuntimeNull` and allows for initial configuration.
    *   **Usage:** `(registry-install :my-language {:config {:some-setting "value"}})`
*   **`registry-uninstall`**:
    *   **Purpose:** Removes a context and all its associated runtimes from the registry.
    *   **Usage:** `(registry-uninstall :my-language)`
*   **`registry-get`**:
    *   **Purpose:** Retrieves the configuration map for a specific context.
    *   **Usage:** `(registry-get :postgres)`
*   **`registry-rt-list`**:
    *   **Purpose:** Lists all runtime keys for all contexts, or for a specific context.
    *   **Usage:** `(registry-rt-list)`, `(registry-rt-list :postgres)`
*   **`registry-rt-add`**:
    *   **Purpose:** Adds or updates a specific runtime configuration within a context.
    *   **Usage:** `(registry-rt-add :postgres {:key :dev-db :resource :my-db-resource})`
*   **`registry-rt-remove`**:
    *   **Purpose:** Removes a specific runtime configuration from a context.
    *   **Usage:** `(registry-rt-remove :postgres :dev-db)`
*   **`registry-rt`**:
    *   **Purpose:** Retrieves the full configuration for a specific runtime within a context, merging context-level and runtime-level settings.
    *   **Usage:** `(registry-rt :postgres :default)`
*   **`registry-scratch`**:
    *   **Purpose:** Retrieves the "scratch" runtime associated with a context, often used for temporary or experimental evaluations.
    *   **Usage:** `(registry-scratch :postgres)`

### Usage Pattern:

This namespace is critical for:
*   **Extensibility:** Allowing new languages and execution environments to be easily integrated into the `foundation-base` system.
*   **Configuration:** Centralizing the configuration of different language runtimes.
*   **Dynamic Switching:** Enabling the application to switch between different runtimes or contexts at runtime.
*   **Testing:** Providing a structured way to set up and tear down specific execution environments for tests.

By providing a clear and programmatic way to manage contexts and runtimes, `std.lib.context.registry` underpins the multi-language capabilities of the `foundation-base` project.