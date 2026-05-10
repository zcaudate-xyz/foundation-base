(ns js.cell.service.db-supabase-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.cell.service.db-supabase :as db-supabase]
             [xt.lang.spec-base :as xt]
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

  (!.js
   (var db (xtd/obj-assign (@! +db+)
                          {"execute" (fn [compiled _]
                                       (return [true compiled]))}))
   [(db-supabase/supabase-capable? db)
    (db-supabase/supabase-capable? (@! +db+))])
  => [true false])

^{:refer js.cell.service.db-supabase/compile-select-item :added "4.1"}
(fact "compiles individual return entries to Supabase select syntax"

  (!.js
   [(db-supabase/compile-select-item "status")
    (db-supabase/compile-select-item ["account" ["nickname"]])])
  => ["status"
      "account(nickname)"])

^{:refer js.cell.service.db-supabase/compile-select :added "4.1"}
(fact "compiles a full return vector to Supabase select syntax"

  (!.js
   (db-supabase/compile-select
    ["status"
     ["account" ["nickname"]]]))
  => "status,account(nickname)")

^{:refer js.cell.service.db-supabase/compile-filters-into :added "4.1"}
(fact "compiles nested where clauses into Supabase filter descriptors"

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

  (!.js
   (var compiled
        (db-supabase/compile-query
         (@! +db+)
         ["Order"
          {"account" {"id" "acct-1"}}
          ["status"
           ["account" ["nickname"]]]]
         {}))
   [(. compiled ["type"])
    (. compiled ["table"])
    (. compiled ["select"])
    (. compiled ["url"])])
  => ["query"
      "Order"
      "status,account(nickname)"
      "/rest/v1/Order?select=status,account(nickname)&account.id=eq.acct-1"])

^{:refer js.cell.service.db-supabase/execute-query :added "4.1"}
(fact "executes a compiled query via the injected executor"

  (!.js
   (var [ok compiled]
        (db-supabase/execute-query
         {"execute" (fn [compiled _]
                      (return [true compiled]))}
         ["Order"
          {"account" {"id" "acct-1"}}
          ["status"
           ["account" ["nickname"]]]]
         {}))
   [ok
    (. compiled ["table"])
    (. compiled ["url"])])
  => [true
      "Order"
      "/rest/v1/Order?select=status,account(nickname)&account.id=eq.acct-1"]

  (!.js
   (var seen nil)
   [(db-supabase/execute-query
      (xtd/obj-assign (@! +db+)
                      {"request_sync" (fn [request _]
                                        (:= seen request)
                                        (return {"body" [{"id" "ord-1"
                                                          "status" "open"}]}))
                       "base_url" "https://db.test"})
       ["Order"
        {"account" {"id" "acct-1"}}
        ["status"
         ["account" ["nickname"]]]]
      {})
    (. seen ["url"])])
  => [[true [{"id" "ord-1"
              "status" "open"}]]
      "https://db.test/rest/v1/Order?select=status,account(nickname)&account.id=eq.acct-1"] )

^{:refer js.cell.service.db-supabase/map-supabase-error :added "4.1"}
(fact "maps execution errors into the local error contract"

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

  (!.js
   (var db (xtd/obj-assign (@! +db+)
                          {"execute" (fn [compiled _]
                                       (return [true compiled]))}))
   (var [ok compiled]
        (db-supabase/run-supabase-query
         db
         {:table "Order"
          :select-method "by_account"
          :return-method "default"}
         {"args" ["acct-1"]}))
   [ok
    (. compiled ["table"])
    (. compiled ["url"])])
  => [true
      "Order"
      "/rest/v1/Order?select=status,account(nickname)&account.id=eq.acct-1"])
