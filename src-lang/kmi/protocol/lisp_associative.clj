(ns kmi.protocol.lisp-associative
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.lisp-scalar :as p-lisp-scalar] [kmi.protocol.collection :as p-collection] [kmi.protocol.associative :as p-associative] [kmi.protocol.seqable :as p-seqable] [kmi.protocol.seq :as p-seq]]})

(def.xt ILispAssociative
  (proto/iface-combine [p-lisp-scalar/ILispScalar p-collection/ICollection p-associative/IAssociative p-seqable/ISeqable p-seq/ISeq]))
