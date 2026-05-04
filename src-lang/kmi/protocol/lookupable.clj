(ns kmi.protocol.lookupable
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.lookup :as p-lookup] [kmi.protocol.find :as p-find]]})

(def.xt ILookupable
  (proto/iface-combine [p-lookup/ILookup p-find/IFind]))
