(ns kmi.protocol.named
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.namespaced :as p-namespaced]]})

(def.xt INamed
  (proto/iface-combine [p-namespaced/INamespaced]))
