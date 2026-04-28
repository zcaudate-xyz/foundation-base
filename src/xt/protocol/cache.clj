(ns xt.protocol.cache
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(defn.xt get
  "abstract cache lookup"
  {:added "4.1"}
  [cache key]
  (xt/x:err "xt.protocol.cache/get is abstract"))

(defn.xt set
  "abstract cache write"
  {:added "4.1"}
  [cache key value]
  (xt/x:err "xt.protocol.cache/set is abstract"))

(defn.xt list
  "abstract cache listing"
  {:added "4.1"}
  [cache]
  (xt/x:err "xt.protocol.cache/list is abstract"))

(defn.xt swap
  "abstract cache update"
  {:added "4.1"}
  [cache key f args]
  (xt/x:err "xt.protocol.cache/swap is abstract"))
