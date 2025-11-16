(ns xt.runtime.type-vector-node
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]
             [xt.runtime.interface-common :as interface-common]]})

(def.xt BITS 5)
(def.xt WIDTH (k/pow 2 -/BITS))
(def.xt MASK (- -/WIDTH 1))

(defmacro.xt impl-mask
  "masks an integer value"
  {:added "4.0"}
  [x]
  (list 'x:bit-and x `-/MASK))

(defn.xt impl-offset
  "gets the tail off"
  {:added "4.0"}
  [size]
  (cond (< size -/WIDTH)
        (return 0)

        :else
        (do (var last-idx (- size 1))
            (return (k/bit-lshift
                     (k/bit-rshift last-idx -/BITS)
                     -/BITS)))))

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
              (-/node-clone node))))

(defn.xt ensure-editable
  "ensures that the node is editable"
  {:added "4.0"}
  [node]
  (var #{edit-id} node)
  (when (k/nil? edit-id)
    (k/err "Not Editable")))

(defn.xt ensure-persistent
  "ensures that the node is not editable"
  {:added "4.0"}
  [node]
  (var #{edit-id children} node)
  (cond (k/nil? edit-id)
        (return node)

        :else
        (return (-/node-create nil (k/arr-clone children)))))

(defn.xt node-array-for
  "gets the node array"
  {:added "4.0"}
  [node size shift tail idx editable]
  (when (or (< idx 0)
            (>= idx size))
    (return nil))
  
  (when (>= idx (-/impl-offset size))
    (return tail))
  
  (var nnode node)
  (var level shift)
  (while (> level 0)
    (var nidx (-/impl-mask (k/bit-rshift idx level)))
    (var #{children} nnode)
    (:= nnode (k/get-idx children (x:offset nidx)))
    (when editable
      (:= nnode (-/node-editable nnode (k/get-key node "edit_id"))))
    (:= level (- level -/BITS)))
  
  (var #{children} nnode)
  (return children))

(defn.xt node-new-path
  "new path"
  {:added "4.0"}
  [edit-id level node]
  (return
   (:? (<= level 0)
       node
       (-/node-create
        edit-id
        [(-/node-new-path edit-id
                          (- level -/BITS)
                          node)]))))

(defn.xt node-push-tail
  "pushes an element onto node"
  {:added "4.0"}
  [edit-id size level parent tail-node editable]
  (when editable
    (:= parent (-/node-editable parent edit-id)))
  (var sidx (-/impl-mask (k/bit-rshift (- size 1)
                                       level)))
  (var nnode (-/node-clone parent))
  (var #{children} nnode)
  (cond (== level -/BITS)
        (x:arr-push children tail-node)

        :else
        (do (var child (k/get-idx children (x:offset sidx)))
            (cond (k/nil? child)
                  (x:arr-push
                   children
                   (-/node-new-path edit-id
                                    (- level -/BITS)
                                    tail-node))
                  :else
                  (k/set-idx
                   children
                   (x:offset sidx)
                   (-/node-push-tail
                    edit-id size (- level -/BITS) child tail-node editable)))))
  (return nnode))

(defn.xt node-pop-tail
  "pops the last element off node"
  {:added "4.0"}
  [edit-id size level parent editable]
  (when editable
    (:= parent (-/node-editable parent edit-id)))
  (var sidx (-/impl-mask (k/bit-rshift (- size 2)
                                       level)))
  (var #{children} parent)
  (cond (> level -/BITS)
        (do (var nnode (-/node-pop-tail
                        edit-id
                        size
                        (- level -/BITS)
                        (k/get-idx children (x:offset sidx))))
            (cond (and (== nnode nil)
                       (== 0 sidx))
                  (return nil)

                  :else
                  (do (k/set-idx children (x:offset sidx) nnode)
                      (return parent))))

        :else
        (do (x:arr-pop children)
            (return parent))))

(defn.xt node-assoc
  "associates a given node"
  {:added "4.0"}
  [node level idx x]
  (var nnode (-/node-clone node))
  (var #{children} node)
  (cond (== level 0)
        (k/set-idx children
                   (x:offset (-/impl-mask idx))
                   x)
        :else
        (do (var sidx (-/impl-mask (k/bit-rshift idx level)))
            (k/set-idx children
                       (x:offset sidx)
                       (-/node-assoc
                        (k/get-idx children (x:offset sidx))
                        (- level -/BITS)
                        idx
                        x))))
  (return nnode))

;;
;; 
;;

(def.xt EMPTY_VECTOR_NODE
  (-/node-create nil []))

