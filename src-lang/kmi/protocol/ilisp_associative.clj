(ns kmi.protocol.ilisp-associative
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.ilisp-scalar :as p-lisp-scalar] [kmi.protocol.icollection :as p-collection] [kmi.protocol.iassociative :as p-associative] [kmi.protocol.iseqable :as p-seqable] [kmi.protocol.iseq :as p-seq]]})

(def.xt ILispAssociative
  (proto/iface-combine [p-lisp-scalar/ILispScalar p-collection/ICollection p-associative/IAssociative p-seqable/ISeqable p-seq/ISeq]))
