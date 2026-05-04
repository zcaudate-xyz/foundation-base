(ns kmi.protocol.stack
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.push :as p-push] [kmi.protocol.pop :as p-pop]]})

(def.xt IStack
  (proto/iface-combine [p-push/IPush p-pop/IPop]))
