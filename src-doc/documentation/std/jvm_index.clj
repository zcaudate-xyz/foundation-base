(ns documentation.jvm-index)

[[:hero {:title "jvm.*"
         :subtitle "JVM artifacts, classloaders, namespaces, reflection, monitoring, and development tools"
         :lead "Choose a focused JVM page for a walkthrough and complete API reference."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:card-grid
  {:title "JVM libraries"
   :items
   [{:meta "Artifacts" :title "jvm.artifact" :text "Coordinates, paths, and artifact representations." :href "jvm-artifact.html"}
    {:meta "Classpath" :title "jvm.classloader" :text "Classloader inspection and class support." :href "jvm-classloader.html"}
    {:meta "Dependencies" :title "jvm.deps" :text "Resolve and inspect JVM dependencies." :href "jvm-deps.html"}
    {:meta "Monitoring" :title "jvm.monitor" :text "JVM metrics through management beans." :href "jvm-monitor.html"}
    {:meta "Namespaces" :title "jvm.namespace" :text "Inspect, evaluate, clear, and reload namespaces." :href "jvm-namespace.html"}
    {:meta "Protocols" :title "jvm.protocol" :text "Loader and artifact extension points." :href "jvm-protocol.html"}
    {:meta "Reflection" :title "jvm.reflect" :text "Interactive class and member queries." :href "jvm-reflect.html"}
    {:meta "Require" :title "jvm.require" :text "Dependency-aware development requiring." :href "jvm-require.html"}
    {:meta "REPL" :title "jvm.tool" :text "Hotkeys and linked development tools." :href "jvm-tool.html"}]}]]
