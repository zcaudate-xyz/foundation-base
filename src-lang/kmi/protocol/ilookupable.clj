(ns kmi.protocol.ilookupable
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.ilookup :as p-lookup] [kmi.protocol.ifind :as p-find]]})

(def.xt ILookupable
  (proto/iface-combine [p-lookup/ILookup p-find/IFind]))
