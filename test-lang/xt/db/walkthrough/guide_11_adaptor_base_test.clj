(ns xt.db.walkthrough.guide-11-adaptor-base-test
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
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [postgres.core :as pg]
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

^{:refer xt.db.node.adaptor-base/init-db-type :added "4.1"}
(fact "walkthrough: install one live backend on a node"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (adaptor/init-db-type node
                          "db/primary"
                          "postgres"
                          (@! (local-min/+config+ :db))
                          -/Schema
                          -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            {"node_type" (xt/x:get-key node "::")
             "service_type" (xt/x:get-key (substrate/get-service node "db/primary") "::")
             "service_db" (xtd/get-in (substrate/get-service node "db/primary")
                                      ["client" "defaults" "database"])})))))
  => {"node_type" "substrate"
      "service_type" "xt.db.system.impl_postgres/ImplPostgres"
      "service_db" "postgres"})

^{:refer xt.db.node.adaptor-base/init-adaptor-handler :added "4.1"}
(fact "walkthrough: install common, primary, and caching services"
  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-adaptor-handler {"primary" {"type" "postgres"
                                     "defaults" (@! (local-min/+config+ :db))}
                          "caching" {"type" "sqlite"
                                     "defaults" {"filename" ":memory:"}}}
                         -/Schema
                         -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            {"common_schema" (xtd/get-in (substrate/get-service node "db/common")
                                         ["schema" "Log" "message" "type"])
             "primary_type" (xt/x:get-key (substrate/get-service node "db/primary") "::")
             "primary_db" (xtd/get-in (substrate/get-service node "db/primary")
                                      ["client" "defaults" "database"])
             "caching_type" (xt/x:get-key (substrate/get-service node "db/caching") "::")
             "caching_file" (xtd/get-in (substrate/get-service node "db/caching")
                                        ["client" "defaults" "filename"])})))))
  => {"common_schema" "text"
      "primary_type" "xt.db.system.impl_postgres/ImplPostgres"
      "primary_db" "postgres"
      "caching_type" "xt.db.system.impl_sqlite/ImplSqlite"
      "caching_file" ":memory:"})

^{:refer xt.db.node.adaptor-base/init-handlers :added "4.1"}
(fact "walkthrough: init-handlers returns the node unchanged for now"
  (!.js
   (adaptor/init-handlers
    (substrate/node-create {"schema" -/Schema
                            "lookup" -/SchemaLookup
                            "services" {}})
    {}))
  => (contains {"status" "disconnected"}))
