(ns example.xt.protocol.cache
  (:require [std.lang :as l])
  (:refer-clojure :exclude [get set]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(defn.xt get
  "abstract cache lookup for the example contract"
  {:added "4.1"}
  [cache key]
  (xt/x:err "example.xt.protocol.cache/get is abstract"))

(defn.xt set
  "abstract cache write for the example contract"
  {:added "4.1"}
  [cache key value]
  (xt/x:err "example.xt.protocol.cache/set is abstract"))
