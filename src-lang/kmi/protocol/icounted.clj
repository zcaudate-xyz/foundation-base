(ns kmi.protocol.icounted
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.isize :as p-size]]})

(def.xt ICounted
  (proto/iface-combine [p-size/ISize]))
