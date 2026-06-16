(ns kmi.lang.type-symbol
  (:require [hara.lang :as l])
  (:refer-clojure :exclude [symbol]))

(l/script :xtalk
  {:require [[kmi.lang.protocol-base :as p]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as proto]
             [kmi.lang.interface-common :as interface-common]
             [kmi.lang.common-hash :as common-hash]]})

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

(proto/defimpl.xt ^{:rt/tag "symbol"} Symbol
  [_ns _name]
  p/IEq
  {eq -/symbol-eq}
  p/IHash
  {hash (interface-common/wrap-with-cache -/symbol-hash)}
  p/INamespaced
  {name      interface-common/get-name
   namespace interface-common/get-namespace}
  p/IShow
  {show -/symbol-show})

(defn.xt symbol-create
  "creates a symbol"
  {:added "4.0"}
  [ns name]
  (return (-/Symbol ns name)))

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
