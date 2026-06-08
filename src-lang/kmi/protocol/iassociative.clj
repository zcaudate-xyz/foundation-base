(ns kmi.protocol.iassociative
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto]
             [kmi.protocol.iassoc :as p-assoc]
             [kmi.protocol.idissoc :as p-dissoc]
             [kmi.protocol.ilookup :as p-lookup]
             [kmi.protocol.ifind :as p-find]]})

(def.xt IAssociative
  (proto/iface-combine [p-assoc/IAssoc
                        p-dissoc/IDissoc
                        p-lookup/ILookup
                        p-find/IFind]))
