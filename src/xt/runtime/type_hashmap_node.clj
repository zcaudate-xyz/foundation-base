(ns xt.runtime.type-hashmap-node
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-iter :as it]
             [xt.runtime.interface-common :as interface-common]]})

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

(defn.xt impl-index
  [bitmap bitpos]
  (var x (xt/x:bit-and bitmap
                       (- bitpos 1)))
  (var v0 (- x (xt/x:bit-and (xt/x:bit-rshift x 1)
                             (:- "0x55555555"))))
  (var v1 (+ (xt/x:bit-and v0 (:- "0x33333333"))
             (xt/x:bit-and (xt/x:bit-rshift v0 2)
                           (:- "0x33333333"))))
  (return
   (xt/x:bit-rshift
    (* (xt/x:bit-and (+ v1 (xt/x:bit-rshift v1 4))
                     (:- "0x0F0F0F0F"))
       (:- "0x1010101"))
    24)))

(defn.xt impl-edit-allowed
  [edit-0 edit-1]
  (return (and (xt/x:not-nil? edit-0)
               (== edit-0 edit-1))))

(defn.xt node-create
  "creates a bitmap indexed node"
  {:added "4.1"}
  [edit-id bitmap children]
  (var out {"::" "hashmap.node"
            :bitmap bitmap
            :children children})
  (when (xt/x:not-nil? edit-id)
    (xt/x:set-key out "edit_id" edit-id))
  (return out))

(defn.xt leaf-create
  "creates a hashmap leaf"
  {:added "4.1"}
  [hash key val]
  (return {"::" "hashmap.leaf"
           :_hash hash
           :_key key
           :_val (interface-common/impl-normalise val)}))

(defn.xt collision-create
  "creates a hash collision node"
  {:added "4.1"}
  [edit-id hash children]
  (var out {"::" "hashmap.collision"
            :_hash hash
            :children children})
  (when (xt/x:not-nil? edit-id)
    (xt/x:set-key out "edit_id" edit-id))
  (return out))

(defn.xt leaf-value
  [leaf]
  (return (interface-common/impl-denormalise (. leaf _val))))

(defn.xt node-clone
  "clones a node/collision"
  {:added "4.1"}
  [node]
  (var tag (xt/x:get-key node "::"))
  (cond (== tag "hashmap.node")
        (return (-/node-create (xt/x:get-key node "edit_id")
                               (. node bitmap)
                               (xt/x:arr-clone (. node children))))

        (== tag "hashmap.collision")
        (return (-/collision-create (xt/x:get-key node "edit_id")
                                    (. node _hash)
                                    (xt/x:arr-clone (. node children))))

        :else
        (return node)))

(defn.xt node-editable
  "creates an editable node"
  {:added "4.1"}
  [node edit-id]
  (var tag (xt/x:get-key node "::"))
  (cond (== tag "hashmap.node")
        (do (var out (-/node-create edit-id
                                    (. node bitmap)
                                    []))
            (xt/for:array [child (. node children)]
              (xt/x:arr-push (. out children)
                             (:? (xt/x:is-object? child)
                                 (-/node-editable child edit-id)
                                 child)))
            (return out))

        (== tag "hashmap.collision")
        (do (var out (-/collision-create edit-id
                                         (. node _hash)
                                         []))
            (xt/for:array [child (. node children)]
              (xt/x:arr-push (. out children) child))
            (return out))

        :else
        (return node)))

(defn.xt node-editable-root
  "creates an editable root"
  {:added "4.1"}
  [node]
  (var edit-id (xt/x:random))
  (return (-/node-editable node edit-id)))

(defn.xt ensure-editable
  "ensures that the node is editable"
  {:added "4.1"}
  [node]
  (when (xt/x:nil? (xt/x:get-key node "edit_id"))
    (xt/x:err "Not Editable")))

(defn.xt ensure-persistent
  "ensures that the node is not editable"
  {:added "4.1"}
  [node]
  (var tag (xt/x:get-key node "::"))
  (cond (== tag "hashmap.node")
        (do (var out (-/node-create nil
                                    (. node bitmap)
                                    []))
            (xt/for:array [child (. node children)]
              (xt/x:arr-push (. out children)
                             (:? (xt/x:is-object? child)
                                 (-/ensure-persistent child)
                                 child)))
            (return out))

        (== tag "hashmap.collision")
        (do (var out (-/collision-create nil
                                         (. node _hash)
                                         []))
            (xt/for:array [child (. node children)]
              (xt/x:arr-push (. out children) child))
            (return out))

        :else
        (return node)))

