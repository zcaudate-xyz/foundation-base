(ns kmi.protocol.map-entry
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.pair :as p-pair]]})

(def.xt IMapEntry
  (proto/iface-combine [p-pair/IPair]))
