(ns xt.db.system.impl-postgres-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :postgres
  {:runtime :jdbc.client
   :require [[postgres.sample.scratch-v0 :as scratch]
             [postgres.core :as pg]]
   :config {:dbname "test-scratch"}})

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.db.system.impl-postgres :as impl]
             [xt.db.text.sql-util :as ut]
             [xt.db.helpers.data-main-test :as sample]
             [js.lib.driver-postgres :as js-pg]]})

(fact:global
  {:setup [(l/rt:restart)
           (l/rt:setup :postgres)]
   :teardown [(l/rt:teardown :postgres)
              (l/rt:stop)]})

(def +app+ (pg/app "scratch_v0"))

(def +tree+
  (pg/bind-schema (:schema +app+)))

(def.js Schema
  (@! +tree+))

(def.js SchemaLookup
  (@! (pg/bind-app +app+)))

^{:refer xt.db.system.impl-postgres/client-postgres :added "4.1"}
(fact "creates the thin postgres client record with stored context"

  (!.js
    (impl/client-postgres
     -/Schema
     -/SchemaLookup
     (ut/postgres-opts -/SchemaLookup)
     {"database" "test-scratch"}))
  => {"settings" {"database" "test-scratch"},
      "schema"
      {"Log"
       {"message"
        {"ident" "message",
         "scope" "data",
         "order" 1,
         "required" true,
         "type" "text",
         "cardinality" "one"},
        "author_id"
        {"ident" "author_id",
         "scope" "data",
         "order" 2,
         "type" "uuid",
         "cardinality" "one"},
        "id"
        {"ident" "id",
         "primary" true,
         "scope" "id",
         "order" 0,
         "type" "uuid",
         "cardinality" "one"}}},
      "lookup"
      {"Log"
       {"schema" "scratch_v0",
        "schema_update" false,
        "position" 0,
        "public" true,
        "schema_primary" {"id" "id", "type" "uuid"}}},
      "opts"
      {"values" {"cast" true, "replace" {}},
       "types"
       {"map" "jsonb",
        "image" "jsonb",
        "long" "bigint",
        "array" "jsonb",
        "enum" "text"},
       "strict" true,
       "coerce" {}},
      "::" "db.client.postgres"})

^{:refer xt.db.system.impl-postgres/pull-async :added "4.1"
  :setup [(scratch/log-append-public "hello")]}
(fact "pull-async reads through async postgres semantics"

  (notify/wait-on :js
    (-> (impl/client-postgres
         -/Schema
         -/SchemaLookup
         {}
         {"host" "localhost"
          "port" "5432"
          "user" "postgres"
          "password" "postgres"
          "database" "test-scratch"})
        (impl/client-postgres-init js-pg/driver)
        (promise/x:promise-then
         (fn [client]
           (return
            (impl/pull-async client ["Log"]))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains-in
      [{"author_id" nil,
        "id" string?
        "message" "hello"}]))

^{:refer xt.db.system.impl-postgres/rpc-call-async :added "4.1"}
(fact "rpc-call-async compiles snake_case postgres function calls"

  (notify/wait-on :js
    (var state {"queries" []})
    (var client
         (impl/client-postgres
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
