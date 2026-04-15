(ns js.cell.service.db-sync-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.cell.service.db-sync :as db-sync]
             [xt.db :as xdb]
             [xt.lang.common-lib :as k]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +db+
  {"schema"
   {"Order"
    {"id" {"ident" "id", "type" "text", "order" 0}
     "status" {"ident" "status", "type" "text", "order" 1}}}})

(def +lookup+
  {"Order" {"position" 0}})

^{:refer js.cell.service.db-sync/sync-capable? :added "4.1"}
(fact "checks whether a descriptor can process sync requests"
  ^:hidden

  (!.js
   [(db-sync/sync-capable? (@! +db+))
    (db-sync/sync-capable? {})])
  => [true false])

^{:refer js.cell.service.db-sync/normalize-sync :added "4.1"}
(fact "normalizes local sync and remove keys into the canonical contract"
  ^:hidden

  (!.js
   (db-sync/normalize-sync
    (@! +db+)
    {:sync {"Order" [{"id" "ord-1"
                      "status" "open"}]}
     :remove {"Order" ["ord-2"]}}
    {}))
  => {"db/sync" {"Order" [{"id" "ord-1"
                           "status" "open"}]}
      "db/remove" {"Order" ["ord-2"]}})

^{:refer js.cell.service.db-sync/prepare-sync :added "4.1"}
(fact "validates and prepares the canonical sync request"
  ^:hidden

  (!.js
   [(db-sync/prepare-sync
     (@! +db+)
     {:sync {"Order" [{"id" "ord-1"
                       "status" "open"}]}}
     {})
    (db-sync/prepare-sync
     (@! +db+)
     {}
     {})])
  => [[true
       {"db/sync" {"Order" [{"id" "ord-1"
                             "status" "open"}]}}]
      [false
       {"status" "error"
        "tag" "db/sync-empty-request"}]])

^{:refer js.cell.service.db-sync/execute-sync :added "4.1"}
(fact "applies sync and remove requests to a local cache db"
  ^:hidden

  (!.js
   (var desc (@! +db+))
   (var schema (xt/x:get-key desc "schema"))
   (var local-db (xdb/db-create {"::" "db.cache"}
                                schema
                                (@! +lookup+)
                                nil))
   (xdb/sync-event local-db
                   ["add"
                    {"Order" [{"id" "ord-2"
                               "status" "closed"}]}])
   (var [ok result] (db-sync/execute-sync
                     desc
                     {"db/sync" {"Order" [{"id" "ord-1"
                                           "status" "open"}]}
                      "db/remove" {"Order" ["ord-2"]}}
                     {:db local-db}))
   [ok
    result
    (xdb/db-pull-sync local-db schema ["Order" ["id" "status"]])])
  => [true
      {"db/sync" {"Order" [{"id" "ord-1"
                            "status" "open"}]}
       "db/remove" {"Order" ["ord-2"]}}
      [{"id" "ord-1"
        "status" "open"}]])

^{:refer js.cell.service.db-sync/result->update :added "4.1"}
(fact "summarizes sync results for the binding layer"
  ^:hidden

  (!.js
   [(db-sync/result->update
     (@! +db+)
     {"db/sync" {"Order" [{"id" "ord-1"
                           "status" "open"}]}
      "db/remove" {"Order" ["ord-2"]}}
     {})
    (db-sync/result->update
     (@! +db+)
     {"db/sync" {"Order" [{"id" "ord-1"
                           "status" "open"}]}}
     {"update-mode" "refresh-local"
      "view-id" "orders/list"})])
  => [{"type" "sync"
       "body" {"db/sync" {"Order" ["ord-1"]}
               "db/remove" {"Order" ["ord-2"]}}}
      {"type" "refresh"
       "view_id" "orders/list"
       "body" {"db/sync" {"Order" ["ord-1"]}}}])

^{:refer js.cell.service.db-sync/run-sync :added "4.1"}
(fact "prepares, executes, and emits a summarized sync update"
  ^:hidden

  (!.js
   (var desc (@! +db+))
   (var schema (xt/x:get-key desc "schema"))
   (var local-db (xdb/db-create {"::" "db.cache"}
                                schema
                                (@! +lookup+)
                                nil))
   (db-sync/run-sync
    desc
    {:sync {"Order" [{"id" "ord-1"
                      "status" "open"}]}}
    {:db local-db
     "update-mode" "patch"}))
  => [true
      {"result" {"db/sync" {"Order" [{"id" "ord-1"
                                      "status" "open"}]}}
       "update" {"type" "patch"
                 "body" {"db/sync" {"Order" ["ord-1"]}}}}])
