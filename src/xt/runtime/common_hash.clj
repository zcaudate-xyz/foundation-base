(ns xt.runtime.common-hash
  (:require [std.lang :as l]))

;;
;; JS
;;

(l/script :js
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-iter :as it]
             [xt.lang.common-runtime :as rt]]})

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
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-iter :as it]
             [xt.lang.common-runtime :as rt]]})

(defn.lua hash-float
  "hashes a floating point"
  {:added "4.0"}
  [f]
  (var '[m e] (math.frexp f))
  (return (xt/x:bit-and
           (+ (xt/x:m-floor (* (- m 0.5)
                               (pow 2 31)))
               e)
            (:- "0xFFFFFF"))))

;;
;; PYTHON
;;

(l/script :python
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-iter :as it]
             [xt.lang.common-runtime :as rt]]})

(defn.py hash-float
  "hashes a floating point"
  {:added "4.0"}
  [f]
  (var math (__import__ "math"))
  (var '[m e] (math.frexp f))
  (return (xt/x:bit-and
           (+ (xt/x:m-floor (* (- m 0.5)
                               (pow 2 31)))
               e)
            (:- "0xFFFFFF"))))

;;
;; XTALK
;;

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-iter :as it]
             [xt.lang.common-runtime :as rt]]})

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
  (xt/for:index [i [(xt/x:offset 0) (xt/x:len s)]]
    (:= hval (xt/x:bit-xor hval (xt/x:bit-and (xt/x:str-char s i)
                                         (:- "0xFF"))))
    (:= hval (+ hval
                (xt/x:bit-lshift hval 1)
                (xt/x:bit-lshift hval 4)
                (xt/x:bit-lshift hval 7)
                (xt/x:bit-lshift hval 24))))
  (return (xt/x:bit-and hval (:- "0xFFFFFF"))))

(defn.xt hash-iter
  "hashes an iterator"
  {:added "4.0"}
  [iter hash-fn]
  (var hval (:- "0x811c9dc5"))
  (xt/for:iter [e iter]
    (:= hval (xt/x:bit-xor hval (xt/x:bit-and (hash-fn e)
                                         (:- "0xFF"))))
    (:= hval (+ hval
                (xt/x:bit-lshift hval 1)
                (xt/x:bit-lshift hval 4)
                (xt/x:bit-lshift hval 7)
                (xt/x:bit-lshift hval 24))))
  (return (xt/x:bit-and hval (:- "0xFFFFFF"))))

(defn.xt hash-iter-unordered
  "hashes an unordered set"
  {:added "4.0"}
  [iter hash-fn]
  (var hval (:- "0x811c9dc5"))
  (xt/for:iter [e iter]
    (:= hval (xt/x:bit-xor hval (xt/x:bit-and (hash-fn e)
                                         (:- "0xFF")))))
  (return (xt/x:bit-and hval (:- "0xFFFFFF"))))

(defn.xt hash-integer
  "hashes an integer"
  {:added "4.0"}
  [n]
  (return (xt/x:bit-and n (:- "0xFFFFFF"))))

(defn.xt hash-boolean
  "hashes a boolean"
  {:added "4.0"}
  [s]
  (return (:? s 1 -1)))

(defn.xt hash-native
  "hashes a value"
  {:added "4.0"}
  [x]
  (var t (xt/x:type-native x))
  (cond (== t "nil")
        (return 0)
        
        (== t "string")
        (return (-/hash-string x))
        
        (== t "boolean")
        (return (-/hash-boolean x))

        (== t "number")
        (cond (xt/x:is-integer? x)
              (return (-/hash-integer x))

              :else
              (return (-/hash-float x)))

        (or (== t "array")
            (== t "function"))
        (return (rt/xt-lookup-id x))

        (== t "object")
        (return (or (xt/x:get-key x "hash")
                    (rt/xt-lookup-id x)))))
