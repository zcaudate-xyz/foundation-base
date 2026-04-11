(ns xt.lang.common-func
  (:require [std.lang :as l :refer [defspec.xt]])
  (:refer-clojure :exclude []))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]]})



(defn.xt arr-every
  "checks that every element fulfills thet predicate"
  {:added "4.0"}
  [arr pred]
  (xt/for:array [[i v] arr]
    (if (not (pred v))
      (return false)))
  (return true))

(defn.xt arr-some
  "checks that the array contains an element"
  {:added "4.0"}
  [arr pred]
  (xt/for:array [[i v] arr]
    (if (pred v)
      (return true)))
  (return false))

(defn.xt arr-each
  "performs a function call for each element"
  {:added "4.0"}
  ([arr f]
   (xt/for:array [e arr] (f e))
   (return true)))

(defn.xt arr-find
  "finds first index matching predicate"
  {:added "4.0"}
  [arr pred]
  (xt/for:array [[i v] arr]
    (when (pred v)
      (return (- i (xt/x:offset)))))
  (return -1))

(defn.xt arr-map
  "maps a function across an array"
  {:added "4.0"}
  ([arr f]
   (var out [])
   (xt/for:array [e arr]
     (xt/x:arr-push out (f e)))
   (return out)))

(defn.xt arr-mapcat
  "maps an array function, concatenting results"
  {:added "4.0"}
  [arr f]
  (var out [])
  (xt/for:array [e arr]
    (var res (f e))
    (if (xt/x:not-nil? res)
      (xt/for:array [v res]
        (xt/x:arr-push out v))))
  (return out))

(defn.xt arr-partition
  "partitions an array into arrays of length n"
  {:added "4.0"}
  [arr n]
  (var out [])
  (var i := 0)
  (var sarr [])
  (xt/for:array [e arr]
    (when (== i n)
      (xt/x:arr-push out sarr)
      (:= i 0)
      (:= sarr []))
    (xt/x:arr-push sarr e)
    (:= i (+ i 1)))
  (when (< 0 (xt/x:len sarr))
    (xt/x:arr-push out sarr))
  (return out))

(defn.xt arr-filter
  "applies a filter across an array"
  {:added "4.0"}
  ([arr pred]
   (var out [])
   (xt/for:array [e arr]
     (if (pred e)
       (xt/x:arr-push out e)))
   (return out)))

(defn.xt arr-keep
  "keeps items in an array if output is not nil"
  {:added "4.0"}
  [arr f]
  (var out [])
  (xt/for:array [e arr]
    (var v (f e))
    (if (xt/x:not-nil? v)
      (xt/x:arr-push out v)))
  (return out))

(defn.xt arr-keepf
  "keeps items in an array with transform if predicate holds"
  {:added "4.0"}
  [arr pred f]
  (var out [])
  (xt/for:array [e arr]
    (if (pred e)
      (xt/x:arr-push out (f e))))
  (return out))

(defn.xt arr-juxt
  "constructs a map given a array of pairs"
  {:added "4.0"}
  [arr key-fn val-fn]
  (var out {})
  (when (xt/x:not-nil? arr)
    (xt/for:array [e arr]
      (xt/x:set-key out (key-fn e)
                    (val-fn e))))
  (return out))

(defn.xt arr-foldl
  "performs reduce on an array"
  {:added "4.0"}
  [arr f init]
  (var out := init)
  (xt/for:array [e arr]
    (:= out (f out e)))
  (return out))

(defn.xt arr-foldr
  "performs right reduce"
  {:added "4.0"}
  [arr f init]
  (var out := init)
  (xt/for:index [i [(xt/x:len arr)
                    (xt/x:offset)
                    -1]]
    (:= out (f out (xt/x:get-idx arr (xt/x:offset-rev i)))))
  (return out))

(defn.xt arr-pipel
  "thrushes an input through a function pipeline"
  {:added "4.0"}
  [arr e]
  (return (-/arr-foldl arr -/step-thrush e)))

(defn.xt arr-piper
  "thrushes an input through a function pipeline from reverse"
  {:added "4.0"}
  [arr e]
  (return (-/arr-foldr arr -/step-thrush e)))

(defn.xt arr-group-by
  "groups elements by key and view functions"
  {:added "4.0"}
  ([arr key-fn view-fn]
   (var out {})
   (when (xt/x:not-nil? arr)
     (xt/for:array [e arr]
       (var g := (key-fn e))
       (var garr := (xt/x:get-key out g []))
       (xt/x:set-key out g [])
       (xt/x:arr-push garr (view-fn e))
       (xt/x:set-key out g garr)))
   (return out)))

(defn.xt arr-repeat
  "repeat function or value n times"
  {:added "4.0"}
  [x n]
  (var out [])
  (xt/for:index [i [0 (- n (xt/x:offset))]]
    (xt/x:arr-push out (:? (xt/x:is-function? x)
                           (x)
                           x)))
  (return out))

(defn.xt arr-normalise
  "normalises array elements to 1"
  {:added "4.0"}
  [arr]
  (var total (-/arr-foldl arr -/add 0))
  (return (-/arr-map arr (fn:> [x] (/ x total)))))

(defn.xt arr-sort
  "arr-sort using key function and comparator"
  {:added "4.0"}
  [arr key-fn comp-fn]
  (var out := (xt/x:arr-clone arr))
  (xt/x:arr-sort out key-fn comp-fn)
  (return out))

(defn.xt arr-sorted-merge
  "performs a merge on two sorted arrays"
  {:added "4.0"}
  [arr brr comp-fn]
  (:= arr (or arr []))
  (:= brr (or brr []))
  (var alen (xt/x:len arr))
  (var blen (xt/x:len brr))
  (var i 0)
  (var j 0)
  (var k 0)
  (var out [])
  (while (and (< i alen)
              (< j blen))
    (var aitem (xt/x:get-idx arr (xt/x:offset i)))
    (var bitem (xt/x:get-idx brr (xt/x:offset j)))
    (cond (comp-fn aitem bitem)
          (do (:= i (+ i 1)) 
              (xt/x:arr-push out aitem))

          :else
          (do (:= j (+ j 1))
              (xt/x:arr-push out bitem))))

  (while (< i alen)
    (var aitem (xt/x:get-idx arr (xt/x:offset i)))
    (:= i (+ i 1))
    (xt/x:arr-push out aitem))

  (while (< j blen)
    (var bitem (xt/x:get-idx brr (xt/x:offset j)))
    (:= j (+ j 1))
    (xt/x:arr-push out bitem))
  (return out))
