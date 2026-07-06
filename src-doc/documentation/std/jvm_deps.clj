(ns documentation.jvm-deps
  (:use code.test))

[[:hero {:title "jvm.deps"
         :subtitle "JVM artifact resolution and classpath dependency inspection"
         :lead "Connect artifact coordinates to classpath locations and inspect which dependency versions are available to a loader."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"`jvm.deps` resolves classes and namespaces to classpath locations, maps dependency coordinates to local archive paths, and reports artifacts visible through a classloader."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Resolve code locations"}]]

(comment
  (require '[jvm.deps :as deps])

  (deps/resolve-classloader String)
  (deps/resolve-classloader 'clojure.core)
  (deps/resolve 'clojure.core
                '[org.clojure/clojure "1.11.1"]))

[[:section {:title "Inspect dependencies"}]]

(comment
  (deps/all-loaded-artifacts)
  (deps/version-map)
  (deps/current-version 'org.clojure/clojure)
  (deps/loaded-artifact?
   '[org.clojure/clojure "1.11.1"]))

[[:section {:title "Classpath operations"}]]

"Collection operations are available for adding known local coordinates to a mutable loader and removing them again. The single-coordinate functions are useful when callers need precise control over reporting and error handling."

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "jvm.deps"}]]
