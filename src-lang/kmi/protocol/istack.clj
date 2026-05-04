(ns kmi.protocol.istack
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.ipush :as p-push] [kmi.protocol.ipop :as p-pop]]})

(def.xt IStack
  (proto/iface-combine [p-push/IPush p-pop/IPop]))
