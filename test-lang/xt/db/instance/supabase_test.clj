(ns xt.db.instance.supabase-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.instance.supabase :as supabase]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +compiled-query+
  {"table" "Order"
   "select" "status,account(nickname)"
   "filters" [{"path" "account.id"
               "op" "eq"
               "value" "acct-1"}]})

^{:refer xt.db.instance.supabase/supabase-capable? :added "4.1"}
(fact "checks whether a descriptor can execute compiled supabase queries"

  (!.js
   [(supabase/supabase-capable? {"execute" (fn [_compiled _opts] (return [true true]))})
    (supabase/supabase-capable? {})])
  => [true false])

^{:refer xt.db.instance.supabase/compile-select-item :added "4.1"}
(fact "compiles nested return entries to PostgREST select syntax"

  (!.js
   [(supabase/compile-select-item "status")
    (supabase/compile-select-item ["account" ["nickname"]])])
  => ["status"
      "account(nickname)"])

^{:refer xt.db.instance.supabase/compile-select :added "4.1"}
(fact "compiles return vectors to PostgREST select syntax"

  (!.js
   (supabase/compile-select
    ["status"
     ["account" ["nickname"]]]))
  => "status,account(nickname)")

^{:refer xt.db.instance.supabase/compile-filters-into :added "4.1"}
(fact "compiles nested where clauses into PostgREST filters"

  (!.js
   (supabase/compile-filters-into
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

^{:refer xt.db.instance.supabase/compile-query :added "4.1"}
(fact "compiles a query plan into a PostgREST request"

  (!.js
   (var compiled
        (supabase/compile-query
         ["Order"
          {"account" {"id" "acct-1"}}
          ["status"
           ["account" ["nickname"]]]]))
   [(. compiled ["table"])
    (. compiled ["select"])
    (. (. (. compiled ["filters"]) [0]) ["path"])
    (. (. (. compiled ["filters"]) [0]) ["value"])])
  => ["Order" "status,account(nickname)" "account.id" "acct-1"])

^{:refer xt.db.instance.supabase/execute-query :added "4.1"}
(fact "dispatches compiled supabase queries through the injected executor"

  (!.js
   (var out
        (supabase/execute-query
         {"execute" (fn [compiled _opts]
                      (return [true compiled]))}
         (@! +compiled-query+)
         {}))
   [(. out [0])
    (. (. out [1]) ["table"])
    (. (. out [1]) ["select"])])
  => [true "Order" "status,account(nickname)"]

  (!.js
   (var out
        (supabase/execute-query
         {}
         (@! +compiled-query+)
         {}))
   [(. out [0])
    (. (. out [1]) ["tag"])
    (. (. (. out [1]) ["data"]) ["table"])])
  => [false "db/supabase-execute-not-provided" "Order"])

^{:refer xt.db.instance.supabase/map-supabase-error :added "4.1"}
(fact "maps supabase execution errors"

  (!.js
   [(supabase/map-supabase-error
     {}
     {"status" "error"
      "tag" "supabase/fail"}
     {})
    (supabase/map-supabase-error
     {"map_error" (fn [error _opts]
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

^{:refer xt.db.instance.supabase/supabase-pull-sync :added "4.1"}
(fact "compiles query trees and unwraps the supabase execution result"

  (!.js
   (var out
        (supabase/supabase-pull-sync
         {"execute" (fn [compiled _opts]
                      (return [true compiled]))}
         nil
         ["Order"
          {"account" {"id" "acct-1"}}
          ["status"
           ["account" ["nickname"]]]]
         {}))
   [(. out ["table"])
    (. out ["select"])])
  => ["Order" "status,account(nickname)"]

  (!.js
   (supabase/supabase-pull-sync
    {"execute" (fn [_compiled _opts]
                 (return [false {"status" "error"
                                 "tag" "supabase/fail"}]))}
    nil
    ["Order"
     {"account" {"id" "acct-1"}}
     ["status"
      ["account" ["nickname"]]]]
    {}))
  => {"status" "error"
      "tag" "db/supabase-query-failed"
      "data" {"status" "error"
              "tag" "supabase/fail"}})
