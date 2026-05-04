(ns kmi.protocol.lisp-scalar
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.value :as p-value] [kmi.protocol.meta :as p-meta]]})

(def.xt ILispScalar
  (proto/iface-combine [p-value/IValue p-meta/IMeta]))
