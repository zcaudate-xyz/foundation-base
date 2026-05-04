(ns kmi.protocol.lisp-persistent
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.persistent :as p-persistent] [kmi.protocol.associative-mutable :as p-associative-mutable] [kmi.protocol.stack-mutable :as p-stack-mutable]]})

(def.xt ILispPersistent
  (proto/iface-combine [p-persistent/IPersistent p-associative-mutable/IAssociativeMutable p-stack-mutable/IStackMutable]))
