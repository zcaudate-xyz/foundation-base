(ns xt.lang.base-iter
  (:require [std.lib :as h]
            [std.lang :as l])
  (:refer-clojure :exclude [constantly iterate repeatedly cycle range
                            drop peek take map mapcat concat filter
                            keep partition take-nth]))

(l/script :xtalk
  {:require [[xt.lang.base-macro :as k]]
   :export [MODULE]})

(defmacro.xt ^{:style/indent 1}
  for:iter
  "helper function to `for:iter` macro"
  {:added "4.0"}
  ([[e it] & body]
   (apply list 'for:iter [e it] body)))

;;
;; XLANG ITER
;;

(defmacro.xt ^{:standalone true}
  iter-from-obj
  "creates iterator from object"
  {:added "4.0"}
  ([obj]
   (list 'x:iter-from-obj obj)))

(defmacro.xt ^{:standalone true}
  iter-from-arr
  "creates iterator from arr"
  {:added "4.0"}
  ([arr]
   (list 'x:iter-from-arr arr)))

(defmacro.xt ^{:standalone true}
  iter-from
  "creates iterator from generic"
  {:added "4.0"}
  ([x]
   (list 'x:iter-from x)))

(defmacro.xt ^{:standalone true}
  iter-next
  "gets next value of iterator"
  {:added "4.0"}
  ([it]
   (list 'x:iter-next it)))

(defmacro.xt ^{:standalone true}
  iter-has?
  "checks that type has iterator (for generics)"
  {:added "4.0"}
  ([x]
   (list 'x:iter-has? x)))

(defmacro.xt ^{:standalone true}
  iter-native?
  "checks that input is an iterator"
  {:added "4.0"}
  ([x]
   (list 'x:iter-native? x)))

;;
;; ITERATOR
;;

(defn.xt iter-eq
  "checks that two iterators are equal"
  {:added "4.0"}
  [it0 it1 eq-fn]
  (x:iter-eq it0 it1 eq-fn))

(defgen.xt iter-null
  "creates a null iterator"
  {:added "4.0"}
  []
  (x:iter-null))

(defn.xt iter?
  "checks object is an iter"
  {:added "4.0"}
  [x]
  (return (or (-/iter-native? x)
              (and (k/is-object? x)
                   (x:has-key? x
                               "::"
                               "iterator")))))

(defn.xt iter
  "converts to an iterator"
  {:added "4.0"}
  [x]
  (cond (k/nil? x)
        (return (-/iter-null))
        
        (-/iter? x)
        (return x)

        (x:iter-has? x)
        (return (x:iter-from x))

        (x:is-array? x)
        (return (x:iter-from-arr x))
        
        (x:is-object? x)
        (return (x:iter-from-obj x))

        :else
        (return nil)))

(defn.xt collect
  "collects an iterator"
  {:added "4.0"}
  ([it f init]
   (var out := init)
   (for:iter [e it]
             (:= out (f out e)))
   (return out)))

(defn.xt nil<
  "consumes an iterator, returns nil"
  {:added "4.0"}
  [it]
  (for:iter [e it])
  (return nil))

(defn.xt arr<
  "converts an array to iterator"
  {:added "4.0"}
  [it]
  (var out := [])
  (for:iter [e it]
    (x:arr-push out e))
  (return out))

(defn.xt obj<
  "converts an array to object"
  {:added "4.0"}
  [it]
  (var out := {})
  (for:iter [e it]
            (x:set-key out
                       (k/first e)
                       (k/second e)))
  (return out))



;;
;; ITER
;;

(defgen.xt constantly
  "constantly outputs the same value"
  {:added "4.0"}
  [val]
  (while true
    (yield val)))

(defgen.xt iterate
  "iterates a function and a starting value"
  {:added "4.0"}
  [f val]
  (while true
    (yield val)
    (:= val (f val))))

(defgen.xt repeatedly
  "repeatedly calls a function"
  {:added "4.0"}
  [f]
  (while true
    (yield (f))))

(defgen.xt cycle
  "cycles a function"
  {:added "4.0"}
  [seq]
  (var arr := (:? (x:is-array? seq) seq (-/arr< seq)))
  (if (== 0 (x:len arr))
    (x:err "Cannot be empty"))
  (while true
    (k/for:array [e arr]
      (yield e))))

(defgen.xt range
  "setup a range function"
  {:added "4.0"}
  [x]
  (var arr    := (:? (x:is-array? x) x [x]))
  (var arrlen := (x:len arr))
  (var start  (:? (< 1 arrlen) (k/first arr) 0))
  (var finish (:? (< 1 arrlen) (k/second arr) (k/first arr)))
  (var step   (:? (< 2 arrlen) (k/nth arr 2) 1))
  (var i := start)
  (cond (and (k/pos? step)
             (< start finish))
        (while (< i finish)
          (yield i)
          (:= i (+ i step)))
        
        (and (k/neg? step)
             (< finish start))
        (while (> i finish)
          (yield i)
          (:= i (+ i step)))

        :else (return)))

(defgen.xt drop
  "drop elements from seq"
  {:added "4.0"}
  ([n seq]
   (var i := n)
   (for:iter [e (-/iter seq)]
     (if (< 0 i)
       (:= i (- i 1))
       (yield e)))))

(defgen.xt peek
  "peeks at value and passes it on"
  {:added "4.0"}
  ([f seq]
   (for:iter [e (-/iter seq)]
     (f e)
     (yield e))))

(defgen.xt take
  "take elements from seq"
  {:added "4.0"}
  ([n seq]
   (var i := 0)
   (for:iter [e (-/iter seq)]
     (if (< i n)
       (do (:= i (+ i 1))
           (yield e))
       (return)))))

(defgen.xt map
  "maps a function across seq"
  {:added "4.0"}
  ([f seq]
   (for:iter [e (-/iter seq)]
     (yield (f e)))))

(defgen.xt mapcat
  "maps a function a concats"
  {:added "4.0"}
  ([f seq]
   (for:iter [e0 (-/iter seq)]
     (var s0 (f e0))
     (for:iter [e1 (-/iter s0)]
       (yield e1)))))

(defgen.xt concat
  "concats seqs into iterator"
  {:added "4.0"}
  ([seq]
   (for:iter [e (-/mapcat (fn:> [x] x) seq)]
     (yield e))))

(defgen.xt filter
  "filters a seq using a function"
  {:added "4.0"}
  ([pred seq]
   (for:iter [e (-/iter seq)]
     (if (pred e)
       (yield e)))))

(defgen.xt keep
  "keeps a seq using a function"
  {:added "4.0"}
  ([f seq]
   (for:iter [e (-/iter seq)]
     (var v (f e))
     (if v (yield v)))))

(defgen.xt partition
  "partition seq into n items"
  {:added "4.0"}
  ([n seq]
   (if (> 1 n)
     (x:err "Partition should be positive"))
   (var out := [])
   (for:iter [e (-/iter seq)]
     (if (< (x:len out) n)
       (x:arr-push out e)
       (do (yield out)
           (:= out []))))
   (if (< 1 (x:len out))
     (yield out))))

(defgen.xt take-nth
  "takes first and then every nth item of a seq"
  {:added "4.0"}
  ([n seq]
   (if (> 1 n)
     (x:err "Partition should be positive"))
   (var i := 0)
   (for:iter [e (-/iter seq)]
     (if (== i 0)
       (do (yield e)
           (:= i (- n 1)))
       (:= i (- i 1))))))

(def.xt MODULE (!:module))
