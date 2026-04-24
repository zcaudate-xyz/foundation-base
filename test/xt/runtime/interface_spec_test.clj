 (ns xt.runtime.interface-spec-test
   (:require [std.lang :as l]
             [xt.lang.common-notify :as notify])
   (:use code.test))

  (l/script- :js
   {:runtime :basic
    :require [[xt.runtime.interface-spec :as spec]
              [xt.lang.spec-base :as xt]
              [xt.lang.common-repl :as repl]]})

  (l/script- :lua
   {:runtime :basic
    :require [[xt.runtime.interface-spec :as spec]
              [xt.lang.spec-base :as xt]
              [xt.lang.common-repl :as repl]]})

 (fact:global
  {:setup    [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

 ^{:refer xt.runtime.interface-spec/iface-combine :added "4.1"}
 (fact "combines interface vectors into a stable, deduped surface"
   (!.js
    (spec/iface-combine [spec/IValue spec/ICounted spec/IValue]))
   => ["eq" "hash" "show" "size"]

   (!.lua
    (spec/iface-combine [spec/IValue spec/ICounted spec/IValue]))
   => ["eq" "hash" "show" "size"])

 ^{:refer xt.runtime.interface-spec/prototype-group :added "4.1"}
 (fact "creates grouped protocol entries for prototype-spec"
   (!.js
    (spec/prototype-spec
     [(spec/prototype-group
       [spec/IValue spec/INamed]
       {:eq "eq"
        :hash "hash"
        :show "show"
        :name "name"
        :namespace "namespace"})]))
   => {"eq" "eq"
       "hash" "hash"
       "show" "show"
       "name" "name"
       "namespace" "namespace"}

   (!.lua
    (spec/prototype-spec
     [(spec/prototype-group
       [spec/IValue spec/INamed]
       {:eq "eq"
        :hash "hash"
        :show "show"
        :name "name"
        :namespace "namespace"})]))
   => {"eq" "eq"
       "hash" "hash"
       "show" "show"
       "name" "name"
       "namespace" "namespace"})

 ^{:refer xt.runtime.interface-spec/ILispSequential :added "4.1"}
 (fact "defines immutable Lisp-facing protocol surfaces"
   (!.js
    [spec/ILispScalar
     spec/ILispNamed
     spec/ILispSequential
     spec/ILispAssociative
     spec/ILispPersistent])
   => [["eq" "hash" "show" "meta" "with_meta"]
       ["eq" "hash" "show" "meta" "with_meta" "name" "namespace"]
       ["eq" "hash" "show" "meta" "with_meta"
        "to_iter" "to_array" "empty" "size" "nth"
        "push" "pop"
        "seq" "first" "rest" "next"
        "conj" "cons" "peek"]
       ["eq" "hash" "show" "meta" "with_meta"
        "to_iter" "to_array" "empty" "size"
        "assoc" "dissoc" "keys" "vals" "lookup" "find"
        "seq" "first" "rest" "next"]
       ["is_mutable" "to_mutable" "is_persistent" "to_persistent"
        "assoc_mutable" "dissoc_mutable"
        "push_mutable" "pop_mutable"]]

   (!.lua
    [spec/ILispScalar
     spec/ILispNamed
     spec/ILispSequential
     spec/ILispAssociative
     spec/ILispPersistent])
   => [["eq" "hash" "show" "meta" "with_meta"]
       ["eq" "hash" "show" "meta" "with_meta" "name" "namespace"]
       ["eq" "hash" "show" "meta" "with_meta"
        "to_iter" "to_array" "empty" "size" "nth"
        "push" "pop"
        "seq" "first" "rest" "next"
        "conj" "cons" "peek"]
       ["eq" "hash" "show" "meta" "with_meta"
        "to_iter" "to_array" "empty" "size"
        "assoc" "dissoc" "keys" "vals" "lookup" "find"
        "seq" "first" "rest" "next"]
        ["is_mutable" "to_mutable" "is_persistent" "to_persistent"
         "assoc_mutable" "dissoc_mutable"
         "push_mutable" "pop_mutable"]])

 ^{:refer xt.runtime.interface-spec/runtime-attach :added "4.1"}
 (fact "attaches runtime dispatch entries directly to managed objects"

   (!.js
   (var protocol (spec/prototype-create
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
   (var protocol (spec/prototype-create
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


^{:refer xt.runtime.interface-spec/prototype-create :added "4.1"}
(fact "creates a prototype object suitable for runtime dispatch"

  (!.js
   (var proto (spec/prototype-create
               {"value" 4
                "sum" (fn [x]
                        (return x))}))
   [(xt/x:get-key proto "value")
    (xt/x:is-function? (xt/x:get-key proto "sum"))])
  => [4 true]

  (!.lua
   (var proto (spec/prototype-create
               {"value" 4
                "sum" (fn [x]
                        (return x))}))
   [(xt/x:get-key proto "value")
    (xt/x:is-function? (xt/x:get-key proto "sum"))
    (== proto (xt/x:get-key proto "__index"))])
  => [4 true true])

^{:refer xt.runtime.interface-spec/prototype-spec :added "4.1"}
(fact "merges protocol entries into a validated spec map"

  (!.js
   (spec/prototype-spec
    [[["eq" "hash"] {"eq" "eq" "hash" "hash"}]
     [["show"] {"show" "show"}]]))
  => {"eq" "eq" "hash" "hash" "show" "show"}

  (!.lua
   (spec/prototype-spec
    [[["eq" "hash"] {"eq" "eq" "hash" "hash"}]
     [["show"] {"show" "show"}]]))
  => {"eq" "eq" "hash" "hash" "show" "show"})
