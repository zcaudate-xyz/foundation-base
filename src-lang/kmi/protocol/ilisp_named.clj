(ns kmi.protocol.ilisp-named
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.ilisp-scalar :as p-lisp-scalar] [kmi.protocol.inamed :as p-named]]})

(def.xt ILispNamed
  (proto/iface-combine [p-lisp-scalar/ILispScalar p-named/INamed]))
