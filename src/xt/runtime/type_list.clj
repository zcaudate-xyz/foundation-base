(ns xt.runtime.type-list
  (:require [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [list]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]
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
    (x:arr-push out (. list _head))
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
  (k/set-proto list prototype)
  (return list))

(defn.xt list-push
  "pushs onto the front of the list"
  {:added "4.0"}
  [list x]
  (return (-/list-new x list (k/get-proto list))))

(defn.xt list-pop
  "pops an element from front of list"
  {:added "4.0"}
  [list x]
  (return  (. list _rest)))

(defn.xt list-empty
  "gets the empty list"
  {:added "4.0"}
  [list]
  (return (-/list-new -/EMPTY_MARKER nil (k/get-proto list))))

(def.xt LIST_SPEC
  [[spec/IColl   {:_start_string  "("
                  :_end_string    ")"
                  :_sep_string    ", "
                  :_is_ordered    false
                  :to-iter  -/list-to-iter
                  :to-array -/list-to-array}]
   [spec/IEdit   {:is-mutable (fn:> true)
                  :to-mutable k/identity
                  :is-persistent (fn:> true)
                  :to-persistent k/identity}]
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
      (k/proto-spec)
      (k/proto-create)))

(defn.xt list-create
  "creates a list"
  {:added "4.0"}
  [head rest]  
  (var list {"::" "list"
             :_head head})
  (when rest
    (k/set-key list "_rest" rest))
  (k/set-proto list -/LIST_PROTOTYPE)
  (return list))

(def.xt EMPTY_LIST
  (-/list-create -/EMPTY_MARKER nil))

(defn.xt list
  "creates a list given arguments"
  {:added "4.0"}
  [...]
  (return
   (k/arr-foldr [...]
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

