(ns xtbench.python.db.runtime.supabase-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :python
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

(def +query-tree-basic+
  ["Order"
   {"account" {"id" "acct-1"}}
   ["status"
    ["account" ["nickname"]]]])

^{:refer xt.db.runtime.supabase/supabase-capable? :added "4.1"}
(fact "checks whether a descriptor can execute compiled supabase queries"

  (!.py
   [(supabase/supabase-capable? {"execute" (fn [_compiled _opts] (return [true true]))})
    (supabase/supabase-capable? {"supabase" {"query" (fn [_request _opts] (return {}))}})
    (supabase/supabase-capable? {})])
  => [true true false])

^{:refer xt.db.runtime.supabase/execute-query :added "4.1"}
(fact "dispatches compiled supabase queries through the injected executor"

  (!.py
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

  (!.py
   (var out
        (supabase/execute-query
         {}
         (@! +compiled-query+)
         {}))
   [(. out [0])
    (. (. out [1]) ["tag"])
    (. (. (. out [1]) ["data"]) ["table"])])
  => [false "db/supabase-execute-not-provided" "Order"])

^{:refer xt.db.runtime.supabase/map-supabase-error :added "4.1"}
(fact "maps supabase execution errors"

  (!.py
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

  (!.py
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

^{:refer xt.db.runtime.supabase/supabase-pull-sync :added "4.1"}
(fact "uses nested query handlers when no explicit executor is provided"

  (!.py
   (var seen nil)
   (var out
        (supabase/supabase-pull-sync
         {"supabase" {"query" (fn [request _opts]
                                (:= seen request)
                                (return {"data" [{"id" "ord-1"
                                                  "status" "open"}]}))
                       "headers" {"x-client" "nested"}}
          "base-url" "https://db.test"}
         nil
         (@! +query-tree+)
         {"auth" "token-1"}))
   [out
    (. (. seen ["headers"]) ["x-client"])
    (. (. seen ["headers"]) ["Authorization"])])
  => [[{"id" "ord-1"
         "status" "open"}]
      "nested"
      "Bearer token-1"])

^{:refer xt.db.runtime.supabase/thenable? :added "4.1"}
(fact "detects thenable outputs"

  (!.py
   [(supabase/thenable? {"then" (fn [_] (return nil))})
    (supabase/thenable? {"from" (fn [_] (return nil))})
    (supabase/thenable? nil)])
  => [true false false])

^{:refer xt.db.runtime.supabase/query-client? :added "4.1"}
(fact "detects supabase query clients"

  (!.py
   [(supabase/query-client? {"query" (fn [_ _opts] (return {}))})
     (supabase/query-client? {"then" (fn [_] (return {}))})
     (supabase/query-client? nil)])
  => [true false false])

^{:refer xt.db.runtime.supabase/resolve-client :added "4.1"}
(fact "resolves clients in priority order"

  (!.py
   (var db-sup {"tag" "db-sup" "query" (fn [_ _opts] (return {}))})
   (var db-client {"tag" "db-client" "query" (fn [_ _opts] (return {}))})
   (var opt-client {"tag" "opt-client" "query" (fn [_ _opts] (return {}))})
   [(. (supabase/resolve-client {"supabase" db-sup
                                 "client" db-client}
                                {"client" opt-client}) ["tag"])
    (. (supabase/resolve-client {"client" db-client}
                                {"supabase" opt-client}) ["tag"])
    (. (supabase/resolve-client {}
                                {"client" opt-client}) ["tag"])
    (supabase/resolve-client {} {})])
  => ["db-sup" "db-client" "opt-client" nil])

^{:refer xt.db.runtime.supabase/resolve-schema-name :added "4.1"}
(fact "resolves schema names in priority order"

  (!.py
   [(supabase/resolve-schema-name {"schema-name" "api"}
                                  {"schema-name" "ignored"})
    (supabase/resolve-schema-name {"schema" "public"}
                                  {"schema-name" "ignored"})
    (supabase/resolve-schema-name {}
                                  {"schema_name" "alt"})
    (supabase/resolve-schema-name {} {})])
  => ["api" "public" "alt" nil])

^{:refer xt.db.runtime.supabase/invoke-method-1 :added "4.1"}
(fact "invokes single-argument methods by key"

  (!.py
   (supabase/invoke-method-1 {"hello" (fn [x]
                                        (return ["hello" x]))}
                             "hello"
                             1))
  => ["hello" 1])

^{:refer xt.db.runtime.supabase/invoke-method-2 :added "4.1"}
(fact "invokes two-argument methods by key"

  (!.py
   (supabase/invoke-method-2 {"hello" (fn [x y]
                                        (return ["hello" x y]))}
                             "hello"
                             1
                             2))
  => ["hello" 1 2])

^{:refer xt.db.runtime.supabase/apply-filter :added "4.1"}
(fact "delegates filter compilation to PostgREST params"

  (!.py
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

  (!.py
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

  (!.py
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
    (. (. seen ["headers"]) ["Content-Profile"])
    (supabase/execute-query-default {} compiled {})])
  => [{"marker" "query"}
      "https://db.test/rest/v1/Order?select=status,account(nickname)&account.id=eq.acct-1&id=in.(ord-1,ord-2)"
      "api"
      nil])

^{:refer xt.db.runtime.supabase/unwrap-query-output :added "4.1"}
(fact "unwraps tuples, data wrappers, and errors"

  (!.py
   [(supabase/unwrap-query-output {}
                                  [true {"data" [{"id" "ord-1"}]}]
                                  {})
     (supabase/unwrap-query-output {}
                                  [false {"status" "error"
                                          "tag" "supabase/fail"}]
                                  {})
     (supabase/unwrap-query-output {}
                                   {"status" 200
                                    "body" [{"id" "ord-http"}]}
                                   {})
     (supabase/unwrap-query-output {}
                                   {"error" {"tag" "supabase/fail"}}
                                   {})
    (supabase/unwrap-query-output {}
                                  {"data" [{"id" "ord-2"}]}
                                  {})
    (supabase/unwrap-query-output {}
                                   {"status" "ok"}
                                   {})])
   => [[{"id" "ord-1"}]
       {"status" "error"
        "tag" "db/supabase-query-failed"
        "data" {"status" "error"
                "tag" "supabase/fail"}}
       [{"id" "ord-http"}]
       {"status" "error"
        "tag" "db/supabase-query-failed"
        "data" {"tag" "supabase/fail"}}
       [{"id" "ord-2"}]
      {"status" "ok"}])

^{:refer xt.db.runtime.supabase/snake->kebab :added "4.1"}
(fact "converts snake keys to kebab keys"

  (!.py
   [(supabase/snake->kebab "time_updated")
    (supabase/snake->kebab "id")
    (supabase/snake->kebab 1)])
  => ["time-updated" "id" 1])

^{:refer xt.db.runtime.supabase/normalize-row :added "4.1"}
(fact "normalizes row keys while keeping values intact"

  (!.py
   [(supabase/normalize-row {"time_updated" 1
                             "user_id" "U1"})
    (supabase/normalize-row nil)])
  => [{"time-updated" 1
       "user-id" "U1"}
      nil])

^{:refer xt.db.runtime.supabase/payload->xdb-events :added "4.1"}
(fact "translates insert delete and primary-key update payloads"

  (!.py
   [(supabase/payload->xdb-events
     {"type" "postgres_changes"
      "eventType" "INSERT"
      "table" "Currency"
      "new" {"id" "USD"
             "time_updated" 1}})
    (supabase/payload->xdb-events
     {"type" "postgres_changes"
      "eventType" "DELETE"
      "table" "Currency"
      "old" {"id" "USD"}})
    (supabase/payload->xdb-events
     {"type" "postgres_changes"
      "eventType" "UPDATE"
      "table" "Currency"
      "old" {"id" "USD"}
      "new" {"id" "USD2"
             "time_updated" 2}})])
  => [[["add" {"Currency" [{"id" "USD"
                            "time-updated" 1}]}]]
      [["remove" {"Currency" [{"id" "USD"}]}]]
      [["remove" {"Currency" [{"id" "USD"}]}]
       ["add" {"Currency" [{"id" "USD2"
                            "time-updated" 2}]}]]])

^{:refer xt.db.runtime.supabase/process-triggers-local :added "4.1"}
(fact "runs triggers whose listen tables were touched"

  (!.py
   (var calls [])
   (var out
        (supabase/process-triggers-local
         {"triggers" {"currency" {"listen" ["Currency"]
                                  "callback" (fn [_db _trigger]
                                               (xt/x:arr-push calls "currency"))}
                      "wallet" {"listen" ["Wallet"]
                                "callback" (fn [_db _trigger]
                                             (xt/x:arr-push calls "wallet"))}}}
         {"Currency" true}))
   [out calls])
  => [["currency"]
      ["currency"]])

^{:refer xt.db.runtime.supabase/sync-event-local :added "4.1"}
(fact "passes sync handler outputs through local trigger processing"

  (!.py
   (supabase/sync-event-local
    {"sync_handler" (fn [_event]
                      (return ["Currency" "Wallet"]))
     "triggers" {"wallet" {"listen" ["Wallet"]
                           "callback" (fn [_db _trigger]
                                        (return true))}}}
    ["add" {}]))
  => [["wallet"]
      {"Currency" true
       "Wallet" true}])

^{:refer xt.db.runtime.supabase/apply-payload :added "4.1"}
(fact "applies payloads through local sync handlers"

  (!.py
   (var seen [])
   (var db {"sync_handler" (fn [event]
                             (xt/x:arr-push seen event)
                             (var [_tag body] event)
                             (return (xt/x:obj-keys body)))
            "triggers" {}})
   (var res
        (supabase/apply-payload
         db
         {"type" "postgres_changes"
          "eventType" "INSERT"
          "table" "Currency"
          "new" {"id" "USD"
                 "time_updated" 1}}
         nil nil nil {}))
   [seen res])
  => [[["add" {"Currency" [{"id" "USD"
                            "time-updated" 1}]}]]
      {"table" "Currency"
       "ids" ["USD"]
       "events" [["add" {"Currency" [{"id" "USD"
                                      "time-updated" 1}]}]]}])

^{:refer xt.db.runtime.supabase/attach-events :added "4.1"}
(fact "attaches realtime handlers and applies payloads through them"

  (!.py
   (var seen [])
   (var handlers [])
   (var channel nil)
   (:= channel {"on" (fn [_type _binding handler]
                       (xt/x:arr-push handlers handler)
                       (return channel))
                "subscribe" (fn [] (return channel))
                "unsubscribe" (fn [] (return true))})
   (var db {"sync_handler" (fn [event]
                             (xt/x:arr-push seen event)
                             (return ["Currency"]))
            "triggers" {}})
   (var res
        (supabase/attach-events
         {"supabase" {"channel" (fn [_name]
                                  (return channel))}
          "xdb" db
          "channel-name" "test"
          "bindings" [{"event" "*"
                       "schema" "public"
                       "table" "Currency"}]}))
   ((xt/x:first handlers)
    {"type" "postgres_changes"
     "eventType" "INSERT"
     "table" "Currency"
     "new" {"id" "USD"}})
   [(xt/x:len handlers)
    seen
    ((xt/x:get-key res "detach-fn"))])
  => [1
      [["add" {"Currency" [{"id" "USD"}]}]]
      true])
