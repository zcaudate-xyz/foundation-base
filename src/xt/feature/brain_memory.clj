(ns xt.feature.brain-memory
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.protocol.cache :as cache]]})

(defn.xt brain-get
  "reads from the configured cache backend"
  {:added "4.1"}
  [store key]
  (return (cache/get store key)))

(defn.xt brain-set
  "writes to the configured cache backend"
  {:added "4.1"}
  [store key value]
  (return (cache/set store key value)))

(defn.xt brain-list
  "lists keys from the configured cache backend"
  {:added "4.1"}
  [store]
  (return (cache/list store)))

(defn.xt brain-swap
  "updates a cache entry with the configured backend"
  {:added "4.1"}
  [store key f args]
  (return (cache/swap store key f args)))
