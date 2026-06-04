(ns xt.db.system.impl-postgres-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.db.system.impl-postgres :as impl]
             [xt.db.text.sql-util :as ut]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.impl-postgres/postgres-client :added "4.1"}
(fact "creates the thin postgres client record with stored context"

  (!.js
   (var client
        (impl/postgres-client
         sample/Schema
         sample/SchemaLookup
         (ut/postgres-opts sample/SchemaLookup)
         {"database" "test-scratch"}))
   {"tag" (. client ["::"])
    "has_instance" (xt/x:has-key? client "instance")
    "database" (. (. client ["settings"]) ["database"])})
  => {"tag" "db.client.postgres"
      "has_instance" false
      "database" "test-scratch"})

^{:refer xt.db.system.impl-postgres/pull-async :added "4.1"}
(fact "pull-async reads through async postgres semantics"

  (notify/wait-on :js
    (var state {"queries" []})
    (var client
         (impl/postgres-client
          sample/Schema
          sample/SchemaLookup
          (ut/postgres-opts sample/SchemaLookup)
          {"schema_name" "scratch"}))
    (xt/x:set-key client
                  "instance"
                  (dbsql/connection-create
                   state
                   {"query_async" (fn [input-state input]
                                    (xt/x:arr-push (. input-state ["queries"]) input)
                                    (return (promise/x:promise-run [{"id" "USER-0"}])))}))
    (promise/x:promise-then
     (impl/pull-async
      client
      ["UserAccount" {"where" [] "data" ["id"] "links" [] "custom" []}])
     (fn [rows]
       (repl/notify [(xt/x:len (. state ["queries"]))
                     rows]))))
  => [1 [{"id" "USER-0"}]])

^{:refer xt.db.system.impl-postgres/rpc-call-async :added "4.1"}
(fact "rpc-call-async compiles snake_case postgres function calls"

  (notify/wait-on :js
    (var state {"queries" []})
    (var client
         (impl/postgres-client
          sample/Schema
          sample/SchemaLookup
          (ut/postgres-opts sample/SchemaLookup)
          {"schema_name" "scratch"}))
    (xt/x:set-key client
                  "instance"
                  (dbsql/connection-create
                   state
                   {"query_async" (fn [input-state input]
                                    (xt/x:arr-push (. input-state ["queries"]) input)
                                    (return (promise/x:promise-run 3)))}))
    (promise/x:promise-then
     (impl/rpc-call-async
      client
      "add-f"
      {"b" 2 "a" 1})
     (fn [out]
       (repl/notify [(xtd/get-in state ["queries" 0]) out]))))
  => ["SELECT \"scratch\".\"add_f\"(a := '1', b := '2');"
      3])
