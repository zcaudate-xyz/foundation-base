(ns documentation.std-lib-component
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.component` is part of the standard foundation library set. This page collects the public API reference for the namespace."

[[:chapter {:title "API" :link "std.lib.component"}]]

[[:api {:namespace "std.lib.component"}]]

;; BEGIN merged documentation: plans/slop/summary/std_lib_component_summary.md
;; sha256: f4c140c7e575449d29600c86eb65ba56c0b0a56be29a1849df4d7ed2342d00fd
[[:chapter {:title "std.lib.component: A Comprehensive Summary (including std.lib.component.track)" :link "merged-plans-slop-summary-std-lib-component-summary-md"}]]

"The `std.lib.component` namespace, along with its sub-namespace `std.lib.component.track`, provides a robust and extensible framework for managing the lifecycle of components in Clojure applications. It introduces a protocol-based approach for defining start/stop/kill operations, querying component status, and tracking component instances. This module is crucial for building modular, testable, and maintainable systems, especially in complex applications with many interconnected parts."

[[:section {:title "std.lib.component (Component Lifecycle Management)" :link "merged-plans-slop-summary-std-lib-component-summary-md-std-lib-component-component-lifecycle-management"}]]

"This namespace defines the core `std.protocol.component/IComponent` protocol and provides functions for interacting with components that implement it. It focuses on the lifecycle (start, stop, kill) and introspection (info, health, properties) of components."

"**Core Concepts:**"

"*   **`IComponent` Protocol:** The central abstraction for defining components. It specifies the following methods:\n    *   `-start`: Initializes and starts the component.\n    *   `-stop`: Shuts down and cleans up the component.\n    *   `-kill`: Forcefully terminates the component (potentially without graceful shutdown).\n    *   `-info`: Returns information about the component at a given level of detail.\n    *   `-health`: Returns the health status of the component.\n    *   `-remote?`: Indicates if the component connects to a remote resource.\n    *   `-get-options`: Retrieves component-specific options.\n    *   `-props`: Returns a map of component properties with getter/setter functions.\n*   **Lifecycle Hooks:** Components can define `setup` and `teardown` functions, as well as `pre-start`, `post-start`, `pre-stop`, `post-stop` hooks, allowing for flexible initialization and cleanup.\n*   **Tracking Integration:** Seamlessly integrates with `std.lib.component.track` to automatically track component instances."

"**Key Functions:**"

"*   **`component?`**: Checks if an object implements the `IComponent` protocol.\n*   **`started?`**: Checks if a component is in a started state.\n*   **`stopped?`**: Checks if a component is in a stopped state.\n*   **`start`**: Initiates the component's lifecycle, running pre-start hooks, the `-start` method, setup functions, and post-start hooks. It also tracks the component.\n*   **`stop`**: Halts the component's lifecycle, running pre-stop hooks, teardown functions, the `-stop` method, and post-stop hooks. It also untracks the component.\n*   **`kill`**: Forcefully stops a component, setting a dynamic flag `*kill*` to `true` before calling `stop`.\n*   **`info`**: Retrieves information about the component.\n*   **`health`**: Returns the health status of the component (e.g., `:ok`, `:errored`).\n*   **`remote?`**: Checks if the component interacts with remote resources.\n*   **`all-props`, `get-prop`, `set-prop`**: Functions for inspecting and manipulating component properties defined via the `-props` method.\n*   **`with` (macro)**: A convenient macro for ensuring a component is started before a block of code executes and stopped afterwards, similar to `with-open`.\n*   **`wrap-start`, `wrap-stop`**: Higher-order functions to add additional setup/teardown steps to the start/stop process, useful for extending runtime behavior."

[[:section {:title "std.lib.component.track (Component Instance Tracking)" :link "merged-plans-slop-summary-std-lib-component-summary-md-std-lib-component-track-component-instance-tracking"}]]

"This sub-namespace provides a global registry for tracking component instances, allowing for introspection, management, and debugging of active components across the application."

"**Core Concepts:**"

"*   **`ITrack` Protocol:** Components can implement this protocol to define a `track-path` (a vector of keywords) where they should be registered in the global `*registry*`.\n*   **Global Registry (`*registry*`):** An atom holding a nested map that stores `TrackEntry` records for all tracked components.\n*   **`TrackEntry`:** A record that stores metadata about a tracked component, including its creation time, namespace, and the component instance itself.\n*   **Actions:** A mechanism to define and apply functions to tracked components (e.g., `stop`, `start`, `println`)."

"**Key Functions:**"

"*   **`track-path`**: Retrieves the tracking path for a trackable object.\n*   **`trackable?`**: Checks if an object implements the `ITrack` protocol.\n*   **`track`**: Registers a component in the global registry. It assigns a unique ID (`track/id`) to the component if it doesn't have one.\n*   **`tracked?`**: Checks if a component instance is currently being tracked.\n*   **`untrack`**: Removes a component instance from the global registry.\n*   **`untrack-all`**: Clears all or a subset of tracked components from the registry.\n*   **`tracked:action:add`, `tracked:action:remove`, `tracked:action:get`, `tracked:action:list`**: Functions for managing a global map of named actions that can be performed on tracked components.\n*   **`tracked:all`**: Returns the entire `*registry*` or a sub-section of it.\n*   **`tracked`**: Applies a specified action (function) to all components found at a given path in the registry.\n*   **`tracked:count`**: Returns the count of tracked components at a given path.\n*   **`tracked:locate`**: Finds tracked components that match specific metadata.\n*   **`tracked:list`**: Returns a list of tracked components, optionally applying an action to each.\n*   **`tracked:last`**: Retrieves the `n` most recently tracked components at a given path.\n*   **`track:with-metadata` (macro)**: Binds additional metadata to be associated with components tracked within its scope."

[[:section {:title "Usage Pattern:" :link "merged-plans-slop-summary-std-lib-component-summary-md-usage-pattern"}]]

"The `std.lib.component` module is fundamental for:"

"*   **Application Architecture:** Structuring applications into manageable, independent components.\n*   **Testing:** Easily starting and stopping components for isolated testing.\n*   **Monitoring and Debugging:** Gaining insight into the state and health of running components.\n*   **Resource Management:** Ensuring proper allocation and deallocation of resources."

"The `std.lib.component.track` submodule enhances this by providing a centralized, dynamic view of all active components, enabling powerful runtime introspection and control, which is particularly valuable in long-running or complex systems."

^{:id merged-plans-slop-summary-std-lib-component-summary-md-example-1 :added "4.0"}
(fact "Usage Pattern: example"
  ;; Example of a component
  (defrecord Database []
    std.protocol.track/ITrack
    (-track-path [db] [:my-app :database])

    std.protocol.component/IComponent
    (-start [db] (assoc db :status "started"))
    (-stop [db] (dissoc db :status))
    (-health [db] {:status :ok}))

  ;; Usage
  (require '[std.lib.component :as comp])
  (require '[std.lib.component.track :as track])

  (def db-instance (-> (Database.) comp/start))
  ;; db-instance is now tracked and started

  (comp/health db-instance)
  => {:status :ok}

  (track/tracked [:my-app :database] comp/info)
  => {:my-app {:database {<uuid> {:info true, :status "started"}}}}

  (comp/stop db-instance)
  ;; db-instance is now stopped and untracked
)
;; END merged documentation: plans/slop/summary/std_lib_component_summary.md
