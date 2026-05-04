(ns kmi.protocol.ivalue
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.ieq :as p-eq] [kmi.protocol.ihash :as p-hash] [kmi.protocol.ishow :as p-show]]})

(def.xt IValue
  (proto/iface-combine [p-eq/IEq p-hash/IHash p-show/IShow]))
