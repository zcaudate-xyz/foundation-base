(ns kmi.protocol.value
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.eq :as p-eq] [kmi.protocol.hash :as p-hash] [kmi.protocol.show :as p-show]]})

(def.xt IValue
  (proto/iface-combine [p-eq/IEq p-hash/IHash p-show/IShow]))
