(ns kmi.lang.type-pair
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-iter :as it]
             [xt.lang.common-protocol :as proto]
             [kmi.protocol.icoll :as p-coll]
             [kmi.protocol.ieq :as p-eq]
             [kmi.protocol.ihash :as p-hash]
             [kmi.protocol.inth :as p-nth]
             [kmi.protocol.isize :as p-size]
             [kmi.protocol.ishow :as p-show]
             [kmi.lang.interface-spec :as spec]
             [kmi.lang.interface-common :as interface-common]
             [kmi.lang.interface-collection :as interface-collection]
             [kmi.lang.type-vector-node :as node]]})

(defn.xt pair-new
  "creates a pair new"
  {:added "4.0"}
  [key val protocol]
  (var pair {"::" "pair"
             :_key key
             :_val val})
  (return (spec/runtime-attach pair protocol)))

(def.xt PAIR_SPEC
   [[p-coll/IColl   {:_start_string  "["
                     :_end_string    "]"
                     :_sep_string    ", "
                     :_is_ordered    true
                     :to-iter  (fn:> [e] (it/iter [(. e _key)
                                                   (. e _val)]))
                     :to-array (fn:> [e] [(. e _key)
                                          (. e _val)])}]
   [p-eq/IEq     {:eq     interface-collection/coll-eq}]
   [p-hash/IHash   {:hash   (interface-common/wrap-with-cache
                             interface-collection/coll-hash-ordered)}]
   [p-nth/INth    {:nth    (fn:> [e i]
                              (:? (== i 0)
                                  (. e _key)
                                  (== i 1)
                                  (. e _val)
                                  :else nil))}]
   [p-size/ISize   {:size   (fn:> [e] 2)}]
   [p-show/IShow   {:show   interface-collection/coll-show}]])

(def.xt PAIR_PROTOTYPE
  (-> -/PAIR_SPEC
      (proto/proto-spec)
      (spec/proto-create)))

(defn.xt pair
  "creates a pair"
  {:added "4.0"}
  [key val]
  (return (-/pair-new key val -/PAIR_PROTOTYPE)))
