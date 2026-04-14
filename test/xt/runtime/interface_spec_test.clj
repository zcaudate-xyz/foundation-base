 (ns xt.runtime.interface-spec-test
   (:require [std.lang :as l]
             [xt.lang.common-notify :as notify])
   (:use code.test))

 (l/script- :js
   {:runtime :basic
    :require [[xt.runtime.interface-spec :as spec]
              [xt.lang.common-repl :as repl]]})

 (l/script- :lua
   {:runtime :basic
    :require [[xt.runtime.interface-spec :as spec]
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

 ^{:refer xt.runtime.interface-spec/proto-group :added "4.1"}
 (fact "creates grouped protocol entries for proto-spec"
   (!.js
    (spec/proto-spec
     [(spec/proto-group
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
    (spec/proto-spec
     [(spec/proto-group
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


^{:refer xt.runtime.interface-spec/proto-create :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.interface-spec/proto-spec :added "4.1"}
(fact "TODO")