(ns example.xt.feature.memory-brain
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[example.xt.protocol.cache :as cache]]})

(defn.xt memory-get
  "reads memory from the configured example cache backend"
  {:added "4.1"}
  [store key]
  (return (cache/get store key)))

(defn.xt memory-set
  "writes memory to the configured example cache backend"
  {:added "4.1"}
  [store key value]
  (return (cache/set store key value)))
