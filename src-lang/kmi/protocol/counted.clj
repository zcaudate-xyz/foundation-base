(ns kmi.protocol.counted
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.size :as p-size]]})

(def.xt ICounted
  (proto/iface-combine [p-size/ISize]))
