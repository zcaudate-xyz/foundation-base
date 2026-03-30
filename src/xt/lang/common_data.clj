(ns xt.lang.common-data
  (:require [std.lang :as l :refer [defspec.xt]])
  (:refer-clojure :exclude [first second nth last get-in]))

(l/script :xtalk
  {:require [[xt.lang.common-lib :as lib]]})

(def$.xt arr-push x:arr-push)
(def$.xt arr-pop x:arr-pop)
(def$.xt arr-push-first x:arr-push-first)
(def$.xt arr-pop-first x:arr-pop-first)
(def$.xt arr-insert x:arr-insert)


(defn.xt arr?
  "checks if object is an array"
  {:added "4.1"}
  [x]
  (return (x:is-array? x)))

(defn.xt obj?
  "checks if object is a map type"
  {:added "4.1"}
  [x]
  (return (x:is-object? x)))

(defn.xt first
  "gets the first item"
  {:added "4.1"}
  [arr]
  (return (x:get-idx arr (x:offset 0))))

(defn.xt second
  "gets the second item"
  {:added "4.1"}
  [arr]
  (return (x:get-idx arr (x:offset 1))))

(defn.xt arr-first
  "gets the first item"
  {:added "4.1"}
  [arr]
  (return (x:get-idx arr (x:offset 0))))

(defn.xt arr-second
  "gets the second item"
  {:added "4.1"}
  [arr]
  (return (x:get-idx arr (x:offset 1))))

(defn.xt arr-last
  "gets the first item"
  {:added "4.1"}
  [arr]
  (return (x:arr-last arr)))

(defn.xt arr-second-last
  "gets the second item"
  {:added "4.1"}
  [arr]
  (return (x:arr-second-last arr)))

(defn.xt nth
  "gets the nth item"
  {:added "4.1"}
  [arr i]
  (return (x:get-idx arr (x:offset i))))

(defn.xt last
  "gets the last item"
  {:added "4.1"}
  [arr]
  (return (x:get-idx arr (x:offset (x:len arr)))))

(defn.xt is-empty?
  "checks that value is empty"
  {:added "4.1"}
  [res]
  (cond (x:nil? res) (return true)
        (x:is-string? res) (return (== 0 (x:str-len res)))
        (x:is-array? res) (return (== 0 (x:len res)))
        (x:is-object? res)
        (do (for:object [[k v] res]
              (return false))
            (return true))
        :else
        (return false)))

(defn.xt not-empty?
  "checks that value is not empty"
  {:added "4.1"}
  [res]
  (return (not (-/is-empty? res))))

(defn.xt arrayify
  "makes something into an array"
  {:added "4.1"}
  [x]
  (return
   (:? (x:is-array? x)
       x
       (== nil x)
       []
       :else [x])))

(defn.xt obj-keys
  "gets keys of an object"
  {:added "4.1"}
  [obj]
  (var out := [])
  (when (x:not-nil? obj)
    (for:object [[k _] obj]
      (x:arr-push out k)))
  (return out))

(defn.xt get-in
  "gets item in object"
  {:added "4.1"}
  [obj arr]
  (cond (x:nil? obj)
        (return nil)
        (== 0 (x:len arr))
        (return obj)
        (== 1 (x:len arr))
        (return (x:get-key obj (-/arr-first arr)))
        :else
        (do (var total := (x:len arr))
            (var i := 0)
            (var curr := obj)
            (while (< i total)
              (var k (x:get-idx arr (x:offset i)))
              (:= curr (x:get-key curr k))
              (if (x:nil? curr)
                (return nil)
                (:= i (+ i 1))))
            (return curr))))

(defn.xt path-fn
  "creates a getter from a path"
  {:added "4.1"}
  [path]
  (return (fn:> [x] (-/get-in x path))))

(defn.xt set-in
  "sets item in object"
  {:added "4.1"}
  [obj arr v]
  (cond (== 0 (x:len (or arr [])))
        (return obj)
        (not (x:is-object? obj))
        (do (var idx := (x:len arr))
            (var out := v)
            (while true
              (if (== idx 0)
                (return out))
              (var nested := {})
              (var k (x:get-idx arr (x:offset-rev idx)))
              (x:set-key nested k out)
              (:= out nested)
              (:= idx (- idx 1))))
        :else
        (do (var k := (-/arr-first arr))
            (var narr := [])
            (for:index [i [1 (x:len arr)]]
              (x:arr-push narr (x:get-idx arr i)))
            (var child := (x:get-key obj k))
            (if (== 0 (x:len narr))
              (x:set-key obj k v)
              (x:set-key obj k (-/set-in child narr v)))
            (return obj))))

