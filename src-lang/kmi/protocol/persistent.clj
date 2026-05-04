(ns kmi.protocol.persistent
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.edit :as p-edit]]})

(def.xt IPersistent
  (proto/iface-combine [p-edit/IEdit]))
