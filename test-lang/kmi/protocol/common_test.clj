(ns kmi.protocol.common-test
  (:use code.test)
  (:require [hara.lang :as l]
            [kmi.protocol.common :refer :all]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-protocol :as proto]
             [kmi.protocol.common :as kproto]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-protocol :as proto]
             [kmi.protocol.common :as kproto]]})

(fact:global
  {:setup [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-protocol/iface-combine :added "4.1"}
(fact "combines KMI interface vectors into a stable surface"
  (!.js
   (proto/iface-combine [kproto/IValue kproto/ICounted kproto/IValue]))
  => ["eq" "hash" "show" "size"]

  (!.lua
   (proto/iface-combine [kproto/IValue kproto/ICounted kproto/IValue]))
  => ["eq" "hash" "show" "size"])

^{:refer xt.lang.common-protocol/proto-group :added "4.1"}
(fact "creates grouped protocol entries for KMI specs"
  (!.js
   (proto/proto-spec
    [(proto/proto-group
      [kproto/IValue kproto/INamed]
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
   (proto/proto-spec
    [(proto/proto-group
      [kproto/IValue kproto/INamed]
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

^{:refer kmi.protocol.common/ILispSequential :added "4.1"}
(fact "defines immutable Lisp-facing KMI protocol surfaces"
  (!.js
   [kproto/ILispScalar
    kproto/ILispNamed
    kproto/ILispSequential
    kproto/ILispAssociative
    kproto/ILispPersistent])
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
   [kproto/ILispScalar
    kproto/ILispNamed
    kproto/ILispSequential
    kproto/ILispAssociative
    kproto/ILispPersistent])
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
