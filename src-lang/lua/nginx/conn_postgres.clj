(ns lua.nginx.conn-postgres
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :lua.nginx
  {:import [["pgmoon" :as ngxpg]]
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-promise :as common-promise]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-resource :as rt]
             [xt.net.conn-sql :as conn-sql]]})

(defn.lua default-env
  "gets the default env"
  {:added "4.0"}
  []
  (return (or (rt/xt-config "lua.nginx.conn-postgres")
              {:host     "127.0.0.1"
               :port     "5432"
               :user     "postgres"
               :password "postgres"
               :database "test"
               :dev true})))

(defn.lua default-env-set
  "sets the default env"
  {:added "4.0"}
  [m]
  (var env (xtd/obj-assign (xtd/obj-clone (-/default-env)) m))
  (rt/xt-config-set "lua.nginx.conn-postgres" env)
  (return env))

(def.lua PGMSG
  {"S"  "status"
   "R"  "auth"
   "K"  "backend_key"
   "Z"  "ready_for_query"
   "N"  "notice"
   "A"  "notification"
   "p"  "password"
   "T"  "row_description"
   "D"  "data_row"
   "C"  "command_complete"
   "E"  "error"})

(def.lua PGERROR
  {"S"  "severity"
   "V"  "verbose"
   "C"  "code"
   "M"  "message"
   "H"  "hint"
   "P"  "position"
   "D"  "detail"
   "F"  "file"
   "L"  "line"
   "W"  "message"
   "R"  "raise"
   "s"  "schema"
   "t"  "table"
   "n"  "constraint"})

(def$.lua KEEPALIVE 120000)

(defn.lua coerce-number-string
  [value]
  (if (xt/x:is-string? value)
    (return (or (tonumber value)
                value))
    (return value)))

(defn.lua normalise-scalar-output
  [value]
  (cond (or (xt/x:nil? value)
            (k/is-boolean? value)
            (xt/x:is-object? value))
        (return value)

        (xt/x:is-string? value)
        (return (-/coerce-number-string value))

        :else
        (return value)))

(defn.lua normalise-query-output
  [ret]
  (cond (k/is-array? ret)
        (do
          (when (== 0 (xt/x:len ret))
            (return []))
          (local row (xtd/first ret))
          (if (and (== 1 (xt/x:len ret))
                   (xt/x:is-object? row)
                   (== 1 (xt/x:len (xtd/obj-keys row))))
            (return (-/normalise-scalar-output
                     (xtd/obj-first-val row)))
            (return ret)))

        (k/is-boolean? ret)
        (return [])

        :else
        (return (-/normalise-scalar-output ret))))

(defn.lua db-error
  "gets the db error"
  {:added "4.0"}
  ([s is-dev query]
   (local '[fields pattern] '[{} "([^%z]+)"])
   (local output {})
   (local is-processed
          (pcall (fn []
                     (string.gsub s pattern (fn [c]
                                         (:= (. fields
                                                [(string.sub c 1 1)])
                                             (string.sub c 2)))))))
   
   (if is-processed
     (:= (. output ["debug"]) fields)
     (:= (. output ["debug"]) {:raw s}))
   
   (local #{detail} fields)

    (pcall (fn []
             (xt/x:set-key output "query" query)
             (if detail
               (xtd/obj-assign output (cjson.decode detail)))))
    (return output)))

(defn.lua raw-query
  "creates a raw-query"
  {:added "4.0"}
  [conn query]
  (local '[ret err] (. conn (query query)))
  (. conn (keepalive -/KEEPALIVE 10))
  
  ;; pgmoon returns the number of executed statements as its second value.
  ;; A single-statement query therefore succeeds with `err` equal to 1.
  (when (not= 1 err)
    (xt/x:err err))
  (return (-/normalise-query-output ret)))

(defn.lua client-connect
  [client opts]
  (var #{defaults} client)
  (var env (xtd/obj-clone (-/default-env)))
  (xtd/obj-assign env defaults)
  (xtd/obj-assign env (or opts {}))
  (var conn (ngxpg.new env))
  (var query-opts {})
  (:= (. conn
         ["type_deserializers"]
         ["json"])
      nil)
  (:= (. conn
         ["type_deserializers"]
         ["string"])
      cjson.encode)
  (:= (. conn
         ["parse_error"])
      (fn [self err] (return (-/db-error err
                                          (. env ["dev"])
                                          (. query-opts query)))))
  (var '[ok err] (. conn (connect)))
  (when (not ok) (xt/x:err err))
  (xt/x:set-key client "raw" conn)
  (return client))

(defn.lua client-disconnect
  [client]
  (var #{raw} client)
  (return (. raw (disconnect))))

(defn.lua client-query
  [client input]
  (var #{raw} client)
  (return (-/raw-query raw input)))

(defn.lua client-query-async
  [client input]
  (return (common-promise/promise-run (-/client-query client input))))

(defimpl.xt ^{:lang :lua}
  LuaNginxPostgresClient
  [defaults raw]
  conn-sql/ISqlClient
  {conn-sql/connect      -/client-connect
   conn-sql/disconnect   -/client-disconnect
   conn-sql/query        -/client-query
   conn-sql/query-async  -/client-query-async})

(defn.lua create
  [defaults]
  (var client (-/LuaNginxPostgresClient (or defaults {}) nil))
  (xt/x:set-key client "::/override"
                {"connect" -/client-connect
                 "disconnect" -/client-disconnect
                 "query" -/client-query
                 "query_async" -/client-query-async})
  (return client))

(comment
  (./create-tests))
