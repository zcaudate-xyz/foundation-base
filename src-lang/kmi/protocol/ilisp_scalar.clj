(ns kmi.protocol.ilisp-scalar
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.ivalue :as p-value] [kmi.protocol.imeta :as p-meta]]})

(def.xt ILispScalar
  (proto/iface-combine [p-value/IValue p-meta/IMeta]))
