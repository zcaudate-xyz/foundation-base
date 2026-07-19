(ns kmi.lang.type-hashmap
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[kmi.lang.protocol-base :as p]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-iter :as it]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-protocol :as proto]
             [kmi.lang.common-util :as util]
             [kmi.lang.common-coll :as coll]
             [kmi.lang.type-hashmap-node :as node]
             [kmi.lang.type-pair :as type-pair]]})

(def.xt NOT_FOUND {})

(defn.xt hashmap-collect-pairs
  [node out]
  (xt/for:array [child (xt/x:get-key node "children")]
    (var tag (xt/x:get-key child "::"))
    (cond (== tag "hashmap.leaf")
          (xt/x:arr-push out
                         (type-pair/pair (xt/x:get-key child "_key")
                                         (util/impl-denormalise (xt/x:get-key child "_val"))))

          (== tag "hashmap.collision")
          (xt/for:array [leaf (xt/x:get-key child "children")]
            (xt/x:arr-push out
                           (type-pair/pair (xt/x:get-key leaf "_key")
                                           (util/impl-denormalise (xt/x:get-key leaf "_val")))))

          :else
          (-/hashmap-collect-pairs child out)))
  (return out))

(defn.xt hashmap-to-iter
  "converts hashmap to iterator"
  {:added "4.1"}
  [hashmap]
  (return (it/iter (-/hashmap-collect-pairs (xt/x:get-key hashmap "_root") []))))

(defn.xt hashmap-to-array
  "converts hashmap to an array of entry arrays"
  {:added "4.1"}
  [hashmap]
  (var out [])
  (xt/for:iter [entry (-/hashmap-to-iter hashmap)]
    (xt/x:arr-push out [(xt/x:get-key entry "_key")
                        (xt/x:get-key entry "_val")]))
  (return out))

(defn.xt hashmap-new
  "creates a new hashmap"
  {:added "4.1"}
  [root size]
  (return {"::" "hashmap"
           "_root" root
           "_size" size}))

(defn.xt hashmap-empty
  "creates an empty hashmap from the current hashmap"
  {:added "4.1"}
  [hashmap]
  (return (-/hashmap-new node/EMPTY_HASHMAP_NODE
                         0)))

(defn.xt hashmap-is-editable
  "checks if hashmap is editable"
  {:added "4.1"}
  [hashmap]
  (return (xt/x:not-nil? (xt/x:get-key (xt/x:get-key hashmap "_root") "edit_id"))))

(defn.xt hashmap-to-mutable!
  "creates a mutable hashmap"
  {:added "4.1"}
  [hashmap]
  (if (-/hashmap-is-editable hashmap)
    (return hashmap)
    (return (-/hashmap-new (node/node-editable-root (xt/x:get-key hashmap "_root"))
                           (xt/x:get-key hashmap "_size")))))

(defn.xt hashmap-to-persistent!
  "creates a persistent hashmap"
  {:added "4.1"}
  [hashmap]
  (if (-/hashmap-is-editable hashmap)
    (return (-/hashmap-new (node/ensure-persistent (xt/x:get-key hashmap "_root"))
                           (xt/x:get-key hashmap "_size")))
    (return hashmap)))

(defn.xt hashmap-find-key
  "finds the entry for a key"
  {:added "4.1"}
  [hashmap key]
  (var leaf (node/node-find-leaf (xt/x:get-key hashmap "_root")
                                 0
                                 (util/hash key)
                                 key))
  (when leaf
    (return (type-pair/pair (xt/x:get-key leaf "_key")
                            (util/impl-denormalise (xt/x:get-key leaf "_val"))))))

(defn.xt hashmap-lookup-key
  "looks up a key in the hashmap"
  {:added "4.1"}
  [hashmap key default-val]
  (return (node/node-lookup (xt/x:get-key hashmap "_root")
                            0
                            (util/hash key)
                            key
                            default-val)))

(defn.xt hashmap-keys
  "returns the hashmap keys"
  {:added "4.1"}
  [hashmap]
  (var out [])
  (xt/for:iter [entry (-/hashmap-to-iter hashmap)]
    (xt/x:arr-push out (xt/x:get-key entry "_key")))
  (return out))

(defn.xt hashmap-vals
  "returns the hashmap values"
  {:added "4.1"}
  [hashmap]
  (var out [])
  (xt/for:iter [entry (-/hashmap-to-iter hashmap)]
    (xt/x:arr-push out (xt/x:get-key entry "_val")))
  (return out))

(defn.xt hashmap-assoc
  "associates a key/value pair into a persistent hashmap"
  {:added "4.1"}
  [hashmap key val]
  (var result (node/node-assoc (xt/x:get-key hashmap "_root")
                               nil
                               0
                               (util/hash key)
                               key
                               val))
  (return (-/hashmap-new (xt/x:get-key result "node")
                         (+ (xt/x:get-key hashmap "_size")
                            (:? (xt/x:get-key result "added") 1 0)))))

(defn.xt hashmap-assoc!
  "associates a key/value pair into a mutable hashmap"
  {:added "4.1"}
  [hashmap key val]
  (when (not (-/hashmap-is-editable hashmap))
    (xt/x:err "Not Editable"))
  (var root (xt/x:get-key hashmap "_root"))
  (var edit-id (xt/x:get-key root "edit_id"))
  (var result (node/node-assoc root
                               edit-id
                               0
                               (util/hash key)
                               key
                               val))
  (xt/x:set-key hashmap "_root" (xt/x:get-key result "node"))
  (when (xt/x:get-key result "added")
    (xt/x:set-key hashmap "_size" (+ (xt/x:get-key hashmap "_size") 1)))
  (return hashmap))

