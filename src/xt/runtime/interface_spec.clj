(ns xt.runtime.interface-spec
  (:require [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-iter :as it]]})

(defn.js proto-create
  "creates a prototype map from a spec map"
  {:added "4.1"}
  [spec-map]
  (var out {})
  (xt/for:object [[k f] spec-map]
    (if (xt/x:is-function? f)
      (xt/x:set-key out k
                    (fn [a b c d]
                      (return (f this a b c d))))
      (xt/x:set-key out k f)))
  (return out))

(l/script :lua
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-iter :as it]]})

(defn.lua proto-create
  "creates a prototype map from a spec map"
  {:added "4.1"}
  [spec-map]
  (xt/x:set-key spec-map "__index" spec-map)
  (return spec-map))

(l/script :python
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-iter :as it]]})

(defn.py proto-create
  "creates a prototype map from a spec map"
  {:added "4.1"}
  [spec-map]
  (xt/x:set-key spec-map "__index" spec-map)
  (return spec-map))

(defn.js runtime-attach
  "attaches runtime dispatch using native JS prototype linkage"
  {:added "4.1"}
  [obj protocol]
  (when protocol
    (Object.setPrototypeOf obj protocol))
  (return obj))

(defn.js runtime-protocol
  "gets runtime dispatch from a managed JS object"
  {:added "4.1"}
  [obj]
  (return (Object.getPrototypeOf obj)))

(defn.lua runtime-attach
  "attaches runtime dispatch using native Lua metatables"
  {:added "4.1"}
  [obj protocol]
  (when protocol
    (setmetatable obj protocol))
  (return obj))

(defn.lua runtime-protocol
  "gets runtime dispatch from a managed Lua object"
  {:added "4.1"}
  [obj]
  (return (getmetatable obj)))

(defn.py runtime-attach
  "attaches runtime dispatch for Python-managed objects"
  {:added "4.1"}
  [obj protocol]
  (xt/x:set-key obj "_rt_protocol" protocol)
  (return obj))

(defn.py runtime-protocol
  "gets runtime dispatch from a managed Python object"
  {:added "4.1"}
  [obj]
  (return (xt/x:get-key obj "_rt_protocol")))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-iter :as it]
             [xt.lang.common-data :as xtd]]})

;;
;; Primitive runtime hooks
;;

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

(defn.xt iface-combine
  "combines interface vectors into a stable, deduped protocol surface"
  {:added "4.1"}
  [interfaces]
  (var seen {})
  (var out [])
  (xt/for:array [iface interfaces]
    (xt/for:array [key iface]
      (when (xt/x:nil? (xt/x:get-key seen key))
        (xt/x:set-key seen key true)
        (xt/x:arr-push out key))))
  (return out))

(defn.xt proto-group
  "creates a grouped protocol entry from interface vectors and an implementation map"
  {:added "4.1"}
  [interfaces spec-map]
  (return [(-/iface-combine interfaces) spec-map]))

;;
;; Immutable Lisp-facing groupings
;;

(def.xt ICounted
  (-/iface-combine [-/ISize]))

(def.xt INamed
  (-/iface-combine [-/INamespaced]))

(def.xt IMapEntry
  (-/iface-combine [-/IPair]))

(def.xt IValue
  (-/iface-combine [-/IEq
                    -/IHash
                    -/IShow]))

(def.xt ILookupable
  (-/iface-combine [-/ILookup
                    -/IFind]))

(def.xt IAssociative
  (-/iface-combine [-/IAssoc
                    -/IDissoc
                    -/ILookup
                    -/IFind]))

(def.xt IAssociativeMutable
  (-/iface-combine [-/IAssocMutable
                    -/IDissocMutable]))

(def.xt ICollection
  (-/iface-combine [-/IColl
                    -/IEmpty
                    -/ISize]))

(def.xt ISequential
  (-/iface-combine [-/IColl
                    -/IEmpty
                    -/ISize
                    -/INth]))

(def.xt IStack
  (-/iface-combine [-/IPush
                    -/IPop]))

(def.xt IStackMutable
  (-/iface-combine [-/IPushMutable
                    -/IPopMutable]))

(def.xt IPersistent
  (-/iface-combine [-/IEdit]))

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
  (-/iface-combine [-/IValue
                    -/IMeta]))

(def.xt ILispNamed
  (-/iface-combine [-/ILispScalar
                    -/INamed]))

(def.xt ILispSequential
  (-/iface-combine [-/ILispScalar
                    -/ISequential
                    -/IStack
                    -/ISeqable
                    -/ISeq
                    -/IConj
                    -/ICons
                    -/IPeek]))

(def.xt ILispAssociative
  (-/iface-combine [-/ILispScalar
                    -/ICollection
                    -/IAssociative
                    -/ISeqable
                    -/ISeq]))

(def.xt ILispPersistent
  (-/iface-combine [-/IPersistent
                    -/IAssociativeMutable
                    -/IStackMutable]))

(defn.xt proto-spec
  "creates a prototype spec map from interface entries"
  {:added "4.0"}
  [spec-arr]
  (var acc {})
  (xt/for:array [e spec-arr]
    (var spec-i (xt/x:first e))
    (var spec-map (xt/x:second e))
    (xt/for:array [key spec-i]
      (when (xt/x:nil? (xt/x:get-key spec-map key))
        (xt/x:err
         (xt/x:cat "NOT VALID."
                   (xt/x:json-encode {:required key
                                      :actual (xt/x:obj-keys spec-map)})))))
     (:= acc (xt/x:obj-assign acc spec-map)))
  (return acc))

(defabstract.xt proto-create
  [spec-map])

(defabstract.xt runtime-attach
  [obj protocol])

(defabstract.xt runtime-protocol
  [obj])
