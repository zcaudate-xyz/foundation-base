(ns xtalk.lib.db.call
  (:require [std.lang :as l]
            [xtalk.lib.db.check :as check]
            [xtalk.lib.db.sql :as sql]))

(l/script :xtalk
  {:require [[xtalk.lib.db.check :as check]
             [xtalk.lib.db.sql :as sql]]})

(defn.xt decode-return
  "decodes the return value"
  {:added "4.1"}
  [outstr alt]
  (var out (x:json-decode outstr))
  (var status (x:get-key out "status"))
  (var data   (x:get-key out "data"))
  (when (== "error" status)
    (x:err (x:cat "ERR - API: " outstr)))
  (return data))

(defn.xt call-format-input
  "formats the inputs"
  {:added "4.1"}
  [spec args]
  (var targs (x:get-key spec "input"))
  (var out [])
  (var i := 0)
  (while (< i (x:len args))
    (var arg   (x:get-idx args i))
    (var input (x:get-idx targs i))
    (var dbarg nil)
    (if (== (x:get-key input "type")
            "jsonb")
      (if (x:is-string? arg)
        (:= dbarg arg)
        (:= dbarg (sql/encode-json arg)))
      (:= dbarg (sql/encode-value arg)))
    (x:arr-push out dbarg)
    (:= i (+ i 1)))
  (return out))

(defn.xt call-format-query
  "formats a query"
  {:added "4.1"}
  [spec args]
  (var schema (x:get-key spec "schema"))
  (var id     (x:get-key spec "id"))
  (var dbname (x:cat "\"" schema "\"." (x:str-replace id "-" "_")))
  (var dbargs (x:str-join ", " (-/call-format-input spec args)))
  (return (x:cat "SELECT " dbname "(" dbargs ");")))

(defn.xt call-check-input
  "checks args against a database function spec"
  {:added "4.1"}
  [spec args]
  (var targs (x:get-key spec "input"))
  (var [l-ok l-err] (check/check-args-length args targs))
  (when (not l-ok)
    (return [l-ok l-err]))
  (var [t-ok t-err] (check/check-args-type args targs))
  (when (not t-ok)
    (return [t-ok t-err]))
  (return [true]))
