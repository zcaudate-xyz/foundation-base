(ns xt.runtime.type-list
  (:require [std.lang :as l])
  (:refer-clojure :exclude [list]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-iter :as it]
             [xt.runtime.interface-spec :as spec]
             [xt.runtime.interface-common :as interface-common]
             [xt.runtime.interface-collection :as interface-collection]]})

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
  (xt/x:proto-set list prototype nil)
  (return list))

(defn.xt list-push
  "pushs onto the front of the list"
  {:added "4.0"}
  [list x]
  (return (-/list-new x list (xt/x:proto-get list nil))))

(defn.xt list-pop
  "pops an element from front of list"
  {:added "4.0"}
  [list x]
  (return  (. list _rest)))

(defn.xt list-empty
  "gets the empty list"
  {:added "4.0"}
  [list]
  (return (-/list-new -/EMPTY_MARKER nil (xt/x:proto-get list nil))))

(def.xt LIST_SPEC
  [[spec/IColl   {:_start_string  "("
                  :_end_string    ")"
                  :_sep_string    ", "
                  :_is_ordered    false
                  :to-iter  -/list-to-iter
                  :to-array -/list-to-array}]
   [spec/IEdit   {:is-mutable (fn:> true)
                  :to-mutable (fn [x] (return x))
                  :is-persistent (fn:> true)
                  :to-persistent (fn [x] (return x))}]
   [spec/IEmpty  {:empty  -/list-empty}]
   [spec/IEq     {:eq     interface-collection/coll-eq}]
   [spec/IHash   {:hash   (interface-common/wrap-with-cache
                           interface-collection/coll-hash-unordered)}]    
   [spec/IPush   {:push   -/list-push}]
   [spec/IPushMutable   {:push-mutable   -/list-push}]
   [spec/IPop    {:pop    -/list-pop}]
   [spec/IPopMutable    {:pop-mutable    -/list-pop}]
   [spec/ISize   {:size   -/list-size}]
   [spec/IShow   {:show   interface-collection/coll-show}]])

(def.xt LIST_PROTOTYPE
  (-> -/LIST_SPEC
      (spec/proto-spec)
      (spec/proto-create)))

(defn.xt list-create
  "creates a list"
  {:added "4.0"}
  [head rest]  
  (var list {"::" "list"
             :_head head})
  (when rest
    (xt/x:set-key list "_rest" rest))
  (xt/x:proto-set list -/LIST_PROTOTYPE nil)
  (return list))

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
