(ns xt.db.base-check
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]
            [std.json :as json]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(defn.xt is-uuid?
  "checks that a string input is a uuid"
  {:added "4.0"}
  [s]
  (when (not (k/is-string? s))
    (return false))

  (when (not (== 36 (x:str-len s)))
    (return false))

  (k/for:array [i [8 13 18 23]]
    (when (not (== "-" (k/substring s i (+ i 1))))
      (return false)))

  (return true))

(defn.xt check-arg-type
  "checks the arg type of an input"
  {:added "4.0"}
  [arg-type arg]
  (cond (== arg-type "any")
        (return true)
        
        (or (== arg-type "citext")
            (== arg-type "inet")
            (== arg-type "text"))
        (return (k/is-string? arg))

        (== arg-type "uuid")
        (return (-/is-uuid? arg))
        
        (== arg-type "boolean")
        (return (k/is-boolean? arg))

        (or (== arg-type "integer")
            (== arg-type "int")
            (== arg-type "long")
            (== arg-type "bigint")
            (== arg-type "float"))
        (return (k/is-number? arg))

        (== arg-type "numeric")
        (return (or (k/is-number? arg)
                    (k/is-string? arg)))
        
        (== arg-type "jsonb")
        (return (or (k/obj? arg)
                    (k/arr? arg)))

        :else
        (return false)))

(defn.xt check-args-type
  "checks the arg type of inputs"
  {:added "4.0"}
  [args targs]
  ;;
  ;; CHECK TYPE
  ;;
  (k/for:array [[i spec] targs]
    (var arg (k/get-idx args i))
    (if (not (-/check-arg-type (k/get-key spec "type") arg))
      (return [false {:status "error"
                      :tag "net/arg-typecheck-failed"
                      :data {:input arg
                             :spec spec}}])))
    
  (return [true]))

(defn.xt check-args-length
  "checks that input and spec are of the same length"
  {:added "4.0"}
  [args targs]
  (when (not= (k/len args)
              (k/len targs))
    (return [false {:status "error"
                    :tag "net/args-not-same-length",
                    :data {:expected (k/len targs)
                           :actual (k/len args)
                           :input args}}]))
  (return [true]))

(comment
  (str (h/uuid))
  
  #_
  (defn.xt check-args
    [args meta]
    (var targs (. meta ["args"]))
    (var scope (or (. meta ["scope"]) {}))

    (when (not targs)
      (return))

    ;;
    ;; CHECK LENGTTH
    ;;
    
    (var is-debug (. scope ["debug"]))
    (var tlen (:? is-debug (- (len targs) 1) (len targs)))
    
    (-/check-args-length args tlen)
    (-/check-args-type args tlen targs)
    (return true)))

(comment
  (./create-tests)
  )
