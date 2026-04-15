(ns xt.runtime.type-vector-node
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-iter :as it]
             [xt.runtime.interface-common :as interface-common]]})

(def.xt BITS 5)
(def.xt WIDTH (xt/x:m-pow 2 -/BITS))
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
            (return (xt/x:bit-lshift
                     (xt/x:bit-rshift last-idx -/BITS)
                     -/BITS)))))

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
              (-/node-clone node))))

(defn.xt ensure-editable
  "ensures that the node is editable"
  {:added "4.0"}
  [node]
  (var #{edit-id} node)
  (when (xt/x:nil? edit-id)
    (xt/x:err "Not Editable")))

(defn.xt ensure-persistent
  "ensures that the node is not editable"
  {:added "4.0"}
  [node]
  (var #{edit-id children} node)
  (cond (xt/x:nil? edit-id)
        (return node)

        :else
        (return (-/node-create nil (xt/x:arr-clone children)))))

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
    (var nidx (-/impl-mask (xt/x:bit-rshift idx level)))
    (var #{children} nnode)
    (:= nnode (xt/x:get-idx children (xt/x:offset nidx)))
    (when editable
      (:= nnode (-/node-editable nnode (xt/x:get-key node "edit_id"))))
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
  (var sidx (-/impl-mask (xt/x:bit-rshift (- size 1)
                                       level)))
  (var nnode (-/node-clone parent))
  (var #{children} nnode)
  (cond (== level -/BITS)
        (xt/x:arr-push children tail-node)

        :else
        (do (var child (xt/x:get-idx children (xt/x:offset sidx)))
            (cond (xt/x:nil? child)
                  (xt/x:arr-push
                   children
                   (-/node-new-path edit-id
                                    (- level -/BITS)
                                    tail-node))
                  :else
                  (xt/x:set-idx
                   children
                   (xt/x:offset sidx)
                   (-/node-push-tail
                    edit-id size (- level -/BITS) child tail-node editable)))))
  (return nnode))

(defn.xt node-pop-tail
  "pops the last element off node"
  {:added "4.0"}
  [edit-id size level parent editable]
  (when editable
    (:= parent (-/node-editable parent edit-id)))
  (var sidx (-/impl-mask (xt/x:bit-rshift (- size 2)
                                       level)))
  (var #{children} parent)
  (cond (> level -/BITS)
        (do (var nnode (-/node-pop-tail
                        edit-id
                        size
                        (- level -/BITS)
                        (xt/x:get-idx children (xt/x:offset sidx))))
            (cond (and (== nnode nil)
                       (== 0 sidx))
                  (return nil)

                  :else
                  (do (xt/x:set-idx children (xt/x:offset sidx) nnode)
                      (return parent))))

        :else
        (do (xt/x:arr-pop children)
            (return parent))))

(defn.xt node-assoc
  "associates a given node"
  {:added "4.0"}
  [node level idx x]
  (var nnode (-/node-clone node))
  (var #{children} nnode)
  (cond (== level 0)
        (xt/x:set-idx children
                   (xt/x:offset (-/impl-mask idx))
                   x)
        :else
        (do (var sidx (-/impl-mask (xt/x:bit-rshift idx level)))
            (xt/x:set-idx children
                       (xt/x:offset sidx)
                       (-/node-assoc
                        (xt/x:get-idx children (xt/x:offset sidx))
                        (- level -/BITS)
                        idx
                        x))))
  (return nnode))

;;
;; 
;;

(def.xt EMPTY_VECTOR_NODE
  (-/node-create nil []))
