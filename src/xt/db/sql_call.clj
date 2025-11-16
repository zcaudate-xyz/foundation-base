(ns xt.db.sql-call
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.db.sql-util :as ut]
             [xt.db.base-check :as check]
             [xt.sys.conn-dbsql :as driver]]})

(defn.xt decode-return
  "decodes the return value"
  {:added "4.0"}
  [outstr alt]
  (var out (k/json-decode outstr))
  (var #{status data} out)
  (when (== "error" status)
    (k/err (k/cat "ERR - API: " outstr)))
  (return data))

(defn.xt call-format-input
  "formats the inputs"
  {:added "4.0"}
  [spec args]
  (var targs  (k/get-key spec "input"))
  (var out [])
  (k/for:array [[i arg] args]
    (var input (k/get-idx targs i))
    (var dbarg nil)
    (cond (== (k/get-key input "type")
              "jsonb")
          (if (k/is-string? arg)
            (:= dbarg arg)
            (:= dbarg (ut/encode-json arg)))

          :else
          (:= dbarg (ut/encode-value arg)))
    (x:arr-push out dbarg))
  (return out))

(defn.xt call-format-query
  "formats a query"
  {:added "4.0"}
  [spec args]
  (var #{schema id} spec)
  (var dbname (k/cat "\"" schema "\"." (k/replace id "-" "_") ""))
  (var dbargs (k/join ", " (-/call-format-input spec args)))
  (return (k/cat "SELECT " dbname "(" dbargs  ");")))
  
(defn.xt call-raw
  "calls a database function"
  {:added "4.0"}
  [conn spec args cb]
  (var targs (k/get-key spec "input"))
  (var [l-ok l-err] (check/check-args-length args targs))
  (when (not l-ok)
    (k/err (k/cat "ERR: - " (k/json-encode l-err))))
  (var [t-ok t-err] (check/check-args-type args targs))
  (when (not t-ok)
    (k/err (k/cat "ERR: - " (k/json-encode t-err))))
  (var q  (-/call-format-query spec args))
  (var success-fn
       (fn [val]
         (cond (== "jsonb" (k/get-key spec "return"))
               (if (or (k/nil? val)
                       (== val ""))
                 (return nil)
                 (return (:? (k/is-string? val)
                             (k/json-decode val)
                             val)))
               :else
               (return val))))
  (var error-fn
       (fn [err]
         (k/err (k/cat "ERR: - " (k/json-encode err)))))
  (return (driver/query conn q
                        (k/obj-assign
                         {:success  success-fn
                          :error    error-fn}
                         cb))))

(defn.xt call-api
  "results an api style result"
  {:added "4.0"}
  [conn spec args]
  (var targs (k/get-key spec "input"))
  (var [l-ok l-err] (check/check-args-length args targs))
  (when (not l-ok)
    (return (k/json-encode {:status "error"
                          :data l-err})))
  
  (var [t-ok t-err] (check/check-args-type args targs))
  (when (not t-ok)
    (return (k/json-encode {:status "error"
                          :data t-err})))
  (var q  (-/call-format-query spec args))
  
  (var success-fn (fn [val]
                    (return
                     (k/cat "{\"status\": \"ok\", \"data\":"
                            (:? (== "jsonb" (k/get-key spec "return"))
                                (or val "null")
                                (k/json-encode val))
                            "}"))))
  (var error-fn (fn [err]
                  (if (. err ["status"])
                    (return (k/json-encode err))
                    (return (k/json-encode {:status "error"
                                          :data err})))))
  (return (driver/query
           conn q
           {:success  success-fn
            :error    error-fn})))

(comment
  (./create-tests)
  
  (h/template-entries [l/tmpl-macro {:base "pgmoon"
                                     :inst "pg"
                                     :tag "lua"}]
                      [[connect           []]
                       [settimeout        [time]]
                       [disconnect        []]
                       [keepalive         [] {:vargs cmds}]
                       [query             [s]]
                       [escape_literal    [val]]
                       [escape_identifier [val]]]))
