(ns kmi.protocol.collection
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.coll :as p-coll] [kmi.protocol.empty :as p-empty] [kmi.protocol.size :as p-size]]})

(def.xt ICollection
  (proto/iface-combine [p-coll/IColl p-empty/IEmpty p-size/ISize]))
