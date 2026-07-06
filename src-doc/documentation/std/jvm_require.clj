(ns documentation.jvm-require
  (:use code.test))

[[:hero {:title "jvm.require"
         :subtitle "dependency-aware namespace requiring for development"
         :lead "Retry a namespace require after discovering and loading a missing namespace dependency from the compiler error chain."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"`force-require` is a development helper for namespaces that fail to reload because a referenced namespace has not yet been loaded. It follows missing-namespace errors, requires the dependency, and then returns through the original stack."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Require a namespace"}]]

(comment
  (require '[jvm.require :as jvm-require])

  (jvm-require/force-require
   'example.application))

[[:section {:title "Use in a reload workflow"}]]

"Call this helper when generating or rearranging namespaces during development. Normal application startup should generally continue to use ordinary `require` declarations so dependency problems remain explicit."

(comment
  (doseq [namespace
          '[example.model
            example.service
            example.application]]
    (jvm-require/force-require namespace)))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "jvm.require"}]]
