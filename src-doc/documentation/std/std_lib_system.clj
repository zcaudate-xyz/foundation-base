(ns documentation.std-lib-system
  (:require [std.lib.system :refer :all])
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.system` is part of the standard foundation library set. This page collects the public API reference for the namespace."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Primitives and system predicates"}]]

"`primitive?` recognises basic scalar values, and `system?` checks whether a value satisfies the `ISystem` protocol."

(fact "detect primitive values"
  (primitive? 1)
  => true

  (primitive? "hello")
  => true

  (primitive? {:a 1})
  => false)

(fact "a plain map is not a system"
  (system? {})
  => false)

[[:section {:title "System arrays"}]]

"`array` builds a component array from a vector of configs. `array?` checks the result, and `info-array` summarises the array for display."

(fact "construct and inspect a component array"
  (let [arr (array {:constructor identity} [{:id 1} {:id 2}])]
    (array? arr)
    => true

    (count arr)
    => 2

    (vec arr)
    => [{:id 1} {:id 2}]))

[[:section {:title "Topology helpers"}]]

"Topologies can be written in short form and expanded with `long-form`. From the expanded form you can extract dependencies and exposed keys."

(fact "expand a topology to long form"
  (long-form {:db    [identity]
              :cache [identity :db]})
  => (contains {:db    (contains {:type :build
                                  :constructor identity
                                  :dependencies []})
                :cache (contains {:type :build
                                  :constructor identity
                                  :dependencies [:db]})}))

(fact "extract dependencies and exposed keys"
  (let [topo (long-form {:db     [identity]
                         :cache  [identity :db]
                         :public {:expose identity :in :cache}})]
    (get-dependencies topo)
    => {:db [] :cache [:db] :public [:cache]}

    (get-exposed topo)
    => [:public]))

[[:section {:title "Partial systems"}]]

"`valid-subcomponents` and `subsystem` let you work with a subset of a larger system. These are useful for tests and for starting only the components required by a particular feature."

(fact "find valid subcomponents for a partial system"
  (let [topo {:a [identity]
              :b [identity :a]
              :c [identity :b]
              :d [identity]}]
    (valid-subcomponents topo [:c])
    => (contains [:a :b :c] :in-any-order)))

[[:section {:title "End-to-end: build, start, and inspect a system"}]]

"A complete workflow defines a topology, constructs a system, starts it, checks its health, inspects its info, and stops it."

(fact "build and lifecycle a small system"
  (let [topo  {:db    [identity]
               :cache [identity :db]}
        sys   (system topo
                      {:db    {:host "localhost"}
                       :cache {:ttl 60}})
        sys   (start-system sys)]
    (keys sys)
    => (contains [:db :cache] :in-any-order)

    (health-system sys)
    => {:status :ok}

    (:db (info-system sys))
    => {:host "localhost"}

    (keys (stop-system sys))
    => (contains [:db :cache] :in-any-order)))

[[:chapter {:title "API" :link "std.lib.system"}]]

[[:api {:namespace "std.lib.system"}]]

;; BEGIN merged documentation: plans/slop/summary/std_lib_system_summary.md
;; sha256: 9a264371d11b022bccc1038e96967ac625f3b0e7aee7969ada2def3425a2c7a0
[[:chapter {:title "std.lib.system: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-system-summary-md"}]]

"The `std.lib.system` module provides a powerful and flexible framework for defining, building, and managing complex systems composed of various components. It introduces a declarative way to define system topologies, manage component lifecycles (start, stop, health checks), and handle dependencies between components. This module is crucial for orchestrating application startup, shutdown, and dynamic reconfiguration within the `foundation-base` project."

"The module is organized into several sub-namespaces:"

[[:section {:title "std.lib.system.common" :link "merged-plans-slop-summary-std-lib-system-summary-md-std-lib-system-common"}]]

"This namespace defines basic predicates and interfaces for system components."

"*   **`ISystem` Protocol**: A marker protocol for system components.\n*   **`system? [obj]`**: Checks if an object implements the `ISystem` protocol.\n*   **`primitive? [x]`**: Checks if an object is a primitive Clojure type (string, number, boolean, regex, UUID, URI, URL)."

[[:section {:title "std.lib.system.array" :link "merged-plans-slop-summary-std-lib-system-summary-md-std-lib-system-array"}]]

"This namespace provides utilities for managing arrays of components, treating them as a single component."

"*   **`ComponentArray` Deftype**: A record that wraps a vector of components, allowing it to be treated as a single component. It implements `clojure.lang.Seqable`, `clojure.lang.IObj`, `clojure.lang.IMeta`, `clojure.lang.Counted`, `clojure.lang.Indexed`, and `std.protocol.component/IComponent` and `IComponentQuery`.\n*   **`info-array [arr]`**: Returns information about the elements within a `ComponentArray`.\n*   **`health-array [carr]`**: Returns the health status of a `ComponentArray`, aggregating the health of its individual components.\n*   **`start-array [carr]`**: Starts all components within a `ComponentArray`.\n*   **`stop-array [carr]`**: Stops all components within a `ComponentArray`.\n*   **`array [opts config]`**: Constructs a `ComponentArray` from a configuration, applying a constructor and defaults to each element.\n*   **`array? [x]`**: Checks if an object is a `ComponentArray`."

[[:section {:title "std.lib.system.display" :link "merged-plans-slop-summary-std-lib-system-summary-md-std-lib-system-display"}]]

"This namespace is currently empty, suggesting it's a placeholder for future display-related functionalities for system components."

[[:section {:title "std.lib.system.partial" :link "merged-plans-slop-summary-std-lib-system-summary-md-std-lib-system-partial"}]]

"This namespace provides functionalities for working with partial systems, allowing for the selection of subcomponents and waiting for their readiness."

"*   **`*timeout*`**: A dynamic var for the default timeout in milliseconds for `wait`.\n*   **`*callback*`**: A dynamic var for default callback functions used in `wait`.\n*   **`valid-subcomponents [full-topology keys]`**: Filters a topology to return only components that are valid subcomponents given a set of `keys` and exposed components.\n*   **`system-subkeys [system keys]`**: Recursively finds all subcomponents that are dependencies of the given `keys` within a system.\n*   **`subsystem [system keys]`**: Extracts a subsystem from a larger system, including only the specified `keys` and their dependencies.\n*   **`wait [system key & [callback]]`**: Waits for a specific component (`key`) within a `system` to become healthy, with retry logic and configurable callbacks.\n*   **`wait-for [system keys & [callback]]`**: Waits for all specified `keys` within a `system` to become healthy."

[[:section {:title "std.lib.system.scaffold" :link "merged-plans-slop-summary-std-lib-system-summary-md-std-lib-system-scaffold"}]]

"This namespace provides a scaffolding mechanism for managing system instances, particularly for testing and development environments. It allows for registering, creating, starting, stopping, and restarting system configurations."

"*   **`*registry*`**: A global atom storing registered scaffold configurations.\n*   **`*running*`**: A global atom tracking currently running scaffold instances.\n*   **`*timeout*`**: A dynamic var for the default timeout.\n*   **`scaffold:register [& [ns m]]`**: Registers a system configuration (topology, config, instance var) for a given namespace.\n*   **`scaffold:deregister [& [ns]]`**: Deregisters a system configuration.\n*   **`scaffold:current [& [ns]]`**: Returns the registered scaffold configuration for a namespace.\n*   **`scaffold:create [& [ns]]`**: Creates a new system instance from a registered scaffold configuration.\n*   **`scaffold:new [& [ns]]`**: Creates and starts a new system instance from a registered scaffold.\n*   **`scaffold:stop [& [ns]]`**: Stops a running system instance.\n*   **`scaffold:start [& [ns]]`**: Starts a system instance.\n*   **`scaffold:clear [& [ns]]`**: Clears (stops and unsets) a running system instance.\n*   **`scaffold:restart [& [ns]]`**: Restarts a running system instance.\n*   **`scaffold:registered []`**: Lists all registered scaffold configurations.\n*   **`scaffold:all []`**: Lists all currently running scaffold instances.\n*   **`scaffold:stop-all []`**: Stops all running scaffold instances."

[[:section {:title "std.lib.system.type" :link "merged-plans-slop-summary-std-lib-system-summary-md-std-lib-system-type"}]]

"This namespace defines the core `ComponentSystem` record and the logic for building, starting, and stopping complex systems based on a declarative topology."

"*   **`ComponentSystem` Deftype**: The central record for a system, which is essentially a map of components. It implements `std.lib.system.common/ISystem`, `std.protocol.component/IComponent`, and `std.protocol.track/ITrack`.\n*   **`*stop-fn*`**: A dynamic var that can be bound to `component/stop` or `component/kill` to control how components are stopped.\n*   **`info-system [sys]`**: Returns detailed information about the components within a system.\n*   **`health-system [system]`**: Returns the overall health status of a system, aggregating the health of its components.\n*   **`remote?-system [system]`**: Checks if any component in the system is a remote resource.\n*   **`system-string [sys]`**: Provides a string representation of a system.\n*   **`system [topology config & [opts]]`**: The main function for creating a `ComponentSystem`. It takes a `topology` (declarative definition of components and their dependencies), `config` (component-specific configurations), and `opts`. It uses `topology/long-form` to expand the topology and `sort/topological-sort` to determine the startup order.\n*   **`system-import [component system import]`**: Imports a component from the `system` into another `component` (e.g., an array of components).\n*   **`system-expose [component system opts]`**: Exposes a subcomponent from the `system` based on options.\n*   **`start-system [system]`**: Starts all components in the `system` according to their topological order, handling imports, exposes, and lifecycle hooks.\n*   **`system-deport [component import]`**: Deports (removes) an imported component from another component.\n*   **`stop-system [system]`**: Stops all components in the `system` in reverse topological order, handling deports and lifecycle hooks.\n*   **`kill-system [system]`**: Forcefully stops all components in the system by binding `*stop-fn*` to `component/kill`."

[[:section {:title "std.lib.system (Facade Namespace)" :link "merged-plans-slop-summary-std-lib-system-summary-md-std-lib-system-facade-namespace"}]]

"This namespace acts as a facade, re-exporting key functions from its sub-namespaces for convenience."

"*   **`h/intern-in`**: Interns various functions from `common`, `array`, `type`, `partial`, and `scaffold` into this namespace."

"**Overall Importance:**"

"The `std.lib.system` module is a cornerstone of the `foundation-base` project's architecture, providing a robust and declarative way to manage complex applications. Its key contributions include:"

"*   **Declarative System Definition:** Allows developers to define application structure and dependencies in a clear, concise, and extensible manner.\n*   **Automated Lifecycle Management:** Handles the intricate details of starting, stopping, and managing the health of interconnected components, including dependency resolution.\n*   **Modularity and Reusability:** Promotes the creation of reusable components that can be easily integrated into different systems.\n*   **Dynamic Configuration:** Supports flexible configuration of components and their interactions.\n*   **Testing and Development Support:** The scaffolding mechanism simplifies the setup and teardown of test environments.\n*   **Extensibility:** The protocol-driven design allows for easy integration of new component types and custom lifecycle behaviors."

"By offering these comprehensive system management capabilities, `std.lib.system` significantly enhances the `foundation-base` project's ability to build, deploy, and maintain sophisticated multi-language applications with greater reliability and efficiency."
;; END merged documentation: plans/slop/summary/std_lib_system_summary.md
