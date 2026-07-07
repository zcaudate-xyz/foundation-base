(ns documentation.std-lib-resource
  (:require [std.lib.resource :refer :all])
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.resource` is part of the standard foundation library set. This page collects the public API reference for the namespace."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Registering resource specs"}]]

"Resources are described by specs and variants. `res:spec-add` registers a new spec, `res:variant-add` adds a variant, and `res:spec-list` and `res:variant-list` query the registry."

(fact "register and inspect a resource spec"
  (res:spec-add {:type :demo/counter
                 :mode {:key :id
                        :allow #{:global :shared}
                        :default :shared}
                 :instance {:create (fn [config] (atom (:start config 0)))
                            :setup identity
                            :teardown identity}})
  => any?

  (res:spec-list)
  => (contains [:demo/counter])

  (res:variant-add :demo/counter {:id :default})
  => any?

  (res:variant-list :demo/counter)
  => (contains [:default]))

[[:section {:title "Inspecting specs and modes"}]]

"Retrieve the merged spec/variant with `res:variant-get`, and query the default mode with `res:mode`."

(fact "inspect a registered resource"
  (res:mode :demo/counter)
  => :shared

  (:mode (res:variant-get :demo/counter))
  => (contains {:default :shared}))

[[:section {:title "Resource keys and paths"}]]

"`res-key` computes the key used to identify an active resource, and `res-path` builds the full path used to store it."

(fact "compute resource keys and paths"
  (res-key :shared :demo/counter :default {:id :main})
  => :main

  (res-path :shared :demo/counter :default {:id :main})
  => '(:shared [:demo/counter :default] :main))

[[:section {:title "Starting and stopping resources"}]]

"The `res` function (and friends such as `res:start` and `res:stop`) manage the lifecycle of active resources."

(fact "start and stop a resource"
  (let [counter (res :demo/counter {:id :main :start 10})]
    @counter
    => 10

    (res:exists? :demo/counter {:id :main})
    => true

    (res:stop :demo/counter {:id :main})
    => any?))

[[:section {:title "End-to-end: register, start, query, and tear down"}]]

"A complete resource workflow: register a spec and variant, start an instance, verify it is active, then stop it and confirm it is gone."

(fact "lifecycle a counter resource"
  (res:spec-add {:type :demo/gauge
                 :mode {:key :id
                        :allow #{:shared}
                        :default :shared}
                 :instance {:create (fn [config] (atom (:value config 0)))
                            :setup identity
                            :teardown identity}})
  (res:variant-add :demo/gauge {:id :default})

  (let [g (res :demo/gauge {:id :primary :value 42})]
    @g
    => 42)

  (res:active :demo/gauge)
  => (contains {:shared (contains {:demo/gauge (contains {:default (contains [:primary])})})})

  (res:stop :demo/gauge {:id :primary})

  (res:exists? :demo/gauge {:id :primary})
  => false)

[[:chapter {:title "API" :link "std.lib.resource"}]]

[[:api {:namespace "std.lib.resource"}]]

;; BEGIN merged documentation: plans/slop/summary/std_lib_resource_summary.md
;; sha256: 2b508e9f96f5a6477bf239bc8dfe83661d6c3010a9434aba716ed24c24074e11
[[:chapter {:title "std.lib.resource: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-resource-summary-md"}]]

"The `std.lib.resource` namespace provides a powerful and extensible framework for managing application resources. It defines a registry for resource specifications (`specs`) and their variations (`variants`), and offers a lifecycle management system for creating, setting up, tearing down, and accessing resource instances. This module is crucial for ensuring consistent and controlled access to external dependencies, services, or any managed components within the `foundation-base` project."

"**Key Features and Concepts:**"

"1.  **Resource Registry and Specifications:**\n    *   **`*namespace*`**: A dynamic var for the current namespace, used in resource key resolution.\n    *   **`*alias*`**: A global atom mapping resource aliases to their `[type id]` pairs.\n    *   **`*registry*`**: A global atom storing all registered resource specifications (`specs`).\n    *   **`*active*`**: A global atom tracking all active resource instances, categorized by `mode` (`:global`, `:namespace`, `:shared`).\n    *   **`+type+`**: A default resource specification template, defining structure for `mode`, `config`, `instance` (create/setup/teardown functions), and `variant` hooks.\n    *   **`res:spec-list []`**: Lists all registered resource specification types.\n    *   **`res:spec-add [spec]`**: Adds a new resource specification to the registry, merging with `+type+`.\n    *   **`res:spec-remove [type]`**: Removes a resource specification from the registry.\n    *   **`res:spec-get [type]`**: Retrieves a resource specification by its `type`.\n    *   **`res:variant-list [& [type]]`**: Lists all variants for a given resource `type` or for all types.\n    *   **`res:variant-add [type spec]`**: Adds a new variant to a resource specification, optionally creating an alias.\n    *   **`res:variant-remove [type id]`**: Removes a variant from a resource specification.\n    *   **`res:variant-get [type & [id]]`**: Retrieves a specific variant of a resource specification, merging it with the base spec.\n\n2.  **Resource Instance Management:**\n    *   **`res:mode [type & [variant]]`**: Determines the default management mode (`:global`, `:namespace`, `:shared`) for a resource type and variant.\n    *   **`res:active [& [type variant]]`**: Lists all active resource instances, optionally filtered by `type` and `variant`.\n    *   **`res-setup [type variant config]`**: Creates and sets up a resource instance based on its `type`, `variant`, and `config`. It invokes `create` and `setup` functions defined in the spec, along with `pre-setup` and `post-setup` hooks.\n    *   **`res-teardown [type variant instance]`**: Tears down a resource instance, invoking the `teardown` function and `pre-teardown`/`post-teardown` hooks.\n\n3.  **Resource Access and Lifecycle:**\n    *   **`res-input [input]`**: Normalizes various input formats (keyword, map, symbol, string) into a `[type config]` pair.\n    *   **`res-key [mode type variant input & [args]]`**: Generates a unique key for a resource instance based on its `mode`, `type`, `variant`, and `input`.\n    *   **`res-path [mode type variant input]`**: Constructs the path within the `*active*` atom to store/retrieve a resource instance.\n    *   **`res-access-get [mode type variant key]`**: Retrieves an active resource entry from `*active*`.\n    *   **`res-access-set [mode type variant key entry]`**: Stores an active resource entry in `*active*`.\n    *   **`res-start [mode type variant key config]`**: Starts a new resource instance and adds it to `*active*`.\n    *   **`res-stop [mode type variant key]`**: Stops and removes a resource instance from `*active*`.\n    *   **`res-restart [mode type variant key]`**: Stops and then restarts an existing resource instance.\n    *   **`res-base [mode type variant key config]`**: The core function for getting or starting a resource instance. It ensures the resource is started only once and respects the allowed modes.\n\n4.  **API Generation (User-Friendly Functions):**\n    *   **`res-call-fn [f]`**: A helper function that creates a flexible dispatch function for resource operations, allowing various argument combinations to resolve to the full `[mode type variant key args]` signature.\n    *   **`res-api-fn [res-call extra post default]`**: A helper function to create user-friendly API calls (e.g., `res:start`, `res:stop`) that wrap `res-call-fn`.\n    *   **`res-api-tmpl`**: A template for generating resource API functions using `std.lib.template/deftemplate`.\n    *   **Generated API Functions**: The module uses `h/template-entries` with `res-api-tmpl` to generate a set of high-level functions:\n        *   **`res:exists?`**: Checks if a resource is active.\n        *   **`res:set`**: Sets an active resource instance.\n        *   **`res:stop`**: Stops a resource.\n        *   **`res:path`**: Gets the path for a resource.\n        *   **`res:start`**: Starts a resource.\n        *   **`res:restart`**: Restarts a resource.\n        *   **`res`**: The main entry point for getting or starting a resource."

"**Overall Importance:**"

"The `std.lib.resource` module is fundamental to the `foundation-base` project's ability to manage its complex dependencies and runtime components. Its key contributions include:"

"*   **Centralized Resource Management:** Provides a single, consistent mechanism for defining, registering, and managing all types of application resources.\n*   **Lifecycle Control:** Ensures that resources are properly created, set up, used, and torn down, preventing leaks and ensuring system stability.\n*   **Extensibility:** The protocol-driven design allows for easy integration of new resource types and custom lifecycle hooks.\n*   **Dynamic Configuration:** Supports different resource variants and configurations, enabling flexible adaptation to various environments or use cases.\n*   **Modularity and Decoupling:** Decouples resource usage from their concrete implementations, promoting a more modular and maintainable architecture.\n*   **Namespace-Awareness:** Supports namespace-specific resource instances, which is crucial for multi-tenant or modular applications."

"By offering these comprehensive resource management capabilities, `std.lib.resource` significantly enhances the `foundation-base` project's ability to build and manage its sophisticated multi-language development ecosystem."
;; END merged documentation: plans/slop/summary/std_lib_resource_summary.md
