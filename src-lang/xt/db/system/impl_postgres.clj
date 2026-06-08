(ns xt.db.system.impl-postgres
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.system.impl-common :as impl-common]
             [xt.db.text.sql-graph :as sql-graph]
             [xt.db.text.sql-util :as sql-util]
             [xt.db.text.sql-call :as sql-call]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.conn-sql :as conn-sql]]})

(defn.xt pull-async
  "runs a tree ir pull with async postgres semantics"
  {:added "4.1"}
  [impl tree]
  (var #{client
         schema
         opts} impl)
  (return
   (conn-sql/query-async client (sql-graph/select schema tree opts))))

(defn.xt rpc-call-async
  [impl rpc-spec args]
  (var #{client} impl)
  (return
   (sql-call/call-raw client rpc-spec args)))

(defn.xt impl-postgres
  "creates the thin postgres impl record with stored context"
  {:added "4.1"}
  [client schema lookup]
  (return
   (impl-common/impl-base "db.impl.postgres"
                          client
                          schema
                          lookup
                          (sql-util/postgres-opts lookup))))

(defn.xt impl-postgres-init
  "connects the thin postgres impl through a runtime sql driver"
  {:added "4.1"}
  [impl]
  (var #{client
         schema
         lookup
         opts} impl)
  (return
   (-> (conn-sql/connect client)
       (promise/x:promise-then
        (fn [client]
          (return impl))))))



