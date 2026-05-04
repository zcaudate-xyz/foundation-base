(ns kmi.protocol.lisp-sequential
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.lisp-scalar :as p-lisp-scalar] [kmi.protocol.sequential :as p-sequential] [kmi.protocol.stack :as p-stack] [kmi.protocol.seqable :as p-seqable] [kmi.protocol.seq :as p-seq] [kmi.protocol.conj :as p-conj] [kmi.protocol.cons :as p-cons] [kmi.protocol.peek :as p-peek]]})

(def.xt ILispSequential
  (proto/iface-combine [p-lisp-scalar/ILispScalar p-sequential/ISequential p-stack/IStack p-seqable/ISeqable p-seq/ISeq p-conj/IConj p-cons/ICons p-peek/IPeek]))
