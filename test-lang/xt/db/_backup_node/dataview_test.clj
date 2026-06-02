(ns xt.db._backup-node.dataview-test
  (:require [hara.lang :as l]
            [xt.db.helpers.data-main-test :as sample])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db._backup-node.dataview :as dataview]
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

(def +inline-query+
  {:table "UserAccount"
   :select-entry {"input" [{"symbol" "i_organisation_id", "type" "uuid"}]
                 "return" "jsonb"
                 "view" {"query" {"organisation_accesses"
                                  {"organisation" "{{i_organisation_id}}"}}}}
   :return-entry {"input" [{"symbol" "i_account_id", "type" "uuid"}]
                 "return" "jsonb"
                 "view" {"query" [["profile" ["*/standard"]]
                                  "nickname"
                                  "id"]}}
   :select-args ["00000000-0000-0000-0000-000000000001"]})

^{:refer xt.db._backup-node.dataview/view-query-entry :added "4.1"}
(fact "normalizes inline query entries without a registry"

  (!.js
    (dataview/view-query-entry
     "UserAccount"
     {"input" [{"symbol" "i_organisation_id", "type" "uuid"}]
      "view" {"query" {"nickname" "root"}}}
     "select"))
  => {"input" [{"symbol" "i_organisation_id", "type" "uuid"}]
      "return" "jsonb"
      "flags" {}
      "view" {"table" "UserAccount"
              "type" "select"
              "query" {"nickname" "root"}}})

^{:refer xt.db._backup-node.dataview/view-query-return-entry :added "4.1"}
(fact "creates a return entry from an inline return query"

  ^{:seedgen/base
    {:lua
     {:expect
      (l/as-lua
       {"input" []
        "return" "jsonb"
        "flags" {}
        "view" {"table" "UserAccount",
                "type" "return",
                "query" ["nickname"]}})}}}
  (!.js
    (dataview/view-query-return-entry
     "UserAccount"
     ["nickname" ["profile" ["first_name"]]]
     true))
  => {"input" [],
      "return" "jsonb",
      "flags" {},
      "view" {"table" "UserAccount",
              "type" "return",
              "query" ["nickname"]}})

^{:refer xt.db._backup-node.dataview/view-query-return-combined :added "4.1"}
(fact "merges inline return-query fragments into an existing return entry"

  (!.js
    (dataview/view-query-return-combined
     "UserAccount"
     {"view" {"query" ["nickname"]}}
     ["id" ["profile" ["first_name"]]]
     true))
  => {"view" {"query" ["nickname" "id"]}})

^{:refer xt.db._backup-node.dataview/view-query-entries :added "4.1"}
(fact "gets select and return entries from the state"

  (!.js
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
     (dataview/view-query-entries
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

^{:refer xt.db._backup-node.dataview/view-query-entries.inline :added "4.1"}
(fact "gets inline select and return entries without state views"

  (!.js
     (var state (state/base-state {"schema" sample/Schema}))
     (xt/x:set-key state "::" event-type/STATE_TAG)
     (xt/x:set-key state "schema" sample/Schema)
     (xt/x:set-key state "views" {})
     (xt/x:set-key state "lookup" {})
     (xt/x:set-key state "queries" {})
     (xt/x:set-key state "watch" {})
     (xt/x:set-key state "view_watch" {})
     (xt/x:set-key state "pending" {})
     (xt/x:set-key state "remote" {})
     (xt/x:set-key state "db" nil)
     (dataview/view-query-entries
      state
      "UserAccount"
      (@! +inline-query+)
      false))
  => (contains-in
      {"select_entry" {"view" {"table" "UserAccount"
                               "type" "select"}}
       "return_entry" {"view" {"table" "UserAccount"
                               "type" "return"}}}))

^{:refer xt.db._backup-node.dataview/view-triggers :added "4.1"}
(fact "collects dependent tables touched by a query"

  (!.js
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
     (dataview/view-triggers
     state
     "UserAccount"
     {:select-method "by_organisation"
      :return-method "info"}))
  => (contains {"UserAccount" true
                "UserProfile" true
                "OrganisationAccess" true
                "Organisation" true}))

^{:refer xt.db._backup-node.dataview/view-local-transform :added "4.1"}
(fact "removes __deleted__ markers from local view entries"

  ^{:seedgen/base {:lua {:expect (l/as-lua {"view" {"query" {"status" "open"}}
                                            "input" []})}}}
  (!.js
    (dataview/view-local-transform
     {"view" {"query" {"status" "open"
                        "__deleted__" true}}
      "input" []}))
  => {"view" {"query" {"status" "open"}}
      "input" []})

^{:refer xt.db._backup-node.dataview/query-check :added "4.1"}
(fact "checks argument length and type against a view entry"

  ^{:seedgen/base
    {:lua
     {:expect
      (l/as-lua
       [[true nil]
        [false {"status" "error"
                "tag" "net/arg-typecheck-failed"
                "data" {"input" 1
                        "spec" {"symbol" "i_organisation_id", "type" "uuid"}}}]])}}}
  (!.js
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

^{:refer xt.db._backup-node.dataview/normalize-query :added "4.1"}
(fact "normalizes query specs using the view args by default"

  ^{:seedgen/base
    {:lua
     {:expect
      (l/as-lua
       {"table" "UserAccount"
        "select_method" "by_organisation"
        "select_args" ["00000000-0000-0000-0000-000000000001"]
        "return_method" "info"
        "return_args" []})}}}
  (!.js
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

^{:refer xt.db._backup-node.dataview/query-key :added "4.1"}
(fact "uses an explicit query key or computes a stable key"

  (!.js
    [(dataview/query-key {:key "orders/main"} {})
     (xt/x:is-string?
      (dataview/query-key
       {:table "UserAccount"
        :select-method "by_organisation"}
       {:model-id "orders"
        :view-id "main"
        :args ["00000000-0000-0000-0000-000000000001"]}))])
  => ["orders/main" true])

^{:refer xt.db._backup-node.dataview/prepare-query :added "4.1"}
(fact "prepares a cache query plan and trigger set"

  (!.js
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

^{:refer xt.db._backup-node.dataview/prepare-query.inline :added "4.1"}
(fact "prepares a cache query plan from inline entries"

  (!.js
    (var state (state/base-state {"schema" sample/Schema}))
    (xt/x:set-key state "::" event-type/STATE_TAG)
    (xt/x:set-key state "schema" sample/Schema)
    (xt/x:set-key state "views" {})
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
          (@! +inline-query+)
          {:model-id "orders"
           :view-id "main"
           :args []}))
    [ok
     (xt/x:first (. prepared ["plan"]))
     (xtd/get-in (. prepared ["plan"]) [1 "organisation_accesses" "organisation"])
     (. (. prepared ["tables"]) ["UserProfile"])])
  => [true
      "UserAccount"
      "00000000-0000-0000-0000-000000000001"
      true])
