(ns js.cell.service.db-supabase-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.cell.service.db-supabase :as db-supabase]
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
     "nickname" {"ident" "nickname", "type" "text", "order" 1}}}
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
                        ["account" ["nickname"]]]}}}}}})

^{:refer js.cell.service.db-supabase/supabase-capable? :added "4.1"}
(fact "checks whether a descriptor can execute compiled Supabase queries"
  ^:hidden

  (!.js
   (var db (k/obj-assign (@! +db+)
                         {"execute" (fn [compiled _]
                                      (return [true compiled]))}))
   [(db-supabase/supabase-capable? db)
    (db-supabase/supabase-capable? (@! +db+))])
  => [true false])

^{:refer js.cell.service.db-supabase/compile-select-item :added "4.1"}
(fact "compiles individual return entries to Supabase select syntax"
  ^:hidden

  (!.js
   [(db-supabase/compile-select-item "status")
    (db-supabase/compile-select-item ["account" ["nickname"]])])
  => ["status"
      "account(nickname)"])

^{:refer js.cell.service.db-supabase/compile-select :added "4.1"}
(fact "compiles a full return vector to Supabase select syntax"
  ^:hidden

  (!.js
   (db-supabase/compile-select
    ["status"
     ["account" ["nickname"]]]))
  => "status,account(nickname)")

^{:refer js.cell.service.db-supabase/compile-filters-into :added "4.1"}
(fact "compiles nested where clauses into Supabase filter descriptors"
  ^:hidden

  (!.js
   (db-supabase/compile-filters-into
    ""
    {"account" {"id" "acct-1"}
     "status" "open"}
    []))
  => [{"path" "account.id"
       "op" "eq"
       "value" "acct-1"}
      {"path" "status"
       "op" "eq"
       "value" "open"}])

^{:refer js.cell.service.db-supabase/compile-query :added "4.1"}
(fact "compiles a prepared query plan into a Supabase request"
  ^:hidden

  (!.js
   (db-supabase/compile-query
    (@! +db+)
    ["Order"
     {"account" {"id" "acct-1"}}
     ["status"
      ["account" ["nickname"]]]]
    {}))
  => {"table" "Order"
      "select" "status,account(nickname)"
      "filters" [{"path" "account.id"
                  "op" "eq"
                  "value" "acct-1"}]})

^{:refer js.cell.service.db-supabase/execute-query :added "4.1"}
(fact "executes a compiled query via the injected executor"
  ^:hidden

  (!.js
   (db-supabase/execute-query
    {"execute" (fn [compiled _]
                 (return [true compiled]))}
    ["Order"
     {"account" {"id" "acct-1"}}
     ["status"
      ["account" ["nickname"]]]]
    {}))
  => [true
      {"table" "Order"
       "select" "status,account(nickname)"
       "filters" [{"path" "account.id"
                   "op" "eq"
                   "value" "acct-1"}]}])

^{:refer js.cell.service.db-supabase/map-supabase-error :added "4.1"}
(fact "maps execution errors into the local error contract"
  ^:hidden

  (!.js
   [(db-supabase/map-supabase-error
     (@! +db+)
     {"status" "error"
      "tag" "supabase/fail"}
     {})
    (db-supabase/map-supabase-error
     {"map_error" (fn [error _]
                    (return {"status" "error"
                             "tag" "mapped/error"
                             "data" error}))}
     {"status" "error"
      "tag" "supabase/fail"}
     {})])
  => [{"status" "error"
       "tag" "db/supabase-query-failed"
       "data" {"status" "error"
               "tag" "supabase/fail"}}
      {"status" "error"
       "tag" "mapped/error"
       "data" {"status" "error"
               "tag" "supabase/fail"}}])

^{:refer js.cell.service.db-supabase/run-supabase-query :added "4.1"}
(fact "prepares, compiles, and executes a Supabase query"
  ^:hidden

  (!.js
   (var db (k/obj-assign (@! +db+)
                         {"execute" (fn [compiled _]
                                      (return [true compiled]))}))
   (db-supabase/run-supabase-query
    db
    {:table "Order"
     :select-method "by_account"
     :return-method "default"}
    {"args" ["acct-1"]}))
  => [true
      {"table" "Order"
       "select" "status,account(nickname)"
       "filters" [{"path" "account.id"
                   "op" "eq"
                   "value" "acct-1"}]}])
