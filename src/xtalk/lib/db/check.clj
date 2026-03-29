(ns xtalk.lib.db.check
  (:require [std.lang :as l]))

(l/script :xtalk
  {})

(defn.xt is-uuid?
  "checks that a string input is a uuid"
  {:added "4.1"}
  [s]
  (when (not (x:is-string? s))
    (return false))
  (when (not (== 36 (x:str-len s)))
    (return false))
  (var markers [8 13 18 23])
  (var i := 0)
  (while (< i (x:len markers))
    (var pos (x:get-idx markers i))
    (when (not (== "-" (x:str-substring s (x:offset pos) (+ pos 1))))
      (return false))
    (:= i (+ i 1)))
  (return true))

(defn.xt check-arg-type
  "checks the arg type of an input"
  {:added "4.1"}
  [arg-type arg]
  (cond (== arg-type "any")
        (return true)

        (or (== arg-type "citext")
            (== arg-type "inet")
            (== arg-type "text"))
        (return (x:is-string? arg))

        (== arg-type "uuid")
        (return (-/is-uuid? arg))

        (== arg-type "boolean")
        (return (x:is-boolean? arg))

        (or (== arg-type "integer")
            (== arg-type "int")
            (== arg-type "long")
            (== arg-type "bigint")
            (== arg-type "float"))
        (return (x:is-number? arg))

        (== arg-type "numeric")
        (return (or (x:is-number? arg)
                    (x:is-string? arg)))

        (== arg-type "jsonb")
        (return (or (x:is-object? arg)
                    (x:is-array? arg)))

        :else
        (return false)))

(defn.xt check-args-type
  "checks the arg type of inputs"
  {:added "4.1"}
  [args targs]
  (var i := 0)
  (while (< i (x:len targs))
    (var spec (x:get-idx targs i))
    (var arg  (x:get-idx args i))
    (when (not (-/check-arg-type (x:get-key spec "type") arg))
      (return [false {:status "error"
                      :tag "net/arg-typecheck-failed"
                      :data {:input arg
                             :spec spec}}]))
    (:= i (+ i 1)))
  (return [true]))

(defn.xt check-args-length
  "checks that input and spec are of the same length"
  {:added "4.1"}
  [args targs]
  (when (not= (x:len args)
              (x:len targs))
    (return [false {:status "error"
                    :tag "net/args-not-same-length"
                    :data {:expected (x:len targs)
                           :actual (x:len args)
                           :input args}}]))
  (return [true]))
