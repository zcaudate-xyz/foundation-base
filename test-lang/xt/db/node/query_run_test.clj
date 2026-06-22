(ns xt.db.node.query-run-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.query-view :as db-view]
             [xt.db.node.query-run :as db-query]
             [xt.db.system.main :as main]
             [xt.db.system.impl-common :as impl-common]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-notify :as notify]]})

(fact:global
 {:setup [(l/rt:restart)]
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

^{:refer xt.db.node.query-run/query-capable? :added "4.1"}
(fact "checks whether a descriptor can prepare queries"

  (!.js
   [(db-query/query-capable? (@! +db+))
    (db-query/query-capable? {})])
  => [true false])

^{:refer xt.db.node.query-run/view-local-transform :added "4.1"}
(fact "removes `__deleted__` from the local view query"

  (!.js
   (db-query/view-local-transform
    {"view" {"query" {"status" "open"
                       "__deleted__" true}}
     "input" []}))
  => {"view" {"query" {"status" "open"}}
      "input" []})

^{:refer xt.db.node.query-run/query-check :added "4.1"}
(fact "checks argument length and type against a view entry"

  (!.js
   [(db-query/query-check
     {"input" [{"symbol" "i_account_id", "type" "text"}]}
     ["acct-1"]
     false)
    (db-query/query-check
     {"input" [{"symbol" "i_account_id", "type" "text"}]}
     [1]
     false)])
  => [[true nil]
      [false {"status" "error"
              "tag" "net/arg-typecheck-failed"
              "data" {"input" 1
                      "spec" {"symbol" "i_account_id", "type" "text"}}}]])

^{:refer xt.db.node.query-run/normalize-query :added "4.1"}
(fact "normalizes a query spec with defaults from the view context"

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

^{:refer xt.db.node.query-run/prepare-query :added "4.1"}
(fact "prepares a canonical query tree from the descriptor"

  (!.js
   (var [ok tree] (db-query/prepare-query
                   (@! +db+)
                   {:table "Order"
                    :select-method "by_account"
                    :return-method "default"}
                   {:args ["acct-1"]}))
   [ok
    (xt/x:first tree)
    (xtd/get-in tree [1 "where" 0 "account" "id"])
    (xtd/get-in tree [1 "data"])
    (xtd/get-in tree [1 "links" 0 0])])
  => [true
      "Order"
      "acct-1"
      ["status"]
      "account"])


^{:refer xt.db.node.query-run/execute-query :added "4.1"}
(fact "executes a prepared query against a local system impl"

  (notify/wait-on [:js 10000]
    (-> (main/create-impl "memory" {} (db-view/get-schema (@! +db+)) (@! +lookup+))
        (main/create-impl-init)
        (promise/x:promise-then
         (fn [impl]
           (impl-common/process-add-event impl
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
                                                     "account_id" "acct-2"}]}).
           (var [ok plan] (db-query/prepare-query
                           (@! +db+)
                           {:table "Order"
                            :select-method "by_account"
                            :return-method "default"}
                           {:args ["acct-1"]}))
           (var [e-ok result] (db-query/execute-query
                               (@! +db+)
                               plan
                               {:db impl}))
           (repl/notify [ok e-ok result])))))
  => [true
      true
      [{"status" "open"
        "account" nil}]])

^{:refer xt.db.node.query-run/run-query :added "4.1"}
(fact "executes a local system query when db is provided"

  (notify/wait-on [:js 10000]
    (-> (main/create-impl "memory" {} (db-view/get-schema (@! +db+)) (@! +lookup+))
        (main/create-impl-init)
        (promise/x:promise-then
         (fn [impl]
           (impl-common/process-add-event impl
                                          {"Profile" [{"id" "profile-1"
                                                       "display_name" "Alpha"}]
                                           "Account" [{"id" "acct-1"
                                                       "nickname" "primary"
                                                       "profile_id" "profile-1"}]
                                           "Order" [{"id" "ord-1"
                                                     "status" "open"
                                                     "account_id" "acct-1"}]})
           (repl/notify
            (db-query/run-query (@! +db+)
                                {:table "Order"
                                 :select-method "by_account"
                                 :return-method "default"}
                                {:args ["acct-1"]
                                 :db impl}))))))
  => [true
      [{"status" "open"
        "account" nil}]])

^{:refer xt.db.node.query-run/run-query :added "4.1"}
(fact "returns an error when local db is not provided"

  (!.js
   (db-query/run-query (@! +db+)
                       {:table "Order"
                        :select-method "by_account"}
                       {:args ["acct-1"]}))
  => [false {"status" "error"
             "tag" "db/local-db-not-provided"}])
