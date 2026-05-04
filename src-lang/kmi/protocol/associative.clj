(ns kmi.protocol.associative
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.assoc :as p-assoc] [kmi.protocol.dissoc :as p-dissoc] [kmi.protocol.lookup :as p-lookup] [kmi.protocol.find :as p-find]]})

(def.xt IAssociative
  (proto/iface-combine [p-assoc/IAssoc p-dissoc/IDissoc p-lookup/ILookup p-find/IFind]))
