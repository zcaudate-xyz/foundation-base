(ns xt.lang.common-lib
  (:require [std.lang :as l :refer [defspec.xt]]
            [std.lang.base.grammar-xtalk :as xtalk])
  (:refer-clojure :exclude [identity fn?]))

(l/script :xtalk)

(def$.xt del x:del)
(def$.xt cat x:cat)
(def$.xt len x:len)
(def$.xt err x:err)
(def$.xt ^{:arglists '([value])} type-native x:type-native)
(def$.xt ^{:arglists '([value])} to-string x:to-string)
(def$.xt ^{:arglists '([value])} to-number x:to-number)
(def$.xt ^{:arglists '([value])} is-string? x:is-string?)
(def$.xt ^{:arglists '([value])} is-number? x:is-number?)
(def$.xt ^{:arglists '([value])} is-integer? x:is-integer?)
(def$.xt ^{:arglists '([value])} is-boolean? x:is-boolean?)
(def$.xt ^{:arglists '([lookup key] [lookup key default])} lu-get x:lu-get)
(def$.xt ^{:arglists '([lookup key value])} lu-set x:lu-set)
(def$.xt ^{:arglists '([lookup key])} lu-del x:lu-del)
(def$.xt ^{:arglists '([obj key] [obj key present?])} has-key? x:has-key?)
(def$.xt ^{:arglists '([value])} print x:print)

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
