(ns xt.db.sql-call
  (:require [std.lang :as l]
            [std.lib.foundation :as f]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.sql-util :as ut]
             [xt.db.base-check :as check]
             [xt.sys.conn-dbsql :as driver]]})

(defn.xt decode-return
  "decodes the return value"
  {:added "4.0"}
  [outstr alt]
  (var out (xt/x:json-decode outstr))
  (var #{status data} out)
  (when (== "error" status)
    (xt/x:err (xt/x:cat "ERR - API: " outstr)))
  (return data))

(defn.xt call-format-input
  "formats the inputs"
  {:added "4.0"}
  [spec args]
  (var targs  (xt/x:get-key spec "input"))
  (var out [])
  (xt/for:array [[i arg] args]
    (var input (xt/x:get-idx targs i))
    (var dbarg nil)
    (cond (== (xt/x:get-key input "type")
              "jsonb")
          (if (xt/x:is-string? arg)
            (:= dbarg arg)
            (:= dbarg (ut/encode-json arg)))

          :else
          (:= dbarg (ut/encode-value arg)))
    (xt/x:arr-push out dbarg))
  (return out))

(defn.xt call-format-query
  "formats a query"
  {:added "4.0"}
  [spec args]
  (var #{schema id} spec)
  (var dbname (xt/x:cat "\"" schema "\"." (xt/x:str-replace id "-" "_") ""))
  (var dbargs (xt/x:str-join ", " (-/call-format-input spec args)))
  (return (xt/x:cat "SELECT " dbname "(" dbargs  ");")))
  
(defn.xt call-raw
  "calls a database function"
  {:added "4.0"}
  [conn spec args cb]
  (var targs (xt/x:get-key spec "input"))
  (var [l-ok l-err] (check/check-args-length args targs))
  (when (not l-ok)
    (xt/x:err (xt/x:cat "ERR: - " (xt/x:json-encode l-err))))
  (var [t-ok t-err] (check/check-args-type args targs))
  (when (not t-ok)
    (xt/x:err (xt/x:cat "ERR: - " (xt/x:json-encode t-err))))
  (var q  (-/call-format-query spec args))
  (var success-fn
       (fn [val]
         (cond (== "jsonb" (xt/x:get-key spec "return"))
               (if (or (xt/x:nil? val)
                       (== val ""))
                 (return nil)
                 (return (:? (xt/x:is-string? val)
                             (xt/x:json-decode val)
                             val)))
               :else
               (return val))))
  (var error-fn
       (fn [err]
         (xt/x:err (xt/x:cat "ERR: - " (xt/x:json-encode err)))))
  (return (driver/query conn q
                        (xt/x:obj-assign
                         {:success  success-fn
                          :error    error-fn}
                         cb))))

(defn.xt call-api
  "results an api style result"
  {:added "4.0"}
  [conn spec args]
  (var targs (xt/x:get-key spec "input"))
  (var [l-ok l-err] (check/check-args-length args targs))
  (when (not l-ok)
    (return (xt/x:json-encode {:status "error"
                          :data l-err})))
  
  (var [t-ok t-err] (check/check-args-type args targs))
  (when (not t-ok)
    (return (xt/x:json-encode {:status "error"
                          :data t-err})))
  (var q  (-/call-format-query spec args))
  
  (var success-fn (fn [val]
                    (return
                     (xt/x:cat "{\"status\": \"ok\", \"data\":"
                            (:? (== "jsonb" (xt/x:get-key spec "return"))
                                (or val "null")
                                (xt/x:json-encode val))
                            "}"))))
  (var error-fn (fn [err]
                  (if (. err ["status"])
                    (return (xt/x:json-encode err))
                    (return (xt/x:json-encode {:status "error"
                                          :data err})))))
  (return (driver/query
           conn q
           {:success  success-fn
            :error    error-fn})))

(comment
  (./create-tests)
  
  (f/template-entries [l/tmpl-macro {:base "pgmoon"
                                     :inst "pg"
                                     :tag "lua"}]
                      [[connect           []]
                       [settimeout        [time]]
                       [disconnect        []]
                       [keepalive         [] {:vargs cmds}]
                       [query             [s]]
                       [escape_literal    [val]]
                       [escape_identifier [val]]]))
