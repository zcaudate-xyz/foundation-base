(ns xt.db.base-check
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]]})

(defn.xt is-uuid?
  "checks that a string input is a uuid"
  {:added "4.0"}
  [s]
  (when (not (xt/x:is-string? s))
    (return false))

  (when (not (== 36 (xt/x:str-len s)))
    (return false))

  (var parts (xt/x:str-split s "-"))
  (return
   (and (== 5 (xt/x:len parts))
        (== 8 (xt/x:str-len (xt/x:get-idx parts (xt/x:offset 0))))
        (== 4 (xt/x:str-len (xt/x:get-idx parts (xt/x:offset 1))))
        (== 4 (xt/x:str-len (xt/x:get-idx parts (xt/x:offset 2))))
        (== 4 (xt/x:str-len (xt/x:get-idx parts (xt/x:offset 3))))
        (== 12 (xt/x:str-len (xt/x:get-idx parts (xt/x:offset 4)))))))

(defn.xt check-arg-type
  "checks the arg type of an input"
  {:added "4.0"}
  [arg-type arg]
  (cond (== arg-type "any")
        (return true)
        
        (or (== arg-type "citext")
            (== arg-type "inet")
            (== arg-type "text"))
        (return (xt/x:is-string? arg))

        (== arg-type "uuid")
        (return (-/is-uuid? arg))
        
        (== arg-type "boolean")
        (return (xt/x:is-boolean? arg))

        (or (== arg-type "integer")
            (== arg-type "int")
            (== arg-type "long")
            (== arg-type "bigint")
            (== arg-type "float"))
        (return (xt/x:is-number? arg))

        (== arg-type "numeric")
        (return (or (xt/x:is-number? arg)
                    (xt/x:is-string? arg)))
        
        (== arg-type "jsonb")
        (return (or (xt/x:is-object? arg)
                    (xt/x:is-array? arg)))

        :else
        (return false)))

(defn.xt check-args-type
  "checks the arg type of inputs"
  {:added "4.0"}
  [args targs]
  ;;
  ;; CHECK TYPE
  ;;
  (xt/for:array [[i spec] targs]
    (var arg (xt/x:get-idx args i))
    (if (not (-/check-arg-type (xt/x:get-key spec "type") arg))
      (return [false {:status "error"
                      :tag "net/arg-typecheck-failed"
                      :data {:input arg
                             :spec spec}}])))
    
  (return [true]))

(defn.xt check-args-length
  "checks that input and spec are of the same length"
  {:added "4.0"}
  [args targs]
  (when (not= (xt/x:len args)
              (xt/x:len targs))
    (return [false {:status "error"
                    :tag "net/args-not-same-length",
                    :data {:expected (xt/x:len targs)
                           :actual (xt/x:len args)
                           :input args}}]))
  (return [true]))
