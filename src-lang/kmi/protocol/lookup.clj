(ns kmi.protocol.lookup
  (:require [hara.lang :as l]))

(l/script :xtalk)

(def.xt ILookup ["keys"
                "vals"
                "lookup"])
