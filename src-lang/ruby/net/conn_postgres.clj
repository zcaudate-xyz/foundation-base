(ns ruby.net.conn-postgres
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :ruby
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-resource :as rt]
             [xt.net.conn-sql :as conn-sql]]})

(defn.rb default-env []
  (return (or (rt/xt-config "ruby.net.conn-postgres")
              {:host "127.0.0.1"
               :port 5432
               :user "postgres"
               :password "postgres"
               :database "test"})))

(defn.rb default-env-set [m]
  (var env (xtd/obj-assign (xtd/obj-clone (-/default-env)) m))
  (rt/xt-config-set "ruby.net.conn-postgres" env)
  (return env))

(defn.rb coerce-number-string [value]
  (when (not (xt/x:is-string? value)) (return value))
  (var trimmed (. value (strip)))
  (when (== trimmed "") (return value))
  (try
    (return (Integer trimmed))
    (catch err
      (try (return (Float trimmed))
           (catch err (return value))))))

(defn.rb normalise-query-output [rows]
  (cond (== 0 (xt/x:len rows)) (return [])
        (and (== 1 (xt/x:len rows))
             (== 1 (xt/x:len (. rows [0]))))
        (return (-/coerce-number-string (. rows [0] [0])))
        :else (return rows)))

(defn.rb client-connect [client opts]
  (require "pg")
  (var env (xtd/obj-clone (-/default-env)))
  (xtd/obj-assign env (or (xt/x:get-key client "defaults") {}))
  (xtd/obj-assign env (or opts {}))
  (var database (xt/x:get-key env "database"))
  (when (xt/x:not-nil? database)
    (xt/x:set-key env "dbname" database)
    (xt/x:del-key env "database"))
  (var raw (. PG (connect env)))
  (xt/x:set-key client "raw" raw)
  (return client))

(defn.rb client-disconnect [client]
  (. (xt/x:get-key client "raw") (close))
  (return true))

(defn.rb client-query [client query]
  (var result (. (xt/x:get-key client "raw") (exec query)))
  (var rows (. result (values)))
  (. result (clear))
  (return (-/normalise-query-output rows)))

(defn.rb client-query-async [client query]
  (return (promise/x:promise-run (-/client-query client query))))

(defimpl.xt ^{:lang :ruby}
  RubyPostgresClient [defaults raw]
  conn-sql/ISqlClient
  {conn-sql/connect -/client-connect
   conn-sql/disconnect -/client-disconnect
   conn-sql/query -/client-query
   conn-sql/query-async -/client-query-async})

(defn.rb create [defaults]
  (return (-/RubyPostgresClient (or defaults {}) nil)))
