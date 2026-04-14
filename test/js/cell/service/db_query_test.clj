(ns js.cell.service.db-query-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.cell.service.db-query :as db-query]
              [xt.db :as xdb]
              [xt.lang.common-spec :as xt]
              [xt.lang.common-data :as xtd]]})

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
               "query" {"account" {"id" "{{i_account_id}}"}
                        "__deleted__" true}}}}
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
                          ["profile" ["display_name"]]]]
                        "__deleted__"]}}}}}})

(def +lookup+
  {"Profile" {"position" 0}
   "Account" {"position" 1}
   "Order" {"position" 2}})

^{:refer js.cell.service.db-query/query-capable? :added "4.1"}
(fact "checks whether a descriptor can prepare queries"
  ^:hidden

  (!.js
   [(db-query/query-capable? (@! +db+))
    (db-query/query-capable? {})])
  => [true false])

^{:refer js.cell.service.db-query/view-local-transform :added "4.1"}
(fact "removes `__deleted__` from the local view query"
  ^:hidden

  (!.js
   (db-query/view-local-transform
    {"view" {"query" {"status" "open"
                       "__deleted__" true}}
     "input" []}))
  => {"view" {"query" {"status" "open"}}
      "input" []})

^{:refer js.cell.service.db-query/query-check :added "4.1"}
(fact "checks argument length and type against a view entry"
  ^:hidden

  (!.js
   [(db-query/query-check
     {"input" [{"symbol" "i_account_id", "type" "text"}]}
     ["acct-1"]
     false)
    (db-query/query-check
     {"input" [{"symbol" "i_account_id", "type" "text"}]}
     [1]
     false)])
  => [[true]
      [false {"status" "error"
              "tag" "net/arg-typecheck-failed"
              "data" {"input" 1
                      "spec" {"symbol" "i_account_id", "type" "text"}}}]])

^{:refer js.cell.service.db-query/normalize-query :added "4.1"}
(fact "normalizes a query spec with defaults from the view context"
  ^:hidden

  (!.js
   (db-query/normalize-query
    (@! +db+)
    {:table "Order"
     :select-method "by_account"
     :return-method "default"}
    {:args ["acct-1"]}))
  => {"table" "Order"
      "select_method" "by_account"
      "select_args" ["acct-1"]
      "return_method" "default"
      "return_args" []})

^{:refer js.cell.service.db-query/prepare-query :added "4.1"}
(fact "prepares a cache-view query tree from the descriptor"
  ^:hidden

  (!.js
   (var [ok tree] (db-query/prepare-query
                   (@! +db+)
                   {:table "Order"
                    :select-method "by_account"
                    :return-method "default"}
                   {:args ["acct-1"]}))
   [ok
     (xt/x:first tree)
     (xtd/get-in tree [1 "account" "id"])
     (xtd/last tree)])
  => [true
      "Order"
      "acct-1"
      ["status"
       ["account"
        ["nickname"
         ["profile" ["display_name"]]]]]])

^{:refer js.cell.service.db-query/execute-query :added "4.1"}
(fact "executes a prepared query against a local cache db"
  ^:hidden

  (!.js
   (var desc (@! +db+))
   (var local-db (xdb/db-create {"::" "db.cache"}
                                 (xt/x:get-key desc "schema")
                                (@! +lookup+)
                                nil))
   (xdb/sync-event local-db
                   ["add"
                    {"Profile" [{"id" "profile-1"
                                 "display_name" "Alpha"}
                                {"id" "profile-2"
                                 "display_name" "Beta"}]
                     "Account" [{"id" "acct-1"
                                 "nickname" "primary"
                                 "profile_id" "profile-1"}
                                {"id" "acct-2"
                                 "nickname" "backup"
                                 "profile_id" "profile-2"}]
                     "Order" [{"id" "ord-1"
                               "status" "open"
                               "account_id" "acct-1"}
                              {"id" "ord-2"
                               "status" "closed"
                               "account_id" "acct-2"}]}])
   (var [ok plan] (db-query/prepare-query
                   desc
                   {:table "Order"
                    :select-method "by_account"
                    :return-method "default"}
                   {:args ["acct-1"]}))
   (var [e-ok result] (db-query/execute-query desc
                                              plan
                                              {:db local-db}))
   [ok
    e-ok
    result])
  => [true
      true
      [{"status" "open"
        "account" [{"nickname" "primary"
                    "profile" [{"display_name" "Alpha"}]}]}]])

^{:refer js.cell.service.db-query/run-query :added "4.1"}
(fact "prepares and executes a local cache query"
  ^:hidden

  (!.js
   (var desc (@! +db+))
   (var local-db (xdb/db-create {"::" "db.cache"}
                                 (xt/x:get-key desc "schema")
                                (@! +lookup+)
                                nil))
   (xdb/sync-event local-db
                   ["add"
                    {"Profile" [{"id" "profile-1"
                                 "display_name" "Alpha"}]
                     "Account" [{"id" "acct-1"
                                 "nickname" "primary"
                                 "profile_id" "profile-1"}]
                     "Order" [{"id" "ord-1"
                               "status" "open"
                               "account_id" "acct-1"}]}])
   [(db-query/run-query desc
                        {:table "Order"
                         :select-method "by_account"
                         :return-method "default"}
                        {:args ["acct-1"]
                         :db local-db})
    (db-query/run-query desc
                        {:table "Order"
                         :select-method "by_account"}
                        {:args ["acct-1"]})])
  => [[true
       [{"status" "open"
         "account" [{"nickname" "primary"
                     "profile" [{"display_name" "Alpha"}]}]}]]
      [false {"status" "error"
              "tag" "db/local-db-not-provided"}]])
