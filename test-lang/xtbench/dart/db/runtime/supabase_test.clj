(ns xtbench.dart.db.runtime.supabase-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [xt.db.runtime.supabase :as supabase]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +compiled-query+
  {"table" "Order"
   "select" "status,account(nickname)"
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

  (!.dt
   [(supabase/supabase-capable? {"execute" (fn [_compiled _opts] (return [true true]))})
    (supabase/supabase-capable? {"supabase" {"from" (fn [_table] (return {}))}})
    (supabase/supabase-capable? {})])
  => [true true false])

^{:refer xt.db.runtime.supabase/execute-query :added "4.1"}
(fact "dispatches compiled supabase queries through the injected executor"

  (!.dt
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

  (!.dt
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

  (!.dt
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
(fact "uses the default client path in js"

  (!.dt
   (var calls [])
   (var query nil)
   (:= query {"select" (fn [cols]
                         (xt/x:arr-push calls ["select" cols])
                         (return query))
              "eq" (fn [path value]
                     (xt/x:arr-push calls ["eq" path value])
                     (return query))
              "in" (fn [path value]
                     (xt/x:arr-push calls ["in" path value])
                     (return query))
              "then" (fn [f]
                       (xt/x:arr-push calls ["then"])
                       (return (f {"data" [{"id" "ord-1"
                                            "status" "open"}]})))})
   (var schema-client {"from" (fn [table]
                                (xt/x:arr-push calls ["from" table])
                                (return query))})
   (var client {"from" (fn [table]
                         (xt/x:arr-push calls ["from/root" table])
                         (return query))
                "schema" (fn [schema-name]
                           (xt/x:arr-push calls ["schema" schema-name])
                           (return schema-client))})
   (var out
        (supabase/supabase-pull-sync
         {"supabase" client
          "schema-name" "api"}
         nil
         (@! +query-tree+)
         {}))
   [out calls])
  => [[{"id" "ord-1"
        "status" "open"}]
      [["schema" "api"]
       ["from" "Order"]
       ["select" "status,account(nickname)"]
       ["eq" "account.id" "acct-1"]
       ["in" "id" ["ord-1" "ord-2"]]
       ["then"]]])

^{:refer xt.db.runtime.supabase/supabase-pull-sync :added "4.1"}
(fact "uses the default client path in js"

  (!.dt
   (var calls [])
   (var query nil)
   (:= query {"select" (fn [cols]
                         (xt/x:arr-push calls ["select" cols])
                         (return query))
              "eq" (fn [path value]
                     (xt/x:arr-push calls ["eq" path value])
                     (return query))
              "in" (fn [path value]
                     (xt/x:arr-push calls ["in" path value])
                     (return query))
              "then" (fn [f]
                       (xt/x:arr-push calls ["then"])
                       (return (f {"data" [{"id" "ord-1"
                                            "status" "open"}]})))})
   (var schema-client {"from" (fn [table]
                                (xt/x:arr-push calls ["from" table])
                                (return query))})
   (var client {"from" (fn [table]
                         (xt/x:arr-push calls ["from/root" table])
                         (return query))
                "schema" (fn [schema-name]
                           (xt/x:arr-push calls ["schema" schema-name])
                           (return schema-client))})
   (var out
        (supabase/supabase-pull-sync
         {"supabase" client
          "schema-name" "api"}
         nil
         (@! +query-tree+)
         {}))
   [out calls])
  => [[{"id" "ord-1"
        "status" "open"}]
      [["schema" "api"]
       ["from" "Order"]
       ["select" "status,account(nickname)"]
       ["eq" "account.id" "acct-1"]
       ["in" "id" ["ord-1" "ord-2"]]
       ["then"]]])

^{:refer xt.db.runtime.supabase/thenable? :added "4.1"}
(fact "detects thenable outputs"

  (!.dt
   [(supabase/thenable? {"then" (fn [_] (return nil))})
    (supabase/thenable? {"from" (fn [_] (return nil))})
    (supabase/thenable? nil)])
  => [true false false])

^{:refer xt.db.runtime.supabase/query-client? :added "4.1"}
(fact "detects supabase query clients"

  (!.dt
   [(supabase/query-client? {"from" (fn [_] (return {}))})
    (supabase/query-client? {"then" (fn [_] (return {}))})
    (supabase/query-client? nil)])
  => [true false false])

^{:refer xt.db.runtime.supabase/resolve-client :added "4.1"}
(fact "resolves clients in priority order"

  (!.dt
   (var db-sup {"tag" "db-sup" "from" (fn [_] (return {}))})
   (var db-client {"tag" "db-client" "from" (fn [_] (return {}))})
   (var opt-client {"tag" "opt-client" "from" (fn [_] (return {}))})
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

  (!.dt
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

  (!.dt
   (supabase/invoke-method-1 {"hello" (fn [x]
                                        (return ["hello" x]))}
                             "hello"
                             1))
  => ["hello" 1])

^{:refer xt.db.runtime.supabase/invoke-method-2 :added "4.1"}
(fact "invokes two-argument methods by key"

  (!.dt
   (supabase/invoke-method-2 {"hello" (fn [x y]
                                        (return ["hello" x y]))}
                             "hello"
                             1
                             2))
  => ["hello" 1 2])

^{:refer xt.db.runtime.supabase/apply-filter :added "4.1"}
(fact "applies compiled filters to a single query builder method"

  (!.dt
   (var calls [])
   (var query nil)
   (:= query {"eq" (fn [path value]
                     (xt/x:arr-push calls ["eq" path value])
                     (return query))
              "match" (fn [value]
                        (xt/x:arr-push calls ["match" value])
                        (return query))})
   (supabase/apply-filter query {"path" "account.id"
                                 "op" "eq"
                                 "value" "acct-1"})
   (supabase/apply-filter query {"path" "ignored"
                                 "op" "match"
                                 "value" {"status" "open"}})
   calls)
  => [["eq" "account.id" "acct-1"]
      ["match" {"status" "open"}]])

^{:refer xt.db.runtime.supabase/apply-filters :added "4.1"}
(fact "applies compiled filters in sequence"

  (!.dt
   (var calls [])
   (var query nil)
   (:= query {"eq" (fn [path value]
                     (xt/x:arr-push calls ["eq" path value])
                     (return query))
              "in" (fn [path value]
                     (xt/x:arr-push calls ["in" path value])
                     (return query))})
   (supabase/apply-filters query [{"path" "account.id"
                                   "op" "eq"
                                   "value" "acct-1"}
                                  {"path" "id"
                                   "op" "in"
                                   "value" ["ord-1" "ord-2"]}])
   calls)
  => [["eq" "account.id" "acct-1"]
      ["in" "id" ["ord-1" "ord-2"]]])

^{:refer xt.db.runtime.supabase/execute-query-default :added "4.1"}
(fact "builds and filters default client queries"

  (!.dt
   (var calls [])
   (var query nil)
   (:= query {"marker" "query"
              "select" (fn [cols]
                         (xt/x:arr-push calls ["select" cols])
                         (return query))
              "eq" (fn [path value]
                     (xt/x:arr-push calls ["eq" path value])
                     (return query))
              "in" (fn [path value]
                     (xt/x:arr-push calls ["in" path value])
                     (return query))})
   (var schema-client {"from" (fn [table]
                                (xt/x:arr-push calls ["from" table])
                                (return query))})
   (var client {"schema" (fn [schema-name]
                           (xt/x:arr-push calls ["schema" schema-name])
                           (return schema-client))
                "from" (fn [table]
                         (xt/x:arr-push calls ["from/root" table])
                         (return query))})
   (var compiled {"table" "Order"
                  "select" "status,account(nickname)"
                  "filters" [{"path" "account.id"
                              "op" "eq"
                              "value" "acct-1"}
                             {"path" "id"
                              "op" "in"
                              "value" ["ord-1" "ord-2"]}]})
   [(. (supabase/execute-query-default {"supabase" client
                                        "schema-name" "api"}
                                       compiled
                                       {}) ["marker"])
    calls
    (supabase/execute-query-default {} compiled {})])
  => ["query"
      [["schema" "api"]
       ["from" "Order"]
       ["select" "status,account(nickname)"]
       ["eq" "account.id" "acct-1"]
       ["in" "id" ["ord-1" "ord-2"]]]
      nil])

^{:refer xt.db.runtime.supabase/unwrap-query-output :added "4.1"}
(fact "unwraps tuples, data wrappers, and errors"

  (!.dt
   [(supabase/unwrap-query-output {}
                                  [true {"data" [{"id" "ord-1"}]}]
                                  {})
    (supabase/unwrap-query-output {}
                                  [false {"status" "error"
                                          "tag" "supabase/fail"}]
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
      {"status" "error"
       "tag" "db/supabase-query-failed"
       "data" {"tag" "supabase/fail"}}
      [{"id" "ord-2"}]
      {"status" "ok"}])

^{:refer xt.db.runtime.supabase/snake->kebab :added "4.1"}
(fact "converts snake keys to kebab keys"

  (!.dt
   [(supabase/snake->kebab "time_updated")
    (supabase/snake->kebab "id")
    (supabase/snake->kebab 1)])
  => ["time-updated" "id" 1])

^{:refer xt.db.runtime.supabase/normalize-row :added "4.1"}
(fact "normalizes row keys while keeping values intact"

  (!.dt
   [(supabase/normalize-row {"time_updated" 1
                             "user_id" "U1"})
    (supabase/normalize-row nil)])
  => [{"time-updated" 1
       "user-id" "U1"}
      nil])

^{:refer xt.db.runtime.supabase/payload->xdb-events :added "4.1"}
(fact "translates insert delete and primary-key update payloads"

  (!.dt
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

  (!.dt
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

  (!.dt
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

  (!.dt
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

  (!.dt
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
