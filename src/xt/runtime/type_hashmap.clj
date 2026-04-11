^{:no-test true}
(ns xt.runtime.type-hashmap
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-iter :as it]
             [xt.runtime.interface-common :as data-common]]})

(def.xt BITS 5)
(def.xt WIDTH (xt/x:pow 2 -/BITS))
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
      :bitmap (-/impl-mask (xt/x:get-key leaf "hash") shift)
      :nodemap (-/impl-mask (xt/x:get-key leaf "hash") shift)
      :nodes  [leaf]
      :shift shift}))

  (defn.xt data-node-create
    [edit-id shift leaf]
    (return
     {"::" "hashmap.node"
      :bitmap (-/impl-mask (xt/x:get-key leaf "hash") shift)
      :nodes  [leaf]
      :shift shift}))

  (defn.xt node-create
    "creates a new node"
    {:added "4.0"}
    [edit-id children]
    (var out {"::" "vector.node"
              :children children})
    (when (xt/x:not-nil? edit-id)
      (xt/x:set-key out "edit_id" edit-id))
    (return out))

  (defn.xt node-clone
    "clones the node"
    {:added "4.0"}
    [node]
    (var #{edit-id children} node)
    (return (-/node-create edit-id
                           (xt/x:arr-clone children))))

  (defn.xt node-editable-root
    "creates an editable root"
    {:added "4.0"}
    [node]
    (var #{children} node)
    (return (-/node-create (xt/x:random) (xt/x:arr-clone children))))

  (defn.xt node-editable
    "creates an editable node"
    {:added "4.0"}
    [node edit-id]
    (return (:? (== edit-id (xt/x:get-key node "edit_id"))
                node
                (-/node-clone node)))))

