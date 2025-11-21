## std.lib.resource: A Comprehensive Summary

The `std.lib.resource` namespace provides a powerful and extensible framework for managing application resources. It defines a registry for resource specifications (`specs`) and their variations (`variants`), and offers a lifecycle management system for creating, setting up, tearing down, and accessing resource instances. This module is crucial for ensuring consistent and controlled access to external dependencies, services, or any managed components within the `foundation-base` project.

**Key Features and Concepts:**

1.  **Resource Registry and Specifications:**
    *   **`*namespace*`**: A dynamic var for the current namespace, used in resource key resolution.
    *   **`*alias*`**: A global atom mapping resource aliases to their `[type id]` pairs.
    *   **`*registry*`**: A global atom storing all registered resource specifications (`specs`).
    *   **`*active*`**: A global atom tracking all active resource instances, categorized by `mode` (`:global`, `:namespace`, `:shared`).
    *   **`+type+`**: A default resource specification template, defining structure for `mode`, `config`, `instance` (create/setup/teardown functions), and `variant` hooks.
    *   **`res:spec-list []`**: Lists all registered resource specification types.
    *   **`res:spec-add [spec]`**: Adds a new resource specification to the registry, merging with `+type+`.
    *   **`res:spec-remove [type]`**: Removes a resource specification from the registry.
    *   **`res:spec-get [type]`**: Retrieves a resource specification by its `type`.
    *   **`res:variant-list [& [type]]`**: Lists all variants for a given resource `type` or for all types.
    *   **`res:variant-add [type spec]`**: Adds a new variant to a resource specification, optionally creating an alias.
    *   **`res:variant-remove [type id]`**: Removes a variant from a resource specification.
    *   **`res:variant-get [type & [id]]`**: Retrieves a specific variant of a resource specification, merging it with the base spec.

2.  **Resource Instance Management:**
    *   **`res:mode [type & [variant]]`**: Determines the default management mode (`:global`, `:namespace`, `:shared`) for a resource type and variant.
    *   **`res:active [& [type variant]]`**: Lists all active resource instances, optionally filtered by `type` and `variant`.
    *   **`res-setup [type variant config]`**: Creates and sets up a resource instance based on its `type`, `variant`, and `config`. It invokes `create` and `setup` functions defined in the spec, along with `pre-setup` and `post-setup` hooks.
    *   **`res-teardown [type variant instance]`**: Tears down a resource instance, invoking the `teardown` function and `pre-teardown`/`post-teardown` hooks.

3.  **Resource Access and Lifecycle:**
    *   **`res-input [input]`**: Normalizes various input formats (keyword, map, symbol, string) into a `[type config]` pair.
    *   **`res-key [mode type variant input & [args]]`**: Generates a unique key for a resource instance based on its `mode`, `type`, `variant`, and `input`.
    *   **`res-path [mode type variant input]`**: Constructs the path within the `*active*` atom to store/retrieve a resource instance.
    *   **`res-access-get [mode type variant key]`**: Retrieves an active resource entry from `*active*`.
    *   **`res-access-set [mode type variant key entry]`**: Stores an active resource entry in `*active*`.
    *   **`res-start [mode type variant key config]`**: Starts a new resource instance and adds it to `*active*`.
    *   **`res-stop [mode type variant key]`**: Stops and removes a resource instance from `*active*`.
    *   **`res-restart [mode type variant key]`**: Stops and then restarts an existing resource instance.
    *   **`res-base [mode type variant key config]`**: The core function for getting or starting a resource instance. It ensures the resource is started only once and respects the allowed modes.

4.  **API Generation (User-Friendly Functions):**
    *   **`res-call-fn [f]`**: A helper function that creates a flexible dispatch function for resource operations, allowing various argument combinations to resolve to the full `[mode type variant key args]` signature.
    *   **`res-api-fn [res-call extra post default]`**: A helper function to create user-friendly API calls (e.g., `res:start`, `res:stop`) that wrap `res-call-fn`.
    *   **`res-api-tmpl`**: A template for generating resource API functions using `std.lib.template/deftemplate`.
    *   **Generated API Functions**: The module uses `h/template-entries` with `res-api-tmpl` to generate a set of high-level functions:
        *   **`res:exists?`**: Checks if a resource is active.
        *   **`res:set`**: Sets an active resource instance.
        *   **`res:stop`**: Stops a resource.
        *   **`res:path`**: Gets the path for a resource.
        *   **`res:start`**: Starts a resource.
        *   **`res:restart`**: Restarts a resource.
        *   **`res`**: The main entry point for getting or starting a resource.

**Overall Importance:**

The `std.lib.resource` module is fundamental to the `foundation-base` project's ability to manage its complex dependencies and runtime components. Its key contributions include:

*   **Centralized Resource Management:** Provides a single, consistent mechanism for defining, registering, and managing all types of application resources.
*   **Lifecycle Control:** Ensures that resources are properly created, set up, used, and torn down, preventing leaks and ensuring system stability.
*   **Extensibility:** The protocol-driven design allows for easy integration of new resource types and custom lifecycle hooks.
*   **Dynamic Configuration:** Supports different resource variants and configurations, enabling flexible adaptation to various environments or use cases.
*   **Modularity and Decoupling:** Decouples resource usage from their concrete implementations, promoting a more modular and maintainable architecture.
*   **Namespace-Awareness:** Supports namespace-specific resource instances, which is crucial for multi-tenant or modular applications.

By offering these comprehensive resource management capabilities, `std.lib.resource` significantly enhances the `foundation-base` project's ability to build and manage its sophisticated multi-language development ecosystem.
