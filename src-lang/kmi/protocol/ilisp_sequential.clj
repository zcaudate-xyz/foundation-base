(ns kmi.protocol.ilisp-sequential
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto] [kmi.protocol.ilisp-scalar :as p-lisp-scalar] [kmi.protocol.isequential :as p-sequential] [kmi.protocol.istack :as p-stack] [kmi.protocol.iseqable :as p-seqable] [kmi.protocol.iseq :as p-seq] [kmi.protocol.iconj :as p-conj] [kmi.protocol.icons :as p-cons] [kmi.protocol.ipeek :as p-peek]]})

(def.xt ILispSequential
  (proto/iface-combine [p-lisp-scalar/ILispScalar p-sequential/ISequential p-stack/IStack p-seqable/ISeqable p-seq/ISeq p-conj/IConj p-cons/ICons p-peek/IPeek]))
