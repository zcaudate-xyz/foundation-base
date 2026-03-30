(ns xt.lang.common-lib
  (:require [std.lang :as l :refer [defspec.xt]]
            [std.lang.base.grammar-xtalk :as xtalk])
  (:refer-clojure :exclude [identity fn?]))

(l/script :xtalk)

(def$.xt del x:del)
(def$.xt cat x:cat)
(def$.xt len x:len)
(def$.xt err x:err)

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

(defmacro.xt type-native
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
