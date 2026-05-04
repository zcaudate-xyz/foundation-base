(ns kmi.protocol.ilisp-persistent
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.ipersistent :as p-persistent] [kmi.protocol.iassociative-mutable :as p-associative-mutable] [kmi.protocol.istack-mutable :as p-stack-mutable]]})

(def.xt ILispPersistent
  (proto/iface-combine [p-persistent/IPersistent p-associative-mutable/IAssociativeMutable p-stack-mutable/IStackMutable]))
