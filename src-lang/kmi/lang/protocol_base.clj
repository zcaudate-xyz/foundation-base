(ns kmi.lang.protocol-base
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :as proto])
  (:refer-clojure :exclude [assoc conj cons dissoc empty find first hash key keys meta name namespace next nth peek pop reduce rest seq to-array val vals with-meta])
)

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto]]})

(proto/defprotocol.xt IAssoc (assoc [impl key val]))

(proto/defprotocol.xt IAssocMutable (assoc_mutable [impl key val]))

(proto/defprotocol.xt IColl (to_iter [impl]) (to_array [impl]))

(proto/defprotocol.xt IConj (conj [impl x]))

(proto/defprotocol.xt ICons (cons [impl x]))

(proto/defprotocol.xt IDissoc (dissoc [impl key]))

(proto/defprotocol.xt IDissocMutable (dissoc_mutable [impl key]))

(proto/defprotocol.xt IEdit (is_mutable [impl]) (to_mutable [impl]) (is_persistent [impl]) (to_persistent [impl]))

(proto/defprotocol.xt IEmpty (empty [impl]))

(proto/defprotocol.xt IEq (eq [impl o]))

(proto/defprotocol.xt IFind (find [impl key]))

(proto/defprotocol.xt IHash (hash [impl]))

(proto/defprotocol.xt IIndexed (index_of [impl value]))

(proto/defprotocol.xt IIndexedKV (index_of_key [impl key]) (index_of_val [impl val]))

(proto/defprotocol.xt IInvokable (invoke [impl & args]))

(proto/defprotocol.xt ILookup (keys [impl]) (vals [impl]) (lookup [impl key default-val]))

(proto/defprotocol.xt IMeta (meta [impl]) (with_meta [impl meta]))

(proto/defprotocol.xt INamespaced (name [impl]) (namespace [impl]))

(proto/defprotocol.xt INth (nth [impl idx]))

(proto/defprotocol.xt IPair (key [impl]) (val [impl]))

(proto/defprotocol.xt IPeek (peek [impl]))

(proto/defprotocol.xt IPop (pop [impl]))

(proto/defprotocol.xt IPopMutable (pop_mutable [impl]))

(proto/defprotocol.xt IPush (push [impl x]))

(proto/defprotocol.xt IPushMutable (push_mutable [impl x]))

(proto/defprotocol.xt IReduce (reduce [impl f init]))

(proto/defprotocol.xt ISeq (first [impl]) (rest [impl]) (next [impl]))

(proto/defprotocol.xt ISeqable (seq [impl]))

(proto/defprotocol.xt IShow (show [impl]))

(proto/defprotocol.xt ISize (size [impl]))

(proto/defprotocol.xt IAssociative (assoc [impl key val]) (dissoc [impl key]) (keys [impl]) (vals [impl]) (lookup [impl key default-val]) (find [impl key]))

(proto/defprotocol.xt IAssociativeMutable (assoc_mutable [impl key val]) (dissoc_mutable [impl key]))

(proto/defprotocol.xt ICollection (to_iter [impl]) (to_array [impl]) (empty [impl]) (size [impl]))

(proto/defprotocol.xt ICounted (size [impl]))

(proto/defprotocol.xt ILookupable (keys [impl]) (vals [impl]) (lookup [impl key default-val]) (find [impl key]))

(proto/defprotocol.xt IMapEntry (key [impl]) (val [impl]))

(proto/defprotocol.xt INamed (name [impl]) (namespace [impl]))

(proto/defprotocol.xt IPersistent (is_mutable [impl]) (to_mutable [impl]) (is_persistent [impl]) (to_persistent [impl]))

(proto/defprotocol.xt ISequential (to_iter [impl]) (to_array [impl]) (empty [impl]) (size [impl]) (nth [impl idx]))

(proto/defprotocol.xt IStack (push [impl x]) (pop [impl]))

(proto/defprotocol.xt IStackMutable (push_mutable [impl x]) (pop_mutable [impl]))

(proto/defprotocol.xt IValue (eq [impl o]) (hash [impl]) (show [impl]))

(proto/defprotocol.xt ILispPersistent (is_mutable [impl]) (to_mutable [impl]) (is_persistent [impl]) (to_persistent [impl]) (assoc_mutable [impl key val]) (dissoc_mutable [impl key]) (push_mutable [impl x]) (pop_mutable [impl]))

(proto/defprotocol.xt ILispScalar (eq [impl o]) (hash [impl]) (show [impl]) (meta [impl]) (with_meta [impl meta]))

(proto/defprotocol.xt ILispAssociative (eq [impl o]) (hash [impl]) (show [impl]) (meta [impl]) (with_meta [impl meta]) (to_iter [impl]) (to_array [impl]) (empty [impl]) (size [impl]) (assoc [impl key val]) (dissoc [impl key]) (keys [impl]) (vals [impl]) (lookup [impl key default-val]) (find [impl key]) (seq [impl]) (first [impl]) (rest [impl]) (next [impl]))

(proto/defprotocol.xt ILispNamed (eq [impl o]) (hash [impl]) (show [impl]) (meta [impl]) (with_meta [impl meta]) (name [impl]) (namespace [impl]))

(proto/defprotocol.xt ILispSequential (eq [impl o]) (hash [impl]) (show [impl]) (meta [impl]) (with_meta [impl meta]) (to_iter [impl]) (to_array [impl]) (empty [impl]) (size [impl]) (nth [impl idx]) (push [impl x]) (pop [impl]) (seq [impl]) (first [impl]) (rest [impl]) (next [impl]) (conj [impl x]) (cons [impl x]) (peek [impl]))

;;
;; Protocols (in dependency order)
;;
