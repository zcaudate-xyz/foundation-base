(ns kmi.lang.common-coll
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-iter :as it]
             [kmi.lang.common-hash :as common-hash]
             [kmi.lang.common-util :as common-util]
             [kmi.lang.protocol-base :as p]]})

(defn.xt start-string
  "TODO"
  {:added "4.0"}
  [coll]
  (return (xt/x:get-key coll "_start_string")))

(defn.xt end-string
  "TODO"
  {:added "4.0"}
  [coll]
  (return (xt/x:get-key coll "_end_string")))

(defn.xt sep-string
  "TODO"
  {:added "4.0"}
  [coll]
  (return (xt/x:get-key coll "_sep_string")))

(defn.xt is-ordered?
  "TODO"
  {:added "4.0"}
  [coll]
  (return (xt/x:get-key coll "_is_ordered")))

(defn.xt coll-reduce
  [coll f init]
  (return
   (it/collect (p/to-iter coll)
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
    (p/to-iter coll)
    common-util/hash)))

(defn.xt coll-hash-unordered
  "TODO"
  {:added "4.0"}
  [coll]
  (return
   (common-hash/hash-iter-unordered
    (p/to-iter coll)
    common-util/hash)))

(defn.xt coll-show-with
  "TODO"
  {:added "4.0"}
  [coll start-string end-string sep-string]
  (var s    start-string)
  (if (== 0 (-/coll-size coll))
    (return (xt/x:cat s
                      end-string))
    (do (xt/for:iter [e (p/to-iter coll)]
          (:= s (xt/x:cat s
                          (common-util/show e)
                          sep-string)))
        (return (xt/x:cat (xt/x:str-substring s 0 (- (xt/x:len s)
                                                     (xt/x:len sep-string)))
                          end-string)))))

(defn.xt coll-show
  "TODO"
  {:added "4.0"}
  [coll]
  (return (-/coll-show-with coll
                            (-/start-string coll)
                            (-/end-string coll)
                            (-/sep-string coll))))

(defn.xt coll-into-iter
  "TODO"
  {:added "4.0"}
  [coll iter]
  (var mutable (p/is-mutable coll))
  (var ncoll
       (it/collect iter
                   p/push-mutable
                   (p/to-mutable coll)))
  (if mutable
    (return ncoll)
    (return (p/to-persistent ncoll))))

(defn.xt coll-into-array
  "TODO"
  {:added "4.0"}
  [coll arr]
  (var mutable (p/is-mutable coll))
  (var ncoll
       (xt/x:arr-foldl arr
                    p/push-mutable
                    (p/to-mutable coll)))
  (if mutable
    (return ncoll)
    (return (p/to-persistent ncoll))))

(defn.xt coll-eq
  "TODO"
  {:added "4.0"}
  [o1 o2]
  (return
   (it/iter-eq (p/to-iter o1)
               (p/to-iter o2)
               p/eq)))
