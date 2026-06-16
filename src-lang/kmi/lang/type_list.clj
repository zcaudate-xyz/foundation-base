(ns kmi.lang.type-list
  (:require [hara.lang :as l])
  (:refer-clojure :exclude [list]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.common-iter :as it]
             [xt.lang.common-protocol :as proto]
             [kmi.lang.protocol-base :as p]
             [kmi.lang.common-util :as util]
             [kmi.lang.common-coll :as coll]]})

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

        :else (return (+ 1 (util/count (. list _rest))))))  

(defn.xt list-new
  "creates a new list"
  {:added "4.0"}
  [head rest]
  (return {"::" "list"
           "_head" head
           "_rest" rest}))

(defn.xt list-push
  "pushs onto the front of the list"
  {:added "4.0"}
  [list x]
  (return (-/list-new x list)))

(defn.xt list-pop
  "pops an element from front of list"
  {:added "4.0"}
  [list x]
  (return  (. list _rest)))

(defn.xt list-empty
  "gets the empty list"
  {:added "4.0"}
  [list]
  (return (-/list-new -/EMPTY_MARKER nil)))

(proto/defimpl.xt ^{:rt/tag "list"} List
  [_head _rest]
  p/IColl
  {:_start_string  "("
   :_end_string    ")"
   :_sep_string    ", "
   :_is_ordered    false
   :to-iter        -/list-to-iter
   :to-array       -/list-to-array}
  p/IEdit
  {:is-mutable    k/T
   :to-mutable    k/identity
   :is-persistent k/T
   :to-persistent k/identity}
  p/IEmpty
  {:empty -/list-empty}
  p/IEq
  {:eq coll/coll-eq}
  p/IHash
  {:hash (util/wrap-with-cache coll/coll-hash-unordered)}
  p/IPush
  {:push -/list-push}
  p/IPushMutable
  {:push-mutable -/list-push}
  p/IPop
  {:pop -/list-pop}
  p/IPopMutable
  {:pop-mutable -/list-pop}
  p/ISize
  {:size -/list-size}
  p/IShow
  {:show coll/coll-show})

(defn.xt list-create
  "creates a list"
  {:added "4.0"}
  [head rest]  
  (return (-/List head rest)))

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
