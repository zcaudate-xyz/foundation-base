(ns kmi.lang.type-pair
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[kmi.lang.protocol-base :as p]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-iter :as it]
             [xt.lang.common-protocol :as proto]
             [kmi.lang.interface-common :as interface-common]
             [kmi.lang.interface-collection :as interface-collection]]})

(defgen.xt pair-to-iter
  "pair to iterator"
  {:added "4.0"}
  [pair]
  (yield (. pair _key))
  (yield (. pair _val)))

(defn.xt pair-to-array
  "pair to array"
  {:added "4.0"}
  [pair]
  (return [(. pair _key)
           (. pair _val)]))

(defn.xt pair-nth
  "pair nth"
  {:added "4.0"}
  [pair i]
  (return (:? (== i 0)
              (. pair _key)
              (== i 1)
              (. pair _val)
              :else nil)))

(proto/defimpl.xt ^{:rt/tag "pair"} Pair
  [_key _val _start_string _end_string _sep_string _is_ordered]
  p/IColl
  {to-iter -/pair-to-iter
   to-array -/pair-to-array}
  p/IEq
  {eq interface-collection/coll-eq}
  p/IHash
  {hash (interface-common/wrap-with-cache
         interface-collection/coll-hash-ordered)}
  p/INth
  {nth -/pair-nth}
  p/ISize
  {size (fn:> [e] 2)}
  p/IShow
  {show interface-collection/coll-show})

(defn.xt pair
  "creates a pair"
  {:added "4.0"}
  [key val]
  (return (-/Pair key val
                  "[" "]" ", " true)))

(defn.xt pair-new
  "creates a pair new"
  {:added "4.0"}
  [key val _protocol]
  (return (-/pair key val)))
