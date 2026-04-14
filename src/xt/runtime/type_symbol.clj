(ns xt.runtime.type-symbol
  (:require [std.lang :as l])
  (:refer-clojure :exclude [symbol]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.base-runtime :as rt :with [defvar.xt]]
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
  (return
   (-> (xt/x:get-key common-hash/SEED "symbol")
       (xt/x:bit-xor (common-hash/hash-string (xt/x:sym-full _ns _name))))))

(defn.xt symbol-show
  "shows the symbol"
  {:added "4.0"}
  [sym]
  (var #{_ns _name} sym)
  (return
   (xt/x:sym-full _ns _name)))

(defn.xt symbol-eq
  "gets symbol equality"
  {:added "4.0"}
  [sym o]
  (return (and (== "symbol" (xt/x:type-class o))
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
      (xt/x:proto-spec)
      (xt/x:proto-create)))

(defn.xt symbol-create
  "creates a symbol"
  {:added "4.0"}
  [ns name]
  (var sym {"::" "symbol"
            :_ns   ns
            :_name name})
  (xt/x:set-proto sym -/SYMBOL_PROTOTYPE)
  (return sym))

(defn.xt symbol
  "creates the symbol or pulls it from cache"
  {:added "4.0"}
  [ns name]
  (var lu -/SYMBOL_LOOKUP)
  (var key (xt/x:sym-full ns name))
  (var out (xt/x:get-key lu key))
  (when (xt/x:nil? out)
    (var sym (-/symbol-create ns name))
    (xt/x:set-key lu key sym)
    (return sym))
  (return out))

