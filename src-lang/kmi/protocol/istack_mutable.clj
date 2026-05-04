(ns kmi.protocol.istack-mutable
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.ipush-mutable :as p-push-mutable] [kmi.protocol.ipop-mutable :as p-pop-mutable]]})

(def.xt IStackMutable
  (proto/iface-combine [p-push-mutable/IPushMutable p-pop-mutable/IPopMutable]))
