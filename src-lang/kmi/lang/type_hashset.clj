(ns kmi.lang.type-hashset
  (:require [hara.lang :as l])
  (:refer-clojure :exclude [hashset]))

(l/script :xtalk
  {:require [[kmi.lang.protocol-base :as p]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-iter :as it]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-protocol :as proto]
             [kmi.lang.common-util :as util]
             [kmi.lang.common-coll :as coll]
             [kmi.lang.type-hashmap :as hashmap]]})

(def.xt NOT_FOUND {})

(defn.xt hashset-to-array
  "converts hashset to an array"
  {:added "4.1"}
  [hashset]
  (return (hashmap/hashmap-keys (xt/x:get-key hashset "_map"))))

(defn.xt hashset-to-iter
  "converts hashset to an iterator"
  {:added "4.1"}
  [hashset]
  (return (it/iter (-/hashset-to-array hashset))))

(defn.xt hashset-new
  "creates a new hashset"
  {:added "4.1"}
  [m]
  (return {"::" "hashset"
           "_map" m
           "_size" (xt/x:get-key m "_size")}))

(defn.xt hashset-empty
  "creates an empty hashset from current"
  {:added "4.1"}
  [hashset]
  (return (-/hashset-new hashmap/EMPTY_HASHMAP)))

(defn.xt hashset-is-editable
  "checks if hashset is editable"
  {:added "4.1"}
  [hashset]
  (return (hashmap/hashmap-is-editable (xt/x:get-key hashset "_map"))))

(defn.xt hashset-to-mutable!
  "creates a mutable hashset"
  {:added "4.1"}
  [hashset]
  (if (-/hashset-is-editable hashset)
    (return hashset)
    (return (-/hashset-new (hashmap/hashmap-to-mutable! (xt/x:get-key hashset "_map"))))))

(defn.xt hashset-to-persistent!
  "creates a persistent hashset"
  {:added "4.1"}
  [hashset]
  (if (-/hashset-is-editable hashset)
    (return (-/hashset-new (hashmap/hashmap-to-persistent! (xt/x:get-key hashset "_map"))))
    (return hashset)))

(defn.xt hashset-find
  "finds a value in the hashset"
  {:added "4.1"}
  [hashset value]
  (var out (hashmap/hashmap-lookup-key (xt/x:get-key hashset "_map") value -/NOT_FOUND))
  (if (== out -/NOT_FOUND)
    (return nil)
    (return value)))

(defn.xt hashset-has?
  "checks membership in the hashset"
  {:added "4.1"}
  [hashset value]
  (return (not (== -/NOT_FOUND
                   (hashmap/hashmap-lookup-key (xt/x:get-key hashset "_map")
                                               value
                                               -/NOT_FOUND)))))

(defn.xt hashset-push
  "adds a value to the persistent hashset"
  {:added "4.1"}
  [hashset value]
  (return (-/hashset-new (hashmap/hashmap-assoc (xt/x:get-key hashset "_map") value true))))

(defn.xt hashset-push!
  "adds a value to the mutable hashset"
  {:added "4.1"}
  [hashset value]
  (when (not (-/hashset-is-editable hashset))
    (xt/x:err "Not Editable"))
  (hashmap/hashmap-assoc! (xt/x:get-key hashset "_map") value true)
  (xt/x:set-key hashset "_size" (xt/x:get-key (xt/x:get-key hashset "_map") "_size"))
  (return hashset))

(defn.xt hashset-dissoc
  "removes a value from the persistent hashset"
  {:added "4.1"}
  [hashset value]
  (return (-/hashset-new (hashmap/hashmap-dissoc (xt/x:get-key hashset "_map") value))))

(defn.xt hashset-dissoc!
  "removes a value from the mutable hashset"
  {:added "4.1"}
  [hashset value]
  (when (not (-/hashset-is-editable hashset))
    (xt/x:err "Not Editable"))
  (hashmap/hashmap-dissoc! (xt/x:get-key hashset "_map") value)
  (xt/x:set-key hashset "_size" (xt/x:get-key (xt/x:get-key hashset "_map") "_size"))
  (return hashset))

(defn.xt hashset-hash
  "hashes the hashset"
  {:added "4.1"}
  [hashset]
  (return (coll/coll-hash-unordered hashset)))

(defn.xt hashset-eq
  "checks hashset equality independent of insertion order"
  {:added "4.1"}
  [s1 s2]
  (when (not= (xt/x:get-key s1 "_size") (xt/x:get-key s2 "_size"))
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
                          (util/show entry)
                          ", ")))
        (return (xt/x:cat (xt/x:str-substring s 0 (- (xt/x:len s) 2))
                          "}")))))

(proto/defimpl.xt ^{:rt/tag "hashset"} Hashset
  [_map _size]
  p/IColl
  {:_start_string "#{"
   :_end_string   "}"
   :_sep_string   ", "
   :_is_ordered   false
   :to-iter       -/hashset-to-iter
   :to-array      -/hashset-to-array}
  p/IEdit
  {:is-mutable    -/hashset-is-editable
   :to-mutable    -/hashset-to-mutable!
   :is-persistent (fn:> [hashset] (not (-/hashset-is-editable hashset)))
   :to-persistent -/hashset-to-persistent!}
  p/IEmpty
  {:empty -/hashset-empty}
  p/IEq
  {:eq -/hashset-eq}
  p/IHash
  {:hash (util/wrap-with-cache
          -/hashset-hash
          [-/hashset-is-editable])}
  p/IFind
  {:find -/hashset-find}
  p/IPush
  {:push -/hashset-push}
  p/IPushMutable
  {:push-mutable -/hashset-push!}
  p/IDissoc
  {:dissoc -/hashset-dissoc}
  p/IDissocMutable
  {:dissoc-mutable -/hashset-dissoc!}
  p/ISize
  {:size coll/coll-size}
  p/IShow
  {:show -/hashset-show})

(defn.xt hashset-create
  "creates a hashset"
  {:added "4.1"}
  [m]
  (return (-/Hashset m (xt/x:get-key m "_size"))))

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
  [(:.. args)]
  (var input args)
  (when (and (== 1 (xt/x:len input))
             (xt/x:is-array? (xt/x:first input)))
    (:= input (xt/x:first input)))
  (if (== 0 (xt/x:len input))
    (return -/EMPTY_HASHSET)
    (do (var out (-/hashset-empty-mutable))
        (xt/for:array [entry input]
          (-/hashset-push! out entry))
        (return (p/to-persistent out)))))
