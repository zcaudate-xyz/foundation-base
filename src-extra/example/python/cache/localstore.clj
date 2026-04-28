(ns example.python.cache.localstore
  (:require [std.lang :as l])
  (:refer-clojure :exclude [get set]))

(l/script :python
  {:require [[xt.lang.spec-base :as xt]]})

(defn.py get
  "python example cache lookup"
  {:added "4.1"}
  [cache key]
  (return (xt/x:get-key cache key)))

(defn.py set
  "python example cache write"
  {:added "4.1"}
  [cache key value]
  (xt/x:set-key cache key value)
  (return cache))
