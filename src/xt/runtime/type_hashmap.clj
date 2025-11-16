^{:no-test true}
(ns xt.runtime.type-hashmap
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

(defn.xt impl-copy-without
  [])

;;
;;
;;

(comment

  (defn.xt data-node-create
    [edit-id shift leaf]
    (return
     {"::" "hashmap.node"
      :bitmap (-/impl-mask (k/get-key leaf "hash") shift)
      :nodemap (-/impl-mask (k/get-key leaf "hash") shift)
      :nodes  [leaf]
      :shift shift}))

  (defn.xt data-node-create
    [edit-id shift leaf]
    (return
     {"::" "hashmap.node"
      :bitmap (-/impl-mask (k/get-key leaf "hash") shift)
      :nodes  [leaf]
      :shift shift}))

  (defn.xt node-create
    "creates a new node"
    {:added "4.0"}
    [edit-id children]
    (var out {"::" "vector.node"
              :children children})
    (when (k/not-nil? edit-id)
      (k/set-key out "edit_id" edit-id))
    (return out))

  (defn.xt node-clone
    "clones the node"
    {:added "4.0"}
    [node]
    (var #{edit-id children} node)
    (return (-/node-create edit-id
                           (k/arr-clone children))))

  (defn.xt node-editable-root
    "creates an editable root"
    {:added "4.0"}
    [node]
    (var #{children} node)
    (return (-/node-create (k/random) (k/arr-clone children))))

  (defn.xt node-editable
    "creates an editable node"
    {:added "4.0"}
    [node edit-id]
    (return (:? (== edit-id (k/get-key node "edit_id"))
                node
                (-/node-clone node)))))

