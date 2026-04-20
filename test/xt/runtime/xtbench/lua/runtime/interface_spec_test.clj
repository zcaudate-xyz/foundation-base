(ns
 xtbench.lua.runtime.interface-spec-test
 (:require [std.lang :as l] [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :lua
 {:runtime :basic,
  :require
  [[xt.runtime.interface-spec :as spec]
   [xt.lang.common-spec :as xt]
   [xt.lang.common-repl :as repl]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.runtime.interface-spec/iface-combine, :added "4.1"}
(fact
 "combines interface vectors into a stable, deduped surface"
 (!.lua (spec/iface-combine [spec/IValue spec/ICounted spec/IValue]))
 =>
 ["eq" "hash" "show" "size"])

^{:refer xt.runtime.interface-spec/proto-group, :added "4.1"}
(fact
 "creates grouped protocol entries for proto-spec"
 (!.lua
  (spec/proto-spec
   [(spec/proto-group
     [spec/IValue spec/INamed]
     {:eq "eq",
      :hash "hash",
      :show "show",
      :name "name",
      :namespace "namespace"})]))
 =>
 {"eq" "eq",
  "hash" "hash",
  "show" "show",
  "name" "name",
  "namespace" "namespace"})

^{:refer xt.runtime.interface-spec/ILispSequential, :added "4.1"}
(fact
 "defines immutable Lisp-facing protocol surfaces"
 (!.lua
  [spec/ILispScalar
   spec/ILispNamed
   spec/ILispSequential
   spec/ILispAssociative
   spec/ILispPersistent])
 =>
 [["eq" "hash" "show" "meta" "with_meta"]
  ["eq" "hash" "show" "meta" "with_meta" "name" "namespace"]
  ["eq"
   "hash"
   "show"
   "meta"
   "with_meta"
   "to_iter"
   "to_array"
   "empty"
   "size"
   "nth"
   "push"
   "pop"
   "seq"
   "first"
   "rest"
   "next"
   "conj"
   "cons"
   "peek"]
  ["eq"
   "hash"
   "show"
   "meta"
   "with_meta"
   "to_iter"
   "to_array"
   "empty"
   "size"
   "assoc"
   "dissoc"
   "keys"
   "vals"
   "lookup"
   "find"
   "seq"
   "first"
   "rest"
   "next"]
  ["is_mutable"
   "to_mutable"
   "is_persistent"
   "to_persistent"
   "assoc_mutable"
   "dissoc_mutable"
   "push_mutable"
   "pop_mutable"]])

^{:refer xt.runtime.interface-spec/runtime-attach, :added "4.1"}
(fact
 "attaches runtime dispatch entries directly to managed objects"
 ^{:hidden true}
 (!.lua
  (var
   protocol
   (spec/proto-create
    {"extra" 4, "read_value" (fn [self] (return (. self value)))}))
  (var obj (spec/runtime-attach {"::" "demo", :value 10} protocol))
  [(. obj extra)
   (. obj (read-value))
   (== protocol (spec/runtime-protocol obj))])
 =>
 [4 10 true])

^{:refer xt.runtime.interface-spec/proto-create, :added "4.1"}
(fact
 "creates a prototype object suitable for runtime dispatch"
 ^{:hidden true}
 (!.lua
  (var
   proto
   (spec/proto-create {"value" 4, "sum" (fn [x] (return x))}))
  [(xt/x:get-key proto "value")
   (xt/x:is-function? (xt/x:get-key proto "sum"))
   (== proto (xt/x:get-key proto "__index"))])
 =>
 [4 true true])

^{:refer xt.runtime.interface-spec/proto-spec, :added "4.1"}
(fact
 "merges protocol entries into a validated spec map"
 ^{:hidden true}
 (!.lua
  (spec/proto-spec
   [[["eq" "hash"] {"eq" "eq", "hash" "hash"}]
    [["show"] {"show" "show"}]]))
 =>
 {"eq" "eq", "hash" "hash", "show" "show"})
