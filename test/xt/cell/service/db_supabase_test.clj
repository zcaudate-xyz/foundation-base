(ns xt.cell.service.db-supabase-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.cell.service.db-supabase :as db-supabase]]})

(fact:global
  {:setup    [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

^{:refer xt.cell.service.db-supabase/supabase-capable? :added "4.1"}
(fact "checks whether a db descriptor can execute Supabase queries"

  (!.js
   [(db-supabase/supabase-capable? {"execute" (fn:> [compiled ctx] [true compiled])})
    (db-supabase/supabase-capable? {})])
  => [true false])

^{:refer xt.cell.service.db-supabase/compile-select-item :added "4.1"}
(fact "compiles a single select item into Supabase syntax"

  (!.js
   [(db-supabase/compile-select-item "id")
    (db-supabase/compile-select-item ["profile" ["bio" "avatar"]])])
  => ["id"
      "profile(bio,avatar)"])

^{:refer xt.cell.service.db-supabase/compile-select :added "4.1"}
(fact "compiles a select vector into Supabase syntax"

  (!.js
   (db-supabase/compile-select
    ["id" ["profile" ["bio"]]]))
  => "id,profile(bio)")

^{:refer xt.cell.service.db-supabase/compile-filters-into :added "4.1"}
(fact "flattens nested where filters into Supabase filter descriptors"

  (!.js
   (db-supabase/compile-filters-into
    ""
    {"profile" {"id" ["in" [["p1" "p2"]]]
                "active" true}}
    []))
  => [{"path" "profile.id"
       "op" "in"
       "value" ["p1" "p2"]}
      {"path" "profile.active"
       "op" "eq"
       "value" true}])

^{:refer xt.cell.service.db-supabase/compile-query :added "4.1"}
(fact "compiles a query plan into a Supabase request descriptor"

  (!.js
   (db-supabase/compile-query
    {}
    ["User"
     {"profile" {"active" true}}
     ["id" ["profile" ["bio"]]]]
    {}))
  => {"table" "User"
      "select" "id,profile(bio)"
      "filters" [{"path" "profile.active"
                  "op" "eq"
                  "value" true}]})

^{:refer xt.cell.service.db-supabase/execute-query :added "4.1"}
(fact "requires an injected executor for compiled Supabase queries"

  (!.js
   [(db-supabase/execute-query {} ["User" ["id"]] {})
    (db-supabase/execute-query
     {"execute" (fn [compiled ctx]
                  (return [true {"compiled" compiled}]))}
     ["User" ["id"]]
     {"view-id" "list"})])
  => [[false
       {"status" "error"
        "tag" "db/supabase-execute-not-provided"
        "data" {"table" "User"
                "select" "id"
                "filters" []}}]
      [true
       {"compiled" {"table" "User"
                    "select" "id"
                    "filters" []}}]])

^{:refer xt.cell.service.db-supabase/map-supabase-error :added "4.1"}
(fact "maps Supabase execution errors into the local contract"

  (!.js
    [(db-supabase/map-supabase-error {} {"message" "boom"} {})
     (db-supabase/map-supabase-error
      {"map_error" (fn [error ctx]
                     (return {"status" "error"
                              "tag" "custom/supabase"
                              "data" {"error" error}}))}
      {"message" "boom"}
      {"view-id" "list"})])
  => [{"status" "error"
       "tag" "db/supabase-query-failed"
       "data" {"message" "boom"}}
      {"status" "error"
       "tag" "custom/supabase"
       "data" {"error" {"message" "boom"}}}])

^{:refer xt.cell.service.db-supabase/run-supabase-query :added "4.1"}
(fact "returns query preparation errors before execution"

  (!.js
   (db-supabase/run-supabase-query
    {"schema" {}
     "views" {"User" {"select" {} "return" {}}}}
    {"table" "User"
     "select_method" "missing"}
    {}))
  => [false
      {"status" "error"
       "tag" "net/select-method-not-found"
       "data" {"input" "missing"}}])
