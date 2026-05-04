(ns kmi.lang.type-list
  (:require [hara.lang :as l])
  (:refer-clojure :exclude [list]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-iter :as it]
             [xt.lang.common-protocol :as proto]
             [kmi.protocol.icoll :as p-coll]
             [kmi.protocol.iedit :as p-edit]
             [kmi.protocol.iempty :as p-empty]
             [kmi.protocol.ieq :as p-eq]
             [kmi.protocol.ihash :as p-hash]
             [kmi.protocol.ipush :as p-push]
             [kmi.protocol.ipush-mutable :as p-push-mutable]
             [kmi.protocol.ipop :as p-pop]
             [kmi.protocol.ipop-mutable :as p-pop-mutable]
             [kmi.protocol.isize :as p-size]
             [kmi.protocol.ishow :as p-show]
             [kmi.lang.interface-spec :as spec]
             [kmi.lang.interface-common :as interface-common]
             [kmi.lang.interface-collection :as interface-collection]]})

(def.xt EMPTY_MARKER
  {})

(defgen.xt list-to-iter
  "list to iterator"
  {:added "4.0"}
  [list]
  (while (not= (. list _head) -/EMPTY_MARKER)
    (yield (. list _head))
    (:= list (. list _rest))))

(defn.xt list-to-array
  "list to array"
  {:added "4.0"}
  [list]
  (var out [])
  (while (not= (. list _head) -/EMPTY_MARKER)
    (xt/x:arr-push out (. list _head))
    (:= list (. list _rest)))
  (return out))

(defn.xt list-size
  "gets the list size"
  {:added "4.0"}
  [list]
  (cond (== (. list _head)
            -/EMPTY_MARKER)
        (return 0)

        :else (return (+ 1 (. list _rest (size))))))  

(defn.xt list-new
  "creates a new list"
  {:added "4.0"}
  [head rest prototype]
  (var list {"::" "list"
             :_head head
             :_rest rest})
  (return (spec/runtime-attach list prototype)))

(defn.xt list-push
  "pushs onto the front of the list"
  {:added "4.0"}
  [list x]
  (return (-/list-new x list (spec/runtime-protocol list))))

(defn.xt list-pop
  "pops an element from front of list"
  {:added "4.0"}
  [list x]
  (return  (. list _rest)))

(defn.xt list-empty
  "gets the empty list"
  {:added "4.0"}
  [list]
  (return (-/list-new -/EMPTY_MARKER nil (spec/runtime-protocol list))))

(def.xt LIST_SPEC
   [[p-coll/IColl   {:_start_string  "("
                     :_end_string    ")"
                     :_sep_string    ", "
                     :_is_ordered    false
                     :to-iter  -/list-to-iter
                     :to-array -/list-to-array}]
    [p-edit/IEdit   {:is-mutable (fn:> true)
                     :to-mutable (fn [x] (return x))
                     :is-persistent (fn:> true)
                     :to-persistent (fn [x] (return x))}]
    [p-empty/IEmpty  {:empty  -/list-empty}]
    [p-eq/IEq     {:eq     interface-collection/coll-eq}]
    [p-hash/IHash   {:hash   (interface-common/wrap-with-cache
                              interface-collection/coll-hash-unordered)}]
    [p-push/IPush   {:push   -/list-push}]
    [p-push-mutable/IPushMutable   {:push-mutable   -/list-push}]
    [p-pop/IPop    {:pop    -/list-pop}]
    [p-pop-mutable/IPopMutable    {:pop-mutable    -/list-pop}]
    [p-size/ISize   {:size   -/list-size}]
    [p-show/IShow   {:show   interface-collection/coll-show}]])

(def.xt LIST_PROTOTYPE
  (-> -/LIST_SPEC
      (proto/proto-spec)
      (spec/proto-create)))

(defn.xt list-create
  "creates a list"
  {:added "4.0"}
  [head rest]  
  (var list {"::" "list"
             :_head head})
  (when rest
    (xt/x:set-key list "_rest" rest))
  (return (spec/runtime-attach list -/LIST_PROTOTYPE)))

(def.xt EMPTY_LIST
  (-/list-create -/EMPTY_MARKER nil))

(defn.xt list
  "creates a list given arguments"
  {:added "4.0"}
  [...]
  (return
   (xt/x:arr-foldr [...]
                -/list-push
                -/EMPTY_LIST)))

(defn.xt list-map
  "maps function across list"
  {:added "4.0"}
  [list f]
  (var #{_head _rest} list)

  (if (== _rest nil)
    (return list)
    (return
     (-/list-create (f _head)
                    (-/list-map _rest f)))))
