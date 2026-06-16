^{:no-test true}
(ns kmi.lang.interface-collection
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-iter :as it]
             [kmi.lang.common-hash :as common-hash]
             [kmi.lang.interface-common :as interface-common]]})

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
   (it/collect (interface-common/to-iter coll)
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
    (interface-common/to-iter coll)
    interface-common/hash)))

(defn.xt coll-hash-unordered
  "TODO"
  {:added "4.0"}
  [coll]
  (return
   (common-hash/hash-iter-unordered
    (interface-common/to-iter coll)
    interface-common/hash)))

(defn.xt coll-show
  "TODO"
  {:added "4.0"}
  [coll]
  (var s    (-/start-string coll))
  (var sep  (-/sep-string coll))
  (if (== 0 (-/coll-size coll))
    (return (xt/x:cat s
                      (-/end-string coll)))
    (do (xt/for:iter [e (interface-common/to-iter coll)]
          (:= s (xt/x:cat s
                          (interface-common/show e)
                          sep)))
        (return (xt/x:cat (xt/x:str-substring s 0 (- (xt/x:len s)
                                                     (xt/x:len sep)))
                          (-/end-string coll))))))

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
       (xt/x:arr-foldl arr
                    interface-common/push-mutable
                    (interface-common/to-mutable coll)))
  (if mutable
    (return ncoll)
    (return (interface-common/to-persistent ncoll))))

(defn.xt coll-eq
  "TODO"
  {:added "4.0"}
  [o1 o2]
  (return
   (it/iter-eq (interface-common/to-iter o1)
               (interface-common/to-iter o2)
               interface-common/eq)))


(def.xt IColl
  ["start_string"
   "end_string"
   "sep_string"])