(defn.xt obj-difference
  "finds the difference between two map lookups"
  {:added "4.1"}
  [obj other]
  (var out := [])
  (for:object [[k _] other]
    (if (not (x:has-key? obj k))
      (x:arr-push out k)))
  (return out))

(defn.xt eq-basic
  "basic shallow equality comparator"
  {:added "4.1"}
  [src dst eq-obj eq-arr cache]
  (return (== src dst)))

(defn.xt eq-nested-loop
  "switch for nested check"
  {:added "4.1"}
  [src dst eq-obj eq-arr cache]
  (cond (and (x:is-object? src) (x:is-object? dst))
        (if (and cache
                 (x:lu-get cache src)
                 (x:lu-get cache dst))
          (return true)
          (return (eq-obj src dst eq-obj eq-arr (or cache (x:lu-create)))))

        (and (x:is-array? src) (x:is-array? dst))
        (if (and cache
                 (x:lu-get cache src)
                 (x:lu-get cache dst))
          (return true)
          (return (eq-arr src dst eq-obj eq-arr (or cache (x:lu-create)))))

        :else
        (return (== src dst))))

(defn.xt eq-nested-obj
  "checks object equality"
  {:added "4.1"}
  [src dst eq-obj eq-arr cache]
  (x:lu-set cache src src)
  (x:lu-set cache dst dst)
  (var ks-src (-/obj-keys src))
  (var ks-dst (-/obj-keys dst))
  (if (not= (x:len ks-src) (x:len ks-dst))
    (return false))
  (for:array [k ks-src]
    (if (not (-/eq-nested-loop (x:get-key src k)
                               (x:get-key dst k)
                               eq-obj
                               eq-arr
                               cache))
      (return false)))
  (return true))

(defn.xt eq-nested-arr
  "checks array equality"
  {:added "4.1"}
  [src-arr dst-arr eq-obj eq-arr cache]
  (x:lu-set cache src-arr src-arr)
  (x:lu-set cache dst-arr dst-arr)
  (if (not= (x:len src-arr) (x:len dst-arr))
    (return false))
  (for:array [[i v] src-arr]
    (if (not (-/eq-nested-loop v
                               (. dst-arr [i])
                               eq-obj
                               eq-arr
                               cache))
      (return false)))
  (return true))

(defn.xt eq-nested
  "checks for nested equality"
  {:added "4.1"}
  [obj m]
  (return (-/eq-nested-loop
           obj m
           -/eq-nested-obj
           -/eq-nested-arr
           nil)))

(defn.xt eq-shallow
  "checks for shallow equality"
  {:added "4.1"}
  [obj m]
  (return (-/eq-nested-loop
           obj m
           -/eq-basic
           -/eq-basic
           nil)))

(defn.xt obj-diff
  "diffs only keys within map"
  {:added "4.1"}
  [obj m]
  (if (x:nil? m)   (return {}))
  (if (x:nil? obj) (return m))
  (var out := {})
  (for:object [[k v] m]
    (if (not (-/eq-nested (x:get-key obj k)
                          (x:get-key m k)))
      (x:set-key out k v)))
  (return out))

(defn.xt obj-diff-nested
  "diffs nested keys within map"
  {:added "4.1"}
  [obj m]
  (if (x:nil? m)   (return {}))
  (if (x:nil? obj) (return m))
  (var out := {})
  (var ks (-/obj-keys m))
  (for:array [k ks]
    (var v   (x:get-key obj k))
    (var mv  (x:get-key m k))
    (cond (and (x:is-object? v) (x:is-object? mv))
          (do (var dv (-/obj-diff-nested v mv))
              (if (not (-/is-empty? dv))
                (x:set-key out k dv)))
          (not (-/eq-nested v mv))
          (x:set-key out k mv)))
  (return out))
