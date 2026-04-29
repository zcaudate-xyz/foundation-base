(ns example.python.cache.localstore
  (:require [std.lang :as l])
  (:refer-clojure :exclude [get set]))

(l/script :python
  {:implements example.xt.protocol.cache})

(defn.py get
  "python example cache lookup"
  {:added "4.1"}
  [cache key]
  (return (. cache [key])))

(defn.py set
  "python example cache write"
  {:added "4.1"}
  [cache key value]
  (:= (. cache [key]) value)
  (return cache))
