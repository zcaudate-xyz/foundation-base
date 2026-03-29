(ns xt.lang.common-base
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]])
  (:refer-clojure :exclude [abs bit-and bit-or bit-xor type get-in identity inc
                            dec zero? pos? neg? even? odd? max min mod quot
                            cat eval apply print nil? fn? first second nth
                            replace last sort sort-by throw]))

(l/script :xtalk
  {:require [[xt.lang.base-macro :as k]]})

(l/intern-macros :xtalk 'xt.lang.base-macro)

(defspec.xt AnyDict
  [:xt/dict :xt/str :xt/any])

(defspec.xt proto-create
  [:fn [AnyDict] AnyDict])

(defspec.xt type-native
  [:fn [:xt/any] :xt/str])

(defspec.xt type-class
  [:fn [:xt/any] :xt/str])

(defspec.xt fn?
  [:fn [:xt/any] :xt/bool])

(defspec.xt arr?
  [:fn [:xt/any] :xt/bool])

(defspec.xt obj?
  [:fn [:xt/any] :xt/bool])

(defspec.xt identity
  [:fn [:xt/any] :xt/any])

(defspec.xt noop
  [:fn [] :xt/nil])

(defspec.xt T
  [:fn [:xt/any] :xt/bool])

(defspec.xt F
  [:fn [:xt/any] :xt/bool])

(defn.xt proto-create
  "creates the prototype map"
  {:added "4.1"}
  [m]
  (return (x:proto-create m)))

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
