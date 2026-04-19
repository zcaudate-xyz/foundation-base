(ns xt.runtime.type-symbol
  (:require [std.lang :as l])
  (:refer-clojure :exclude [symbol]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.runtime.interface-common :as interface-common]
             [xt.runtime.interface-spec :as spec]
             [xt.runtime.common-hash :as common-hash]]})

(def.xt SYMBOL_LOOKUP
  {})

(defn.xt symbol-hash
  "gets the symbol hash"
  {:added "4.0"}
  [sym]
  (var #{_ns _name} sym)
  (var sname (:? (xt/x:nil? _ns)
                 _name
                 (xt/x:cat _ns "/" _name)))
  (return
   (-> (xt/x:get-key common-hash/SEED "symbol")
       (xt/x:bit-xor (common-hash/hash-string sname)))))

(defn.xt symbol-show
  "shows the symbol"
  {:added "4.0"}
  [sym]
  (var #{_ns _name} sym)
  (var sname (:? (xt/x:nil? _ns)
                 _name
                 (xt/x:cat _ns "/" _name)))
  (return
   sname))

(defn.xt symbol-eq
  "gets symbol equality"
  {:added "4.0"}
  [sym o]
  (var oclass (common-hash/native-class o))
  (return (and (== "symbol" oclass)
               (== (. sym _ns) (. o _ns))
               (== (. sym _name) (. o _name)))))

(def.xt SYMBOL_SPEC
  [[spec/IEq         {:eq        -/symbol-eq}]
   [spec/IHash       {:hash      (interface-common/wrap-with-cache
                                  -/symbol-hash)}]
   [spec/INamespaced {:name      interface-common/get-name
                      :namespace interface-common/get-namespace} ]
   [spec/IShow       {:show      -/symbol-show}]])

(def.xt SYMBOL_PROTOTYPE
  (-> -/SYMBOL_SPEC
      (spec/proto-spec)
      (spec/proto-create)))

(defn.xt symbol-create
  "creates a symbol"
  {:added "4.0"}
  [ns name]
  (var sym {"::" "symbol"
            :_ns   ns
            :_name name})
  (return (spec/runtime-attach sym -/SYMBOL_PROTOTYPE)))

(defn.xt symbol
  "creates the symbol or pulls it from cache"
  {:added "4.0"}
  [ns name]
  (var lu -/SYMBOL_LOOKUP)
  (var key (:? (xt/x:nil? ns)
               name
               (xt/x:cat ns "/" name)))
  (var out (xt/x:get-key lu key))
  (when (xt/x:nil? out)
    (var sym (-/symbol-create ns name))
    (xt/x:set-key lu key sym)
    (return sym))
  (return out))
