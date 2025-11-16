^{:no-test true}
(ns xt.runtime.type-hashmap-node
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]
             [xt.runtime.interface-common :as data-common]]})

(def.xt BITS 5)
(def.xt WIDTH (k/pow 2 -/BITS))
(def.xt MASK (- -/WIDTH 1))

(defn.xt impl-mask
  [hash shift]
  (return (k/bit-and (k/bit-rshift hash shift)
                                     -/MASK)))

(defn.xt impl-bitpos
  [hash shift]
  (return (k/bit-lshift 1 (-/impl-mask hash shift))))

(defn.xt impl-edit-allowed
  [edit-0 edit-1]
  (return (and (k/not-nil? edit-0)
               (== edit-0 edit-1))))

(defn.xt node-create
  "creates a new node"
  {:added "4.0"}
  [edit-id bitmap children]
  (var out {"::" "hashmap.node"
            :bitmap bitmap
            :children children})
  (when (k/not-nil? edit-id)
    (k/set-key out "edit_id" edit-id))
  (return out))

(defn.xt node-set-
  "creates a new node"
  {:added "4.0"}
  [node edit-id idx val])



