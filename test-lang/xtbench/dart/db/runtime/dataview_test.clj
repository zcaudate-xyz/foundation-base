(ns xtbench.dart.db.runtime.dataview-test
  (:require [hara.lang :as l]
            [xt.db.helpers.data-main-test :as sample])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.db.runtime.dataview :as dataview]
              [xt.db.node.event-type :as event-type]
              [xt.db.helpers.data-main-test :as sample]
              [xt.db.node.state :as state]
              [xt.lang.spec-base :as xt]
              [xt.lang.common-data :as xtd]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +views+
  {"UserAccount"
   {"select"
    {"by_organisation"
     {"input" [{"symbol" "i_organisation_id", "type" "uuid"}]
      "return" "jsonb"
      "schema" "scratch-sample-db"
      "id" "user_account_by_organisation"
      "flags" {}
      "view" {"table" "UserAccount"
              "type" "select"
              "tag" "by_organisation"
              "query" {"organisation_accesses"
                       {"organisation" "{{i_organisation_id}}"}}}}}
    "return"
    {"info"
     {"input" [{"symbol" "i_account_id", "type" "uuid"}]
      "return" "jsonb"
      "schema" "scratch-sample-db"
      "id" "user_account_info"
      "flags" {"public" true}
      "view" {"table" "UserAccount"
              "type" "return"
              "tag" "info"
              "query" [["profile" ["*/standard"]]
                       "nickname"
                       "id"]}}}}})

^{:refer xt.db.runtime.dataview/query-return-entry :added "4.1"}
(fact "creates a return entry from an inline return query"

  (!.dt
    (dataview/query-return-entry
     "UserAccount"
     ["nickname" ["profile" ["first_name"]]]
     true))
  => {"input" [],
      "return" "jsonb",
      "flags" {},
      "view" {"table" "UserAccount",
              "type" "return",
              "query" ["nickname"]}})

^{:refer xt.db.runtime.dataview/query-return-combined :added "4.1"}
(fact "merges inline return-query fragments into an existing return entry"

  (!.dt
    (dataview/query-return-combined
     "UserAccount"
     {"view" {"query" ["nickname"]}}
     ["id" ["profile" ["first_name"]]]
     true))
  => {"view" {"query" ["nickname" "id"]}})

^{:refer xt.db.runtime.dataview/query-entries :added "4.1"}
(fact "gets select and return entries from the state"

  (!.dt
     (var state (state/base-state {"schema" sample/Schema
                                  "views" (@! +views+)}))
     (xt/x:set-key state "::" event-type/STATE_TAG)
     (xt/x:set-key state "schema" sample/Schema)
     (xt/x:set-key state "views" (@! +views+))
     (xt/x:set-key state "lookup" {})
     (xt/x:set-key state "queries" {})
     (xt/x:set-key state "watch" {})
     (xt/x:set-key state "view_watch" {})
     (xt/x:set-key state "pending" {})
     (xt/x:set-key state "remote" {})
     (xt/x:set-key state "db" nil)
     (dataview/query-entries
     state
     "UserAccount"
     {:select-method "by_organisation"
      :return-method "info"}
     false))
  => (contains-in
      {"select_entry" {"view" {"table" "UserAccount"
                               "type" "select"}}
       "return_entry" {"view" {"table" "UserAccount"
                               "type" "return"}}}))

^{:refer xt.db.runtime.dataview/query-triggers :added "4.1"}
(fact "collects dependent tables touched by a query"

  (!.dt
     (var state (state/base-state {"schema" sample/Schema
                                  "views" (@! +views+)}))
     (xt/x:set-key state "::" event-type/STATE_TAG)
     (xt/x:set-key state "schema" sample/Schema)
     (xt/x:set-key state "views" (@! +views+))
     (xt/x:set-key state "lookup" {})
     (xt/x:set-key state "queries" {})
     (xt/x:set-key state "watch" {})
     (xt/x:set-key state "view_watch" {})
     (xt/x:set-key state "pending" {})
     (xt/x:set-key state "remote" {})
     (xt/x:set-key state "db" nil)
     (dataview/query-triggers
     state
     "UserAccount"
     {:select-method "by_organisation"
      :return-method "info"}))
  => (contains {"UserAccount" true
                "UserProfile" true
                "OrganisationAccess" true
                "Organisation" true}))

