(ns xt.db.runtime.roundtrip-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

  (l/script- :js
    {:runtime :basic
     :require [[js.cell.service.db-query :as db-query]
               [xt.db.helpers.data-main-test :as sample]
               [xt.db.instance :as xdb]
               [xt.db.text.pgrest :as pgrest]
               [xt.lang.spec-base :as xt]
               [xt.lang.common-data :as xtd]
               [xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-manage :as manage]
             [js.lib.driver-sqlite :as js-sqlite]]})

(defn bootstrap-js
  []
  (notify/wait-on [:js 2000]
    (. (dbsql/connect (js-sqlite/driver) {})
       (then (fn [conn]
               (try
                 (:= (!:G DBCACHE)
                     (xdb/db-create {"::" "db.cache"}
                                    sample/Schema
                                    sample/SchemaLookup
                                    nil))
                 (:= (!:G DBSQL)
                     (xdb/db-create {"::" "db.sql"
                                     :instance conn}
                                    sample/Schema
                                    sample/SchemaLookup
                                    (ut/sqlite-opts nil)))
                 (dbsql/query-sync (xt/x:get-key DBSQL "instance")
                                   (str/join "\n\n"
                                             (manage/table-create-all
                                              sample/Schema
                                              sample/SchemaLookup
                                              (ut/sqlite-opts nil))))
                 (repl/notify true)
                 (catch e (repl/notify e))))))))

(fact:global
 {:setup [(l/rt:restart)
          (do (l/rt:scaffold :js) true)
          (bootstrap-js)]
  :teardown [(l/rt:stop)]})

^{:seedgen/root {:all true}}
(defn.js get-db-desc
  []
  (return
   {"schema" sample/Schema
    "views"
    {"UserAccount"
     {"select"
      {"by_id"
       {"input" [{"symbol" "i_account_id", "type" "text"}]
        "return" "jsonb"
        "view" {"table" "UserAccount",
                "type" "select",
                "tag" "by_id",
                "access" {"roles" {}},
                "guards" [],
                "query" {"id" "{{i_account_id}}"}}}}
      "return"
      {"profile_emails"
       {"input" [{"symbol" "i_order_id", "type" "text"}]
        "return" "jsonb"
        "view" {"table" "UserAccount",
                "type" "return",
                "tag" "profile_emails",
                "access" {"roles" {}},
                "guards" [],
                "query" ["nickname"
                         ["profile" ["first_name"]]]}}}}}}))

(defn.js get-db-schema
  []
  (return (xt/x:get-key (-/get-db-desc) "schema")))

(defn.js get-db-views
  []
  (return (xt/x:get-key (-/get-db-desc) "views")))

(defn.js get-root-user-id
  []
  (return "00000000-0000-0000-0000-000000000000"))

(defn.js get-root-user-summary
  []
  (return [{"nickname" "root"
            "profile" [{"first_name" "Root"}]}]))

(defn.js get-root-user-nickname
  []
  (return [{"nickname" "root"}]))

(defn.js get-currency-bulk
  []
  (return [{"id" "USD" "name" "US Dollar"}
           {"id" "XLM" "name" "Stellar Coin"}]))

(defn.js get-currency-tree
  []
  (return ["Currency"
           {:id ["in" [["USD" "XLM"]]]}
           ["id" "name"]]))

(defn.js get-user-nickname-tree
  []
  (return ["UserAccount"
           {:id (-/get-root-user-id)}
           ["nickname"]]))

(defn.js get-prepared-user-tree
  []
  (return (-/prepare-tree
           {:table "UserAccount"
            :select-method "by_id"
            :return-method "profile_emails"}
           [(-/get-root-user-id)])))

(defn.js seed-roundtrip-fixture
  []
  (var payload {"UserAccount" [sample/RootUser]
                "Currency" (@! sample/+currency+)})
  (xdb/sync-event DBCACHE ["add" payload])
  (xdb/sync-event DBSQL ["add" payload])
  (return true))

(defn.js prepare-tree
  [query-spec args]
  (var [ok tree] (db-query/prepare-query (-/get-db-desc) query-spec {"args" args}))
  (when (not ok)
    (xt/x:throw tree))
  (return tree))

(defn.js make-supabase-db
  [trees]
  (var schema (-/get-db-schema))
  (var compiled-map {})
  (xt/for:array [tree trees]
    (xt/x:set-key compiled-map
                  (JSON.stringify (pgrest/compile-query tree))
                  tree))
  (return {"::" "db.supabase"
           :instance {"execute"
                      (fn [compiled _opts]
                        (var key (JSON.stringify compiled))
                        (var tree (xt/x:get-key compiled-map key))
                        (when (xt/x:nil? tree)
                          (return [false {:status "error"
                                          :tag "db/supabase-plan-not-found"
                                          :data {"compiled" compiled}}]))
                        (return [true (xdb/db-pull-sync DBSQL schema tree)]))}}))

(defn.js run-roundtrip
  [tree]
  (var schema (-/get-db-schema))
  (var supa-db (-/make-supabase-db [tree]))
  (return [(xdb/db-pull-sync DBCACHE schema tree)
           (xdb/db-pull-sync DBSQL schema tree)
           (xdb/db-pull-sync supa-db schema tree)]))

(defn.js sort-roundtrip
  [results field]
  (return
   (xt/x:arr-map
    results
    (fn [rows]
      (return (xtd/arr-sort rows
                            (fn [row]
                              (return (xt/x:get-key row field)))
                            xt/x:str-comp))))))

^{:refer xt.db.instance/db-pull-sync :added "4.1"}
(fact "prepared view queries roundtrip to the same nested datastructure"

  (!.js
   (-/seed-roundtrip-fixture)
   (-/run-roundtrip (-/get-prepared-user-tree)))
  => [[{"nickname" "root"
         "profile" [{"first_name" "Root"}]}]
       [{"nickname" "root"
         "profile" [{"first_name" "Root"}]}]
       [{"nickname" "root"
         "profile" [{"first_name" "Root"}]}]])

(fact "direct flat trees roundtrip to the same datastructure"

  (!.js
   (-/seed-roundtrip-fixture)
   (-/run-roundtrip (-/get-user-nickname-tree)))
  => [[{"nickname" "root"}]
       [{"nickname" "root"}]
       [{"nickname" "root"}]])

(fact "bulk `in` filters roundtrip to the same flat row datastructure"

  (!.js
   (-/seed-roundtrip-fixture)
   (-/sort-roundtrip (-/run-roundtrip (-/get-currency-tree)) "id"))
  => [[{"id" "USD" "name" "US Dollar"}
        {"id" "XLM" "name" "Stellar Coin"}]
       [{"id" "USD" "name" "US Dollar"}
        {"id" "XLM" "name" "Stellar Coin"}]
       [{"id" "USD" "name" "US Dollar"}
        {"id" "XLM" "name" "Stellar Coin"}]])
