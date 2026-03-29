(ns xtalk.lib.db.sql
  (:require [std.lang :as l]))

(l/script :xtalk
  {})

(defn.xt encode-bool
  "encodes a boolean to sql"
  {:added "4.1"}
  [b]
  (cond (== b true) (return "TRUE")
        (== b false) (return "FALSE")
        :else (x:err "Not Valid")))

(defn.xt encode-number
  "encodes a number to sql"
  {:added "4.1"}
  [v]
  (return (x:cat "'" (x:to-string v) "'")))

(defn.xt encode-json
  "encodes a json value"
  {:added "4.1"}
  [v]
  (return (x:cat "'"
                 (x:str-replace (x:json-encode v)
                                "'" "''")
                 "'")))

(defn.xt encode-value
  "encodes a value to sql"
  {:added "4.1"}
  [v]
  (cond (x:nil? v)
        (return "NULL")

        (x:is-string? v)
        (return (x:cat "'"
                       (x:str-replace v "'" "''")
                       "'"))

        (x:is-boolean? v)
        (return (-/encode-bool v))

        (or (x:is-array? v)
            (x:is-object? v))
        (return (-/encode-json v))

        (x:is-number? v)
        (return (-/encode-number v))

        :else
        (return (x:cat "'"
                       (x:to-string v)
                       "'"))))
