(ns kmi.lang.type-hashset
  (:require [hara.lang :as l])
  (:refer-clojure :exclude [hashset]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-iter :as it]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-protocol :as proto]
             [kmi.protocol.icoll :as p-coll]
             [kmi.protocol.iedit :as p-edit]
             [kmi.protocol.iempty :as p-empty]
             [kmi.protocol.ieq :as p-eq]
             [kmi.protocol.ihash :as p-hash]
             [kmi.protocol.ifind :as p-find]
             [kmi.protocol.ipush :as p-push]
             [kmi.protocol.ipush-mutable :as p-push-mutable]
             [kmi.protocol.idissoc :as p-dissoc]
             [kmi.protocol.idissoc-mutable :as p-dissoc-mutable]
             [kmi.protocol.isize :as p-size]
             [kmi.protocol.ishow :as p-show]
             [kmi.lang.interface-spec :as spec]
             [kmi.lang.interface-common :as interface-common]
             [kmi.lang.interface-collection :as interface-collection]
             [kmi.lang.type-hashmap :as hashmap]]})

(def.xt NOT_FOUND {})

(defn.xt hashset-to-array
  "converts hashset to an array"
  {:added "4.1"}
  [hashset]
  (return (hashmap/hashmap-keys (. hashset _map))))

(defgen.xt hashset-to-iter
  "converts hashset to an iterator"
  {:added "4.1"}
  [hashset]
  (xt/for:array [entry (-/hashset-to-array hashset)]
    (yield entry)))

(defn.xt hashset-new
  "creates a new hashset"
  {:added "4.1"}
  [m protocol]
  (var hashset {"::" "hashset"
                :_map m
                :_size (. m _size)})
  (return (spec/runtime-attach hashset protocol)))

(defn.xt hashset-empty
  "creates an empty hashset from current"
  {:added "4.1"}
  [hashset]
  (return (-/hashset-new hashmap/EMPTY_HASHMAP
                         (spec/runtime-protocol hashset))))

(defn.xt hashset-is-editable
  "checks if hashset is editable"
  {:added "4.1"}
  [hashset]
  (return (hashmap/hashmap-is-editable (. hashset _map))))

(defn.xt hashset-to-mutable!
  "creates a mutable hashset"
  {:added "4.1"}
  [hashset]
  (if (-/hashset-is-editable hashset)
    (return hashset)
    (return (-/hashset-new (hashmap/hashmap-to-mutable! (. hashset _map))
                           (spec/runtime-protocol hashset)))))

(defn.xt hashset-to-persistent!
  "creates a persistent hashset"
  {:added "4.1"}
  [hashset]
  (if (-/hashset-is-editable hashset)
    (return (-/hashset-new (hashmap/hashmap-to-persistent! (. hashset _map))
                           (spec/runtime-protocol hashset)))
    (return hashset)))

(defn.xt hashset-find
  "finds a value in the hashset"
  {:added "4.1"}
  [hashset value]
  (var out (hashmap/hashmap-lookup-key (. hashset _map) value -/NOT_FOUND))
  (if (== out -/NOT_FOUND)
    (return nil)
    (return value)))

(defn.xt hashset-has?
  "checks membership in the hashset"
  {:added "4.1"}
  [hashset value]
  (return (not (== -/NOT_FOUND
                   (hashmap/hashmap-lookup-key (. hashset _map)
                                               value
                                               -/NOT_FOUND)))))

(defn.xt hashset-push
  "adds a value to the persistent hashset"
  {:added "4.1"}
  [hashset value]
  (return (-/hashset-new (hashmap/hashmap-assoc (. hashset _map) value true)
                         (spec/runtime-protocol hashset))))

(defn.xt hashset-push!
  "adds a value to the mutable hashset"
  {:added "4.1"}
  [hashset value]
  (when (not (-/hashset-is-editable hashset))
    (xt/x:err "Not Editable"))
  (hashmap/hashmap-assoc! (. hashset _map) value true)
  (xt/x:set-key hashset "_size" (. (. hashset _map) _size))
  (return hashset))

