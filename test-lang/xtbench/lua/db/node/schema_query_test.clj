(ns xtbench.lua.db.node.schema-query-test
  (:require [hara.lang :as l]
            [xt.db.helpers.data-main-test :as sample])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :require [[xt.db.node.schema-query :as schema-query]
              [xt.db.node.schema-state :as schema-state]
              [xt.db.helpers.data-main-test :as sample]
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

^{:refer xt.db.node.schema-query/view-query-return-entry :added "4.1"}
(fact "creates a return entry from an inline return query"

  (!.lua
    (schema-query/view-query-return-entry
     "UserAccount"
     ["nickname" ["profile" ["first_name"]]]
     true))
  => {"input" [],
      "return" "jsonb",
      "flags" {},
      "view" {"table" "UserAccount",
              "type" "return",
              "query" ["nickname"],
              "access" {"roles" {}},
              "guards" []}})

^{:refer xt.db.node.schema-query/view-query-return-combined :added "4.1"}
(fact "merges inline return-query fragments into an existing return entry"

  (!.lua
    (schema-query/view-query-return-combined
     "UserAccount"
     {"view" {"query" ["nickname"]}}
     ["id" ["profile" ["first_name"]]]
     true))
  => {"view" {"query" ["nickname" "id"]}})

^{:refer xt.db.node.schema-query/view-query-entries :added "4.1"}
(fact "gets select and return entries from the state"

  (!.lua
     (schema-query/view-query-entries
     (schema-state/base-state {"schema" sample/Schema
                                "views" (@! +views+)})
     "UserAccount"
     {:select-method "by_organisation"
      :return-method "info"}
     false))
  => (contains-in
      {"select_entry" {"view" {"table" "UserAccount"
                               "type" "select"}}
       "return_entry" {"view" {"table" "UserAccount"
                               "type" "return"}}}))

^{:refer xt.db.node.schema-query/view-triggers :added "4.1"}
(fact "collects dependent tables touched by a query"

  (!.lua
     (schema-query/view-triggers
     (schema-state/base-state {"schema" sample/Schema
                                "views" (@! +views+)})
     "UserAccount"
     {:select-method "by_organisation"
      :return-method "info"}))
  => (contains {"UserAccount" true
                "UserProfile" true
                "OrganisationAccess" true
                "Organisation" true}))

^{:refer xt.db.node.schema-query/view-local-transform :added "4.1"}
(fact "removes __deleted__ markers from local view entries"

  (!.lua
    (schema-query/view-local-transform
     {"view" {"query" {"status" "open"
                        "__deleted__" true}}
      "input" []}))
  => {"view" {"query" {"status" "open"}}
      "input" []})

^{:refer xt.db.node.schema-query/query-check :added "4.1"}
(fact "checks argument length and type against a view entry"

  (!.lua
    [(schema-query/query-check
      {"input" [{"symbol" "i_organisation_id", "type" "uuid"}]}
      ["00000000-0000-0000-0000-000000000001"]
      false)
     (schema-query/query-check
      {"input" [{"symbol" "i_organisation_id", "type" "uuid"}]}
      [1]
      false)])
  => [[true nil]
      [false {"status" "error"
              "tag" "net/arg-typecheck-failed"
              "data" {"input" 1
                      "spec" {"symbol" "i_organisation_id", "type" "uuid"}}}]])

^{:refer xt.db.node.schema-query/normalize-query :added "4.1"}
(fact "normalizes query specs using the view args by default"

  (!.lua
    (schema-query/normalize-query
     {:table "UserAccount"
       :select-method "by_organisation"
       :return-method "info"}
     {:args ["00000000-0000-0000-0000-000000000001"]}))
  => {"table" "UserAccount"
      "select_method" "by_organisation"
      "select_args" ["00000000-0000-0000-0000-000000000001"]
      "return_method" "info"
      "return_args" []})

^{:refer xt.db.node.schema-query/query-key :added "4.1"}
(fact "uses an explicit query key or computes a stable key"

  (!.lua
    [(schema-query/query-key {:key "orders/main"} {})
     (xt/x:is-string?
      (schema-query/query-key
       {:table "UserAccount"
        :select-method "by_organisation"}
       {:model-id "orders"
        :view-id "main"
        :args ["00000000-0000-0000-0000-000000000001"]}))])
  => ["orders/main" true])

^{:refer xt.db.node.schema-query/prepare-query :added "4.1"}
(fact "prepares a cache query plan and trigger set"

  (!.lua
    (var [ok prepared]
         (schema-query/prepare-query
          (schema-state/base-state {"schema" sample/Schema
                                     "views" (@! +views+)})
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
