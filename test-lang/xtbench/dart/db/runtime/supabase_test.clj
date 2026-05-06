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
  => [true false false])

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
(fact "unwraps rows from explicit supabase executors"

  (!.dt
   (supabase/supabase-pull-sync
    {"execute" (fn [compiled _opts]
                 (return [true {"data" [{"table" (. compiled ["table"])
                                         "select" (. compiled ["select"])}]}]))}
    nil
    (@! +query-tree+)
    {}))
  => [{"table" "Order"
       "select" "status,account(nickname)"}])

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
