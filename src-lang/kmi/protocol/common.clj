(ns kmi.protocol.common
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto]]})

(def.xt IAssoc  ["assoc"])

(def.xt IAssocMutable  ["assoc_mutable"])

(def.xt IDissoc ["dissoc"])

(def.xt IDissocMutable ["dissoc_mutable"])

(def.xt IColl   ["to_iter"
                 "to_array"])

(def.xt IEdit   ["is_mutable"
                 "to_mutable"
                 "is_persistent"
                 "to_persistent"])

(def.xt IFind   ["find"])

(def.xt INth    ["nth"])

(def.xt IPush   ["push"])

(def.xt IPop    ["pop"])

(def.xt IPushMutable   ["push_mutable"])

(def.xt IPopMutable    ["pop_mutable"])

(def.xt ISize   ["size"])

(def.xt IHash   ["hash"])

(def.xt IEmpty  ["empty"])

(def.xt IEq     ["eq"])

(def.xt IIndexed ["index_of"])

(def.xt IIndexedKV ["index_of_key"
                    "index_of_val"])

(def.xt ILookup  ["keys"
                  "vals"
                  "lookup"])

(def.xt INamespaced  ["name"
                      "namespace"])

(def.xt IPair        ["key"
                      "val"])

(def.xt IShow    ["show"])

(def.xt ICounted
  (proto/iface-combine [-/ISize]))

(def.xt INamed
  (proto/iface-combine [-/INamespaced]))

(def.xt IMapEntry
  (proto/iface-combine [-/IPair]))

(def.xt IValue
  (proto/iface-combine [-/IEq
                        -/IHash
                        -/IShow]))

(def.xt ILookupable
  (proto/iface-combine [-/ILookup
                        -/IFind]))

(def.xt IAssociative
  (proto/iface-combine [-/IAssoc
                        -/IDissoc
                        -/ILookup
                        -/IFind]))

(def.xt IAssociativeMutable
  (proto/iface-combine [-/IAssocMutable
                        -/IDissocMutable]))

(def.xt ICollection
  (proto/iface-combine [-/IColl
                        -/IEmpty
                        -/ISize]))

(def.xt ISequential
  (proto/iface-combine [-/IColl
                        -/IEmpty
                        -/ISize
                        -/INth]))

(def.xt IStack
  (proto/iface-combine [-/IPush
                        -/IPop]))

(def.xt IStackMutable
  (proto/iface-combine [-/IPushMutable
                        -/IPopMutable]))

(def.xt IPersistent
  (proto/iface-combine [-/IEdit]))

(def.xt ISeqable
  ["seq"])

(def.xt ISeq
  ["first"
   "rest"
   "next"])

(def.xt IConj
  ["conj"])

(def.xt ICons
  ["cons"])

(def.xt IPeek
  ["peek"])

(def.xt IReduce
  ["reduce"])

(def.xt IMeta
  ["meta"
   "with_meta"])

(def.xt IInvokable
  ["invoke"])

(def.xt ILispScalar
  (proto/iface-combine [-/IValue
                        -/IMeta]))

(def.xt ILispNamed
  (proto/iface-combine [-/ILispScalar
                        -/INamed]))

(def.xt ILispSequential
  (proto/iface-combine [-/ILispScalar
                        -/ISequential
                        -/IStack
                        -/ISeqable
                        -/ISeq
                        -/IConj
                        -/ICons
                        -/IPeek]))

(def.xt ILispAssociative
  (proto/iface-combine [-/ILispScalar
                        -/ICollection
                        -/IAssociative
                        -/ISeqable
                        -/ISeq]))

(def.xt ILispPersistent
  (proto/iface-combine [-/IPersistent
                        -/IAssociativeMutable
                        -/IStackMutable]))
