(ns kmi.protocol.sequential
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.coll :as p-coll] [kmi.protocol.empty :as p-empty] [kmi.protocol.size :as p-size] [kmi.protocol.nth :as p-nth]]})

(def.xt ISequential
  (proto/iface-combine [p-coll/IColl p-empty/IEmpty p-size/ISize p-nth/INth]))
