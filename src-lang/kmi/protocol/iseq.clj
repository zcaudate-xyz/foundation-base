(ns kmi.protocol.iseq
  (:require [hara.lang :as l]))

(l/script :xtalk)

(def.xt ISeq ["first"
                "rest"
                "next"])
