  (ns kmi.lang.interface-spec-test
   (:require [hara.lang :as l])
   (:use code.test))

  (l/script- :js
   {:runtime :basic
    :require [[kmi.lang.interface-spec :as spec]
              [xt.lang.spec-base :as xt]]})

  (l/script- :lua
   {:runtime :basic
    :require [[kmi.lang.interface-spec :as spec]
              [xt.lang.spec-base :as xt]]})

 (fact:global
  {:setup    [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

 ^{:refer kmi.lang.interface-spec/runtime-attach :added "4.1"}
 (fact "attaches runtime dispatch entries directly to managed objects"

    (!.js
   (var protocol (spec/proto-create
                   {"extra" 4
                    "read_value" (fn [self]
                                   (return (. self value)))}))
    (var obj (spec/runtime-attach {"::" "demo"
                                   :value 10}
                                  protocol))
    [(. obj extra)
     (. obj (read-value))
     (== protocol (spec/runtime-protocol obj))])
   => [4 10 true]

    (!.lua
   (var protocol (spec/proto-create
                   {"extra" 4
                    "read_value" (fn [self]
                                   (return (. self value)))}))
    (var obj (spec/runtime-attach {"::" "demo"
                                   :value 10}
                                  protocol))
    [(. obj extra)
     (. obj (read-value))
     (== protocol (spec/runtime-protocol obj))])
   => [4 10 true])


^{:refer kmi.lang.interface-spec/proto-create :added "4.1"}
(fact "creates a prototype object suitable for runtime dispatch"

  (!.js
   (var proto (spec/proto-create
               {"value" 4
                "sum" (fn [x]
                        (return x))}))
   [(xt/x:get-key proto "value")
    (xt/x:is-function? (xt/x:get-key proto "sum"))])
  => [4 true]

  (!.lua
   (var proto (spec/proto-create
               {"value" 4
                "sum" (fn [x]
                        (return x))}))
   [(xt/x:get-key proto "value")
    (xt/x:is-function? (xt/x:get-key proto "sum"))
   (== proto (xt/x:get-key proto "__index"))])
  => [4 true true])

^{:refer kmi.lang.interface-spec/runtime-protocol :added "4.1"}
(fact "gets runtime dispatch entries from managed objects"
  (!.js
   (var protocol (spec/proto-create {"extra" 4}))
   (var obj (spec/runtime-attach {"::" "demo"} protocol))
   (== protocol (spec/runtime-protocol obj)))
  => true

  (!.lua
   (var protocol (spec/proto-create {"extra" 4}))
   (var obj (spec/runtime-attach {"::" "demo"} protocol))
   (== protocol (spec/runtime-protocol obj)))
  => true)
