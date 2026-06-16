(ns kmi.lang.interface-common
  (:require [hara.lang :as l])
  (:refer-clojure :exclude [hash count pop nth assoc dissoc to-array find empty keyword symbol vector]))

(l/script :xtalk
  {:require [[kmi.lang.protocol-base :as p]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-iter :as it]
             [kmi.lang.common-hash :as common-hash]]})

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
   (and (xt/x:is-object? x)
        (xt/x:not-nil? (. x ["::"])))))

(defn.xt is-syntax?
  "checks if object is of type syntax"
  {:added "4.0"}
  [x]
  (return
   (and (xt/x:is-object? x)
        (== "syntax" (. x ["::"])))))

(defn.xt hash
  "gets the hash of an object"
  {:added "4.0"}
  [x]
  (var hash-id (common-hash/hash-native x))
  (if (xt/x:is-number? hash-id)
    (return hash-id)
    (return (p/hash x))))

(defn.xt get-name
  "gets the name of a symbol, keyword or var"
  {:added "4.0"}
  [x]
  (return (. x _name)))

(defn.xt get-namespace
  "gets the namespace of a symbol, keyword or var"
  {:added "4.0"}
  [x]
  (return (. x _ns)))

(defn.xt hash-with-cache
  "gets a memoized cache id"
  {:added "4.0"}
  [obj hash-fn]
  (var hash-id (. obj _hash))
  (when (xt/x:nil? hash-id)
    (:= hash-id (hash-fn obj))
    (xt/x:set-key obj "_hash" hash-id))
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
  (var t (common-hash/native-type x))
  (cond (== t "nil")
        (return "nil")
        
        (== t "string")
        (return (xt/x:cat "\"" x "\""))
        
        (-/is-managed? x)
        (return (p/show x))
        
        :else
        (return (xt/x:to-string x))))

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

        (-/is-managed? o1)
        (return (p/eq o1 o2))
        
        (-/is-managed? o2)
        (return (p/eq o2 o1))

        :else
        (return (== o1 o2))))

(defn.xt count
  "gets the count for a"
  {:added "4.0"}
  [x]
  (cond (xt/x:is-string? x)
        (return (xt/x:str-len x))

        (xt/x:is-array? x)
        (return (xt/x:len x))

        (-/is-managed? x)
        (return (p/size x))))

(defn.xt is-persistent?
  "checks if collection is persistent"
  {:added "4.0"}
  [coll]
  (return (p/is_persistent coll)))

(defn.xt is-mutable?
  "checks if collection is mutable"
  {:added "4.0"}
  [coll]
  (return (p/is_mutable coll)))

(defn.xt to-persistent
  "converts to persistent"
  {:added "4.0"}
  [coll]
  (return (p/to_persistent coll)))

(defn.xt to-mutable
  "converts to mutable"
  {:added "4.0"}
  [coll]
  (return (p/to_mutable coll)))

(defn.xt push
  "pushs elements"
  {:added "4.0"}
  [coll x]
  (return (p/push coll x)))

(defn.xt pop
  "pops element from collection"
  {:added "4.0"}
  [coll]
  (return (p/pop coll)))

(defn.xt nth
  "nth coll"
  {:added "4.0"}
  [coll idx]
  (return (p/nth coll idx)))

(defn.xt push-mutable
  "pushes an element into an editable collection"
  {:added "4.0"}
  [coll x]
  (return (p/push_mutable coll x)))

(defn.xt pop-mutable
  "pops an element from an editable collection"
  {:added "4.0"}
  [coll]
  (return (p/pop_mutable coll)))

(defn.xt assoc
  "associates a key value pair into a persistent collection"
  {:added "4.0"}
  [coll k v]
  (return (p/assoc coll k v)))

(defn.xt dissoc
  "disassociates a key from aa persistent collection"
  {:added "4.0"}
  [coll k]
  (return (p/dissoc coll k)))

(defn.xt assoc-mutable
  "associates a key value pair into a mutable collection"
  {:added "4.0"}
  [coll k v]
  (return (p/assoc_mutable coll k v)))

(defn.xt dissoc-mutable
  "disassociates a key pair from a mutable collection"
  {:added "4.0"}
  [coll k]
  (return (p/dissoc_mutable coll k)))

(defn.xt to-iter
  "to iter"
  {:added "4.0"}
  [coll]
  (return (p/to_iter coll)))

(defn.xt to-array
  "to array"
  {:added "4.0"}
  [coll]
  (return (p/to_array coll)))

(defn.xt find
  "find coll"
  {:added "4.0"}
  [coll idx]
  (return (p/find coll idx)))

(defn.xt empty
  "empty coll"
  {:added "4.0"}
  [coll]
  (return (p/empty coll)))
