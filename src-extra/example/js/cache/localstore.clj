(ns example.js.cache.localstore
  (:require [std.lang :as l])
  (:refer-clojure :exclude [get set]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]]})

(defn.js get
  "javascript example cache lookup"
  {:added "4.1"}
  [cache key]
  (return (xt/x:get-key cache key)))

(defn.js set
  "javascript example cache write"
  {:added "4.1"}
  [cache key value]
  (xt/x:set-key cache key value)
  (return cache))
