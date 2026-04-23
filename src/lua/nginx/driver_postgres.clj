(ns lua.nginx.driver-postgres
  (:require [std.lang :as l]
            [xt.sys.conn-dbsql :as dbsql]))

(l/script :lua
  {:import [["pgmoon" :as ngxpg]] :require [[xt.lang.common-lib :as k] [xt.lang.spec-base :as xt] [xt.lang.common-data :as xtd] [xt.lang.common-runtime :as rt]]})

(defn.lua default-env
  "gets the default env"
  {:added "4.0"}
  []
  (return (or (rt/xt-config "lua.nginx.driver-postgres")
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
  (rt/xt-config-set "lua.nginx.driver-postgres" env)
  (return env))

;;
;;
;;

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
                                                [(. -/PGERROR [(string.sub c 1 1)])])
                                             (string.sub c 2)))))))
   
   ;;(when (not= is-dev false))
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
  
  (when (not= 1 err)
    (return false err))
  (:= ret (:? (k/is-array? ret) (xtd/first ret) ret))
  (local val (:? (k/is-boolean? ret)
                 ret
                 (xtd/obj-first-val (or ret {}))))
  (return val))

(defn.lua connect-constructor
  "connects to postgres"
  {:added "4.0"}
  [m]
  (local env (xtd/obj-assign (xtd/obj-clone (-/default-env))
                            m))
  (local conn (ngxpg.new env))
  (local opts {})
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
                                         (. opts query)))))
  (local '[ok err] (. conn (connect)))
  (when (not ok) (return nil err))
  
  (:= (. conn ["::disconnect"])
      (fn []
        (return (. conn (disconnect)))))
  (:= (. conn ["::query"])
      (fn [query]
        (xt/x:set-key opts "query" query)
        (return (-/raw-query conn query))))
  (return conn))

(comment
  (./create-tests))
