(ns kmi.lang.type-syntax
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[kmi.lang.protocol-base :as p]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-iter :as it]
             [xt.lang.common-protocol :as proto]
             [kmi.lang.common-util :as util]
             [kmi.lang.common-hash :as common-hash]]})

(defn.xt syntax-wrap
  "wraps a function to use syntax"
  {:added "4.0"}
  [f]
  (return (fn [syntax (:.. args)]
            (var value (xt/x:get-key syntax "_value"))
            (return (f value (xt/x:unpack args))))))

(defn.xt syntax-to-iter
  "iterates over the wrapped syntax value"
  {:added "4.1"}
  [syntax]
  (return (it/iter (p/to-iter (xt/x:get-key syntax "_value")))))

(proto/defimpl.xt ^{:rt/tag "syntax"} Syntax
  [_value _metadata]
  p/IAssoc
  {:assoc (-/syntax-wrap p/assoc)}
  p/IAssocMutable
  {:assoc-mutable (-/syntax-wrap p/assoc-mutable)}
  p/IColl
  {:to-iter  -/syntax-to-iter
   :to-array (-/syntax-wrap p/to-array)}
  p/IDissoc
  {:dissoc (-/syntax-wrap p/dissoc)}
  p/IDissocMutable
  {:dissoc-mutable (-/syntax-wrap p/dissoc-mutable)}
  p/IEmpty
  {:empty (-/syntax-wrap p/empty)}
  p/IEq
  {:eq (-/syntax-wrap util/eq)}
  p/IFind
  {:find (-/syntax-wrap p/find)}
  p/IHash
  {:hash (-/syntax-wrap util/hash)}
  p/INth
  {:nth (-/syntax-wrap p/nth)}
  p/IPush
  {:push (-/syntax-wrap p/push)}
  p/IPushMutable
  {:push-mutable (-/syntax-wrap p/push-mutable)}
  p/IPop
  {:pop (-/syntax-wrap p/pop)}
  p/IPopMutable
  {:pop-mutable (-/syntax-wrap p/pop-mutable)}
  p/INamespaced
  {:name      (-/syntax-wrap util/get-name)
   :namespace (-/syntax-wrap util/get-namespace)}
  p/ISize
  {:size (-/syntax-wrap util/count)}
  p/IShow
  {:show (-/syntax-wrap util/show)})

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
  (return (:? (util/is-syntax? x)
              (xt/x:get-key x "_metadata")
              nil)))

(defn.xt syntax
  "creates a syntax"
  {:added "4.0"}
  [x metadata]
  (var v (:? (util/is-syntax? x)
             (xt/x:get-key x "_value")
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
