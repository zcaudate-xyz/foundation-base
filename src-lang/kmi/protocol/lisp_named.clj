(ns kmi.protocol.lisp-named
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.lisp-scalar :as p-lisp-scalar] [kmi.protocol.named :as p-named]]})

(def.xt ILispNamed
  (proto/iface-combine [p-lisp-scalar/ILispScalar p-named/INamed]))
