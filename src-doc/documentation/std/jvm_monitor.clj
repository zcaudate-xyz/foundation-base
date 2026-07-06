(ns documentation.jvm-monitor
  (:use code.test))

[[:hero {:title "jvm.monitor"
         :subtitle "JVM management beans exposed as Clojure data"
         :lead "Read class loading, compilation, garbage collection, memory, operating-system, runtime, and thread metrics through one API."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"The namespace wraps `ManagementFactory` MXBeans and registers map-like conversions so callers can work with ordinary Clojure maps rather than Java bean accessors."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Discover metric groups"}]]

(comment
  (require '[jvm.monitor :as monitor])

  (monitor/jvm))

[[:section {:title "Read one group"}]]

(comment
  (monitor/jvm :memory)
  (monitor/jvm :gc)
  (monitor/jvm :thread)
  (monitor/jvm :os))

[[:section {:title "Capture a complete snapshot"}]]

"Use `:all` for diagnostics, support bundles, or periodic telemetry. For high-frequency monitoring, request only the groups required by the caller."

(comment
  (def snapshot (monitor/jvm :all))
  (keys snapshot)
  (get-in snapshot [:memory :heap-memory-usage :used]))

[[:chapter {:title "Direct bean access" :link "beans"}]]

"The individual `*-bean` functions return the underlying MXBean values when Java interop or another object conversion is required."

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "jvm.monitor"}]]