(defn.xt collision-find-leaf
  [collision key]
  (xt/for:array [leaf (. collision children)]
    (when (interface-common/eq (. leaf _key) key)
      (return leaf)))
  (return nil))

(defn.xt collision-assoc
  [collision edit-id leaf]
  (var children (. collision children))
  (var nchildren [])
  (var replaced false)
  (xt/for:array [child children]
    (if (interface-common/eq (. child _key) (. leaf _key))
      (do (xt/x:arr-push nchildren leaf)
          (:= replaced true))
      (xt/x:arr-push nchildren child)))
  (when (not replaced)
    (xt/x:arr-push nchildren leaf))
  (return {:node (-/collision-create edit-id
                                     (. collision _hash)
                                     nchildren)
           :added (not replaced)}))

(defn.xt collision-dissoc
  [collision edit-id key]
  (var nchildren [])
  (var removed false)
  (xt/for:array [child (. collision children)]
    (if (interface-common/eq (. child _key) key)
      (:= removed true)
      (xt/x:arr-push nchildren child)))
  (cond (not removed)
        (return {:node collision :removed false})

        (== 0 (xt/x:len nchildren))
        (return {:node nil :removed true})

        (== 1 (xt/x:len nchildren))
        (return {:node (xt/x:first nchildren) :removed true})

        :else
        (return {:node (-/collision-create edit-id
                                           (. collision _hash)
                                           nchildren)
                 :removed true})))

(defn.xt branch-create
  [edit-id shift leaf-a leaf-b]
  (var hash-a (. leaf-a _hash))
  (var hash-b (. leaf-b _hash))
  (if (or (== hash-a hash-b)
          (>= shift 30))
    (return (-/collision-create edit-id hash-a [leaf-a leaf-b]))
    (do (var bit-a (-/impl-bitpos hash-a shift))
        (var bit-b (-/impl-bitpos hash-b shift))
        (if (== bit-a bit-b)
          (return (-/node-create edit-id
                                 bit-a
                                 [(-/branch-create edit-id
                                                   (+ shift -/BITS)
                                                   leaf-a
                                                   leaf-b)]))
          (return (-/node-create edit-id
                                 (xt/x:bit-or bit-a bit-b)
                                 (:? (< bit-a bit-b)
                                     [leaf-a leaf-b]
                                     [leaf-b leaf-a])))))))

(defn.xt node-lookup
  "looks up a key in a node"
  {:added "4.1"}
  [node shift hash key default-val]
  (var bitpos (-/impl-bitpos hash shift))
  (when (== 0 (xt/x:bit-and (. node bitmap) bitpos))
    (return default-val))
  (var idx (-/impl-index (. node bitmap) bitpos))
  (var child (xt/x:get-idx (. node children) (xt/x:offset idx)))
  (var tag (xt/x:get-key child "::"))
  (cond (== tag "hashmap.leaf")
        (if (interface-common/eq (. child _key) key)
          (return (-/leaf-value child))
          (return default-val))

        (== tag "hashmap.collision")
        (do (var leaf (-/collision-find-leaf child key))
            (if leaf
              (return (-/leaf-value leaf))
              (return default-val)))

        :else
        (return (-/node-lookup child
                               (+ shift -/BITS)
                               hash
                               key
                               default-val))))

(defn.xt node-find-leaf
  [node shift hash key]
  (var bitpos (-/impl-bitpos hash shift))
  (when (== 0 (xt/x:bit-and (. node bitmap) bitpos))
    (return nil))
  (var idx (-/impl-index (. node bitmap) bitpos))
  (var child (xt/x:get-idx (. node children) (xt/x:offset idx)))
  (var tag (xt/x:get-key child "::"))
  (cond (== tag "hashmap.leaf")
        (if (interface-common/eq (. child _key) key)
          (return child)
          (return nil))

        (== tag "hashmap.collision")
        (return (-/collision-find-leaf child key))

        :else
        (return (-/node-find-leaf child
                                  (+ shift -/BITS)
                                  hash
                                  key))))

