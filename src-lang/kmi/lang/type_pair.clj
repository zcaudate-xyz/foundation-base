(ns kmi.lang.type-pair
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[kmi.lang.protocol-base :as p]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-iter :as it]
             [xt.lang.common-protocol :as proto]
             [kmi.lang.common-util :as util]
             [kmi.lang.common-coll :as coll]]})

(defn.xt pair-to-iter
  "pair to iterator"
  {:added "4.0"}
  [pair]
  (return (it/iter [(xt/x:get-key pair "_key")
                    (xt/x:get-key pair "_val")])))

(defn.xt pair-to-array
  "pair to array"
  {:added "4.0"}
  [pair]
  (return [(xt/x:get-key pair "_key")
           (xt/x:get-key pair "_val")]))

(defn.xt pair-nth
  "pair nth"
  {:added "4.0"}
  [pair i]
  (if (== i 0)
    (return (xt/x:get-key pair "_key"))
    (if (== i 1)
      (return (xt/x:get-key pair "_val"))
      (return nil))))

(proto/defimpl.xt ^{:rt/tag "pair"} Pair
  [_key _val _start_string _end_string _sep_string _is_ordered]
  p/IColl
  {to-iter -/pair-to-iter
   to-array -/pair-to-array}
  p/IEq
  {eq coll/coll-eq}
  p/IHash
  {hash (util/wrap-with-cache
         coll/coll-hash-ordered
         [nil])}
  p/INth
  {nth -/pair-nth}
  p/ISize
  {size (fn:> [e] 2)}
  p/IShow
  {show coll/coll-show})

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
