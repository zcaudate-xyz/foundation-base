(ns xt.db.system.impl-postgres
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.system.impl-common :as impl-common]
             [xt.db.text.sql-graph :as sql-graph]
             [xt.db.text.sql-util :as sql-util]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.connection-sql :as dbsql]]})

(defn.xt pull-async
  "runs a tree ir pull with async postgres semantics"
  {:added "4.1"}
  [client tree]
  (var #{instance
         schema
         opts} client)
  (return
   (promise/x:promise-then
    (dbsql/ensure-promise
     (dbsql/query-async instance
                        (sql-graph/select schema tree opts)))
    (fn [output]
      (when (xt/x:is-string? output)
        (xt/x:err "SQL pull expected decoded structured data"))
      (return output)))))

(defn.xt client-postgres
  "creates the thin postgres client record with stored context"
  {:added "4.1"}
  [schema lookup opts settings]
  (return
   (xt/x:obj-assign
    (impl-common/client-base "db.client.postgres"
                             schema
                             lookup
                             (xt/x:obj-assign
                              (sql-util/postgres-opts lookup)
                              (or opts {})))
    {"settings" (or settings {})})))

(defn.xt client-postgres-init
  "connects the thin postgres client through a runtime sql driver"
  {:added "4.1"}
  [client driver-fn]
  (var #{settings} client)
  (var driver (:? (xt/x:is-function? driver-fn)
                  (driver-fn)
                  driver-fn))
  (return
   (promise/x:promise-then
    (dbsql/connect driver settings)
    (fn [instance]
      (var instance-impl (xt/x:get-key instance "_impl"))
      (when (and (xt/x:not-nil? instance-impl)
                 (xt/x:nil? (xt/x:get-key instance-impl "query_async"))
                 (xt/x:not-nil? (xt/x:get-key instance-impl "query")))
        (:= instance
            (dbsql/connection-create
             (xt/x:get-key instance "_raw")
             (xt/x:obj-assign
              (xt/x:obj-clone instance-impl)
              {"query_async" (xt/x:get-key instance-impl "query")}))))
      (xt/x:set-key client "instance" instance)
      (return client)))))


(comment

  (defn.xt rpc-call-async
  "calls a postgres rpc function with async semantics"
  {:added "4.1"}
  [client fn-name args]
  (var #{instance
         settings} client)
  (var rpc-name (xt/x:str-replace (xt/x:str-to-lower fn-name) "-" "_"))
  (var rpc-schema (or (xt/x:get-key settings "rpc_schema")
                      (xt/x:get-key settings "schema_name")
                      "public"))
  (var arg-list [])
  (cond (xt/x:nil? args)
        nil

        (xt/x:is-array? args)
        (:= arg-list (xt/x:arr-map args sql-util/encode-value))

        (xt/x:is-object? args)
        (:= arg-list
            (xt/x:arr-map
             (xtd/arr-sort (xt/x:obj-keys args)
                           xt/x:to-string
                           xt/x:str-comp)
             (fn [key]
               (return
                (xt/x:cat key
                          " := "
                          (sql-util/encode-value
                           (xt/x:get-key args key)))))))

        :else
        (:= arg-list [(sql-util/encode-value args)]))
  (return
   (dbsql/ensure-promise
    (dbsql/query-async
     instance
     (xt/x:cat "SELECT \""
               rpc-schema
               "\".\""
               rpc-name
               "\"("
               (xt/x:str-join ", " arg-list)
               ");")))))
  )
