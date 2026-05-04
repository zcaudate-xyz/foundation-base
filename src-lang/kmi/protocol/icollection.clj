(ns kmi.protocol.icollection
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.icoll :as p-coll] [kmi.protocol.iempty :as p-empty] [kmi.protocol.isize :as p-size]]})

(def.xt ICollection
  (proto/iface-combine [p-coll/IColl p-empty/IEmpty p-size/ISize]))
