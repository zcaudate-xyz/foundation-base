(ns xt.db.text.sql-call
  (:require [hara.lang :as l]
            [std.lib.foundation :as f]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as spec-promise]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.base-check :as check]
             [xt.net.conn-sql :as conn-sql]]})

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
  (var targs  (. spec ["input"]))
  (var out [])
  (xt/for:array [[i arg] args]
    (var input (xt/x:get-idx targs i))
    (var dbarg nil)
    (cond (== (. input ["type"])
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
  [client spec args]
  (var targs (. spec ["input"]))
  (var l-ok-value (check/check-args-length args targs))
  (var [l-ok l-err] l-ok-value)
  (when (not l-ok)
    (xt/x:err (xt/x:cat "ERR: - " (xt/x:json-encode l-err))))
  (var t-ok-value (check/check-args-type args targs))
  (var [t-ok t-err] t-ok-value)
  (when (not t-ok)
    (xt/x:err (xt/x:cat "ERR: - " (xt/x:json-encode t-err))))
  (var q  (-/call-format-query spec args))
  (var success-fn
       (fn [val]
         (cond (== "jsonb" (. spec ["return"]))
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
  (return
   (-> (conn-sql/query-async client q)
       (spec-promise/x:promise-then success-fn)
       (spec-promise/x:promise-catch error-fn))))

(defn.xt call-api
  "results an api style result"
  {:added "4.0"}
  [client spec args]
  (var targs (. spec ["input"]))
  (var l-ok-value (check/check-args-length args targs))
  (var [l-ok l-err] l-ok-value)
  (when (not l-ok)
    (return (xt/x:json-encode {:status "error"
                          :data l-err})))
  
  (var t-ok-value (check/check-args-type args targs))
  (var [t-ok t-err] t-ok-value)
  (when (not t-ok)
    (return (xt/x:json-encode {:status "error"
                           :data t-err})))
  (var q  (-/call-format-query spec args))
  
  (var success-fn (fn [val]
                    (return
                     (xt/x:cat "{\"status\": \"ok\", \"data\":"
                               (:? (== "jsonb" (. spec ["return"]))
                                   (:? (xt/x:is-string? val)
                                       val
                                       (xt/x:json-encode val))
                                   (xt/x:json-encode val))
                               "}"))))
  (var error-fn (fn [err]
                  (if (. err ["status"])
                     (return (xt/x:json-encode err))
                     (return (xt/x:json-encode {:status "error"
                                                :data err})))))
  (return
   (-> (conn-sql/query-async client q)
       (spec-promise/x:promise-then success-fn)
       (spec-promise/x:promise-catch error-fn))))

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
