(ns documentation.jvm-reflect
  (:use code.test))

[[:hero {:title "jvm.reflect"
         :subtitle "interactive Java reflection and member queries"
         :lead "Inspect class metadata and hierarchies, query methods and fields, and pretty-print object views."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"The namespace exposes compact REPL-oriented macros over `std.object`. Selectors can filter members by name, type, modifiers, or regular expression, and output is formatted for interactive exploration."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Inspect a class"}]]

(comment
  (require '[jvm.reflect :as reflect])

  (reflect/.% String)
  (reflect/.%> String)
  (reflect/.? String #"^char" :name))

[[:section {:title "Inspect an instance"}]]

(comment
  (reflect/.* "hello" #"^to" :name)
  (reflect/.& (StringBuilder. "hello")))

[[:section {:title "Thread member access"}]]

"`.>` behaves like `->` while resolving keyword or dot-prefixed member names through the object query system."

(comment
  (reflect/.> "hello" :value String.))

[[:chapter {:title "Supporting output" :link "supporting"}]]

"`jvm.reflect.print` contains the tabular and class-name formatting used by the query macros."

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "jvm.reflect"}]]
[[:api {:namespace "jvm.reflect.print"}]]