(defn.xt hashmap-dissoc
  "dissociates a key from a persistent hashmap"
  {:added "4.1"}
  [hashmap key]
  (var result (node/node-dissoc (xt/x:get-key hashmap "_root")
                                nil
                                0
                                (util/hash key)
                                key))
  (return (-/hashmap-new (or (xt/x:get-key result "node")
                             node/EMPTY_HASHMAP_NODE)
                         (+ (xt/x:get-key hashmap "_size")
                            (:? (xt/x:get-key result "removed") -1 0)))))

(defn.xt hashmap-dissoc!
  "dissociates a key from a mutable hashmap"
  {:added "4.1"}
  [hashmap key]
  (when (not (-/hashmap-is-editable hashmap))
    (xt/x:err "Not Editable"))
  (var root (xt/x:get-key hashmap "_root"))
  (var edit-id (xt/x:get-key root "edit_id"))
  (var result (node/node-dissoc root
                                edit-id
                                0
                                (util/hash key)
                                key))
  (xt/x:set-key hashmap "_root" (or (xt/x:get-key result "node")
                                    (node/node-create edit-id 0 [])))
  (when (xt/x:get-key result "removed")
    (xt/x:set-key hashmap "_size" (- (xt/x:get-key hashmap "_size") 1)))
  (return hashmap))

(defn.xt hashmap-hash
  "hashes the hashmap"
  {:added "4.1"}
  [hashmap]
  (return
   (coll/coll-hash-unordered hashmap)))

(defn.xt hashmap-eq
  "checks hashmap equality independent of insertion order"
  {:added "4.1"}
  [m1 m2]
  (when (not= (xt/x:get-key m1 "_size") (xt/x:get-key m2 "_size"))
    (return false))
  (xt/for:iter [entry (-/hashmap-to-iter m1)]
    (var actual (-/hashmap-lookup-key m2 (xt/x:get-key entry "_key") -/NOT_FOUND))
    (when (or (== actual -/NOT_FOUND)
              (not (util/eq actual (xt/x:get-key entry "_val"))))
      (return false)))
  (return true))

(defn.xt hashmap-show
  "shows the hashmap"
  {:added "4.1"}
  [hashmap]
  (var entries (-/hashmap-to-array hashmap))
  (if (== 0 (xt/x:len entries))
    (return "{}")
    (do (var s "{")
        (xt/for:array [entry entries]
          (:= s (xt/x:cat s
                          (util/show (xt/x:first entry))
                          " "
                          (util/show (xt/x:second entry))
                          ", ")))
        (return (xt/x:cat (xt/x:str-substring s 0 (- (xt/x:len s) 2))
                          "}")))))

(proto/defimpl.xt ^{:rt/tag "hashmap"} Hashmap
  [_root _size]
  p/IColl
  {:_start_string "{"
   :_end_string   "}"
   :_sep_string   ", "
   :_is_ordered   false
   :to-iter       -/hashmap-to-iter
   :to-array      -/hashmap-to-array}
  p/IEdit
  {:is-mutable    -/hashmap-is-editable
   :to-mutable    -/hashmap-to-mutable!
   :is-persistent (fn:> [hashmap] (not (-/hashmap-is-editable hashmap)))
   :to-persistent -/hashmap-to-persistent!}
  p/IEmpty
  {:empty -/hashmap-empty}
  p/IEq
  {:eq -/hashmap-eq}
  p/IHash
  {:hash (util/wrap-with-cache
          -/hashmap-hash
          [-/hashmap-is-editable])}
  p/IAssoc
  {:assoc -/hashmap-assoc}
  p/IAssocMutable
  {:assoc-mutable -/hashmap-assoc!}
  p/IDissoc
  {:dissoc -/hashmap-dissoc}
  p/IDissocMutable
  {:dissoc-mutable -/hashmap-dissoc!}
  p/IFind
  {:find -/hashmap-find-key}
  p/ILookup
  {:keys   -/hashmap-keys
   :vals   -/hashmap-vals
   :lookup -/hashmap-lookup-key}
  p/ISize
  {:size coll/coll-size}
  p/IShow
  {:show -/hashmap-show})

(defn.xt hashmap-create
  "creates a hashmap with the default prototype"
  {:added "4.1"}
  [root size]
  (return (-/Hashmap root size)))

(defn.xt hashmap-empty-mutable
  "creates an empty mutable hashmap"
  {:added "4.1"}
  []
  (return (-/hashmap-create (node/node-create (xt/x:random) 0 [])
                            0)))

(def.xt EMPTY_HASHMAP
  (-/hashmap-create node/EMPTY_HASHMAP_NODE 0))

(defn.xt hashmap
  "creates a hashmap from alternating key/value arguments"
  {:added "4.1"}
  [(:.. args)]
  (var input args)
  (when (and (== 1 (xt/x:len input))
             (xt/x:is-array? (xt/x:first input)))
    (:= input (xt/x:first input)))
  (when (not= 0 (xt/x:m-mod (xt/x:len input) 2))
    (xt/x:err "hashmap requires an even number of arguments"))
  (if (== 0 (xt/x:len input))
    (return -/EMPTY_HASHMAP)
    (do (var out (-/hashmap-empty-mutable))
        (var idx 0)
        (while (< idx (xt/x:len input))
          (-/hashmap-assoc! out
                            (xt/x:get-idx input (xt/x:offset idx))
                            (xt/x:get-idx input (xt/x:offset (+ idx 1))))
          (:= idx (+ idx 2)))
        (return (p/to-persistent out)))))
