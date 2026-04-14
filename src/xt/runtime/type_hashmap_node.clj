^{:no-test true}
(ns xt.runtime.type-hashmap-node
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-iter :as it]
             [xt.runtime.interface-common :as data-common]]})

(def.xt BITS 5)
(def.xt WIDTH (xt/x:m-pow 2 -/BITS))
(def.xt MASK (- -/WIDTH 1))

(defn.xt impl-mask
  [hash shift]
  (return (xt/x:bit-and (xt/x:bit-rshift hash shift)
                                     -/MASK)))

(defn.xt impl-bitpos
  [hash shift]
  (return (xt/x:bit-lshift 1 (-/impl-mask hash shift))))

(defn.xt impl-edit-allowed
  [edit-0 edit-1]
  (return (and (xt/x:not-nil? edit-0)
               (== edit-0 edit-1))))

(defn.xt node-create
  "creates a new node"
  {:added "4.0"}
  [edit-id bitmap children]
  (var out {"::" "hashmap.node"
            :bitmap bitmap
            :children children})
  (when (xt/x:not-nil? edit-id)
    (xt/x:set-key out "edit_id" edit-id))
  (return out))

(defn.xt node-set-
  "creates a new node"
  {:added "4.0"}
  [node edit-id idx val])


