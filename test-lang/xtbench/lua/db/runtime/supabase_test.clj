(ns xtbench.lua.db.runtime.supabase-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.db.runtime.supabase :as supabase]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +compiled-query+
  {"type" "query"
   "table" "Order"
   "method" "GET"
   "path" "/rest/v1/Order"
   "select" "status,account(nickname)"
   "params" ["select=status,account(nickname)"
             "account.id=eq.acct-1"]
   "query" "select=status,account(nickname)&account.id=eq.acct-1"
   "url" "/rest/v1/Order?select=status,account(nickname)&account.id=eq.acct-1"
   "headers" {}
   "filters" [{"path" "account.id"
               "op" "eq"
               "value" "acct-1"}]})

(def +query-tree+
  ["Order"
   {"account" {"id" "acct-1"}
    "id" ["in" [["ord-1" "ord-2"]]]}
   ["status"
    ["account" ["nickname"]]]])

^{:refer xt.db.runtime.supabase/supabase-capable? :added "4.1"}
(fact "checks whether a descriptor can execute compiled supabase queries"

  (!.lua
   [(supabase/supabase-capable? {"execute" (fn [_compiled _opts] (return [true true]))})
    (supabase/supabase-capable? {"supabase" {"query" (fn [_request _opts] (return {}))}})
    (supabase/supabase-capable? {})])
  => [true true false])

^{:refer xt.db.runtime.supabase/execute-query :added "4.1"}
(fact "dispatches compiled supabase queries through the injected executor"

  (!.lua
   (var out
        (supabase/execute-query
         {"execute" (fn [compiled _opts]
                      (return [true compiled]))}
         (@! +compiled-query+)
         {}))
   [(xt/x:first out)
    (. (xt/x:second out) ["table"])
    (. (xt/x:second out) ["select"])])
  => [true "Order" "status,account(nickname)"]

  (!.lua
   (var out
        (supabase/execute-query
         {}
         (@! +compiled-query+)
         {}))
   [(xt/x:first out)
    (. (xt/x:second out) ["tag"])
    (. (. (xt/x:second out) ["data"]) ["table"])])
  => [false "db/supabase-execute-not-provided" "Order"])

