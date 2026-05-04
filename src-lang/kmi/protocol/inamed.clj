(ns kmi.protocol.inamed
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.inamespaced :as p-namespaced]]})

(def.xt INamed
  (proto/iface-combine [p-namespaced/INamespaced]))
