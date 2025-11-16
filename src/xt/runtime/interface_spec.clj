(ns xt.runtime.interface-spec
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]]})
  
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