(defn.xt hashset-dissoc
  "removes a value from the persistent hashset"
  {:added "4.1"}
  [hashset value]
  (return (-/hashset-new (hashmap/hashmap-dissoc (. hashset _map) value)
                         (spec/runtime-protocol hashset))))

(defn.xt hashset-dissoc!
  "removes a value from the mutable hashset"
  {:added "4.1"}
  [hashset value]
  (when (not (-/hashset-is-editable hashset))
    (xt/x:err "Not Editable"))
  (hashmap/hashmap-dissoc! (. hashset _map) value)
  (xt/x:set-key hashset "_size" (. (. hashset _map) _size))
  (return hashset))

(defn.xt hashset-hash
  "hashes the hashset"
  {:added "4.1"}
  [hashset]
  (return (interface-collection/coll-hash-unordered hashset)))

(defn.xt hashset-eq
  "checks hashset equality independent of insertion order"
  {:added "4.1"}
  [s1 s2]
  (when (not= (. s1 _size) (. s2 _size))
    (return false))
  (xt/for:iter [entry (-/hashset-to-iter s1)]
    (when (not (-/hashset-has? s2 entry))
      (return false)))
  (return true))

(defn.xt hashset-show
  "shows the hashset"
  {:added "4.1"}
  [hashset]
  (var entries (-/hashset-to-array hashset))
  (if (== 0 (xt/x:len entries))
    (return "#{}")
    (do (var s "#{")
        (xt/for:array [entry entries]
          (:= s (xt/x:cat s
                          (interface-common/show entry)
                          ", ")))
        (return (xt/x:cat (xt/x:str-substring s 0 (- (xt/x:len s) 2))
                          "}")))))

(def.xt HASHSET_SPEC
   [[p-coll/IColl {:_start_string "#{" 
                   :_end_string "}"
                   :_sep_string ", "
                   :_is_ordered false
                   :to-iter -/hashset-to-iter
                   :to-array -/hashset-to-array}]
    [p-edit/IEdit {:is-mutable -/hashset-is-editable
                   :to-mutable -/hashset-to-mutable!
                   :is-persistent (fn:> [hashset] (not (-/hashset-is-editable hashset)))
                   :to-persistent -/hashset-to-persistent!}]
    [p-empty/IEmpty {:empty -/hashset-empty}]
    [p-eq/IEq {:eq -/hashset-eq}]
    [p-hash/IHash {:hash (interface-common/wrap-with-cache
                          -/hashset-hash
                          -/hashset-is-editable)}]
    [p-find/IFind {:find -/hashset-find}]
    [p-push/IPush {:push -/hashset-push}]
    [p-push-mutable/IPushMutable {:push-mutable -/hashset-push!}]
    [p-dissoc/IDissoc {:dissoc -/hashset-dissoc}]
    [p-dissoc-mutable/IDissocMutable {:dissoc-mutable -/hashset-dissoc!}]
    [p-size/ISize {:size interface-collection/coll-size}]
    [p-show/IShow {:show -/hashset-show}]])

(def.xt HASHSET_PROTOTYPE
  (-> -/HASHSET_SPEC
      (proto/proto-spec)
      (spec/proto-create)))

(defn.xt hashset-create
  "creates a hashset"
  {:added "4.1"}
  [m]
  (return (-/hashset-new m -/HASHSET_PROTOTYPE)))

(def.xt EMPTY_HASHSET
  (-/hashset-create hashmap/EMPTY_HASHMAP))

(defn.xt hashset-empty-mutable
  "creates an empty mutable hashset"
  {:added "4.1"}
  []
  (return (-/hashset-create (hashmap/hashmap-empty-mutable))))

(defn.xt hashset
  "creates a hashset from values"
  {:added "4.1"}
  [...]
  (var input [...])
  (if (xtd/is-empty? input)
    (return -/EMPTY_HASHSET)
    (do (var out (-/hashset-empty-mutable))
        (xt/for:array [entry input]
          (-/hashset-push! out entry))
        (return (interface-common/to-persistent out)))))
