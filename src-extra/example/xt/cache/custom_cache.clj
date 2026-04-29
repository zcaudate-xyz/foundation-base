(ns example.xt.cache.custom-cache
  (:require [std.lang :as l])
  (:refer-clojure :exclude [get set]))

(l/script :xtalk
  {:implements example.xt.protocol.cache
   :require [[xt.lang.spec-base :as xt]]})

(defn.xt get
  "xtalk example cache lookup"
  {:added "4.1"}
  [cache key]
  (return (xt/x:get-key cache key)))

(defn.xt set
  "xtalk example cache write"
  {:added "4.1"}
  [cache key value]
  (xt/x:set-key cache key value)
  (return cache))
