(ns kmi.protocol.ipersistent
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.iedit :as p-edit]]})

(def.xt IPersistent
  (proto/iface-combine [p-edit/IEdit]))
