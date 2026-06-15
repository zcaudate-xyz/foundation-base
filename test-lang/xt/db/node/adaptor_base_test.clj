(ns xt.db.node.adaptor-base-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]))

(do 
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v0 :as scratch-v0]
               [postgres.core :as pg]
               [postgres.core.supabase :as s]]
     :config {:host   (-> local-min/+config+ :db :host)
              :port   (-> local-min/+config+ :db :port)
              :user   (-> local-min/+config+ :db :user)
              :pass   (-> local-min/+config+ :db :password)
              :dbname (-> local-min/+config+ :db :database)
              :startup  local-min/start-supabase
              :shutdown local-min/stop-supabase}
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

  (defrun.pg __init__
    (s/grant-usage #{"scratch_v0"})))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.adaptor-base :as adaptor]
             [xt.db.system.impl-common :as impl-common]
             [xt.substrate :as substrate]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))


(fact:global
 {:setup [(l/rt:restart)
          (l/rt:teardown :postgres)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.adaptor-base/set-impl :added "4.1"}
(fact "set-impl installs a live impl on the node"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (adaptor/set-impl node
                          "db/primary"
                          "postgres"
                          (@! (local-min/+config+ :db))
                          -/Schema
                          -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            (substrate/get-service node "db/primary"))))))
  => (contains-in
      {"schema" map? "lookup" map? "opts" map?
       "::" "xt.db.system.impl_postgres/ImplPostgres"
       "::/protocols" ["xt.db.system.impl_common/ISourceRemote"]
       "client" {"::" "js.net.conn_postgres/PostgresClient"
                 "::/protocols" ["xt.net.conn_sql/ISqlClient"]
                 "raw" map?}})
  
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (adaptor/set-impl node
                          "db/caching"
                          "sqlite"
                          {}
                          -/Schema
                          -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            (substrate/get-service node "db/caching"))))))
  => (contains-in
      {"schema" map? "lookup" map? "opts" map?
       "::" "xt.db.system.impl_sqlite/ImplSqlite"
       "::/protocols" ["xt.db.system.impl_common/ISourceLocal"
                       "xt.db.system.impl_common/ISourceRemote"]
       "client" {"::" "js.net.conn_sqlite/SqliteClient"
                 "::/protocols" ["xt.net.conn_sql/ISqlClient"]
                 "raw" map?}}))

^{:refer xt.db.node.adaptor-base/init-db :added "4.1"}
(fact "init-db installs the db/common db/primary and db/caching services"
  
  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-db {"primary" {"type" "postgres"
                                     "defaults" (@! (local-min/+config+ :db))}
                          "caching" {"type" "sqlite"
                                     "defaults" {"filename" ":memory:"}}}
                         -/Schema
                         -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            (substrate/get-services node))))))
  => (contains-in
      {"db/caching" map?,
       "db/primary" map?,
       "db/common" map?}))

^{:refer xt.db.node.adaptor-base/call-primary-handler :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "call-primary-handler routes rpc args through the live primary impl"
  
  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-db {"primary" {"type" "postgres"
                                     "defaults" (@! (local-min/+config+ :db))}
                          "caching" {"type" "sqlite"
                                     "defaults" {"filename" ":memory:"}}}
                         -/Schema
                         -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (return
            (adaptor/call-primary-handler
             nil
             [{"input" [{"symbol" "i_message" "type" "text"}]
               "return" "jsonb"
               "schema" "scratch_v0"
               "id" "log_append_public"
               "flags" {}}
              ["hello"]]
             nil
             node))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify out)))))
  => (contains-in {"message" "hello"}))

^{:refer xt.db.node.adaptor-base/init-handlers :added "4.1"}



(fact "init-handlers returns the node unchanged for now"
  
  (!.js
   (adaptor/init-handlers
    (substrate/node-create {"schema" -/Schema
                            "lookup" -/SchemaLookup
                            "services" {}})
    {}))
  => nil)


^{:refer xt.db.node.adaptor-base/call-rpc-handler :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.adaptor-base/call-fetch-handler :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.adaptor-base/_ :added "4.1"}
(fact "TODO")