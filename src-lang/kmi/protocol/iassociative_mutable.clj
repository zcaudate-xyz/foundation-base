(ns kmi.protocol.iassociative-mutable
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.iassoc-mutable :as p-assoc-mutable] [kmi.protocol.idissoc-mutable :as p-dissoc-mutable]]})

(def.xt IAssociativeMutable
  (proto/iface-combine [p-assoc-mutable/IAssocMutable p-dissoc-mutable/IDissocMutable]))
