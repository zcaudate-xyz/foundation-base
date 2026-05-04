(ns kmi.protocol.imap-entry
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.ipair :as p-pair]]})

(def.xt IMapEntry
  (proto/iface-combine [p-pair/IPair]))
