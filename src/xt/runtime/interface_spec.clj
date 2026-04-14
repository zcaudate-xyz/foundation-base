(ns xt.runtime.interface-spec
  (:require [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-iter :as it]]})

(defn.js proto-create
  "creates a prototype map from a spec map"
  {:added "4.1"}
  [spec-map]
  (var out {})
  (xt/for:object [[k f] spec-map]
    (if (xt/x:is-function? f)
      (xt/x:set-key out k
                    (fn [a b c d]
                      (return (f (xt/x:this) a b c d))))
      (xt/x:set-key out k f)))
  (return out))

(l/script :lua
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-iter :as it]]})

(defn.lua proto-create
  "creates a prototype map from a spec map"
  {:added "4.1"}
  [spec-map]
  (xt/x:set-key spec-map "__index" spec-map)
  (return spec-map))

(l/script :python
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-iter :as it]]})

(defn.py proto-create
  "creates a prototype map from a spec map"
  {:added "4.1"}
  [spec-map]
  (xt/x:set-key spec-map "__index" spec-map)
  (return spec-map))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-iter :as it]]})
  
(def.xt IAssoc  ["assoc"])

(def.xt IAssocMutable  ["assoc_mutable"])

(def.xt IDissoc ["dissoc"])

(def.xt IDissocMutable ["dissoc_mutable"])

(def.xt IColl   ["to_iter"
                 "to_array"])
(def.xt IEdit   ["is_mutable"
                 "to_mutable"
                 "is_persistent"
                 "to_persistent"])

(def.xt IFind   ["find"])

(def.xt INth    ["nth"])

(def.xt IPush   ["push"])

(def.xt IPop    ["pop"])

(def.xt IPushMutable   ["push_mutable"])

(def.xt IPopMutable    ["pop_mutable"])

(def.xt ISize   ["size"])

(def.xt IHash   ["hash"])

(def.xt IEmpty  ["empty"])

(def.xt IEq     ["eq"])

(def.xt IIndexed ["index_of"])

(def.xt IIndexedKV ["index_of_key"
                    "index_of_val"])

(def.xt ILookup  ["keys"
                  "vals"
                  "lookup"])

(def.xt INamespaced  ["name"
                      "namespace"])

(def.xt IPair        ["key"
                      "val"])

(def.xt IShow    ["show"])

(defn.xt proto-spec
  "creates a prototype spec map from interface entries"
  {:added "4.0"}
  [spec-arr]
  (var acc {})
  (xt/for:array [e spec-arr]
    (var spec-i (xt/x:first e))
    (var spec-map (xt/x:second e))
    (xt/for:array [key spec-i]
      (when (xt/x:nil? (xt/x:get-key spec-map key))
        (xt/x:err
         (xt/x:cat "NOT VALID."
                   (xt/x:json-encode {:required key
                                      :actual (xt/x:obj-keys spec-map)})))))
     (:= acc (xt/x:obj-assign acc spec-map)))
  (return acc))

(defabstract.xt proto-create
  [spec-map])
