(ns documentation.jvm-artifact
  (:use code.test))

[[:hero {:title "jvm.artifact"
         :subtitle "Maven coordinates, repository paths, and artifact representations"
         :lead "Normalize dependency coordinates and convert them between records, vectors, strings, paths, files, and repository metadata."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"`jvm.artifact` centers on the `Rep` record. A representation stores group, artifact, extension, classifier, version, scope, exclusions, properties, and file information in one value."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Normalize coordinates"}]]

(comment
  (require '[jvm.artifact :as artifact])

  (def clojure-rep
    (artifact/rep '[org.clojure/clojure "1.11.1"]))

  (artifact/rep? clojure-rep)
  (artifact/rep->coord clojure-rep)
  (artifact/rep->string clojure-rep)
  (artifact/rep->path clojure-rep))

[[:section {:title "Convert output formats"}]]

"Use `artifact` when the caller chooses an output tag dynamically. Direct conversion functions are clearer when the format is fixed."

(comment
  (artifact/artifact :string '[org.clojure/clojure "1.11.1"])
  (artifact/artifact :path "org.clojure:clojure:1.11.1")
  (artifact/artifact :default '[org.clojure/clojure "1.11.1"]))

[[:chapter {:title "Supporting namespaces" :link "supporting"}]]

"`jvm.artifact.common` contains repository path and resource-entry helpers. `jvm.artifact.search` searches artifact contents and class entries."

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "jvm.artifact"}]]
[[:api {:namespace "jvm.artifact.common"}]]
[[:api {:namespace "jvm.artifact.search"}]]
