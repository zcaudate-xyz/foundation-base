(ns kmi.protocol.stack-mutable
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.push-mutable :as p-push-mutable] [kmi.protocol.pop-mutable :as p-pop-mutable]]})

(def.xt IStackMutable
  (proto/iface-combine [p-push-mutable/IPushMutable p-pop-mutable/IPopMutable]))
