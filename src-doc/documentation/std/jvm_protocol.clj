(ns documentation.jvm-protocol
  (:use code.test))

[[:hero {:title "jvm.protocol"
         :subtitle "extension points for loaders, artifacts, and class sources"
         :lead "Implement the small protocols and multimethods used by `jvm.classloader` and `jvm.artifact` to support new loader or source types."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"`ILoader` defines URL inspection and mutation. The `-load-class`, `-rep`, and `-artifact` multimethods provide open dispatch for class sources and artifact representations."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Adapt a loader"}]]

(comment
  (require '[jvm.protocol :as protocol])

  (extend-type ExampleLoader
    protocol/ILoader
    (-has-url? [loader path]
      (contains? (:paths loader) path))
    (-get-url [loader path]
      (get (:urls loader) path))
    (-all-urls [loader]
      (vals (:urls loader)))
    (-add-url [loader path]
      (add-path loader path))
    (-remove-url [loader path]
      (remove-path loader path))))

[[:section {:title "Support another artifact input"}]]

(comment
  (defmethod protocol/-rep ExampleCoordinate
    [coordinate]
    (example-coordinate->rep coordinate))

  (defmethod protocol/-artifact :example
    [_ value]
    (rep->example value)))

[[:section {:title "Support another class source"}]]

(comment
  (defmethod protocol/-load-class
    [ExampleSource clojure.lang.DynamicClassLoader]
    [source loader opts]
    (define-example-class source loader opts)))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "jvm.protocol"}]]
