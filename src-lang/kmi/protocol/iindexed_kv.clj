(ns kmi.protocol.iindexed-kv
  (:require [hara.lang :as l]))

(l/script :xtalk)

(def.xt IIndexedKV ["index_of_key"
                "index_of_val"])
