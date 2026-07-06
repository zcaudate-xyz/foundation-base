(ns documentation.lib-aether
  (:use code.test))

[[:hero {:title "lib.aether"
         :subtitle "Maven dependency resolution, installation, and repository operations"
         :lead "Resolve dependency graphs with Eclipse Aether, prepare artifact requests, and connect resolved coordinates to JVM classloaders."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"`lib.aether` builds repository-system requests from `jvm.artifact` coordinates. It can collect dependency graphs, resolve files and versions, install local artifacts, and prepare repository operations."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Resolve dependencies"}]]

(comment
  (require '[lib.aether :as aether])

  (aether/collect-dependencies
   '[org.clojure/data.json "2.5.0"]
   {:type :coord})

  (aether/resolve-dependencies
   '[[org.clojure/data.json "2.5.0"]]
   {:type :coord}))

[[:section {:title "Attach a dependency to a loader"}]]

"`pull` resolves coordinates and adds their local files to a loader. Options control dependency traversal, replacement of another loaded version, and whether loaded entries are retained."

(comment
  (aether/pull
   '[[org.clojure/data.json "2.5.0"]]
   {:keep true})

  (aether/resolve-versions
   '[[org.clojure/data.json "LATEST"]]))

[[:section {:title "Install project artifacts"}]]

(comment
  (aether/install-artifact
   '[example/library "1.0.0"]
   {:artifacts [{:file "target/library.jar"
                 :extension "jar"}
                {:file "target/library.pom"
                 :extension "pom"}]}))

[[:chapter {:title "Supporting namespaces" :link "supporting"}]]

"Request, result, repository, session, authentication, listener, wagon, and dependency namespaces expose the lower-level Aether integration used by the public functions."

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "lib.aether"}]]
[[:api {:namespace "lib.aether.base"}]]
[[:api {:namespace "lib.aether.artifact"}]]
[[:api {:namespace "lib.aether.dependency"}]]
[[:api {:namespace "lib.aether.local-repo"}]]
[[:api {:namespace "lib.aether.remote-repo"}]]
[[:api {:namespace "lib.aether.request"}]]
[[:api {:namespace "lib.aether.result"}]]
[[:api {:namespace "lib.aether.session"}]]
