(ns xt.cell.service.db-view-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.cell.service.db-view :as db-view]]})

(fact:global
  {:setup    [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

(def +db+
  {"schema" {}
   "views"
   {"User"
    {"select"
     {"all"
      {"input" []
       "view" {"query" ["id" "name"]}}}
     "return"
     {"detail"
      {"input" []
       "view" {"query" ["id" ["profile" ["bio"]]]}}}}}})

^{:refer xt.cell.service.db-view/get-views :added "4.1"}
(fact "gets the db views map"

  (!.js
   (db-view/get-views (@! +db+)))
  => {"User"
      {"select"
       {"all"
        {"input" []
         "view" {"query" ["id" "name"]}}}
       "return"
       {"detail"
        {"input" []
         "view" {"query" ["id" ["profile" ["bio"]]]}}}}})

^{:refer xt.cell.service.db-view/get-schema :added "4.1"}
(fact "gets the db schema"

  (!.js
   (db-view/get-schema (@! +db+)))
  => {})

^{:refer xt.cell.service.db-view/view-query-return-entry :added "4.1"}
(fact "creates a synthetic return entry for inline return-query usage"

  (!.js
   (db-view/view-query-return-entry
    "User"
    ["id" ["profile" ["bio"]]]
    true))
  => {"input" []
      "return" "jsonb"
      "flags" {}
      "view" {"table" "User"
              "type" "return"
              "query" ["id"]
              "access" {"roles" {}}
              "guards" []}})

^{:refer xt.cell.service.db-view/view-query-return-combined :added "4.1"}
(fact "extends a return entry with inline return-query fields"

  (!.js
   (db-view/view-query-return-combined
    "User"
    {"view" {"query" ["id"]}}
    ["name" ["profile" ["bio"]]]
    true))
  => {"view" {"query" ["id" "name"]}})

^{:refer xt.cell.service.db-view/view-query-entries :added "4.1"}
(fact "resolves select and return entries for a query descriptor"

  (!.js
   (db-view/view-query-entries
    (@! +db+)
    "User"
    {"select_method" "all"
     "return_method" "detail"
     "return_query" ["name"]}
    true))
  => {"select_entry"
      {"input" []
       "view" {"query" ["id" "name"]}}
      "return_entry"
      {"input" []
       "view" {"query" ["id" ["profile" ["bio"]] "name"]}}})

^{:refer xt.cell.service.db-view/view-triggers :added "4.1"}
(fact "derives the affected tables for a query"

  (!.js
   (db-view/view-triggers
    (@! +db+)
    "User"
    {"select_method" "all"
     "return_method" "detail"}))
  => map?)

^{:refer xt.cell.service.db-view/view-overview :added "4.1"}
(fact "returns an overview of the registered db views"

  (!.js
   (db-view/view-overview (@! +db+)))
  => map?)

^{:refer xt.cell.service.db-view/view-tables :added "4.1"}
(fact "lists the tables that have registered views"

  (!.js
   (db-view/view-tables (@! +db+)))
  => ["User"])

^{:refer xt.cell.service.db-view/view-methods :added "4.1"}
(fact "lists select and return methods for a table"

  (!.js
   (db-view/view-methods (@! +db+) "User"))
  => {"return" ["detail"]
      "select" ["all"]})

^{:refer xt.cell.service.db-view/view-detail :added "4.1"}
(fact "returns a specific view entry by table, type, and id"

  (!.js
   (db-view/view-detail (@! +db+) "User" "select" "all"))
  => {"input" []
      "view" {"query" ["id" "name"]}})
