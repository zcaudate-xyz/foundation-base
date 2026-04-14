(ns xt.lang.common-sort-by
  (:require [std.lang :as l :refer [defspec.xt]])
  (:refer-clojure :exclude [sort-by]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]]})

;;
;; SORT BY
;;

(defn.xt sort-by
  "sorts arrow by comparator"
  {:added "4.0"}
  [arr inputs]
  (var keys    (xt/x:arr-map inputs (fn [e] (return (:? (xt/x:is-array? e) (xt/x:first e) e)))))
  (var inverts (xt/x:arr-map inputs (fn [e] (return (:? (xt/x:is-array? e) (xt/x:second e) false)))))
  (var get-fn
       (fn [e key]
         (cond (xt/x:is-function? key)
               (return (key e))
               
               :else (return (xt/x:get-key e key)))))
  (var key-fn
       (fn [e]
         (return (xt/x:arr-map keys (fn [key] (return (get-fn e key)))))))
  (var comp-fn
       (fn [a0 a1]
         (xt/for:array [[i v0] a0]
           (var v1 (. a1 [i]))
           (var invert (. inverts [i]))
           (when (not= v0 v1)
             (cond invert
                   (cond (xt/x:is-number? v0)
                         (return (xt/x:lt v1 v0))
                         
                         :else
                         (return (xt/x:str-lt (xt/x:to-string v1)
                                              (xt/x:to-string v0))))

                   :else
                   (cond (xt/x:is-number? v0)
                         (return (xt/x:lt v0 v1))
                         
                         :else
                         (return (xt/x:str-lt (xt/x:to-string v0)
                                              (xt/x:to-string v1)))))))
         (return false)))
  (var out (xt/x:arr-clone arr))
  (xt/x:arr-sort out key-fn comp-fn)
  (return out))
