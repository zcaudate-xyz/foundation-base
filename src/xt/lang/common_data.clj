(ns xt.lang.common-data
  (:require [std.lang :as l :refer [defspec.xt]])
  (:refer-clojure :exclude [first second nth last get-in]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

;;
;; EMPTY
;;

(defn.xt is-empty?
  "checks that array is not empty"
  {:added "4.0"}
  [res]
  (cond (xt/x:nil? res) (return true)
        (xt/x:is-string? res) (return (== 0 (xt/x:str-len res)))
        (xt/x:is-array? res)  (return (== 0 (xt/x:len res)))
        (xt/x:is-object? res)
        (do (xt/for:object [[i v] res]
              (return false))
            (return true))
        
        :else
        (do (xt/x:err (xt/x:cat "Invalid type - "
                                (xt/x:to-string res))))))

(defn.xt not-empty?
  "checks that array is not empty"
  {:added "4.0"}
  [res]
  (cond (xt/x:nil? res) (return false)
        (xt/x:is-string? res) (return (< 0 (xt/x:str-len res)))
        (xt/x:is-array? res) (return (< 0 (xt/x:len res)))
        (xt/x:is-object? res)
        (do (xt/for:object [[i v] res]
              (return true))
            (return false))
        
        :else
        (do (xt/x:err (xt/x:cat "Invalid type - "
                                (xt/x:to-string res))))))

;;
;; LOOKUP
;;

(defn.xt lu-create
  ([]
   (return (xt/x:lu-create))))

(defn.xt lu-del
  ([lu key]
   (xt/x:lu-del lu key)
   (return lu)))

(defn.xt lu-get
  ([lu key]
   (return (xt/x:lu-get lu key))))

(defn.xt lu-set
  ([lu key value]
   (xt/x:lu-set lu key value)
   (return lu)))

(defn.xt lu-eq
  ([x y]
   (return (xt/x:lu-eq x y))))

;;
;; ARRAY INDEX
;;

(defn.xt first
  ([arr]
   (return (xt/x:first arr))))

(defn.xt second
  "gets the second item"
  {:added "4.0"}
  ([arr]
   (return (xt/x:second arr))))

(defn.xt nth
  "gets the nth item (index 0)"
  {:added "4.0"}
  ([arr i]
   (return (xt/x:get-idx arr (xt/x:offset i)))))

(defn.xt last
  "gets the last item"
  {:added "4.0"}
  ([arr]
   (return (xt/x:last arr))))

(defn.xt second-last
  "gets the second-last item"
  {:added "4.0"}
  ([arr]
   (return (xt/x:second-last arr))))


;;
;; ARRAY CONTAINER
;;

(defn.xt arr-empty?
  "checks that arrect is empty"
  {:added "4.0"}
  [arr]
  (if (xt/x:nil? arr)
    (return true)
    (return (== 0 (xt/x:len arr)))))

(defn.xt arr-not-empty?
  "checks that arrect is not empty"
  {:added "4.0"}
  [arr]
  (if (xt/x:nil? arr)
    (return false)
    (return (not= 0 (xt/x:len arr)))))

(defn.xt arrayify
  "makes something into an array"
  {:added "4.1"}
  [x]
  (when (xt/x:is-array? x)
    (return x))
  (when (xt/x:nil? x)
    (return []))
  (return [x]))

(defn.xt arr-lookup
  "constructs a lookup given keys"
  {:added "4.0"}
  [arr]
  (var out {})
  (xt/for:array [k arr]
    (xt/x:set-key out k true))
  (return out))

(defn.xt arr-omit
  "emits index from new array"
  {:added "4.0"}
  ([arr i]
   (var out [])
   (xt/for:array [[j e] arr]
     (when (not= (xt/x:offset i) j)
       (xt/x:arr-push out e)))
   (return out)))

(defn.xt arr-reverse
  "reverses the array"
  {:added "4.0"}
  [arr]
  (var out [])
  (xt/for:index [i [(xt/x:len arr)
                    (xt/x:offset)
                    -1]]
    (xt/x:arr-push out (xt/x:get-idx arr (xt/x:offset-rev i))))
  (return out))

(defn.xt arr-zip
  "zips two arrays together into a map"
  {:added "4.0"}
  [ks vs]
  (var out {})
  (xt/for:array [[i k] ks]
    (xt/x:copy-key out vs [k i]))
  (return out))

(defn.xt arr-clone
  "clones an array"
  {:added "4.0"}
  ([arr]
   (var out [])
   (xt/for:array [e arr]
     (xt/x:arr-push out e))
   (return out)))

(defn.xt arr-assign
  "others to the end of an array"
  {:added "4.0"}
  ([arr other]
   (xt/for:array [e other]
     (xt/x:arr-push arr e))
   (return arr)))

(defn.xt arr-concat
  "others to the end of an array"
  {:added "4.0"}
  ([arr other]
   (var out [])
   (xt/for:array [e arr]
     (xt/x:arr-push out e))
   (xt/for:array [e other]
     (xt/x:arr-push out e))
   (return out)))

(defn.xt arr-slice
  "slices an array"
  {:added "4.0"}
  ([arr start finish]
   (var out [])
   (var finish-idx nil)
   (if (xt/x:is-number? finish)
     (:= finish-idx finish)
     (:= finish-idx (xt/x:len arr)))
   (xt/for:index [i [(xt/x:offset start)
                     (xt/x:offset finish-idx)]]
     (xt/x:arr-push out (xt/x:get-idx arr i)))
   (return out)))

(defn.xt arr-rslice
  "gets the reverse of a slice"
  {:added "4.0"}
  ([arr start finish]
   (var out [])
   (xt/for:index [i [(xt/x:offset start)
                     (xt/x:offset finish)]]
     (xt/x:arr-push-first out (xt/x:get-idx arr i)))
   (return out)))

(defn.xt arr-tail
  "gets the tail of the array"
  {:added "4.0"}
  ([arr n]
   (var t  (xt/x:len arr))
   (return (-/arr-rslice arr (xt/x:m-max (- t n) 0) t))))

(defn.xt arr-range
  "creates a range array"
  {:added "4.0"}
  [x]
  (var arr [x])
  (when (xt/x:is-array? x)
    (:= arr x))
  (var arrlen (xt/x:len arr))
  (var start 0)
  (when (< 1 arrlen)
    (:= start (xt/x:first arr)))
  (var finish (xt/x:first arr))
  (when (< 1 arrlen)
    (:= finish (xt/x:second arr)))
  (var step 1)
  (when (< 2 arrlen)
    (:= step (xt/x:get-idx arr (xt/x:offset 2))))
  (var out [start])
  (var i (+ step start))
  (cond (and (< 0 step)
             (< start finish))
        (while (< i finish)
          (xt/x:arr-push out i)
          (:= i (+ i step)))
        
        (and (> 0 step)
             (< finish start))
        (while (> i finish)
          (xt/x:arr-push out i)
          (:= i (+ i step)))
        
        :else (return []))
  (return out))

(defn.xt arr-intersection
  "gets the intersection of two arrays"
  {:added "4.0"}
  [arr other]
  (var lu  (-/arr-lookup arr))
  (var out [])
  (xt/for:array [e other]
    (if (xt/x:has-key? lu e)
      (xt/x:arr-push out e)))
  (return out))

(defn.xt arr-difference
  "gets the difference of two arrays"
  {:added "4.0"}
  [arr other]
  (var lu  (-/arr-lookup arr))
  (var out [])
  (xt/for:array [e other]
    (if (not (xt/x:has-key? lu e))
      (xt/x:arr-push out e)))
  (return out))

(defn.xt arr-union
  "gets the union of two arrays"
  {:added "4.0"}
  [arr other]
  (var lu {})
  (xt/for:array [e arr]
    (xt/x:set-key lu e e))
  (xt/for:array [e other]
    (xt/x:set-key lu e e))
  
  (var out [])
  (xt/for:object [[_ v] lu]
    (xt/x:arr-push out v))
  (return out))

(defn.xt arr-shuffle
  "shuffles the array"
  {:added "4.0"}
  [arr]
  (var tmp-val nil)
  (var tmp-idx nil)
  (var total (xt/x:len arr))
  (xt/for:index [i [(xt/x:offset) total]]
    (:= tmp-idx (+ (xt/x:offset) (xt/x:m-floor (* (xt/x:random) total))))
    (:= tmp-val (xt/x:get-idx arr tmp-idx))
    (xt/x:set-idx arr tmp-idx (xt/x:get-idx arr i))
    (xt/x:set-idx arr i tmp-val))
  (return arr))

(defn.xt arr-pushl
  "pushs an element into array"
  {:added "4.0"}
  [arr v n]
  (xt/x:arr-push arr v)
  (when (> (xt/x:len arr) n)
    (xt/x:arr-pop-first arr))
  (return arr))

(defn.xt arr-pushr
  "pushs an element into array"
  {:added "4.0"}
  [arr v n]
  (xt/x:arr-push-first arr v)
  (when (> (xt/x:len arr) n)
    (xt/x:arr-pop arr))
  (return arr))

(defn.xt arr-interpose
  "puts element between array"
  {:added "4.0"}
  [arr elem]
  (var out [])
  (xt/for:array [e arr]
    (xt/x:arr-push out e)
    (xt/x:arr-push out elem))
  (xt/x:arr-pop out)
  (return out))

(defn.xt arr-random
  "gets a random element from array"
  {:added "4.0"}
  [arr]
  (var idx (xt/x:m-floor (* (xt/x:len arr) (xt/x:random))))
  (return (xt/x:get-idx arr (xt/x:offset idx))))

(defn.xt arr-sample
  "samples array according to probability"
  {:added "4.0"}
  [arr dist]
  (var q (xt/x:random))
  (xt/for:array [[i p] dist]
    (:= q (- q p))
    (when (< q 0)
      (return (. arr [i])))))


;;
;; OBJECT
;;


(defn.xt obj-empty?
  "checks that object is empty"
  {:added "4.0"}
  [obj]
  (xt/for:object [[k _] obj]
    (return false))
  (return true))

(defn.xt obj-not-empty?
  "checks that object is not empty"
  {:added "4.0"}
  [obj]
  (xt/for:object [[k _] obj]
    (return true))
  (return false))

(defn.xt obj-first-key
  "gets the first key"
  {:added "4.0"}
  [obj]
  (xt/for:object [[k _] obj]
    (return k))
  (return nil))

(defn.xt obj-first-val
  "gets the first val"
  {:added "4.0"}
  [obj]
  (xt/for:object [[_ v] obj]
    (return v))
  (return nil))

(defn.xt obj-keys
  "gets keys of an object"
  {:added "4.0"}
  [obj]
  (var out [])
  (when (xt/x:not-nil? obj)
    (xt/for:object [[k _] obj]
      (xt/x:arr-push out k)))
  (return out))

(defn.xt obj-vals
  "gets vals of an object"
  {:added "4.0"}
  [obj]
  (var out [])
  (when (xt/x:not-nil? obj)
    (xt/for:object [[_ v] obj]
      (xt/x:arr-push out v)))
  (return out))

(defn.xt obj-pairs
  "creates entry pairs from object"
  {:added "4.0"}
  ([obj]
   (var out [])
   (when (xt/x:not-nil? obj)
     (xt/for:object [[k v] obj]
       (xt/x:arr-push out [k v])))
   (return out)))

(defn.xt obj-clone
  "clones an object"
  {:added "4.0"}
  [obj]
  (var out {})
  (when (xt/x:not-nil? obj)
    (xt/for:object [[k v] obj]
      (xt/x:set-key out k v)))
  (return out))

(defn.xt obj-assign
  "merges key value pairs from into another"
  {:added "4.0"}
  [obj m]
  (when (xt/x:nil? obj)
    (:= obj {}))
  (if (xt/x:not-nil? m)
    (xt/for:object [[k v] m]
      (xt/x:set-key obj k v)))
  (return obj))

(defn.xt obj-assign-nested
  "merges objects at a nesting level"
  {:added "4.0"}
  ([obj m]
   (when (xt/x:nil? obj)
     (:= obj {}))
   (when (xt/x:not-nil? m)
     (xt/for:object [[k mv] m]
       (var v (xt/x:get-key obj k))
       (cond (and (xt/x:is-object? mv)
                  (xt/x:is-object? v))
             (xt/x:set-key obj k (-/obj-assign-nested v mv))
             
             :else
             (xt/x:set-key obj k mv))))
   (return obj)))

(defn.xt obj-assign-with
  "merges second into first given a function"
  {:added "4.0"}
  [obj m f]
  (when (xt/x:not-nil? m)
    (var input {})
    (when (xt/x:is-object? m)
      (:= input m))
    (xt/for:object [[k mv] input]
      (var merged mv)
      (when (xt/x:has-key? obj k)
        (:= merged (f (xt/x:get-key obj k)
                      mv)))
      (xt/x:set-key obj k merged)))
  (return obj))

(defn.xt obj-from-pairs
  "creates an object from pairs"
  {:added "4.0"}
  ([pairs]
   (var out {})
   (xt/for:array [pair pairs]
     (xt/x:set-key out
                   (xt/x:first pair)
                   (xt/x:second pair)))
   (return out)))

(defn.xt obj-del
  "deletes multiple keys"
  {:added "4.0"}
  [obj ks]
  (xt/for:array [k ks]
    (xt/x:del-key obj k))
  (return obj))

(defn.xt ^{:static/template true}
  obj-del-all
  "deletes all keys"
  {:added "4.0"}
  [obj]
  (xt/for:array [k (xt/x:obj-keys obj)]
    (xt/x:del-key obj k))
  (return obj))

(defn.xt obj-pick
  "select keys in object"
  {:added "4.0"}
  [obj ks]
  (var out {})
  (when (xt/x:nil? obj)
    (return out))
  (xt/for:array [k ks]
    (var v (xt/x:get-key obj k))
    (if (xt/x:not-nil? v)
      (xt/x:set-key out k v)))
  (return out))

(defn.xt obj-omit
  "new object with missing keys"
  {:added "4.0"}
  [obj ks]
  (var out {})
  (var lu {})
  (xt/for:array [k ks]
    (xt/x:set-key lu k true))
  (xt/for:object [[k v] obj]
    (if (not (xt/x:has-key? lu k))
      (xt/x:set-key out k v)))
  (return out))

(defn.xt obj-transpose
  "obj-transposes a map"
  {:added "4.0"}
  [obj]
  (var out {})
  (when (xt/x:not-nil? obj)
    (xt/for:object [[k v] obj]
      (xt/x:set-key out v k)))
  (return out))

(defn.xt obj-nest
  "creates a nested object"
  {:added "4.0"}
  [arr v]
  (var idx (xt/x:len arr))
  (var out v)
  (while true
    (if (== idx 0)
      (return out))
    (var nested {})
    (var k (xt/x:get-idx arr (xt/x:offset-rev idx)))
    (xt/x:set-key nested k out)
    (:= out nested)
    (:= idx (- idx 1))))

(defn.xt get-in
  "gets item in object"
  {:added "4.1"}
  [obj arr]
  (cond (xt/x:nil? obj)
        (return nil)

        (== 0 (xt/x:len arr))
        (return obj)

        (== 1 (xt/x:len arr))
        (return (xt/x:get-key obj (xt/x:first arr))))

  (var total (xt/x:len arr))
  (var i 0)
  (var curr obj)
  (while (< i total)
    (var k (xt/x:get-idx arr (xt/x:offset i)))
    (:= curr (xt/x:get-key curr k))
    (if (xt/x:nil? curr)
      (return nil)
      (:= i (+ i 1))))
  (return curr))

(defn.xt set-in
  "sets item in object"
  {:added "4.1"}
  [obj arr v]
  (when (xt/x:nil? arr)
    (:= arr []))
  (when (== 0 (xt/x:len arr))
    (return obj))

  ;; If the current branch does not exist yet, build the remaining path.
  (when (not (xt/x:is-object? obj))
    (var idx (xt/x:len arr))
    (var out v)
    (while true
      (if (== idx 0)
        (return out))
      (var nested {})
      (var k (xt/x:get-idx arr (xt/x:offset-rev idx)))
      (xt/x:set-key nested k out)
      (:= out nested)
      (:= idx (- idx 1))))
  
  (var k (xt/x:first arr))
  (var narr (-/arr-slice arr 1 nil))
  (var child (xt/x:get-key obj k))
  (if (== 0 (xt/x:len narr))
    (xt/x:set-key obj k v)
    (xt/x:set-key obj k (-/set-in child narr v)))
  (return obj))


(defn.xt obj-intersection
  "finds the intersection between map lookups"
  {:added "4.0"}
  [obj other]
  (var out [])
  (xt/for:object [[k _] other]
    (if (xt/x:has-key? obj k)
      (xt/x:arr-push out k)))
  (return out))

(defn.xt obj-difference
  "finds the difference between two map lookups"
  {:added "4.0"}
  [obj other]
  (var out [])
  (xt/for:object [[k _] other]
    (if (not (xt/x:has-key? obj k))
      (xt/x:arr-push out k)))
  (return out))

(defn.xt obj-keys-nested
  "gets nested keys"
  {:added "4.0"}
  [m path]
  (var out [])
  (xt/for:object [[k v] m]
    (var npath [(xt/x:unpack path)])
    (xt/x:arr-push npath k)
    
    (cond (xt/x:is-object? v)
          (do (xt/for:array [e (-/obj-keys-nested v npath)]
                (xt/x:arr-push out e)))
          
          :else
          (xt/x:arr-push out [npath v])))
  (return out))

(defn.xt obj-difference
  "finds the difference between two map lookups"
  {:added "4.1"}
  [obj other]
  (var out [])
  (xt/for:object [[k _] other]
    (if (not (xt/x:has-key? obj k))
      (xt/x:arr-push out k)))
  (return out))

(defn.xt swap-key
  "swaps a value in the key with a function"
  {:added "4.0"}
  ([obj k f args]
   (var inputs (xt/x:arr-clone args))
   (xt/x:arr-push-first inputs (xt/x:get-key obj k))
   (xt/x:set-key obj k (xt/x:apply f inputs))
   (return obj)))

;;
;; FLAT
;;

(defn.xt to-flat
  "flattens pairs of object into array"
  {:added "4.0"}
  ([obj]
   (var out [])
   (cond (xt/x:is-object? obj)
         (xt/for:object [[k v] obj]
           (xt/x:arr-push out k)
           (xt/x:arr-push out v))
         
         (xt/x:is-array? obj)
         (xt/for:array [e obj]
           (xt/x:arr-push out (xt/x:first e))
           (xt/x:arr-push out (xt/x:second e))))
   (return out)))

(defn.xt set-pair-step
  "sets a pair into an object and returns it"
  {:added "4.1"}
  [out k v]
  (xt/x:set-key out k v)
  (return out))

(defn.xt from-flat
  "creates object from flattened pair array"
  {:added "4.0"}
  ([arr f init]
   (var out init)
   (var k nil)
   (xt/for:array [[i e] arr]
     (if (xt/x:even? (xt/x:offset i))
       (:= k e)
       (:= out (f out k e))))
   (return out)))


;;
;; EQUALITY
;;

(defn.xt eq-nested-basic
  "basic shallow equality comparator"
  {:added "4.1"}
  [src dst eq-obj eq-arr cache]
  (return (== src dst)))

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

(defn.xt eq-shallow
  "checks for shallow equality"
  {:added "4.1"}
  [obj m]
  (return (-/eq-nested-loop
           obj m
           -/eq-nested-basic
           -/eq-nested-basic
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

(defn.xt tree-type-native
  "gets the normalized native type for tree helpers"
  {:added "4.1"}
  [obj]
  (xt/x:type-native obj))

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
        (return (xt/x:cat "<" (-/tree-type-native obj) ">"))))

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
        (return (-/tree-type-native obj))))


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
              (if (not (-/obj-empty? dv))
                (xt/x:set-key out k dv)))
          (not (-/eq-nested v mv))
          (xt/x:set-key out k mv)))
  (return out))


