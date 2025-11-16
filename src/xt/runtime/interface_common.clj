(ns xt.runtime.interface-common
  (:require [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [hash count pop nth assoc dissoc to-array
                            find empty keyword symbol vector]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]
             [xt.runtime.common-hash :as common-hash]
             [xt.runtime.interface-spec :as spec]]})

(def.xt NIL  {})

(defn.xt impl-normalise
  "normalises the value"
  {:added "4.0"}
  [x]
  (if (not= x nil)
    (return x)
    (return -/NIL)))

(defn.xt impl-denormalise
  "denormalises the value"
  {:added "4.0"}
  [x]
  (if (not= x -/NIL)
    (return x)
    (return nil)))

(defn.xt is-managed?
  "checks if object is managed via the runtime"
  {:added "4.0"}
  [x]
  (return
   (and (k/obj? x)
        (k/not-nil? (. x ["::"])))))

(defn.xt is-syntax?
  "checks if object is of type syntax"
  {:added "4.0"}
  [x]
  (return
   (and (k/obj? x)
        (== "syntax" (. x ["::"])))))

(defn.xt hash
  "gets the hash of an object"
  {:added "4.0"}
  [x]
  (var hash-id (common-hash/hash-native x))
  (if (k/is-number? hash-id)
    (return hash-id)
    (return (. x (hash)))))

(defn.xt get-name
  "gets the name of a symbol, keyword or var"
  {:added "4.0"}
  [x]
  (return (. x _name)))

(defn.xt get-namespace
  "gets the namespace of a symbol, keyword or var"
  {:added "4.0"}
  [x]
  (return (. x _namespace)))

(defn.xt hash-with-cache
  "gets a memoized cache id"
  {:added "4.0"}
  [obj hash-fn]
  (var hash-id (. obj _hash))
  (when (k/nil? hash-id)
    (:= hash-id (hash-fn obj))
    (k/set-key obj "_hash" hash-id))
  (return hash-id))

(defn.xt wrap-with-cache
  "wraps hash-fn call with caching"
  {:added "4.0"}
  [hash-fn is-editable]
  (return (fn [obj]
            (if (and is-editable
                     (is-editable obj))
              (return (hash-fn obj))
              (return (-/hash-with-cache obj hash-fn))))))

(defn.xt show
  "show interface"
  {:added "4.0"}
  [x]
  (var t (k/type-native x))
  (cond (== t "nil")
        (return "nil")
        
        (== t "string")
        (return (k/cat "\"" x "\""))
        
        (-/is-managed? x)
        (if (. x show)
          (return (. x (show)))
          (return (k/to-string x)))
        
        :else
        (return (k/to-string x))))

(defn.xt eq
  "equivalence check"
  {:added "4.0"}
  [o1 o2]
  (cond (-/is-syntax? o1)
        (return (-/eq (. o1 _value)
                      o2))
        
        (-/is-syntax? o2)
        (return (-/eq (. o2 _value)
                      o1))

        (and (-/is-managed? o1)
             (. o1 eq))
        (return (. o1 (eq o2)))
        
        (and (-/is-managed? o2)
             (. o2 eq))
        (return (. o2 (eq o1)))

        :else
        (return (== o1 o2))))

(defn.xt count
  "gets the count for a"
  {:added "4.0"}
  [x]
  (cond (and (-/is-managed? x)
             (. x size))
        (return (. x (size)))
        
        (k/is-string? x)
        (return (x:str-len x))

        (k/arr? x)
        (return (k/len x))))

(defmacro.xt ^{:standalone true}
  is-persistent?
  "checks if collection is persistent"
  {:added "4.0"}
  [coll]
  (list '. coll '(is-persistent)))

(defmacro.xt  ^{:standalone true}
  is-mutable?
  "checks if collection is mutable"
  {:added "4.0"}
  [coll]
  (list '. coll '(is-mutable)))

(defmacro.xt ^{:standalone true}
  to-persistent
  "converts to persistent"
  {:added "4.0"}
  [coll]
  (list '. coll '(to-persistent)))

(defmacro.xt  ^{:standalone true}
  to-mutable
  "converts to mutable"
  {:added "4.0"}
  [coll]
  (list '. coll '(to-mutable)))

(defmacro.xt  ^{:standalone true}
  push
  "pushs elements"
  {:added "4.0"}
  [coll x]
  (list '. coll (list 'push x)))

(defmacro.xt  ^{:standalone true}
  pop
  "pops element from collection"
  {:added "4.0"}
  [coll]
  (list '. coll '(pop)))

(defmacro.xt  ^{:standalone true}
  nth
  "nth coll"
  {:added "4.0"}
  [coll idx]
  (list '. coll '(nth idx)))

(defmacro.xt  ^{:standalone true}
  push-mutable
  "pushes an element into an editable collection"
  {:added "4.0"}
  [coll x]
  (list '. coll (list 'push-mutable x)))

(defmacro.xt  ^{:standalone true}
  pop-mutable
  "pops an element from an editable collection"
  {:added "4.0"}
  [coll]
  (list '. coll '(pop-mutable)))

(defmacro.xt  ^{:standalone true}
  assoc
  "associates a key value pair into a persistent collection"
  {:added "4.0"}
  [coll k v]
  (list '. coll (list 'assoc k v)))

(defmacro.xt  ^{:standalone true}
  dissoc
  "disassociates a key from aa persistent collection"
  {:added "4.0"}
  [coll k]
  (list '. coll '(dissoc k)))

(defmacro.xt  ^{:standalone true}
  assoc-mutable
  "associates a key value pair into a mutable collection"
  {:added "4.0"}
  [coll k v]
  (list '. coll (list 'assoc-mutable k v)))

(defmacro.xt  ^{:standalone true}
  dissoc-mutable
  "disassociates a key pair from a mutable collection"
  {:added "4.0"}
  [coll k]
  (list '. coll (list 'dissoc-mutable k)))

(defmacro.xt  ^{:standalone true}
  to-iter
  "to iter"
  {:added "4.0"}
  [coll]
  (list '. coll (list 'to-iter)))

(defmacro.xt  ^{:standalone true}
  to-array
  "to array"
  {:added "4.0"}
  [coll]
  (list '. coll (list 'to-array)))

(defmacro.xt  ^{:standalone true}
  find
  "find coll"
  {:added "4.0"}
  [coll idx]
  (list '. coll '(find idx)))

(defmacro.xt  ^{:standalone true}
  empty
  "empty coll"
  {:added "4.0"}
  [coll idx]
  (list '. coll '(empty)))

