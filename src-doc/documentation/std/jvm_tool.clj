(ns documentation.jvm-tool
  (:use code.test))

[[:hero {:title "jvm.tool"
         :subtitle "REPL hotkeys and development-tool injection"
         :lead "Configure numbered hotkey functions and expose commonly used reflection, build, test, documentation, and namespace tools in the short `s` namespace."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"Loading `jvm.tool` prepares an interactive development environment. It links reflection macros into `clojure.core`, creates the `s` helper namespace, and resolves linked tool vars after their defining namespaces become available."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Configure a hotkey"}]]

(comment
  (require '[jvm.tool :as tool])

  (tool/hotkey-set
   1
   (fn []
     (println "refreshing")))

  (tool/hotkey-1))

[[:section {:title "Use the helper namespace"}]]

"The injected `s` namespace provides short references to frequently used project, test, build, documentation, logging, and namespace-management functions. Treat it as a REPL convenience rather than an application dependency."

(comment
  (s/force-require 'example.application)
  (s/run :all)
  (s/publish '[std]))

[[:chapter {:title "Initialization behavior" :link "initialization"}]]

"Because this namespace intentionally modifies linked vars and print preferences, require it from a user or development profile rather than production library code."

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "jvm.tool"}]]
