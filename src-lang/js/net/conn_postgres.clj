(ns js.net.conn-postgres
  (:require [hara.lang :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [print send]))

(l/script :js
  {:require [[xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.net.conn-sql :as conn-sql]]
   :import [["pg" :as [* Postgres]]]})

(def$.js Client Postgres.Client)

(defn.js coerce-number-string
  [value]
  (if (not (xt/x:is-string? value))
    (return value))
  (var trimmed (. value (trim)))
  (if (== trimmed "")
    (return value))
  (if (. trimmed (match "^[+-]?(?:\\d+(?:\\.\\d+)?|\\.\\d+)(?:[eE][+-]?\\d+)?$"))
    (return (xt/x:to-number trimmed))
    (return value)))

(defn.js normalise-scalar-output
  [value]
  (cond (or (xt/x:nil? value)
            (xt/x:is-boolean? value)
            (xt/x:is-array? value)
            (xt/x:is-object? value))
        (return value)

        (xt/x:is-string? value)
        (return (-/coerce-number-string value))

        :else
        (return value)))

(defn.js normalise-query-output
  [res]
  (var #{rows} res)
  (if (and (== 1 rows.length)
           (== 1 (xt/x:len (xtd/obj-keys (xtd/first rows)))))
    (return (-/normalise-scalar-output
             (xtd/obj-first-val (xtd/first rows))))
    (return rows)))

(defn.js client-connect
  "constructs the postgres instance"
  {:added "4.0"}
  [client opts]
  (var #{defaults} client)
  (var conn (new -/Client (-> {:host "127.0.0.1",
                               :port 5432
                               :user "postgres",
                               :password "postgres"
                               :database "postgres"},
                              (xt/x:obj-assign defaults)
                              (xt/x:obj-assign opts))))
  (return
   (. conn (connect)
      (then (fn []
              (xt/x:set-key client "raw" conn)
              (return client))))))

(defn.js create-methods
  []
  (return
   {"connect"     -/client-connect
    "disconnect"  (fn [client]
                    (var #{raw} client)
                    (. raw (end))
                    (xt/x:del-key client "raw")
                    (return client))
    "query"       (fn [client input]
                    (throw "Not Allowed"))
    "query_async" (fn [client input]
                    (var #{raw} client)
                    (return
                     (. raw
                        (query input)
                        (then -/normalise-query-output))))}))

(defn.js create
  [defaults]
  (return
   (conn-sql/create-base "js.net.conn-postgres"
                         (-/create-methods)
                         defaults)))



