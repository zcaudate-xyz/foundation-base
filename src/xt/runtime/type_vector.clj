(ns xt.runtime.type-vector
  (:require [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [vector]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]
             [xt.runtime.interface-common :as interface-common]
             [xt.runtime.interface-spec :as spec]
             [xt.runtime.interface-collection :as interface-collection]
             [xt.runtime.type-vector-node :as node]
             [xt.runtime.type-pair :as type-pair]]})

(defn.xt vector-get-idx
  "gets the index in the persistent vector"
  {:added "4.0"}
  [vector idx]
  (var #{_root _size _shift _tail} vector)
  (var arr (node/node-array-for _root _size _shift _tail idx false))
  
  (when arr
    (var out (k/get-idx arr (x:offset (node/impl-mask idx))))
    (return (interface-common/impl-denormalise out))))

(defgen.xt vector-to-iter
  "converts vector to iterator"
  {:added "4.0"}
  [vector]
  (var #{_size} vector)
  (k/for:index [idx [0 (x:offset-rlen _size)]]
    (yield (-/vector-get-idx vector idx))))

(defn.xt vector-to-array
  "converts vector to array"
  {:added "4.0"}
  [vector]
  (var #{_size} vector)
  (var out [])
  (k/for:index [idx [0 (x:offset-rlen _size)]]
    (x:arr-push out (-/vector-get-idx vector idx)))
  (return out))

(defn.xt vector-new
  "creates a new vector"
  {:added "4.0"}
  [root size shift tail protocol]
  (var vector {"::" "vector"
               :_root root
               :_size size
               :_shift shift
               :_tail tail})
  (k/set-proto vector protocol)
  (return vector))

(defn.xt vector-empty
  "creates an empty vector from current"
  {:added "4.0"}
  [vector]
  (var protocol (k/get-proto vector))
  (return (-/vector-new node/EMPTY_VECTOR_NODE 0 node/BITS [] protocol)))

(defn.xt vector-is-editable
  "checks that vector is editable"
  {:added "4.0"}
  [vector]
  (var #{_root} vector)
  (var #{edit-id} _root)
  (return (k/not-nil? edit-id)))

;;
;;
;;

(defn.xt vector-push-last
  "push-lastoins an element to the vector"
  {:added "4.0"}
  [vector x]
  (var protocol (k/get-proto vector))
  (var #{_root _size _shift _tail} vector)
  (when (< (- _size (node/impl-offset _size))
           node/WIDTH)
    (var n_tail (k/arr-clone _tail))
    (x:arr-push n_tail (interface-common/impl-normalise x))
    (return (-/vector-new (node/ensure-persistent _root)
                          (+ _size 1)
                          _shift
                          n_tail
                          protocol)))
  
  (var n_root)
  (var _tail-node (node/node-create nil _tail))
  (var n_shift _shift)
  (cond (> (k/bit-rshift _size node/BITS)
           (k/bit-lshift 1 _shift))
        (do (:= n_root
                (node/node-create nil [_root (node/node-new-path nil _shift _tail-node)]))
            (:= n_shift (+ n_shift node/BITS)))
        
        :else
        (:= n_root (node/node-push-tail nil _size _shift _root _tail-node false)))
  (return (-/vector-new n_root
                        (+ 1 _size)
                        n_shift
                        [(interface-common/impl-normalise x)]
                        protocol)))

(defn.xt vector-pop-last
  "pops the last element off vector"
  {:added "4.0"}
  [vector]
  (var protocol (k/get-proto vector))
  (var #{_root _size _shift _tail} vector)
  (when (== _size 0)
    (return vector))
  (when (== _size 1)
    (return (-/vector-empty)))
  (when (> (- _size (node/impl-offset _size))
           1)
    (var n_tail (k/arr-slice _tail 0 (- (k/len _tail) 1)))
    (return (-/vector-new (node/ensure-persistent _root)
                             (- _size 1)
                             _shift
                             n_tail
                             protocol)))

  (var n_tail (node/node-array-for _root _size _shift _tail (- _size 2) false))
  (var n_root (node/node-pop-tail nil _size _shift _root false))
  (var children (and n_root
                     (k/get-key n_root "children")))

  (cond (and (> _shift node/BITS)
             n_root
             (k/nil? (k/second children)))
        (-/vector-new (k/first children)
                         (- _size 1)
                         (- _shift node/BITS)
                         n_tail
                         protocol)
        
        :else
        (return (-/vector-new (or n_root
                                  node/EMPTY_VECTOR_NODE)
                              (- _size 1)
                              _shift
                              n_tail
                              protocol))))

;;
;; mutable
;;

(defn.xt vector-pop-last!
  "pops the last element"
  {:added "4.0"}
  [vector]
  (var #{_root _size _shift _tail} vector)
  (node/ensure-editable _root)
  (when (== _size 0)
    (return vector))
  (when (== _size 1)
    (k/set-key vector "_size" 0)
    (x:arr-pop _tail)
    (return vector))
  (when (<  0 (node/impl-mask (- _size 1)))
    (k/set-key vector "_size" (- _size 1))
    (x:arr-pop _tail)
    (return vector))

  (var #{edit-id} _root)
  (var _tail-node (node/node-array-for _root _size _shift _tail (- _size 2) true))
  (var n_root (node/node-pop-tail _root _size _shift _root true))
  (var children (and n_root
                     (k/get-key n_root "children")))

  (cond (and (> _shift node/BITS)
             n_root
             (k/nil? (k/second children)))
        (do (k/set-key vector "_root"
                       (node/node-editable (k/first children)
                                        edit-id))
            (k/set-key vector "_shift"
                       (- _shift node/BITS)))
        
        
        :else
        (k/set-key vector "_root"
                   (or n_root
                       (node/node-create edit-id []))))
  (k/set-key vector "_tail" _tail-node)
  (k/set-key vector "_size" (- _size 1))
  (return vector))

(defn.xt vector-push-last!
  "pushes the last element into vector"
  {:added "4.0"}
  [vector x]
  (var #{_root _size _shift _tail} vector)
  (node/ensure-editable _root)
  (when (< (- _size (node/impl-offset _size))
           node/WIDTH)
    (x:arr-push _tail (interface-common/impl-normalise x))
    (k/set-key vector "_size" (+ _size 1))
    (return vector))
  
  (var #{edit-id} _root)
  (var _tail-node (node/node-create edit-id _tail))
  (cond (> (k/bit-rshift _size node/BITS)
           (k/bit-lshift 1 _shift))
        (do (k/set-key vector "_root"
                       (node/node-create nil [_root (node/node-new-path nil _shift _tail-node)]))
            (k/set-key vector "_shift" (+ _shift node/BITS)))

        :else
        (k/set-key vector "_root"
                   (node/node-push-tail edit-id _size _shift _root _tail-node true)))
  (k/set-key vector "_tail" [(interface-common/impl-normalise x)])
  (k/set-key vector "_size" (+ _size 1))
  (return vector))

(defn.xt vector-to-mutable!
  "mutates the vector"
  {:added "4.0"}
  [vector]
  (var protocol (k/get-proto vector))
  (var #{_root _size _shift _tail} vector)
  (var #{edit-id} _root)
  (cond (k/not-nil? edit-id)
        (return vector)

        :else
        (return (-/vector-new (node/node-editable-root _root)
                              _size
                              _shift
                              _tail
                              protocol))))

(defn.xt vector-to-persistent!
  "creates persistent vector"
  {:added "4.0"}
  [vector]
  (var protocol (k/get-proto vector))
  (var #{_root _size _shift _tail} vector)
  (node/ensure-editable _root)
  (var #{children} _root)
  (return (-/vector-new (node/node-create nil (k/arr-clone children))
                        _size
                        _shift
                        _tail
                        protocol)))

(defn.xt vector-find-idx
  "finds the pair entry"
  {:added "4.0"}
  [vector idx]
  (var size (interface-collection/coll-size vector))
  (cond (or (< idx 0)
            (>= idx size))
        (return nil)

        :else
        (return (type-pair/pair-new
                 idx
                 (. vector (nth idx))))))

(defn.xt vector-lookup-idx
  "finds the value"
  {:added "4.0"}
  [vector idx defaultVal]
  (var size (interface-collection/coll-size vector))
  (cond (or (< idx 0)
            (>= idx size))
        (return defaultVal)

        :else
        (return (. vector (nth idx)))))

;;
;; ITEROP
;;

(def.xt VECTOR_SPEC
  [[spec/IColl   {:_start_string  "["
                  :_end_string    "]"
                  :_sep_string    ", "
                  :_is_ordered    true
                  :to-iter  -/vector-to-iter
                  :to-array -/vector-to-array}]
   [spec/IEdit   {:is-mutable -/vector-is-editable
                  :to-mutable -/vector-to-mutable!
                  :is-persistent (fn:> [vector] (not (-/vector-is-editable vector)))
                  :to-persistent -/vector-to-persistent!}]
   [spec/IEmpty  {:empty  -/vector-empty}]
   [spec/IEq     {:eq     interface-collection/coll-eq}]    
   [spec/IHash   {:hash   (interface-common/wrap-with-cache
                           interface-collection/coll-hash-ordered
                           -/vector-is-editable)}]
   [spec/IFind   {:find  -/vector-find-idx}]
   [spec/IPush   {:push  -/vector-push-last}]
   [spec/IPushMutable   {:push-mutable   -/vector-push-last!}]
   [spec/IPop    {:pop    -/vector-pop-last}]
   [spec/IPopMutable    {:pop-mutable    -/vector-pop-last!}]
   [spec/INth    {:nth  -/vector-get-idx}]
   [spec/ISize   {:size   interface-collection/coll-size}]
   [spec/IShow   {:show   interface-collection/coll-show}]])

(def.xt VECTOR_PROTOTYPE
  (-> -/VECTOR_SPEC
      (k/proto-spec)
      (k/proto-create)))

(defn.xt vector-create
  "creates a vector"
  {:added "4.0"}
  [root size shift tail]
  (return (-/vector-new root size shift tail -/VECTOR_PROTOTYPE)))

;;
;;
;;

(def.xt EMPTY_VECTOR
  (-/vector-create node/EMPTY_VECTOR_NODE
                   0
                   node/BITS
                   []))

(defn.xt vector-empty-mutable
  "creates an empty mutable vector"
  {:added "4.0"}
  []
  (return (-/vector-create (node/node-create (k/random) [])
                           0
                           node/BITS
                           [])))

(defn.xt vector
  "creates a vector"
  {:added "4.0"}
  [...]
  (var input [...])
  (cond (k/is-empty? input)
        (return -/EMPTY_VECTOR)

        :else
        (return
         (interface-common/to-persistent
          (interface-collection/coll-into-array
           (-/vector-empty-mutable)
           input)))))

;;
;; 
;;

