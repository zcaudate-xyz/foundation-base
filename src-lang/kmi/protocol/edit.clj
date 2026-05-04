(ns kmi.protocol.edit
  (:require [hara.lang :as l]))

(l/script :xtalk)

(def.xt IEdit ["is_mutable"
                "to_mutable"
                "is_persistent"
                "to_persistent"])
