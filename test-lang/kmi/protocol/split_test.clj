(ns kmi.protocol.split-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-protocol :as proto]
             [kmi.protocol.ivalue :as p-value]
             [kmi.protocol.icounted :as p-counted]
             [kmi.protocol.inamed :as p-named]
             [kmi.protocol.ilisp-scalar :as p-lisp-scalar]
             [kmi.protocol.ilisp-named :as p-lisp-named]
             [kmi.protocol.ilisp-sequential :as p-lisp-sequential]
             [kmi.protocol.ilisp-associative :as p-lisp-associative]
             [kmi.protocol.ilisp-persistent :as p-lisp-persistent]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-protocol :as proto]
             [kmi.protocol.ivalue :as p-value]
             [kmi.protocol.icounted :as p-counted]
             [kmi.protocol.inamed :as p-named]
             [kmi.protocol.ilisp-scalar :as p-lisp-scalar]
             [kmi.protocol.ilisp-named :as p-lisp-named]
             [kmi.protocol.ilisp-sequential :as p-lisp-sequential]
             [kmi.protocol.ilisp-associative :as p-lisp-associative]
             [kmi.protocol.ilisp-persistent :as p-lisp-persistent]]})

(fact:global
  {:setup [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-protocol/iface-combine :added "4.1"}
(fact "combines split KMI interface vectors into a stable surface"
  (!.js
   (proto/iface-combine [p-value/IValue p-counted/ICounted p-value/IValue]))
  => ["eq" "hash" "show" "size"]

  (!.lua
   (proto/iface-combine [p-value/IValue p-counted/ICounted p-value/IValue]))
  => ["eq" "hash" "show" "size"])

^{:refer xt.lang.common-protocol/proto-group :added "4.1"}
(fact "creates grouped protocol entries from split KMI namespaces"
  (!.js
   (proto/proto-spec
    [(proto/proto-group
      [p-value/IValue p-named/INamed]
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
      [p-value/IValue p-named/INamed]
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

^{:refer kmi.protocol.ilisp-sequential/ILispSequential :added "4.1"}
(fact "defines immutable Lisp-facing KMI protocol surfaces from split namespaces"
  (!.js
   [p-lisp-scalar/ILispScalar
    p-lisp-named/ILispNamed
    p-lisp-sequential/ILispSequential
    p-lisp-associative/ILispAssociative
    p-lisp-persistent/ILispPersistent])
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
   [p-lisp-scalar/ILispScalar
    p-lisp-named/ILispNamed
    p-lisp-sequential/ILispSequential
    p-lisp-associative/ILispAssociative
    p-lisp-persistent/ILispPersistent])
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
