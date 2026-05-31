(ns xt.db.node.model-query-test
  (:require [hara.lang :as l]
            [xt.db.helpers.data-main-test :as sample])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.model-query :as model-query]
             [xt.db.node.event-type :as event-type]
             [xt.db.helpers.data-main-test :as sample]
             [xt.substrate.page-model :as page-model]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +views+
  {"UserAccount"
   {"select"
    {"by_organisation"
     {"input" [{"symbol" "i_organisation_id", "type" "uuid"}]
      "return" "jsonb"
      "view" {"query" {"organisation_accesses"
                       {"organisation" "{{i_organisation_id}}"}}}}}
    "return"
    {"info"
     {"input" [{"symbol" "i_account_id", "type" "uuid"}]
      "return" "jsonb"
      "view" {"query" ["id" "nickname"]}}}}})

^{:refer xt.db.node.model-query/prepare-resolver :added "4.1"}
(fact "prepares a db/query resolver through schema-query"
  (!.js
    (var state (page-model/base-state {"schema" sample/Schema
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
         (model-query/prepare-resolver
          state
          {"type" "db/query"
           "table" "UserAccount"
           "select_method" "by_organisation"
           "return_method" "info"}
          {"model_id" "orders"
           "view_id" "main"
           "args" ["00000000-0000-0000-0000-000000000001"]}))
    [ok
     (. prepared ["context"] ["model_id"])
     (xt/x:is-object? (. prepared ["tables"]))])
  => [true "orders" true])


^{:refer xt.db.node.model-query/payload-view-context :added "4.1"}
(fact "merges top-level payload view context into the nested view payload"
  (!.js
    [(model-query/payload-view-context
      {"view" {"model_id" "orders"}
       "view_id" "detail"
       "args" ["a"]})
     (model-query/payload-view-context
      {"model_id" "orders"
       "view_id" "detail"
       "input" ["a" "b"]})
     (model-query/payload-view-context {})])
  => [{"model_id" "orders"
       "view_id" "detail"
       "args" ["a"]}
      {"model_id" "orders"
       "view_id" "detail"
       "args" ["a" "b"]}
      {}])

^{:refer xt.db.node.model-query/query-resolver :added "4.1"}
(fact "prefers the nested resolver entry when present"
  (!.js
    [(model-query/query-resolver
      {"resolver" {"type" "db/query"
                   "table" "UserAccount"}})
     (model-query/query-resolver
      {"type" "db/query"
       "table" "UserAccount"})])
  => [{"type" "db/query"
       "table" "UserAccount"}
      {"type" "db/query"
       "table" "UserAccount"}])

^{:refer xt.db.node.model-query/resolver-triggers :added "4.1"}
(fact "collects dependent tables for a db/query resolver"
  (!.js
    (var state (page-model/base-state {"schema" sample/Schema
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
    (var out
         (model-query/resolver-triggers
          state
          {"table" "UserAccount"
           "select_method" "by_organisation"
           "return_method" "info"}))
    [(. out ["OrganisationAccess"])
     (. out ["UserAccount"])])
  => [true true])