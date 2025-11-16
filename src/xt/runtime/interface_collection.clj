^{:no-test true}
(ns xt.runtime.interface-collection
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]
             [xt.runtime.common-hash :as common-hash]
             [xt.runtime.interface-common :as interface-common]]})

(defn.xt start-string
  "TODO"
  {:added "4.0"}
  [coll]
  (return (. coll _start_string)))

(defn.xt end-string
  "TODO"
  {:added "4.0"}
  [coll]
  (return (. coll _end_string)))

(defn.xt sep-string
  "TODO"
  {:added "4.0"}
  [coll]
  (return (. coll _sep_string)))

(defn.xt is-ordered?
  "TODO"
  {:added "4.0"}
  [coll]
  (return (. coll _is_ordered)))

(defn.xt coll-reduce
  [coll f init]
  (return
   (it/collect (. coll (to-iter))
               f
               init)))

(defn.xt coll-size
  "TODO"
  {:added "4.0"}
  [coll]
  (var #{_size} coll)
  (return _size))

(defn.xt coll-hash-ordered
  "TODO"
  {:added "4.0"}
  [coll]
  (return
   (common-hash/hash-iter
    (. coll (to-iter))
    interface-common/hash)))

(defn.xt coll-hash-unordered
  "TODO"
  {:added "4.0"}
  [coll]
  (return
   (common-hash/hash-iter-unordered
    (. coll (to-iter))
    interface-common/hash)))

(defn.xt coll-show
  "TODO"
  {:added "4.0"}
  [coll]
  (var s    (-/start-string coll))
  (var sep  (-/sep-string coll))
  (it/for:iter [e (. coll (to-iter))]
    
    (:= s (k/cat s
                 (interface-common/show e)
                 sep)))
  (return (k/cat (k/substring s 0 (- (k/len s)
                                     (k/len sep)))
                 (-/end-string coll))))

(defn.xt coll-into-iter
  "TODO"
  {:added "4.0"}
  [coll iter]
  (var mutable (interface-common/is-mutable? coll))
  (var ncoll
       (it/collect iter
                   interface-common/push-mutable
                   (interface-common/to-mutable coll)))
  (if mutable
    (return ncoll)
    (return (interface-common/to-persistent ncoll))))

(defn.xt coll-into-array
  "TODO"
  {:added "4.0"}
  [coll arr]
  (var mutable (interface-common/is-mutable? coll))
  (var ncoll
       (k/arr-foldl arr
                    interface-common/push-mutable
                    (interface-common/to-mutable coll)))
  (if mutable
    (return ncoll)
    (return (interface-common/to-persistent ncoll))))

(defn.xt coll-eq
  "TODO"
  {:added "4.0"}
  [o1 o2]
  )


(def.xt IColl
  ["start_string"
   "end_string"
   "sep_string"])

