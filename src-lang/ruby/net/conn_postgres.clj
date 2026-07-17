(ns ruby.net.conn-postgres
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :ruby
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.conn-sql :as conn-sql]]})

;;
;; minimal postgres client over the psql CLI (no pg gem available)
;; mirrors python.net.conn-postgres semantics
;;

(defn.rb default-env
  "gets the default postgres env"
  {:added "4.1"}
  []
  (return {:host     "127.0.0.1"
           :port     5432
           :user     "postgres"
           :password "postgres"
           :database "postgres"}))

(defn.rb decode-json-scalar
  [value]
  (cond (and (xt/x:is-string? value)
             (or (. value (start_with? "["))
                 (. value (start_with? "{"))
                 (== value "true")
                 (== value "false")
                 (== value "null")))
        (return (xt/x:json-decode value))

        :else
        (return value)))

(defn.rb strip-trailing-semicolon
  [query]
  (var trimmed (xt/x:str-trim-right query))
  (while (. trimmed (end_with? ";"))
    (:= trimmed (xt/x:str-trim-right (. trimmed (chomp ";")))))
  (return trimmed))

(defn.rb row-query?
  [query]
  (var first-word (. (. (. (xt/x:str-trim query) split) [0]) upcase))
  (return (or (== first-word "SELECT")
              (== first-word "WITH")
              (== first-word "VALUES")
              (== first-word "TABLE")
              (== first-word "SHOW"))))

(defn.rb run-psql
  "runs a sql string through psql, passing params via argv and PGPASSWORD via env"
  {:added "4.1"}
  [env sql]
  (require "open3")
  (require "json")
  (var res (. (:- "Open3") (capture3
                            {"PGPASSWORD" (xt/x:get-key env "password")}
                            "psql"
                            "-h" (xt/x:get-key env "host")
                            "-p" (xt/x:to-string (xt/x:get-key env "port"))
                            "-U" (xt/x:get-key env "user")
                            "-d" (xt/x:get-key env "database")
                            "-A" "-t"
                            "-c" sql)))
  (var out (. res [0]))
  (var err-out (. res [1]))
  (var status (. res [2]))
  (when (not (. status success?))
    (xt/x:err (xt/x:cat "psql query failed: " err-out)))
  (return (. out strip)))

(defn.rb raw-query
  "runs a raw postgres query through psql and normalises the result shape"
  {:added "4.1"}
  [env query]
  (var stripped (-/strip-trailing-semicolon query))
  (var is-row (-/row-query? stripped))
  (when (not is-row)
    (-/run-psql env stripped)
    (return []))
  (var out (-/run-psql env (xt/x:cat "SELECT json_agg(row_to_json(__t)) FROM ("
                                     stripped
                                     ") __t")))
  (when (== out "")
    (return []))
  (var rows (xt/x:json-decode out))
  (cond (== 0 (xt/x:len rows))
        (return [])

        (and (== 1 (xt/x:len rows))
             (== 1 (xt/x:len (. (. rows [0]) values))))
        (return (-/decode-json-scalar
                 (. (. (. rows [0]) values) [0])))

        :else
        (return rows)))

(defn.rb client-connect
  [client opts]
  (var #{defaults} client)
  (var env (-> (-/default-env)
               (xt/x:obj-assign defaults)
               (xt/x:obj-assign (or opts {}))))
  (xt/x:set-key client "env" env)
  (return client))

(defn.rb client-disconnect
  [client]
  (return true))

(defn.rb client-query
  [client query]
  (var #{env} client)
  (return (-/raw-query env query)))

(defn.rb client-query-async
  [client query]
  (return (promise/x:promise-run (-/client-query client query))))

(defimpl.xt ^{:lang :ruby}
  RubyPostgresClient
  [defaults env]
  conn-sql/ISqlClient
  {conn-sql/connect      -/client-connect
   conn-sql/disconnect   -/client-disconnect
   conn-sql/query        -/client-query
   conn-sql/query-async  -/client-query-async})

(defn.rb create
  [defaults]
  (var client (-/RubyPostgresClient (or defaults {}) nil))
  (xt/x:set-key client "::/override"
                {"connect" -/client-connect
                 "disconnect" -/client-disconnect
                 "query" -/client-query
                 "query_async" -/client-query-async})
  (return client))
