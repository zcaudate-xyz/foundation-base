


(defn.xt fn?
  "checks if object is a function type"
  {:added "4.1"}
  [x]
  (return (xt/x:is-function? x)))

(defn.xt identity
  "identity function"
  {:added "4.1"}
  [x]
  (return x))



(defn.xt id-fn
  "gets the id for an object"
  {:added "4.1"}
  [x]
  (return (xt/x:get-key x "id")))

(defn.xt key-fn
  "creates a key access function"
  {:added "4.1"}
  [k]
  (return (fn [x] (return (xt/x:get-key x k)))))

(defn.xt eq-fn
  "creates an equality comparator"
  {:added "4.1"}
  [k v]
  (return (fn [x]
            (return
             (:? (xt/x:is-function? v)
                 (v (xt/x:get-key x k))
                 (== v (xt/x:get-key x k)))))))

(defn.xt inc-fn
  "creates an increment function by closure"
  {:added "4.1"}
  [init]
  (var i := init)
  (when (xt/x:nil? i)
    (:= i -1))
  (var inc-fn
       (fn []
         (:= i (+ i 1))
         (return i)))
  (return inc-fn))

(defn.xt step-nil
  "nil step for fold"
  {:added "4.1"}
  [obj pair]
  (return nil))

(defn.xt step-thrush
  "thrush step for fold"
  {:added "4.1"}
  [x f]
  (return (f x)))

(defn.xt step-call
  "call step for fold"
  {:added "4.1"}
  [f x]
  (return (f x)))

(defn.xt step-push
  "step to push element into arr"
  {:added "4.1"}
  [arr e]
  (xt/x:arr-push arr e)
  (return arr))

(defn.xt step-set-key
  "step to set key in object"
  {:added "4.1"}
  [obj k v]
  (xt/x:set-key obj k v)
  (return obj))

(defn.xt step-set-fn
  "creates a set key function"
  {:added "4.1"}
  [obj k]
  (return (fn [v] (return (-/step-set-key obj k v)))))

(defn.xt step-set-pair
  "step to set key value pair in object"
  {:added "4.1"}
  [obj e]
  (xt/x:set-key obj
             (xt/x:arr-first e)
             (xt/x:arr-second e))
  (return obj))

(defn.xt step-del-key
  "step to delete key in object"
  {:added "4.1"}
  [obj k]
  (xt/x:del-key obj k)
  (return obj))
