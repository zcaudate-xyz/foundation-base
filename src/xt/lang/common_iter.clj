(ns xt.lang.common-iter
  (:require [std.lang :as l :refer [defspec.xt]])
  (:refer-clojure :exclude [constantly iterate repeatedly cycle range drop peek take map
                            mapcat concat filter keep partition take-nth]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]]})

;;
;; ITERATOR
;;

(defn.xt iter-eq
  "checks that two iterators are equal"
  {:added "4.0"}
  [it0 it1 eq-fn]
  (xt/x:iter-eq it0 it1 eq-fn))

(defgen.xt iter-null
  "creates a null iterator"
  {:added "4.0"}
  []
  (xt/x:iter-null))

(defn.xt iter?
  "checks object is an iter"
  {:added "4.0"}
  [x]
  (return (or (xt/x:iter-native? x)
              (and (xt/x:is-object? x)
                   (xt/x:has-key? x
                                  "::"
                                  "iterator")))))

(defn.xt iter
  "converts to an iterator"
  {:added "4.0"}
  [x]
  (cond (xt/x:nil? x)
        (return (-/iter-null))
        
        (-/iter? x)
        (return x)

        (xt/x:iter-has? x)
        (return (xt/x:iter-from x))

        (xt/x:is-array? x)
        (return (xt/x:iter-from-arr x))
        
        (xt/x:is-object? x)
        (return (xt/x:iter-from-obj x))

        :else
        (return nil)))

(defn.xt collect
  "collects an iterator"
  {:added "4.0"}
  ([it f init]
   (var out init)
   (xt/for:iter [e it]
             (:= out (f out e)))
   (return out)))

(defn.xt nil<
  "consumes an iterator, returns nil"
  {:added "4.0"}
  [it]
  (xt/for:iter [e it])
  (return nil))

(defn.xt arr<
  "converts an array to iterator"
  {:added "4.0"}
  [it]
  (var out [])
  (xt/for:iter [e it]
    (xt/x:arr-push out e))
  (return out))

(defn.xt obj<
  "converts an array to object"
  {:added "4.0"}
  [it]
  (var out {})
  (xt/for:iter [e it]
    (xt/x:set-key out
                  (xt/x:first e)
                  (xt/x:second e)))
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
  (var arr (:? (xt/x:is-array? seq) seq (-/arr< seq)))
  (if (== 0 (xt/x:len arr))
    (xt/x:err "Cannot be empty"))
  (while true
    (xt/for:array [e arr]
      (yield e))))

(defgen.xt range
  "setup a range function"
  {:added "4.0"}
  [x]
  (var arr    (:? (xt/x:is-array? x) x [x]))
  (var arrlen (xt/x:len arr))
  (var start  (:? (< 1 arrlen) (xt/x:first arr) 0))
  (var finish (:? (< 1 arrlen) (xt/x:second arr) (xt/x:first arr)))
  (var step   (:? (< 2 arrlen) (xt/x:get-idx arr 2) 1))
  (var i start)
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
   (var i n)
   (xt/for:iter [e (-/iter seq)]
     (if (< 0 i)
       (:= i (- i 1))
       (yield e)))))

(defgen.xt peek
  "peeks at value and passes it on"
  {:added "4.0"}
  ([f seq]
   (xt/for:iter [e (-/iter seq)]
     (f e)
     (yield e))))

(defgen.xt take
  "take elements from seq"
  {:added "4.0"}
  ([n seq]
   (var i 0)
   (xt/for:iter [e (-/iter seq)]
     (if (< i n)
       (do (:= i (+ i 1))
           (yield e))
       (return)))))

(defgen.xt map
  "maps a function across seq"
  {:added "4.0"}
  ([f seq]
   (xt/for:iter [e (-/iter seq)]
     (yield (f e)))))

(defgen.xt mapcat
  "maps a function a concats"
  {:added "4.0"}
  ([f seq]
   (xt/for:iter [e0 (-/iter seq)]
     (var s0 (f e0))
     (xt/for:iter [e1 (-/iter s0)]
       (yield e1)))))

(defgen.xt concat
  "concats seqs into iterator"
  {:added "4.0"}
  ([seq]
   (xt/for:iter [e (-/mapcat (fn:> [x] x) seq)]
     (yield e))))

(defgen.xt filter
  "filters a seq using a function"
  {:added "4.0"}
  ([pred seq]
   (xt/for:iter [e (-/iter seq)]
     (if (pred e)
       (yield e)))))

(defgen.xt keep
  "keeps a seq using a function"
  {:added "4.0"}
  ([f seq]
   (xt/for:iter [e (-/iter seq)]
     (var v (f e))
     (if v (yield v)))))

(defgen.xt partition
  "partition seq into n items"
  {:added "4.0"}
  ([n seq]
   (if (> 1 n)
     (xt/x:err "Partition should be positive"))
   (var out [])
   (xt/for:iter [e (-/iter seq)]
     (if (< (xt/x:len out) n)
       (xt/x:arr-push out e)
       (do (yield out)
           (:= out []))))
   (if (< 1 (xt/x:len out))
     (yield out))))

(defgen.xt take-nth
  "takes first and then every nth item of a seq"
  {:added "4.0"}
  ([n seq]
   (if (> 1 n)
     (xt/x:err "Partition should be positive"))
   (var i 0)
   (xt/for:iter [e (-/iter seq)]
     (if (== i 0)
       (do (yield e)
           (:= i (- n 1)))
       (:= i (- i 1))))))
