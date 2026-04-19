(ns xt.runtime.type-hashmap
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-iter :as it]
             [xt.lang.common-data :as xtd]
             [xt.runtime.interface-spec :as spec]
             [xt.runtime.interface-common :as interface-common]
             [xt.runtime.interface-collection :as interface-collection]
             [xt.runtime.type-hashmap-node :as node]
             [xt.runtime.type-pair :as type-pair]]})

(def.xt NOT_FOUND {})

(defn.xt hashmap-collect-pairs
  [node out]
  (xt/for:array [child (. node children)]
    (var tag (xt/x:get-key child "::"))
    (cond (== tag "hashmap.leaf")
          (xt/x:arr-push out
                         (type-pair/pair (. child _key)
                                         (interface-common/impl-denormalise (. child _val))))

          (== tag "hashmap.collision")
          (xt/for:array [leaf (. child children)]
            (xt/x:arr-push out
                           (type-pair/pair (. leaf _key)
                                           (interface-common/impl-denormalise (. leaf _val)))))

          :else
          (-/hashmap-collect-pairs child out)))
  (return out))

(defgen.xt hashmap-to-iter
  "converts hashmap to iterator"
  {:added "4.1"}
  [hashmap]
  (xt/for:array [entry (-/hashmap-collect-pairs (. hashmap _root) [])]
    (yield entry)))

(defn.xt hashmap-to-array
  "converts hashmap to an array of entry arrays"
  {:added "4.1"}
  [hashmap]
  (var out [])
  (xt/for:iter [entry (-/hashmap-to-iter hashmap)]
    (xt/x:arr-push out [(. entry _key)
                        (. entry _val)]))
  (return out))

(defn.xt hashmap-new
  "creates a new hashmap"
  {:added "4.1"}
  [root size protocol]
  (var hashmap {"::" "hashmap"
                :_root root
                :_size size})
  (return (spec/runtime-attach hashmap protocol)))

(defn.xt hashmap-empty
  "creates an empty hashmap from the current hashmap"
  {:added "4.1"}
  [hashmap]
  (return (-/hashmap-new node/EMPTY_HASHMAP_NODE
                         0
                         (spec/runtime-protocol hashmap))))

(defn.xt hashmap-is-editable
  "checks if hashmap is editable"
  {:added "4.1"}
  [hashmap]
  (return (xt/x:not-nil? (xt/x:get-key (. hashmap _root) "edit_id"))))

(defn.xt hashmap-to-mutable!
  "creates a mutable hashmap"
  {:added "4.1"}
  [hashmap]
  (if (-/hashmap-is-editable hashmap)
    (return hashmap)
    (return (-/hashmap-new (node/node-editable-root (. hashmap _root))
                           (. hashmap _size)
                           (spec/runtime-protocol hashmap)))))

(defn.xt hashmap-to-persistent!
  "creates a persistent hashmap"
  {:added "4.1"}
  [hashmap]
  (if (-/hashmap-is-editable hashmap)
    (return (-/hashmap-new (node/ensure-persistent (. hashmap _root))
                           (. hashmap _size)
                           (spec/runtime-protocol hashmap)))
    (return hashmap)))

(defn.xt hashmap-find-key
  "finds the entry for a key"
  {:added "4.1"}
  [hashmap key]
  (var leaf (node/node-find-leaf (. hashmap _root)
                                 0
                                 (interface-common/hash key)
                                 key))
  (when leaf
    (return (type-pair/pair (. leaf _key)
                            (interface-common/impl-denormalise (. leaf _val))))))

(defn.xt hashmap-lookup-key
  "looks up a key in the hashmap"
  {:added "4.1"}
  [hashmap key default-val]
  (return (node/node-lookup (. hashmap _root)
                            0
                            (interface-common/hash key)
                            key
                            default-val)))

(defn.xt hashmap-keys
  "returns the hashmap keys"
  {:added "4.1"}
  [hashmap]
  (var out [])
  (xt/for:iter [entry (-/hashmap-to-iter hashmap)]
    (xt/x:arr-push out (. entry _key)))
  (return out))

(defn.xt hashmap-vals
  "returns the hashmap values"
  {:added "4.1"}
  [hashmap]
  (var out [])
  (xt/for:iter [entry (-/hashmap-to-iter hashmap)]
    (xt/x:arr-push out (. entry _val)))
  (return out))

(defn.xt hashmap-assoc
  "associates a key/value pair into a persistent hashmap"
  {:added "4.1"}
  [hashmap key val]
  (var protocol (spec/runtime-protocol hashmap))
  (var result (node/node-assoc (. hashmap _root)
                               nil
                               0
                               (interface-common/hash key)
                               key
                               val))
  (return (-/hashmap-new (. result node)
                         (+ (. hashmap _size)
                            (:? (. result added) 1 0))
                         protocol)))

