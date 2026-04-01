(ns xt.lang.common-lib
  (:require [std.lang :as l :refer [defspec.xt]]
            [std.lib.foundation :as f]
            [clojure.string :as str]
            [std.lang.base.grammar-xtalk :as xtalk])
  (:refer-clojure :exclude [identity fn? cat print]))

(l/script :xtalk)

(f/template-entries [xtalk/tmpl-fragment-fn]
  xtalk/+xt-common-basic+
  xtalk/+xt-common-index+
  xtalk/+xt-common-nil+
  xtalk/+xt-common-number+
  xtalk/+xt-common-primitives+
  xtalk/+xt-common-print+)


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

(defn.xt type-class
  "gets the type of an object"
  {:added "4.1"}
  ([x]
   (var ntype (-/type-native x))
   (if (== ntype "object")
     (return (x:get-key x "::" ntype))
     (return ntype))))
