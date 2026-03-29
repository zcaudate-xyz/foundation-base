(ns xt.lang.common-lib
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk)

(defspec.xt AnyArray
  [:xt/array :xt/any])

(defspec.xt AnyDict
  [:xt/dict :xt/str :xt/any])

(defspec.xt UnaryFn
  [:fn [:xt/any] :xt/any])

(defspec.xt Predicate
  [:fn [:xt/any] :xt/bool])

(defspec.xt CounterFn
  [:fn [] :xt/num])

(defspec.xt Pair
  [:xt/tuple :xt/str :xt/any])

(defspec.xt id-fn
  [:fn [AnyDict] [:xt/maybe :xt/any]])

(defspec.xt key-fn
  [:fn [:xt/any] UnaryFn])

(defspec.xt eq-fn
  [:fn [:xt/any :xt/any] Predicate])

(defspec.xt inc-fn
  [:fn [[:xt/maybe :xt/num]] CounterFn])

(defspec.xt step-nil
  [:fn [:xt/any :xt/any] :xt/nil])

(defspec.xt step-thrush
  [:fn [:xt/any UnaryFn] :xt/any])

(defspec.xt step-call
  [:fn [UnaryFn :xt/any] :xt/any])

(defspec.xt step-push
  [:fn [AnyArray :xt/any] AnyArray])

(defspec.xt step-set-key
  [:fn [AnyDict :xt/str :xt/any] AnyDict])

(defspec.xt step-set-fn
  [:fn [AnyDict :xt/str] UnaryFn])

(defspec.xt step-set-pair
  [:fn [AnyDict Pair] AnyDict])

(defspec.xt step-del-key
  [:fn [AnyDict :xt/str] AnyDict])


;;
;;
;;


(defn.xt identity
  "identity function"
  {:added "4.1"}
  [x]
  (return x))

(defn.xt noop
  "always a no op"
  {:added "4.1"}
  []
  (return nil))

(defn.xt T
  "always true"
  {:added "4.1"}
  [x]
  (return true))

(defn.xt F
  "always false"
  {:added "4.1"}
  [x]
  (return false))

(defn.xt type-native
  "gets the native type"
  {:added "4.1"}
  [obj]
  (return (x:type-native obj)))

(defn.xt type-class
  "gets the type of an object"
  {:added "4.1"}
  [x]
  (var ntype (-/type-native x))
  (if (== ntype "object")
    (return (x:get-key x "::" ntype))
    (return ntype)))

(defn.xt fn?
  "checks if object is a function type"
  {:added "4.1"}
  [x]
  (return (x:is-function? x)))

(defn.xt arr?
  "checks if object is an array"
  {:added "4.1"}
  [x]
  (return (x:is-array? x)))

(defn.xt obj?
  "checks if object is a map type"
  {:added "4.1"}
  [x]
  (return (x:is-object? x)))

(defn.xt id-fn
  "gets the id for an object"
  {:added "4.1"}
  [x]
  (return (x:get-key x "id")))

(defn.xt key-fn
  "creates a key access function"
  {:added "4.1"}
  [k]
  (return (fn [x] (return (x:get-key x k)))))

(defn.xt eq-fn
  "creates an equality comparator"
  {:added "4.1"}
  [k v]
  (return (fn [x]
            (return
             (:? (x:is-function? v)
                 (v (x:get-key x k))
                 (== v (x:get-key x k)))))))

(defn.xt inc-fn
  "creates an increment function by closure"
  {:added "4.1"}
  [init]
  (var i := init)
  (when (x:nil? i)
    (:= i -1))
  (var inc-fn
       (fn []
         (:= i (+ i 1))
         (return i)))
  (return inc-fn))

(defn.xt step-nil
  "nil step for fold"
  {:added "4.1"}
  [obj pair]
  (return nil))

(defn.xt step-thrush
  "thrush step for fold"
  {:added "4.1"}
  [x f]
  (return (f x)))

(defn.xt step-call
  "call step for fold"
  {:added "4.1"}
  [f x]
  (return (f x)))

(defn.xt step-push
  "step to push element into arr"
  {:added "4.1"}
  [arr e]
  (x:arr-push arr e)
  (return arr))

(defn.xt step-set-key
  "step to set key in object"
  {:added "4.1"}
  [obj k v]
  (x:set-key obj k v)
  (return obj))

(defn.xt step-set-fn
  "creates a set key function"
  {:added "4.1"}
  [obj k]
  (return (fn [v] (return (-/step-set-key obj k v)))))

(defn.xt step-set-pair
  "step to set key value pair in object"
  {:added "4.1"}
  [obj e]
  (x:set-key obj
             (x:arr-first e)
             (x:arr-second e))
  (return obj))

(defn.xt step-del-key
  "step to delete key in object"
  {:added "4.1"}
  [obj k]
  (x:del-key obj k)
  (return obj))
