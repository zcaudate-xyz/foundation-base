(ns kmi.protocol.seq
  (:require [hara.lang :as l]))

(l/script :xtalk)

(def.xt ISeq ["first"
                "rest"
                "next"])
