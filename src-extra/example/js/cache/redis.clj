(ns example.js.cache.redis
  (:require [std.lang :as l])
  (:refer-clojure :exclude [get set]))

(l/script :js
  {:implements example.xt.protocol.cache})

(defn.js get
  "javascript redis-flavoured cache lookup"
  {:added "4.1"}
  [cache key]
  (return (. cache [key])))

(defn.js set
  "javascript redis-flavoured cache write"
  {:added "4.1"}
  [cache key value]
  (:= (. cache [key]) value)
  (return cache))
