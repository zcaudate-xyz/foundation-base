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
             [xt.net.conn-sql :as conn-sql]
             [xt.db.system.impl-postgres :as impl]
             [xt.db.text.sql-util :as ut]
             [xt.db.helpers.data-main-test :as sample]
             [js.net.conn-postgres :as js-postgres]]})

(fact:global
 {
  :setup [(l/rt:restart)
           (l/rt:teardown :postgres)
           (l/rt:setup :postgres)]
   :teardown [(l/rt:stop)]})

^{:refer xt.db.system.impl-postgres/pull-async :added "4.1"
  :setup [(scratch/log-append-public "hello")
          (l/rt:restart :js)]}
(fact "pull-async reads through async postgres semantics"

  (notify/wait-on :js
    (-> (impl/impl-postgres
         (js-postgres/create {:database "test-scratch"})
         (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
         (@! (pg/bind-app (pg/app "scratch_v0"))))
        (impl/impl-postgres-init)
        (promise/x:promise-then
         (fn [impl]
           (return
            (impl/pull-async impl ["Log"]))))
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
    (-> (impl/impl-postgres
         (js-postgres/create {:database "test-scratch"})
         (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
         (@! (pg/bind-app (pg/app "scratch_v0"))))
        (impl/impl-postgres-init)
        (promise/x:promise-then
         (fn [impl]
           (return
            (impl/rpc-call-async impl
                                 {:input [{:symbol "message" :type "text"}]
                                  :return "jsonb"
                                  :schema "scratch_v0"
                                  :id "log_append_public"
                                  :flags {}}
                                 ["hello"]))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify out)))))
  => (contains-in
      {"author_id" nil,
       "id" string?
       "message" "hello"})
  
  (notify/wait-on :js
    (-> (impl/impl-postgres
         (js-postgres/create {:database "test-scratch"})
         (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
         (@! (pg/bind-app (pg/app "scratch_v0"))))
        (impl/impl-postgres-init)
        (promise/x:promise-then
         (fn [impl]
           (return
            (impl/rpc-call-async impl
                                 {:input []
                                  :return "text"
                                  :schema "scratch_v0"
                                  :id "ping"
                                  :flags {}}
                                 []))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify out)))))
  => "pong")

^{:refer xt.db.system.impl-postgres/impl-postgres :added "4.1"}
(fact "creates the thin postgres impl record with stored context"
  
  (!.js
    (impl/impl-postgres
     (js-postgres/create {:database "test-scratch"})
     (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
     (@! (pg/bind-app (pg/app "scratch_v0")))))
  => (contains-in
      {"schema" map?, "lookup" map?,
       "opts" map?,
       "::" "xt.db.system.impl_postgres/ImplPostgres",
       "::/protocols" ["xt.db.system.impl_common/ISourceRemote"],
       "client" {"::" "js.net.conn_postgres/PostgresClient",
                 "::/protocols" ["xt.net.conn_sql/ISqlClient"],
                 "defaults" {"database" "test-scratch"}}}))

^{:refer xt.db.system.impl-postgres/impl-postgres-init :added "4.1"}
(fact "initialises a postgres connection")


^{:refer xt.db.system.impl-postgres/stop-db :added "4.1"}
(fact "closes the postgres client and clears raw connection state"

  (notify/wait-on :js
    (-> (impl/impl-postgres
         (js-postgres/create {:database "test-scratch"})
         (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
         (@! (pg/bind-app (pg/app "scratch_v0"))))
        (impl/impl-postgres-init)
        (promise/x:promise-then
         (fn [impl]
           (var client (xt/x:get-key impl "client"))
           (var connected (xt/x:not-nil? (xt/x:get-key client "raw")))
           (impl/stop-db impl)
           (var raw (xt/x:get-key client "raw"))
           (repl/notify {"connected" connected
                         "raw-nil" (xt/x:nil? raw)})))))
  => {"connected" true
      "raw-nil" true})