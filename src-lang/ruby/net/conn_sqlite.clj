(ns ruby.net.conn-sqlite
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :ruby
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.conn-sql :as conn-sql]]})

(defn.rb query-returns-rows? [query]
  (var sql (. (. query (strip)) (downcase)))
  (return (or (. sql (start_with? "select"))
              (. sql (start_with? "pragma"))
              (. sql (start_with? "with"))
              (. sql (start_with? "values"))
              (. sql (start_with? "explain")))))

(defn.rb query-multi-statement? [query]
  (var statements
       (xt/x:arr-filter
        (. query (split ";"))
        (fn [part] (return (> (xt/x:len (. part (strip))) 0)))))
  (return (> (xt/x:len statements) 1)))

(defn.rb decode-json-scalar [value]
  (if (and (xt/x:is-string? value)
           (or (. value (start_with? "["))
               (. value (start_with? "{"))
               (== value "true")
               (== value "false")
               (== value "null")))
    (try (return (xt/x:json-decode value))
         (catch err (return value))))
  (return value))

(defn.rb normalise-query-output [rows]
  (cond (== 0 (xt/x:len rows)) (return [])
        (and (== 1 (xt/x:len rows))
             (== 1 (xt/x:len (. rows [0]))))
        (return (-/decode-json-scalar (. rows [0] [0])))
        :else (return rows)))

(defn.rb client-connect [client opts]
  (require "sqlite3")
  (var config (xt/x:obj-assign
               (xt/x:obj-clone (or (xt/x:get-key client "defaults") {}))
               (or opts {})))
  (var klass (xt/x:eval "SQLite3::Database"))
  (var raw (. klass (new (or (xt/x:get-key config "filename") ":memory:"))))
  (xt/x:set-key client "raw" raw)
  (return client))

(defn.rb client-disconnect [client]
  (. (xt/x:get-key client "raw") (close))
  (return true))

(defn.rb client-query [client query]
  (var raw (xt/x:get-key client "raw"))
  (if (and (not (-/query-returns-rows? query))
           (-/query-multi-statement? query))
    (do (. raw (execute_batch query)) (return [])))
  (return (-/normalise-query-output (. raw (execute query)))))

(defn.rb client-query-async [client query]
  (return (promise/x:promise-run (-/client-query client query))))

(defimpl.xt ^{:lang :ruby}
  RubySqliteClient [defaults raw]
  conn-sql/ISqlClient
  {conn-sql/connect -/client-connect
   conn-sql/disconnect -/client-disconnect
   conn-sql/query -/client-query
   conn-sql/query-async -/client-query-async})

(defn.rb create [defaults]
  (return (-/RubySqliteClient (or defaults {}) nil)))