(defn.xt node-assoc
  "associates a key/value pair into a node"
  {:added "4.1"}
  [node edit-id shift hash key val]
  (var bitpos (-/impl-bitpos hash shift))
  (var bitmap (. node bitmap))
  (var leaf (-/leaf-create hash key val))
  (when (== 0 (xt/x:bit-and bitmap bitpos))
    (var idx (-/impl-index bitmap bitpos))
    (var nchildren (xt/x:arr-clone (. node children)))
    (xt/x:arr-insert nchildren (xt/x:offset idx) leaf)
    (return {:node (-/node-create edit-id
                                  (xt/x:bit-or bitmap bitpos)
                                  nchildren)
             :added true}))
  (var idx (-/impl-index bitmap bitpos))
  (var child (xt/x:get-idx (. node children) (xt/x:offset idx)))
  (var tag (xt/x:get-key child "::"))
  (cond (== tag "hashmap.leaf")
        (if (interface-common/eq (. child _key) key)
          (do (var nchildren (xt/x:arr-clone (. node children)))
              (xt/x:set-idx nchildren (xt/x:offset idx) leaf)
              (return {:node (-/node-create edit-id bitmap nchildren)
                       :added false}))
          (do (var nchild (-/branch-create edit-id
                                           (+ shift -/BITS)
                                           child
                                           leaf))
              (var nchildren (xt/x:arr-clone (. node children)))
              (xt/x:set-idx nchildren (xt/x:offset idx) nchild)
              (return {:node (-/node-create edit-id bitmap nchildren)
                       :added true})))

        (== tag "hashmap.collision")
        (do (var result (-/collision-assoc child edit-id leaf))
            (var nchildren (xt/x:arr-clone (. node children)))
            (xt/x:set-idx nchildren (xt/x:offset idx) (. result node))
            (return {:node (-/node-create edit-id bitmap nchildren)
                     :added (. result added)}))

        :else
        (do (var result (-/node-assoc child
                                      edit-id
                                      (+ shift -/BITS)
                                      hash
                                      key
                                      val))
            (var nchildren (xt/x:arr-clone (. node children)))
            (xt/x:set-idx nchildren (xt/x:offset idx) (. result node))
            (return {:node (-/node-create edit-id bitmap nchildren)
                     :added (. result added)}))))

(defn.xt node-dissoc
  "dissociates a key from a node"
  {:added "4.1"}
  [node edit-id shift hash key]
  (var bitpos (-/impl-bitpos hash shift))
  (var bitmap (. node bitmap))
  (when (== 0 (xt/x:bit-and bitmap bitpos))
    (return {:node node :removed false}))
  (var idx (-/impl-index bitmap bitpos))
  (var child (xt/x:get-idx (. node children) (xt/x:offset idx)))
  (var tag (xt/x:get-key child "::"))
  (cond (== tag "hashmap.leaf")
        (if (interface-common/eq (. child _key) key)
          (do (var nbitmap (xt/x:bit-and bitmap
                                         (xt/x:bit-xor -1 bitpos)))
              (var nchildren (xt/x:arr-clone (. node children)))
              (xt/x:arr-remove nchildren idx)
              (return {:node (:? (== 0 nbitmap)
                                 nil
                                 (-/node-create edit-id nbitmap nchildren))
                       :removed true}))
          (return {:node node :removed false}))

        (== tag "hashmap.collision")
        (do (var result (-/collision-dissoc child edit-id key))
            (if (not (. result removed))
              (return {:node node :removed false})
              (if (xt/x:nil? (. result node))
                (do (var nbitmap (xt/x:bit-and bitmap
                                               (xt/x:bit-xor -1 bitpos)))
                    (var nchildren (xt/x:arr-clone (. node children)))
                    (xt/x:arr-remove nchildren idx)
                    (return {:node (:? (== 0 nbitmap)
                                       nil
                                       (-/node-create edit-id nbitmap nchildren))
                             :removed true}))
                (do (var nchildren (xt/x:arr-clone (. node children)))
                    (xt/x:set-idx nchildren (xt/x:offset idx) (. result node))
                    (return {:node (-/node-create edit-id bitmap nchildren)
                             :removed true})))))

        :else
        (do (var result (-/node-dissoc child
                                       edit-id
                                       (+ shift -/BITS)
                                       hash
                                       key))
            (if (not (. result removed))
              (return {:node node :removed false})
              (if (xt/x:nil? (. result node))
                (do (var nbitmap (xt/x:bit-and bitmap
                                               (xt/x:bit-xor -1 bitpos)))
                    (var nchildren (xt/x:arr-clone (. node children)))
                    (xt/x:arr-remove nchildren idx)
                    (return {:node (:? (== 0 nbitmap)
                                       nil
                                       (-/node-create edit-id nbitmap nchildren))
                             :removed true}))
                (do (var nchildren (xt/x:arr-clone (. node children)))
                    (xt/x:set-idx nchildren (xt/x:offset idx) (. result node))
                    (return {:node (-/node-create edit-id bitmap nchildren)
                             :removed true})))))))

(def.xt EMPTY_HASHMAP_NODE
  (-/node-create nil 0 []))
