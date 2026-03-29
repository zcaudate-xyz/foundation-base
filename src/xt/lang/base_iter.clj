(ns xt.lang.base-iter
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]])
  (:refer-clojure :exclude [constantly iterate repeatedly cycle range drop peek take map mapcat concat filter keep partition take-nth]))

(l/script :xtalk
  {:require [[xt.lang.common-lib :as common-lib]
             [xt.lang.common-data :as common-data]]})

(defspec.xt Iterator
  :xt/any)

(defspec.xt IterEqFn
  [:fn [:xt/any :xt/any] :xt/bool])

(defspec.xt UnaryFn
  [:fn [:xt/any] :xt/any])

(defspec.xt Predicate
  [:fn [:xt/any] :xt/bool])

(defspec.xt MaybeUnaryFn
  [:fn [:xt/any] [:xt/maybe :xt/any]])

(defspec.xt ReducerFn
  [:fn [:xt/any :xt/any] :xt/any])

(defspec.xt Thunk
  [:fn [] :xt/any])

(defspec.xt iter-from-obj
  [:fn [[:xt/dict :xt/str :xt/any]] Iterator])

(defspec.xt iter-from-arr
  [:fn [[:xt/array :xt/any]] Iterator])

(defspec.xt iter-from
  [:fn [:xt/any] Iterator])

(defspec.xt iter-next
  [:fn [Iterator] :xt/any])

(defspec.xt iter-has?
  [:fn [:xt/any] :xt/bool])

(defspec.xt iter-native?
  [:fn [:xt/any] :xt/bool])

(defspec.xt iter-eq
  [:fn [Iterator Iterator IterEqFn] :xt/bool])

(defspec.xt iter-null
  [:fn [] Iterator])

(defspec.xt iter?
  [:fn [:xt/any] :xt/bool])

(defspec.xt iter
  [:fn [:xt/any] [:xt/maybe Iterator]])

(defspec.xt collect
  [:fn [Iterator ReducerFn :xt/any] :xt/any])

(defspec.xt nil<
  [:fn [Iterator] :xt/nil])

(defspec.xt arr<
  [:fn [Iterator] [:xt/array :xt/any]])

(defspec.xt obj<
  [:fn [Iterator] [:xt/dict :xt/str :xt/any]])

(defspec.xt constantly
  [:fn [:xt/any] Iterator])

(defspec.xt iterate
  [:fn [UnaryFn :xt/any] Iterator])

(defspec.xt repeatedly
  [:fn [Thunk] Iterator])

(defspec.xt cycle
  [:fn [:xt/any] Iterator])

(defspec.xt range
  [:fn [:xt/any] Iterator])

(defspec.xt drop
  [:fn [:xt/num :xt/any] Iterator])

(defspec.xt peek
  [:fn [UnaryFn :xt/any] Iterator])

(defspec.xt take
  [:fn [:xt/num :xt/any] Iterator])

(defspec.xt map
  [:fn [UnaryFn :xt/any] Iterator])

(defspec.xt mapcat
  [:fn [UnaryFn :xt/any] Iterator])

(defspec.xt concat
  [:fn [:xt/any] Iterator])

(defspec.xt filter
  [:fn [Predicate :xt/any] Iterator])

(defspec.xt keep
  [:fn [MaybeUnaryFn :xt/any] Iterator])

(defspec.xt partition
  [:fn [:xt/num :xt/any] Iterator])

(defspec.xt take-nth
  [:fn [:xt/num :xt/any] Iterator])

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
              (and (common-lib/obj? x)
                   (x:has-key? x
                               "::"
                               "iterator")))))

(defn.xt iter
  "converts to an iterator"
  {:added "4.0"}
  [x]
  (cond (x:nil? x)
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
                       (common-data/first e)
                       (common-data/second e)))
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
    (for:array [e arr]
      (yield e))))

(defgen.xt range
  "setup a range function"
  {:added "4.0"}
  [x]
  (var arr    := (:? (x:is-array? x) x [x]))
  (var arrlen := (x:len arr))
  (var start  (:? (< 1 arrlen) (common-data/first arr) 0))
  (var finish (:? (< 1 arrlen) (common-data/second arr) (common-data/first arr)))
  (var step   (:? (< 2 arrlen) (common-data/nth arr 2) 1))
  (var i := start)
  (cond (and (> step 0)
             (< start finish))
        (while (< i finish)
          (yield i)
          (:= i (+ i step)))
        
        (and (< step 0)
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
