(ns xt.db.node.model-query-test
  (:require [hara.lang :as l]
            [xt.db.helpers.data-main-test :as sample])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.model-query :as model-query]
             [xt.db.node.schema-state :as schema-state]
             [xt.db.helpers.data-main-test :as sample]
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
    (var [ok prepared]
         (model-query/prepare-resolver
          (schema-state/base-state {"schema" sample/Schema
                                   "views" (@! +views+)})
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
