(ns example.js.cache.localstore
  (:require [std.lang :as l])
  (:refer-clojure :exclude [get set]))

(l/script :js
  {:implements example.xt.protocol.cache})

(defn.js get
  "javascript example cache lookup"
  {:added "4.1"}
  [cache key]
  (return (. cache [key])))

(defn.js set
  "javascript example cache write"
  {:added "4.1"}
  [cache key value]
  (:= (. cache [key]) value)
  (return cache))
