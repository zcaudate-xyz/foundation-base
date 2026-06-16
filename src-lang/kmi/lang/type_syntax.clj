(ns kmi.lang.type-syntax
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[kmi.lang.protocol-base :as p]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as proto]
             [kmi.lang.interface-common :as interface-common]
             [kmi.lang.common-hash :as common-hash]]})

(defn.xt syntax-wrap
  "wraps a function to use syntax"
  {:added "4.0"}
  [f]
  (return (fn [syntax ...]
            (var value (. syntax _value))
            (return (f value ...)))))

(proto/defimpl.xt ^{:rt/tag "syntax"} Syntax
  [_value _metadata]
  p/IAssoc
  {:assoc (-/syntax-wrap interface-common/assoc)}
  p/IAssocMutable
  {:assoc-mutable (-/syntax-wrap interface-common/assoc-mutable)}
  p/IColl
  {:to-iter  (-/syntax-wrap interface-common/to-iter)
   :to-array (-/syntax-wrap interface-common/to-array)}
  p/IDissoc
  {:dissoc (-/syntax-wrap interface-common/dissoc)}
  p/IDissocMutable
  {:dissoc-mutable (-/syntax-wrap interface-common/dissoc-mutable)}
  p/IEmpty
  {:empty (-/syntax-wrap interface-common/empty)}
  p/IEq
  {:eq (-/syntax-wrap interface-common/eq)}
  p/IFind
  {:find (-/syntax-wrap interface-common/find)}
  p/IHash
  {:hash (-/syntax-wrap interface-common/hash)}
  p/INth
  {:nth (-/syntax-wrap interface-common/nth)}
  p/IPush
  {:push (-/syntax-wrap interface-common/push)}
  p/IPushMutable
  {:push-mutable (-/syntax-wrap interface-common/push-mutable)}
  p/IPop
  {:pop (-/syntax-wrap interface-common/pop)}
  p/IPopMutable
  {:pop-mutable (-/syntax-wrap interface-common/pop-mutable)}
  p/INamespaced
  {:name      (-/syntax-wrap interface-common/get-name)
   :namespace (-/syntax-wrap interface-common/get-namespace)}
  p/ISize
  {:size (-/syntax-wrap interface-common/count)}
  p/IShow
  {:show (-/syntax-wrap interface-common/show)})

(defn.xt syntax-create
  "creates a syntax

   (!.js
    (tc/count
    (syn/syntax-create [1 2 3] \"hello\")))"
  {:added "4.0"}
  [value metadata]
  (return (-/Syntax value metadata)))

(defn.xt get-metadata
  "gets metadata"
  {:added "4.0"}
  [x]
  (return (:? (interface-common/is-syntax? x)
              (. x _metadata)
              nil)))

(defn.xt syntax
  "creates a syntax"
  {:added "4.0"}
  [x metadata]
  (var v (:? (interface-common/is-syntax? x)
             (. x _value)
             x))
  (return (:? (xt/x:nil? metadata)
              v
              (-/syntax-create v metadata))))

(comment
  (comment
  #_[spec/IEdit
      spec/IIndexed
      spec/IIndexedKV
      spec/ILookup
     ]
  (xt/x:proto-spec))
  (comment
  [(def.xt IAssoc  ["assoc"])
   (def.xt IDissoc ["dissoc"])
   (def.xt IColl   ["%/start_string"
                    "%/end_string"
                    "%/sep_string"
                    "to_iter"
                    "to_array"])
   (def.xt IPush   ["push"])
   (def.xt IPop    ["pop"])
   (def.xt ISize   ["size"])
   (def.xt IHash    ["hash"])
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
   (def.xt IShow    ["show"])]))