;;
;; ARRAY FUNCTIONAL
;;

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
  (var i 0)
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
  (var out init)
  (xt/for:array [e arr]
    (:= out (f out e)))
  (return out))

(defn.xt arr-foldr
  "performs right reduce"
  {:added "4.0"}
  [arr f init]
  (var out init)
  (xt/for:index [i [(xt/x:len arr)
                    (xt/x:offset)
                    -1]]
    (:= out (f out (xt/x:get-idx arr (xt/x:offset-rev i)))))
  (return out))

(defn.xt arr-pipel
  "thrushes an input through a function pipeline"
  {:added "4.0"}
  [arr e]
  (return (xt/x:arr-foldl arr
                          (fn [x f]
                            (return (f x)))
                          e)))

(defn.xt arr-piper
  "thrushes an input through a function pipeline from reverse"
  {:added "4.0"}
  [arr e]
  (return (xt/x:arr-foldr arr (fn [x f]
                                (return (f x)))
                          
                          e)))

(defn.xt arr-group-by
  "groups elements by key and view functions"
  {:added "4.0"}
  ([arr key-fn view-fn]
   (var out {})
   (when (xt/x:not-nil? arr)
     (xt/for:array [e arr]
       (var g (key-fn e))
       (var garr (xt/x:get-key out g []))
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
    (var item x)
    (when (xt/x:is-function? x)
      (:= item (x)))
    (xt/x:arr-push out item))
  (return out))

(defn.xt arr-normalise
  "normalises array elements to 1"
  {:added "4.0"}
  [arr]
  (var total (xt/x:arr-foldl arr (fn [x y]
                                   (return (+ x y)))
                             0))
  (return (xt/x:arr-map arr (fn [x] (return (/ x total))))))

(defn.xt arr-sort
  "arr-sort using key function and comparator"
  {:added "4.0"}
  [arr key-fn comp-fn]
  (var out [])
  (xt/for:array [e arr]
    (var inserted false)
    (xt/for:index [i [(xt/x:offset) (xt/x:len out)]]
      (when (and (not inserted)
                 (comp-fn (key-fn e)
                          (key-fn (xt/x:get-idx out i))))
        (xt/x:arr-insert out i e)
        (:= inserted true)))
    (when (not inserted)
      (xt/x:arr-push out e)))
  (return out))

(defn.xt arr-sorted-merge
  "performs a merge on two sorted arrays"
  {:added "4.0"}
  [arr brr comp-fn]
  (when (xt/x:nil? arr)
    (:= arr []))
  (when (xt/x:nil? brr)
    (:= brr []))
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


;;
;; OBJECT FUNCTIONAL
;;

(defn.xt obj-map
  "maps a function across the values of an object"
  {:added "4.0"}
  [obj f]
  (var out {})
  (when (xt/x:not-nil? obj)
    (xt/for:object [[k v] obj]
      (xt/x:set-key out k (f v))))
  (return out))

(defn.xt obj-filter
  "applies a filter across the values of an object"
  {:added "4.0"}
  [obj pred]
  (var out {})
  (when (xt/x:not-nil? obj)
    (xt/for:object [[k v] obj]
      (if (pred v)
        (xt/x:set-key out k v))))
  (return out))

(defn.xt obj-keep
  "applies a transform across the values of an object, keeping non-nil values"
  {:added "4.0"}
  [obj f]
  (var out {})
  (when (xt/x:not-nil? obj)
    (xt/for:object [[k e] obj]
      (var v (f e))
      (if (xt/x:not-nil? v)
        (xt/x:set-key out k v))))
  (return out))

(defn.xt obj-keepf
  "applies a transform and filter across the values of an object"
  {:added "4.0"}
  [obj pred f]
  (var out {})
  (when (xt/x:not-nil? obj)
    (xt/for:object [[k e] obj]
      (if (pred e)
        (xt/x:set-key out k (f e)))))
  (return out))

;;
;; CLONE
;;

(defn.xt clone-shallow
  "shallow clones an object or array"
  {:added "4.0"}
  [x]
  (cond (xt/x:nil? x) (return x)
        (xt/x:is-object? x)  (return (xt/x:arr-clone x))
        (xt/x:is-array?  x)  (return (xt/x:obj-clone x))
        :else (return x)))

(defn.xt clone-nested-loop
  "clone nested objects loop"
  {:added "4.0"}
  [x lu]
  (when (xt/x:nil? x)
    (return x))
  
  (var  cached (xt/x:lu-get lu x))
  (cond (xt/x:not-nil? cached)
        (return cached)
        
        (xt/x:is-object? x)
        (do (var out {})
            (xt/x:lu-set lu x out)
            (xt/for:object [[k v] x]
              (xt/x:set-key out k (-/clone-nested-loop v lu)))
            (return out))
        
        (xt/x:is-array? x)
        (do (var out [])
            (xt/x:lu-set lu x out)
            (xt/for:array [e x]
              (xt/x:arr-push out (-/clone-nested-loop e lu)))
            (return out))

        :else
        (return x)))

(defn.xt clone-nested
  "cloning nested xects"
  {:added "4.0"}
  [x]
  (cond (not (or (xt/x:is-object? x)
                 (xt/x:is-array? x)))
        (return x)

        :else
        (return (-/clone-nested-loop x (xt/x:lu-create)))))

;;
;; MEMOIZE
;;

(defn.xt memoize-key-step
  "computes and caches a memoized value"
  {:added "4.1"}
  [f key cache]
  (var value (f key))
  (xt/x:set-key cache key value)
  (return value))

(defn.xt memoize-key
  "memoize for functions of single argument"
  {:added "4.0"}
  [f]
  (var cache {})
  (var cache-fn (fn [key]
                  (return (-/memoize-key-step f key cache))))
  (return (fn [key]
            (return (or (xt/x:get-key cache key)
                        (cache-fn key))))))
