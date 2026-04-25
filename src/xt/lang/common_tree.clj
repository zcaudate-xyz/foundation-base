(ns xt.lang.common-tree
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as xtl]
             [xt.lang.common-data :as xtd]]})

;;
;; EQUALITY
;;


(defn.xt eq-nested-loop
  "switch for nested check"
  {:added "4.1"}
  [src dst eq-obj eq-arr cache]
  (when (xt/x:nil? cache)
    (:= cache (xt/x:lu-create)))
  (cond (and (xt/x:is-object? src) (xt/x:is-object? dst))
        (if (and (xt/x:not-nil? (xt/x:lu-get cache src))
                 (xt/x:not-nil? (xt/x:lu-get cache dst)))
          (return true)
          (return (eq-obj src dst eq-obj eq-arr cache)))
        
        (and (xt/x:is-array? src) (xt/x:is-array? dst))
        (if (and (xt/x:not-nil? (xt/x:lu-get cache src))
                 (xt/x:not-nil? (xt/x:lu-get cache dst)))
          (return true)
          (return (eq-arr src dst eq-obj eq-arr cache)))
        
        :else
        (return (== src dst))))

(defn.xt eq-shallow-raw
  "basic shallow equality comparator"
  {:added "4.1"}
  [src dst eq-obj eq-arr cache]
  (return (xt/x:lu-eq src dst)))

(defn.xt eq-shallow
  "checks for shallow equality"
  {:added "4.1"}
  [obj m]
  (return (-/eq-nested-loop
           obj m
           -/eq-shallow-raw
           -/eq-shallow-raw
           nil)))



(defn.xt eq-nested-obj
  "checks object equality"
  {:added "4.1"}
  [src dst eq-obj eq-arr cache]
  (xt/x:lu-set cache src src)
  (xt/x:lu-set cache dst dst)
  (var ks-src (xt/x:obj-keys src))
  (var ks-dst (xt/x:obj-keys dst))
  (if (not= (xt/x:len ks-src) (xt/x:len ks-dst))
    (return false))
  (xt/for:array [k ks-src]
    (if (not (-/eq-nested-loop (xt/x:get-key src k)
                               (xt/x:get-key dst k)
                               eq-obj
                               eq-arr
                               cache))
      (return false)))
  (return true))

(defn.xt eq-nested-arr
  "checks array equality"
  {:added "4.1"}
  [src-arr dst-arr eq-obj eq-arr cache]
  (xt/x:lu-set cache src-arr src-arr)
  (xt/x:lu-set cache dst-arr dst-arr)
  (if (not= (xt/x:len src-arr) (xt/x:len dst-arr))
    (return false))
  (xt/for:array [[i v] src-arr]
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

;;
;; TREE
;;

(defn.xt tree-walk
  "walks over object"
  {:added "4.0"}
  [x pre-fn post-fn]
  (:= x (pre-fn x))
  (cond (xt/x:nil? x)
        (return (post-fn x))
        
        (xt/x:is-object? x)
        (do (var out := {})
            (xt/for:object [[k v] x]
              (xt/x:set-key out k (-/tree-walk v pre-fn post-fn)))
            (return (post-fn out)))

        (xt/x:is-array? x)
        (do (var out := [])
            (xt/for:array [e x]
              (xt/x:arr-push out (-/tree-walk e pre-fn post-fn)))
            (return (post-fn out)))

        :else
        (return (post-fn x))))

(defn.xt tree-get-data
  "normalizes nested data values for inspection"
  {:added "4.1"}
  [obj]
  (cond (xt/x:nil? obj)
        (return obj)

        (xt/x:is-object? obj)
        (do (var out := {})
            (xt/for:object [[k v] obj]
              (xt/x:set-key out k (-/tree-get-data v)))
            (return out))

        (xt/x:is-array? obj)
        (do (var out := [])
            (xt/for:array [e obj]
              (xt/x:arr-push out (-/tree-get-data e)))
            (return out))

        (or (xt/x:is-string? obj)
            (xt/x:is-number? obj)
            (xt/x:is-boolean? obj))
        (return obj)

        :else
        (return (xt/x:cat "<" (xtl/type-native obj) ">"))))

(defn.xt tree-get-spec
  "normalizes nested values to their runtime-native types"
  {:added "4.1"}
  [obj]
  (cond (xt/x:is-object? obj)
        (do (var out := {})
            (xt/for:object [[k v] obj]
              (xt/x:set-key out k (-/tree-get-spec v)))
            (return out))

        (xt/x:is-array? obj)
        (do (var out := [])
            (xt/for:array [e obj]
              (xt/x:arr-push out (-/tree-get-spec e)))
            (return out))

        :else
        (return (xtl/type-native obj))))

(defn.xt tree-diff
  "diffs only keys within map"
  {:added "4.1"}
  [obj m]
  (if (xt/x:nil? m)   (return {}))
  (if (xt/x:nil? obj) (return m))
  (var out {})
  (xt/for:object [[k v] m]
    (if (not (-/eq-nested (xt/x:get-key obj k)
                          (xt/x:get-key m k)))
      (xt/x:set-key out k v)))
  (return out))

(defn.xt tree-diff-nested
  "diffs nested keys within map"
  {:added "4.1"}
  [obj m]
  (if (xt/x:nil? m)   (return {}))
  (if (xt/x:nil? obj) (return m))
  (var out {})
  (var ks (xt/x:obj-keys m))
  (xt/for:array [k ks]
    (var v   (xt/x:get-key obj k))
    (var mv  (xt/x:get-key m k))
    (cond (and (xt/x:is-object? v) (xt/x:is-object? mv))
          (do (var dv (-/tree-diff-nested v mv))
              (if (not (xtd/obj-empty? dv))
                (xt/x:set-key out k dv)))
          (not (-/eq-nested v mv))
          (xt/x:set-key out k mv)))
  (return out))

