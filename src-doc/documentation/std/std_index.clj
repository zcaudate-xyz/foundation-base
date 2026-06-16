(ns documentation.std-index)

[[:hero {:title "std"
         :subtitle "Standard libraries for the foundation ecosystem."
         :lead "Core utilities for blocks, collections, atoms, streams, environments, and applicative invocation."
         :badges ["Clojure" "Utilities" "Reference"]
         :actions [{:label "Back to home" :href "../index.html" :variant :primary}
                   {:label "Contribute" :href "../guides/index.html"}]}]]

[[:card-grid {:title "Libraries"
              :lead "Each card links to a focused reference page."
              :items [{:meta "Code"
                       :title "std.block"
                       :text "Code representation, traversal, and manipulation."
                       :href "std-block.html"}
                      {:meta "Collections"
                       :title "std.lib.collection"
                       :text "Extended map, sequence, tree, and diff operations."
                       :href "std-lib-collection.html"}
                      {:meta "State"
                       :title "std.lib.atom"
                       :text "Nested atoms, batch updates, cursors, and derived state."
                       :href "std-lib-atom.html"}
                      {:meta "Invocation"
                       :title "std.lib.apply"
                       :text "Applicative invocation and host applicatives."
                       :href "std-lib-apply.html"}
                      {:meta "Streams"
                       :title "std.lib.stream"
                       :text "Producers, collectors, pipelines, and transducers."
                       :href "std-lib-stream.html"}
                      {:meta "Environment"
                       :title "std.lib.env"
                       :text "Namespace, resource, pprint, and debug utilities."
                       :href "std-lib-env.html"}
                      {:meta "Foundation"
                       :title "std.lib.foundation"
                       :text "Basic predicates, constructors, and helpers."
                       :href "std-lib-foundation.html"}]}]]