^{:refer xt.db.runtime.dataview/query-local-transform :added "4.1"}
(fact "removes __deleted__ markers from local view entries"

  (!.dt
    (dataview/query-local-transform
     {"view" {"query" {"status" "open"
                        "__deleted__" true}}
      "input" []}))
  => {"view" {"query" {"status" "open"}}
      "input" []})

^{:refer xt.db.runtime.dataview/query-check :added "4.1"}
(fact "checks argument length and type against a view entry"

  (!.dt
    [(dataview/query-check
      {"input" [{"symbol" "i_organisation_id", "type" "uuid"}]}
      ["00000000-0000-0000-0000-000000000001"]
      false)
     (dataview/query-check
      {"input" [{"symbol" "i_organisation_id", "type" "uuid"}]}
      [1]
      false)])
  => [[true nil]
      [false {"status" "error"
              "tag" "net/arg-typecheck-failed"
              "data" {"input" 1
                      "spec" {"symbol" "i_organisation_id", "type" "uuid"}}}]])

^{:refer xt.db.runtime.dataview/normalize-query :added "4.1"}
(fact "normalizes query specs using the view args by default"

  (!.dt
    (dataview/normalize-query
     {:table "UserAccount"
       :select-method "by_organisation"
       :return-method "info"}
     {:args ["00000000-0000-0000-0000-000000000001"]}))
  => {"table" "UserAccount"
      "select_method" "by_organisation"
      "select_args" ["00000000-0000-0000-0000-000000000001"]
      "return_method" "info"
      "return_args" []})

^{:refer xt.db.runtime.dataview/query-key :added "4.1"}
(fact "uses an explicit query key or computes a stable key"

  (!.dt
    [(dataview/query-key {:key "orders/main"} {})
     (xt/x:is-string?
      (dataview/query-key
       {:table "UserAccount"
        :select-method "by_organisation"}
       {:model-id "orders"
        :view-id "main"
        :args ["00000000-0000-0000-0000-000000000001"]}))])
  => ["orders/main" true])

^{:refer xt.db.runtime.dataview/prepare-query :added "4.1"}
(fact "prepares a cache query plan and trigger set"

  (!.dt
    (var state (state/base-state {"schema" sample/Schema
                                       "views" (@! +views+)}))
    (xt/x:set-key state "::" event-type/STATE_TAG)
    (xt/x:set-key state "schema" sample/Schema)
    (xt/x:set-key state "views" (@! +views+))
    (xt/x:set-key state "lookup" {})
    (xt/x:set-key state "queries" {})
    (xt/x:set-key state "watch" {})
    (xt/x:set-key state "view_watch" {})
    (xt/x:set-key state "pending" {})
    (xt/x:set-key state "remote" {})
    (xt/x:set-key state "db" nil)
    (var [ok prepared]
         (dataview/prepare-query
          state
          {:key "orders/main"
           :table "UserAccount"
           :select-method "by_organisation"
           :return-method "info"}
          {:model-id "orders"
           :view-id "main"
           :args ["00000000-0000-0000-0000-000000000001"]}))
    [ok
     (. prepared ["key"])
     (xt/x:first (. prepared ["plan"]))
     (xtd/get-in (. prepared ["plan"]) [1 "organisation_accesses" "organisation"])
     (. (. prepared ["tables"]) ["UserProfile"])])
  => [true
      "orders/main"
      "UserAccount"
      "00000000-0000-0000-0000-000000000001"
      true])
