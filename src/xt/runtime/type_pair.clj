(ns xt.runtime.type-pair
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]
             [xt.runtime.interface-spec :as spec]
             [xt.runtime.interface-common :as interface-common]
             [xt.runtime.interface-collection :as interface-collection]
             [xt.runtime.type-vector-node :as node]]})

(defn.xt pair-new
  "creates a pair new"
  {:added "4.0"}
  [key val protocol]
  (var pair {"::" "pair"
             :_key key
             :_val val})
  (k/set-proto pair protocol)
  (return pair))

(def.xt PAIR_SPEC
  [[spec/IColl   {:_start_string  "["
                  :_end_string    "]"
                  :_sep_string    ", "
                  :_is_ordered    true
                  :to-iter  (fn:> [e] (it/iter-from-arr [(. e _key)
                                                         (. e _val)]))
                  :to-array (fn:> [e] [(. e _key)
                                       (. e _val)])}]
   [spec/IEq     {:eq     interface-collection/coll-eq}]    
   [spec/IHash   {:hash   (interface-common/wrap-with-cache
                           interface-collection/coll-hash-ordered)}]
   [spec/INth    {:nth    (fn:> [e i]
                            (:? (== i 0)
                                (. e _key)
                                (== i 1)
                                (. e _val)
                                :else nil))}]
   [spec/ISize   {:size   (fn:> [e] 2)}]
   [spec/IShow   {:show   interface-collection/coll-show}]])

(def.xt PAIR_PROTOTYPE
  (-> -/PAIR_SPEC
      (k/proto-spec)
      (k/proto-create)))

(defn.xt pair
  "creates a pair"
  {:added "4.0"}
  [key val]
  (return (-/pair-new key val -/PAIR_PROTOTYPE)))

