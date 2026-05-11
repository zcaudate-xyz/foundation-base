(ns xt.db.instance-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.event.util-throttle :as th]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.db.instance :as instance]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.db.instance/unsupported-op :added "4.1"}
(fact "signals unsupported backend operations"

  (!.js
   (instance/unsupported-op "clear" "db.void"))
  => (throws))

^{:refer xt.db.instance/get-dbtype :added "4.1"}
(fact "gets the backend type with a sql default"

  (!.js
   [(instance/get-dbtype {})
    (instance/get-dbtype {"::" "db.cache"})])
  => ["db.sql" "db.cache"])

^{:refer xt.db.instance/process-event :added "4.1"}
(fact "dispatches events through the backend implementation map"

  (!.js
   (xt/x:set-key instance/IMPL
                 "db.dispatch"
                 {"add" (fn [instance input-tag data schema lookup opts]
                          (return [(. instance ["id"]) input-tag (. data ["value"]) schema lookup (. opts ["flag"])]))})
   [(instance/process-event {"::" "db.dispatch"
                             :instance {"id" "db-1"}}
                            ["add" {"value" 1}]
                            "schema-a"
                            "lookup-a"
                            {"flag" true})
    (instance/process-event {"::" "db.dispatch"
                             :instance {"id" "db-1"}}
                            ["add" {"value" 2} true]
                            "schema-b"
                            "lookup-b"
                            {"flag" false})])
  => [["db-1" "add" 1 "schema-a" "lookup-a" true]
      ["db-1" "input" 2 "schema-b" "lookup-b" false]])

^{:refer xt.db.instance/process-triggers :added "4.1"}
(fact "runs only the listeners that match changed tables"

  (!.js
   (var fired [])
   (var ids
        (instance/process-triggers
         {"::" "db.cache"}
         {"account" {:id "account"
                     :listen ["UserAccount" "UserProfile"]
                     :callback (fn [_ trigger]
                                 (xt/x:arr-push fired (. trigger ["id"])))}
          "wallet"  {:id "wallet"
                     :listen ["Wallet"]
                     :callback (fn [_ trigger]
                                 (xt/x:arr-push fired (. trigger ["id"])))}} 
         {"UserAccount" true}))
   [ids fired])
  => [["account"] ["account"]])

^{:refer xt.db.instance/add-trigger :added "4.1"}
(fact "adds triggers to the db map"

  (!.js
   (var db {:triggers {}})
   [(instance/add-trigger db "watch" {:id "watch"})
    (xtd/get-in db ["triggers" "watch" "id"])])
  => ["watch" "watch"])

^{:refer xt.db.instance/remove-trigger :added "4.1"}
(fact "removes triggers from the db map"

  ^{:seedgen/base {:lua {:expect (l/as-lua [{"id" "watch"} nil])}}}
  (!.js
   (var db {:triggers {"watch" {:id "watch"}}})
   [(instance/remove-trigger db "watch")
    (xtd/get-in db ["triggers" "watch"])])
  => [{"id" "watch"} nil])

^{:refer xt.db.instance/db-trigger :added "4.1"}
(fact "delegates trigger execution through the stored listeners"

  (!.js
   (instance/db-trigger
    {:triggers {"watch" {:id "watch"
                         :listen ["UserAccount"]
                         :callback (fn [_db _trigger] nil)}}}
    {"UserAccount" true}))
  => ["watch"])

^{:refer xt.db.instance/db-create :added "4.1"}
(fact "creates db wrappers with handlers and throttle state"

  ^{:seedgen/base {:lua {:expect (l/as-lua ["db.create" "instance-a" [] {} true true true "test"])}}}
  (!.js
   (xt/x:set-key instance/IMPL
                 "db.create"
                 {"create" (fn [m]
                             (return {"id" (. m ["seed"])}))})
   (var db (instance/db-create {"::" "db.create"
                                :seed "instance-a"}
                               nil
                               nil
                               {"mode" "test"}))
   [(. db ["::"])
    (xtd/get-in db ["instance" "id"])
    (. db ["events"])
    (. db ["triggers"])
    (xt/x:is-function? (. db ["handler"]))
    (xt/x:is-function? (. db ["sync_handler"]))
    (xt/x:is-object? (. db ["throttle"]))
    (xtd/get-in db ["opts" "mode"])])
  => ["db.create" "instance-a" [] {} true true true "test"])

^{:refer xt.db.instance/queue-event :added "4.1"}
(fact "queues events and hands them to the throttle"

  ^{:seedgen/base {:lua {:expect (l/as-lua [1 42 []])}}}
  (!.js
   (var db {:events []
            :throttle (th/throttle-create
                       (fn [id]
                         (return id))
                       (fn []
                         (return 42)))})
   (var entry (instance/queue-event db ["add" {"id" "evt-1"}]))
   [(xt/x:len (. db ["events"]))
    (. entry ["started"])
    (. entry ["args"])])
  => [1 42 []])

