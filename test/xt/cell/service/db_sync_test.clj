(ns xt.cell.service.db-sync-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.cell.service.db-sync :as db-sync]]})

(fact:global
  {:setup    [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

^{:refer xt.cell.service.db-sync/sync-capable? :added "4.1"}
(fact "checks whether a db descriptor supports sync operations"

  (!.js
   [(db-sync/sync-capable? {"schema" {}})
    (db-sync/sync-capable? {})])
  => [true false])

^{:refer xt.cell.service.db-sync/normalize-sync :added "4.1"}
(fact "normalizes sync input from sync and remove aliases"

  (!.js
   (db-sync/normalize-sync
    {}
    {"sync" {"User" [{"id" "u1"}]}}
    {"remove" {"User" ["u2"]}}))
  => {"db/sync" {"User" [{"id" "u1"}]}
      "db/remove" {"User" ["u2"]}})

^{:refer xt.cell.service.db-sync/prepare-sync :added "4.1"}
(fact "validates sync request shapes before execution"

  (!.js
   [(db-sync/prepare-sync {} {} {})
    (db-sync/prepare-sync
     {}
     {"sync" {"User" [{"id" "u1"}]}
      "remove" {"User" ["u2"]}}
     {})])
  => [[false
       {"status" "error"
        "tag" "db/sync-empty-request"}]
      [true
       {"db/sync" {"User" [{"id" "u1"}]}
        "db/remove" {"User" ["u2"]}}]])

^{:refer xt.cell.service.db-sync/execute-sync :added "4.1"}
(fact "requires a local db when executing sync requests"

  (!.js
   (db-sync/execute-sync
    {"schema" {}}
    {"db/sync" {"User" [{"id" "u1"}]}}
    {}))
  => [false
      {"status" "error"
       "tag" "db/local-db-not-provided"}])

^{:refer xt.cell.service.db-sync/result->update :added "4.1"}
(fact "maps sync results into refresh, patch, or sync updates"

  (!.js
   [(db-sync/result->update
     {"schema" {}}
     {"db/remove" {"User" ["u2"]}}
     {"view-id" "list"
      "update-mode" "refresh-local"})
    (db-sync/result->update
     {"schema" {}}
     {"db/remove" {"User" ["u2"]}}
     {"update-mode" "patch"})
    (db-sync/result->update
     {"schema" {}}
     {"db/remove" {"User" ["u2"]}}
     {})])
  => [{"type" "refresh"
       "view_id" "list"
       "body" {"db/sync" nil
               "db/remove" {"User" ["u2"]}}}
      {"type" "patch"
       "body" {"db/sync" nil
               "db/remove" {"User" ["u2"]}}}
      {"type" "sync"
       "body" {"db/sync" nil
               "db/remove" {"User" ["u2"]}}}])

^{:refer xt.cell.service.db-sync/run-sync :added "4.1"}
(fact "returns execution errors when the local db is missing"

  (!.js
   (db-sync/run-sync
    {"schema" {}}
    {"sync" {"User" [{"id" "u1"}]}}
    {}))
  => [false
      {"status" "error"
       "tag" "db/local-db-not-provided"}])
