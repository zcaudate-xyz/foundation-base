(ns documentation.std-index)

[[:hero {:title "std"
         :subtitle "Standard libraries for the foundation ecosystem."
         :lead "Core utilities, concurrency, JVM tooling, integrations, and focused API references."
         :badges ["Clojure" "Utilities" "Reference"]
         :actions [{:label "Back to home" :href "../index.html" :variant :primary}
                   {:label "Contribute" :href "../guides/index.html"}]}]]

[[:card-grid
  {:title "Library families"
   :lead "Start with a family overview, then follow its focused pages and walkthroughs."
   :items
   [{:meta "Concurrency" :title "std.concurrent" :text "Queues, executors, requests, relays, pools, and threads." :href "std-concurrent.html"}
    {:meta "JVM" :title "jvm.*" :text "Artifacts, classloaders, namespaces, reflection, monitoring, and tools." :href "jvm-index.html"}
    {:meta "Integrations" :title "lib.*" :text "Databases, containers, search, messaging, storage, and repositories." :href "lib-index.html"}
    {:meta "Dispatch" :title "std.dispatch" :text "Boards, hubs, queues, debounce, hooks, and dispatch core." :href "std-dispatch.html"}
    {:meta "Filesystem" :title "std.fs" :text "Paths, walking, archives, attributes, and watchers." :href "std-fs.html"}
    {:meta "Scheduling" :title "std.scheduler" :text "Runners, programs, spawns, and scheduled task lifecycle." :href "std-scheduler.html"}
    {:meta "Text" :title "std.string" :text "Case, coercion, paths, pluralization, prose, and wrapping." :href "std-string.html"}
    {:meta "Tasks" :title "std.task" :text "Process and bulk task execution." :href "std-task.html"}
    {:meta "Time" :title "std.time" :text "Time coercion, durations, zones, formats, and representations." :href "std-time.html"}
    {:meta "Timeseries" :title "std.timeseries" :text "Journals, intervals, ranges, computation, and processing." :href "std-timeseries.html"}
    {:meta "Code" :title "std.block" :text "Code representation, traversal, and manipulation." :href "std-block.html"}]}]]

[[:card-grid
  {:title "std.lib"
   :lead "Focused foundational utilities."
   :items
   [{:meta "Invocation" :title "std.lib.apply" :text "Applicative invocation and host applicatives." :href "std-lib-apply.html"}
    {:meta "State" :title "std.lib.atom" :text "Nested atoms, batch updates, cursors, and derived state." :href "std-lib-atom.html"}
    {:meta "Binary" :title "std.lib.bin" :text "Binary data, buffers, and low-level data handling." :href "std-lib-bin.html"}
    {:meta "Classes" :title "std.lib.class" :text "Class, type, and reflection helpers." :href "std-lib-class.html"}
    {:meta "Collections" :title "std.lib.collection" :text "Extended map, sequence, tree, and diff operations." :href "std-lib-collection.html"}
    {:meta "Lifecycle" :title "std.lib.component" :text "Component lifecycle and system composition primitives." :href "std-lib-component.html"}
    {:meta "Encoding" :title "std.lib.encode" :text "Encoding and decoding helpers." :href "std-lib-encode.html"}
    {:meta "Enums" :title "std.lib.enum" :text "Enum and lookup helpers." :href "std-lib-enum.html"}
    {:meta "Environment" :title "std.lib.env" :text "Namespace, resource, pprint, and debug utilities." :href "std-lib-env.html"}
    {:meta "Foundation" :title "std.lib.foundation" :text "Basic predicates, constructors, and helpers." :href "std-lib-foundation.html"}
    {:meta "Async" :title "std.lib.future" :text "Future and asynchronous coordination helpers." :href "std-lib-future.html"}
    {:meta "I/O" :title "std.lib.io" :text "I/O helpers and stream handling." :href "std-lib-io.html"}
    {:meta "Network" :title "std.lib.network" :text "Network and port utilities." :href "std-lib-network.html"}
    {:meta "Resources" :title "std.lib.resource" :text "Classpath and filesystem resource helpers." :href "std-lib-resource.html"}
    {:meta "Schemas" :title "std.lib.schema" :text "Schema definitions, lookups, and validation helpers." :href "std-lib-schema.html"}
    {:meta "Security" :title "std.lib.security" :text "Security, keys, providers, ciphers, and verification helpers." :href "std-lib-security.html"}
    {:meta "Sorting" :title "std.lib.sort" :text "Sorting, ordering, and topological utilities." :href "std-lib-sort.html"}
    {:meta "Streams" :title "std.lib.stream" :text "Producers, collectors, pipelines, and transducers." :href "std-lib-stream.html"}
    {:meta "Systems" :title "std.lib.system" :text "System topology, display, and lifecycle utilities." :href "std-lib-system.html"}
    {:meta "Time" :title "std.lib.time" :text "Time coercion and temporal helpers." :href "std-lib-time.html"}
    {:meta "Transforms" :title "std.lib.transform" :text "Composable data transformation helpers." :href "std-lib-transform.html"}
    {:meta "Traversal" :title "std.lib.walk" :text "Walking and traversal helpers." :href "std-lib-walk.html"}
    {:meta "Zippers" :title "std.lib.zip" :text "Zipper navigation and editing helpers." :href "std-lib-zip.html"}]}]]
