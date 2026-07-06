(ns documentation.jvm-namespace
  (:use code.test))

[[:hero {:title "jvm.namespace"
         :subtitle "namespace inspection, evaluation, cleanup, and reload workflows"
         :lead "Inspect namespace mappings and memory, evaluate forms in controlled namespaces, and reload dependency-ordered namespace sets."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"The namespace combines direct evaluation helpers with `std.pipe` tasks for listing, clearing, resetting, and reloading namespaces. Most task functions accept one namespace, a collection, or the default current namespace."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Inspect a namespace"}]]

(comment
  (require '[jvm.namespace :as namespace])

  (namespace/list-publics 'jvm.namespace)
  (namespace/list-interns 'jvm.namespace)
  (namespace/list-imports 'jvm.namespace)
  (namespace/list-aliases 'jvm.namespace))

[[:section {:title "Evaluate in context"}]]

(comment
  (namespace/eval-ns 'example.scratch
    '(def answer 42))

  (namespace/with-ns 'example.scratch
    answer)

  (namespace/eval-temp-ns
    '(+ 1 2 3)))

[[:section {:title "Reload a dependency set"}]]

"`reload` sorts the supplied namespaces topologically before running the reload task. Use the clear functions narrowly; `reset` removes namespaces under a root and is intended for controlled development workflows."

(comment
  (namespace/reload
   '[example.core example.api])

  (namespace/loaded? 'example.core)
  (namespace/list-in-memory 'example.core))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "jvm.namespace"}]]
[[:api {:namespace "jvm.namespace.common"}]]
[[:api {:namespace "jvm.namespace.context"}]]
[[:api {:namespace "jvm.namespace.dependent"}]]
[[:api {:namespace "jvm.namespace.eval"}]]
