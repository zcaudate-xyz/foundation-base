(ns xt.runtime.common-hash
  (:require [std.lib :as h]
            [std.lang :as l]))

;;
;; JS
;;

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]
             [xt.lang.base-runtime :as rt]]})

(defn.js hash-float
  "hashes a floating point"
  {:added "4.0"}
  [f]
  (var dv (new DataView (new ArrayBuffer 4)))
  (. dv (setFloat32 0 f))
  (return (. dv (getInt32 0))))

;;
;; LUA
;;

(l/script :lua
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]
             [xt.lang.base-runtime :as rt]]})

(defn.lua hash-float
  "hashes a floating point"
  {:added "4.0"}
  [f]
  (var '[m e] (math.frexp f))
  (return (k/bit-and
           (+ (k/floor (* (- m 0.5)
                             (pow 2 31)))
              e)
           (:- "0xFFFFFF"))))

;;
;; PYTHON
;;

(l/script :python
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]
             [xt.lang.base-runtime :as rt]]})

(defn.py hash-float
  "hashes a floating point"
  {:added "4.0"}
  [f]
  (var math (__import__ "math"))
  (var '[m e] (math.frexp f))
  (return (k/bit-and
           (+ (k/floor (* (- m 0.5)
                             (pow 2 31)))
              e)
           (:- "0xFFFFFF"))))

;;
;; XTALK
;;

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]
             [xt.lang.base-runtime :as rt]]})

(defabstract.xt hash-float [f])

(def.xt SEED
  {"keyword" (:- "0x111c9dc5")
   "symbol"  (:- "0x211c9dc5")
   "var"     (:- "0x311c9dc5")})

(defn.xt hash-string
  "hashes a string"
  {:added "4.0"}
  [s]
  (var hval (:- "0x811c9dc5"))
  (k/for:index [i [(x:offset 0) (k/len s)]]
    (:= hval (k/bit-xor hval (k/bit-and (k/get-char s i)
                                        (:- "0xFF"))))
    (:= hval (+ hval
                (k/bit-lshift hval 1)
                (k/bit-lshift hval 4)
                (k/bit-lshift hval 7)
                (k/bit-lshift hval 24))))
  (return (k/bit-and hval (:- "0xFFFFFF"))))

(defn.xt hash-iter
  "hashes an iterator"
  {:added "4.0"}
  [iter hash-fn]
  (var hval (:- "0x811c9dc5"))
  (it/for:iter [e iter]
    (:= hval (k/bit-xor hval (k/bit-and (hash-fn e)
                                        (:- "0xFF"))))
    (:= hval (+ hval
                (k/bit-lshift hval 1)
                (k/bit-lshift hval 4)
                (k/bit-lshift hval 7)
                (k/bit-lshift hval 24))))
  (return (k/bit-and hval (:- "0xFFFFFF"))))

(defn.xt hash-iter-unordered
  "hashes an unordered set"
  {:added "4.0"}
  [iter hash-fn]
  (var hval (:- "0x811c9dc5"))
  (it/for:iter [e iter]
    (:= hval (k/bit-xor hval (k/bit-and (hash-fn e)
                                        (:- "0xFF")))))
  (return (k/bit-and hval (:- "0xFFFFFF"))))

(defn.xt hash-integer
  "hashes an integer"
  {:added "4.0"}
  [n]
  (return (k/bit-and n (:- "0xFFFFFF"))))

(defn.xt hash-boolean
  "hashes a boolean"
  {:added "4.0"}
  [s]
  (return (:? s 1 -1)))

(defn.xt hash-native
  "hashes a value"
  {:added "4.0"}
  [x]
  (var t (k/type-native x))
  (cond (== t "nil")
        (return 0)
        
        (== t "string")
        (return (-/hash-string x))
        
        (== t "boolean")
        (return (-/hash-boolean x))

        (== t "number")
        (cond (k/is-integer? x)
              (return (-/hash-integer x))

              :else
              (return (-/hash-float x)))

        (or (== t "array")
            (== t "function"))
        (return (rt/xt-lookup-id x))

        (== t "object")
        (return (or (k/get-key x "hash")
                    (rt/xt-lookup-id x)))))

