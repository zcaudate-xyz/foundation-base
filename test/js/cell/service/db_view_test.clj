(ns js.cell.service.db-view-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.cell.service.db-view :as db-view]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-lib :as k]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +db+
  {"schema"
   {"Order"
    {"id" {"ident" "id", "type" "text", "order" 0}
     "status" {"ident" "status", "type" "text", "order" 1}
     "account" {"ident" "account",
                "type" "ref",
                "order" 2,
                "ref" {"ns" "Account",
                       "type" "forward",
                       "key" "account"}}}
    "Account"
    {"id" {"ident" "id", "type" "text", "order" 0}
     "nickname" {"ident" "nickname", "type" "text", "order" 1}
     "profile" {"ident" "profile",
                "type" "ref",
                "order" 2,
                "ref" {"ns" "Profile",
                       "type" "forward",
                       "key" "profile"}}}
    "Profile"
    {"id" {"ident" "id", "type" "text", "order" 0}
     "display_name" {"ident" "display_name", "type" "text", "order" 1}}}
   "views"
   {"Order"
    {"select"
     {"by_account"
      {"input" [{"symbol" "i_account_id", "type" "text"}]
       "return" "jsonb"
       "view" {"table" "Order",
               "type" "select",
               "tag" "by_account",
               "access" {"roles" {}},
               "guards" [],
               "query" {"account" {"id" "{{i_account_id}}"}}}}}
     "return"
     {"default"
      {"input" [{"symbol" "i_order_id", "type" "text"}]
       "return" "jsonb"
       "view" {"table" "Order",
               "type" "return",
               "tag" "default",
               "access" {"roles" {}},
               "guards" [],
               "query" ["status"
                        ["account"
                         ["nickname"
                          ["profile" ["display_name"]]]]]}}}}}})

^{:refer js.cell.service.db-view/get-views :added "4.1"}
(fact "gets the db views"

  (!.js
   (xtd/obj-keys (db-view/get-views (@! +db+))))
  => ["Order"])

^{:refer js.cell.service.db-view/get-schema :added "4.1"}
(fact "gets the db schema"

  (!.js
   (xtd/obj-keys (db-view/get-schema (@! +db+))))
  => ["Order" "Account" "Profile"])

^{:refer js.cell.service.db-view/view-query-return-entry :added "4.1"}
(fact "creates the return entry for `return-query` key"

  (!.js
   (db-view/view-query-return-entry "Order"
                                    ["status" ["account" ["nickname"]]]
                                    true))
  => {"input" [],
      "return" "jsonb",
      "flags" {},
      "view" {"table" "Order",
              "type" "return",
              "query" ["status"],
              "access" {"roles" {}},
              "guards" []}})

^{:refer js.cell.service.db-view/view-query-return-combined :added "4.1"}
(fact "creates the combined return entry for `return-query` key"

  (!.js
   (db-view/view-query-return-combined
    "Order"
    {"view" {"query" ["status"]}}
    ["id" ["account" ["nickname"]]]
    true))
  => {"view" {"query" ["status" "id"]}})

^{:refer js.cell.service.db-view/view-query-entries :added "4.1"}
(fact "gets the select and return entries"

  (!.js
   (db-view/view-query-entries
    (@! +db+)
    "Order"
    {:select-method "by_account"
     :return-method "default"}
    false))
  => (contains-in
      {"select_entry" {"view" {"table" "Order"
                               "type" "select"
                               "tag" "by_account"}}
       "return_entry" {"view" {"table" "Order"
                               "type" "return"
                               "tag" "default"}}})

  (!.js
   (db-view/view-query-entries
    (@! +db+)
    "Order"
    {:return-query ["status" "id"]}
    false))
  => {"select_entry" nil
      "return_entry" {"input" [],
                      "return" "jsonb",
                      "flags" {},
                      "view" {"table" "Order",
                              "type" "return",
                              "query" ["status" "id"],
                              "access" {"roles" {}},
                              "guards" []}}})

^{:refer js.cell.service.db-view/view-triggers :added "4.1"}
(fact "gets the triggers for a given view"

  (!.js
   (db-view/view-triggers
    (@! +db+)
    "Order"
    {:select-method "by_account"
     :return-method "default"}))
  => {"Order" true
      "Account" true
      "Profile" true})

^{:refer js.cell.service.db-view/view-overview :added "4.1"}
(fact "gets the view overview"

  (!.js
   (db-view/view-overview (@! +db+)))
  => {"Order"
      {"return" ["default"],
       "select" ["by_account"]}})

^{:refer js.cell.service.db-view/view-tables :added "4.1"}
(fact "gets the view tables"

  (!.js
   (db-view/view-tables (@! +db+)))
  => ["Order"])

^{:refer js.cell.service.db-view/view-methods :added "4.1"}
(fact "gets the view methods"

  (!.js
   (db-view/view-methods (@! +db+) "Order"))
  => {"return" ["default"],
      "select" ["by_account"]})

^{:refer js.cell.service.db-view/view-detail :added "4.1"}
(fact "gets the view detail"

  (!.js
   (db-view/view-detail (@! +db+) "Order" "select" "by_account"))
  => {"input" [{"symbol" "i_account_id", "type" "text"}]
      "return" "jsonb"
      "view" {"table" "Order",
              "type" "select",
              "tag" "by_account",
              "access" {"roles" {}},
              "guards" [],
              "query" {"account" {"id" "{{i_account_id}}"}}}})