^{:refer xt.db.instance/sync-event :added "4.1"}
(fact "returns passthrough values or trigger/table pairs for sync events"

  (!.js
   (instance/sync-event
    {:sync_handler (fn [_]
                     (return "blocked"))}
    ["add" {"id" "evt-1"}]))
  => "blocked"

  (!.js
   (var fired [])
   (var out
        (instance/sync-event
         {:instance {"id" "db-1"}
          :triggers {"watch" {:id "watch"
                              :listen ["UserAccount"]
                              :callback (fn [_ trigger]
                                          (xt/x:arr-push fired (. trigger ["id"])))}} 
          :sync_handler (fn [_]
                          (return ["UserAccount" "UserProfile"]))}
         ["add" {"id" "evt-2"}]))
   [(xtd/get-in out [0 0])
    (xtd/get-in out [1 "UserAccount"])
    (xtd/get-in out [1 "UserProfile"])
    fired])
  => ["watch" true true ["watch"]])

^{:refer xt.db.instance/db-exec-sync :added "4.1"}
(fact "executes raw sql only for sql backends"

  (!.js
   (instance/db-exec-sync
    {"::" "db.sql"
     :instance (sql/connection-create
                {"queries" []}
                {"query_sync" (fn [raw input]
                                (xt/x:arr-push (. raw ["queries"]) input)
                                (return input))})}
    "SELECT 1;"))
  => "SELECT 1;"

  (!.js
   (instance/db-exec-sync
    {"::" "db.cache"
     :instance {}}
    "SELECT 1;"))
  => (throws))

^{:refer xt.db.instance/db-pull-sync :added "4.1"}
(fact "dispatches pull requests through the configured backend"

  (!.js
   (xt/x:set-key instance/IMPL
                 "db.pull"
                 {"pull_sync" (fn [instance schema tree opts]
                                (return [(. instance ["id"]) tree (. opts ["mode"])]))})
   (instance/db-pull-sync
    {"::" "db.pull"
     :instance {"id" "pull-1"}
     :opts {"mode" "cache"}}
    sample/Schema
    ["UserAccount" ["nickname"]]))
  => ["pull-1" ["UserAccount" ["nickname"]] "cache"]

  (!.js
   (instance/db-pull-sync
    {"::" "db.void"
     :instance {}
     :opts {}}
    sample/Schema
    ["UserAccount" ["nickname"]]))
  => (throws))

^{:refer xt.db.instance/db-delete-sync :added "4.1"}
(fact "dispatches deletions through the configured backend"

  (!.js
   (xt/x:set-key instance/IMPL
                 "db.delete"
                 {"delete_sync" (fn [instance schema table-name ids opts]
                                   (return [(. instance ["id"]) table-name ids (. opts ["mode"])]))})
   (instance/db-delete-sync
    {"::" "db.delete"
     :instance {"id" "delete-1"}
     :opts {"mode" "cache"}}
    sample/Schema
    "UserAccount"
    ["user-1"]))
  => ["delete-1" "UserAccount" ["user-1"] "cache"])

^{:refer xt.db.instance/db-clear :added "4.1"}
(fact "clears supported backends and throws for unsupported ones"

  (!.js
   (xt/x:set-key instance/IMPL
                 "db.clear"
                 {"clear" (fn [instance]
                            (return (. instance ["id"])))})
   (instance/db-clear
    {"::" "db.clear"
     :instance {"id" "clear-1"}}))
  => "clear-1"

  (!.js
   (instance/db-clear
    {"::" "db.void"
     :instance {}}))
  => (throws))

^{:refer xt.db.instance/add-view-trigger :added "4.1"}
(fact "creates view triggers that watch linked tables and pull the view tree"

  (!.js
   (xt/x:set-key instance/IMPL
                 "db.view"
                 {"pull_sync" (fn [_instance _schema tree _opts]
                                (return tree))})
   (var db {"::" "db.view"
            :instance {"id" "view-1"}
            :triggers {}
            :opts {}})
   (instance/add-view-trigger
    db
    "account.info"
    sample/Schema
     {:view {:table "UserAccount"
             :query ["nickname" ["profile" ["first_name"]]]}}
    (fn [out]
      (return out)))
   (var entry (xtd/get-in db ["triggers" "account.info"]))
   (var listen (xtd/arr-lookup (. entry ["listen"])))
   (var callback (. entry ["callback"]))
   [(xtd/get-in listen ["UserAccount"])
    (xtd/get-in listen ["UserProfile"])
    (xt/x:is-function? callback)])
  => [true true true])