(defn.xt hashmap-assoc!
  "associates a key/value pair into a mutable hashmap"
  {:added "4.1"}
  [hashmap key val]
  (when (not (-/hashmap-is-editable hashmap))
    (xt/x:err "Not Editable"))
  (var root (. hashmap _root))
  (var edit-id (xt/x:get-key root "edit_id"))
  (var result (node/node-assoc root
                               edit-id
                               0
                               (interface-common/hash key)
                               key
                               val))
  (xt/x:set-key hashmap "_root" (. result node))
  (when (. result added)
    (xt/x:set-key hashmap "_size" (+ (. hashmap _size) 1)))
  (return hashmap))

(defn.xt hashmap-dissoc
  "dissociates a key from a persistent hashmap"
  {:added "4.1"}
  [hashmap key]
  (var protocol (spec/runtime-protocol hashmap))
  (var result (node/node-dissoc (. hashmap _root)
                                nil
                                0
                                (interface-common/hash key)
                                key))
  (return (-/hashmap-new (or (. result node)
                             node/EMPTY_HASHMAP_NODE)
                         (+ (. hashmap _size)
                            (:? (. result removed) -1 0))
                         protocol)))

(defn.xt hashmap-dissoc!
  "dissociates a key from a mutable hashmap"
  {:added "4.1"}
  [hashmap key]
  (when (not (-/hashmap-is-editable hashmap))
    (xt/x:err "Not Editable"))
  (var root (. hashmap _root))
  (var edit-id (xt/x:get-key root "edit_id"))
  (var result (node/node-dissoc root
                                edit-id
                                0
                                (interface-common/hash key)
                                key))
  (xt/x:set-key hashmap "_root" (or (. result node)
                                    (node/node-create edit-id 0 [])))
  (when (. result removed)
    (xt/x:set-key hashmap "_size" (- (. hashmap _size) 1)))
  (return hashmap))

(defn.xt hashmap-hash
  "hashes the hashmap"
  {:added "4.1"}
  [hashmap]
  (return
   (interface-collection/coll-hash-unordered hashmap)))

(defn.xt hashmap-eq
  "checks hashmap equality independent of insertion order"
  {:added "4.1"}
  [m1 m2]
  (when (not= (. m1 _size) (. m2 _size))
    (return false))
  (xt/for:iter [entry (-/hashmap-to-iter m1)]
    (var actual (-/hashmap-lookup-key m2 (. entry _key) -/NOT_FOUND))
    (when (or (== actual -/NOT_FOUND)
              (not (interface-common/eq actual (. entry _val))))
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
                          (interface-common/show (xt/x:first entry))
                          " "
                          (interface-common/show (xt/x:second entry))
                          ", ")))
        (return (xt/x:cat (xt/x:str-substring s 0 (- (xt/x:len s) 2))
                          "}")))))

(def.xt HASHMAP_SPEC
  [[spec/IColl   {:_start_string "{"
                  :_end_string   "}"
                  :_sep_string   ", "
                  :_is_ordered   false
                  :to-iter  -/hashmap-to-iter
                  :to-array -/hashmap-to-array}]
   [spec/IEdit   {:is-mutable    -/hashmap-is-editable
                  :to-mutable    -/hashmap-to-mutable!
                  :is-persistent (fn:> [hashmap] (not (-/hashmap-is-editable hashmap)))
                  :to-persistent -/hashmap-to-persistent!}]
   [spec/IEmpty  {:empty         -/hashmap-empty}]
   [spec/IEq     {:eq            -/hashmap-eq}]
   [spec/IHash   {:hash          (interface-common/wrap-with-cache
                                  -/hashmap-hash
                                  -/hashmap-is-editable)}]
   [spec/IAssoc  {:assoc         -/hashmap-assoc}]
   [spec/IAssocMutable  {:assoc-mutable -/hashmap-assoc!}]
   [spec/IDissoc {:dissoc        -/hashmap-dissoc}]
   [spec/IDissocMutable {:dissoc-mutable -/hashmap-dissoc!}]
   [spec/IFind   {:find          -/hashmap-find-key}]
   [spec/ILookup {:keys          -/hashmap-keys
                  :vals          -/hashmap-vals
                  :lookup        -/hashmap-lookup-key}]
   [spec/ISize   {:size          interface-collection/coll-size}]
   [spec/IShow   {:show          -/hashmap-show}]])

(def.xt HASHMAP_PROTOTYPE
  (-> -/HASHMAP_SPEC
      (spec/proto-spec)
      (spec/proto-create)))

(defn.xt hashmap-create
  "creates a hashmap with the default prototype"
  {:added "4.1"}
  [root size]
  (return (-/hashmap-new root size -/HASHMAP_PROTOTYPE)))

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
  [...]
  (var input [...])
  (when (not= 0 (xt/x:m-mod (xt/x:len input) 2))
    (xt/x:err "hashmap requires an even number of arguments"))
  (if (xtd/is-empty? input)
    (return -/EMPTY_HASHMAP)
    (do (var out (-/hashmap-empty-mutable))
        (var idx 0)
        (while (< idx (xt/x:len input))
          (-/hashmap-assoc! out
                            (xt/x:get-idx input (xt/x:offset idx))
                            (xt/x:get-idx input (xt/x:offset (+ idx 1))))
          (:= idx (+ idx 2)))
        (return (interface-common/to-persistent out)))))