^{:refer xt.db.runtime.supabase/map-supabase-error :added "4.1"}
(fact "maps supabase execution errors"

  (!.lua
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

^{:refer xt.db.runtime.supabase/supabase-pull-sync :added "4.1"}
(fact "prepares query requests for a generic http client"

  (!.lua
   (var seen nil)
   (var out
        (supabase/supabase-pull-sync
         {"request" (fn [request _opts]
                      (:= seen request)
                      (return {"body" [{"id" "ord-1"
                                        "status" "open"}]}))
          "base-url" "https://db.test"
          "schema-name" "api"
          "apikey" "anon-key"}
         nil
         (@! +query-tree+)
         {}))
   [out
    (. seen ["url"])
    (. (. seen ["headers"]) ["Content-Profile"])
    (. (. seen ["headers"]) ["apikey"])
    (. (. seen ["headers"]) ["Authorization"])])
  => [[{"id" "ord-1"
        "status" "open"}]
      "https://db.test/rest/v1/Order?select=status,account(nickname)&account.id=eq.acct-1&id=in.(ord-1,ord-2)"
      "api"
      "anon-key"
      "Bearer anon-key"])

^{:refer xt.db.runtime.supabase/query-client? :added "4.1"}
(fact "detects supabase query clients without relying on js-only syntax"

  (!.lua
   (var thenable {})
   (xt/x:set-key thenable "then" (fn [_] (return nil)))
   [(supabase/query-client? {"query" (fn [_ _opts] (return {}))})
    (supabase/query-client? thenable)
    (supabase/query-client? nil)])
  => [true false false])

^{:refer xt.db.runtime.supabase/resolve-client :added "4.1"}
(fact "resolves clients in priority order"

  (!.lua
   (var db-sup {"tag" "db-sup" "query" (fn [_ _opts] (return {}))})
   (var db-client {"tag" "db-client" "query" (fn [_ _opts] (return {}))})
   (var opt-client {"tag" "opt-client" "query" (fn [_ _opts] (return {}))})
   [(. (supabase/resolve-client {"supabase" db-sup
                                 "client" db-client}
                                {"client" opt-client}) ["tag"])
    (. (supabase/resolve-client {"client" db-client}
                                {"supabase" opt-client}) ["tag"])
    (. (supabase/resolve-client {}
                                {"client" opt-client}) ["tag"])])
  => ["db-sup" "db-client" "opt-client"])

^{:refer xt.db.runtime.supabase/resolve-schema-name :added "4.1"}
(fact "resolves schema names in priority order"

  (!.lua
   [(supabase/resolve-schema-name {"schema-name" "api"}
                                  {"schema-name" "ignored"})
    (supabase/resolve-schema-name {"schema" "public"}
                                  {"schema-name" "ignored"})
    (supabase/resolve-schema-name {}
                                  {"schema_name" "alt"})])
  => ["api" "public" "alt"])

^{:refer xt.db.runtime.supabase/apply-filter :added "4.1"}
(fact "delegates filter compilation to PostgREST params"

  (!.lua
   [(supabase/apply-filter {"path" "account.id"
                            "op" "eq"
                            "value" "acct-1"})
    (supabase/apply-filter {"path" "ignored"
                            "op" "match"
                            "value" {"status" "open"}})])
  => [["account.id=eq.acct-1"]
      ["status=eq.open"]])

^{:refer xt.db.runtime.supabase/apply-filters :added "4.1"}
(fact "compiles filter params in sequence"

  (!.lua
   (supabase/apply-filters [{"path" "account.id"
                             "op" "eq"
                             "value" "acct-1"}
                            {"path" "id"
                             "op" "in"
                             "value" ["ord-1" "ord-2"]}]))
  => ["account.id=eq.acct-1"
      "id=in.(ord-1,ord-2)"])

^{:refer xt.db.runtime.supabase/execute-query-default :added "4.1"}
(fact "prepares and dispatches default transport requests"

  (!.lua
   (var seen nil)
   (var compiled {"type" "query"
                  "table" "Order"
                  "method" "GET"
                  "path" "/rest/v1/Order"
                  "select" "status,account(nickname)"
                  "filters" [{"path" "account.id"
                              "op" "eq"
                              "value" "acct-1"}
                             {"path" "id"
                              "op" "in"
                              "value" ["ord-1" "ord-2"]}]
                  "params" ["select=status,account(nickname)"
                            "account.id=eq.acct-1"
                            "id=in.(ord-1,ord-2)"]
                  "query" "select=status,account(nickname)&account.id=eq.acct-1&id=in.(ord-1,ord-2)"
                  "url" "/rest/v1/Order?select=status,account(nickname)&account.id=eq.acct-1&id=in.(ord-1,ord-2)"
                  "headers" {}})
   [(supabase/execute-query-default {"supabase" {"query" (fn [request _opts]
                                                           (:= seen request)
                                                           (return {"marker" "query"}))}
                                     "base-url" "https://db.test"
                                     "schema-name" "api"}
                                    compiled
                                    {})
    (. seen ["url"])
    (. (. seen ["headers"]) ["Content-Profile"])])
  => [{"marker" "query"}
      "https://db.test/rest/v1/Order?select=status,account(nickname)&account.id=eq.acct-1&id=in.(ord-1,ord-2)"
      "api"])

^{:refer xt.db.runtime.supabase/unwrap-query-output :added "4.1"}
(fact "unwraps tuples, data wrappers, and http bodies"

  (!.lua
   [(supabase/unwrap-query-output {}
                                  [true {"data" [{"id" "ord-1"}]}]
                                  {})
    (supabase/unwrap-query-output {}
                                  {"status" 200
                                   "body" [{"id" "ord-http"}]}
                                  {})
    (supabase/unwrap-query-output {}
                                  {"error" {"tag" "supabase/fail"}}
                                  {})])
  => [[{"id" "ord-1"}]
      [{"id" "ord-http"}]
      {"status" "error"
       "tag" "db/supabase-query-failed"
       "data" {"tag" "supabase/fail"}}])
