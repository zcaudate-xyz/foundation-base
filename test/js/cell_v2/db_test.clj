(ns js.cell-v2.db-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.cell-v2.db :as db]
             [xt.lang.base-lib :as k]
             [xt.db :as xdb]]})

(fact:global
 {:setup     [(l/rt:restart)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell-v2.db/create-db :added "4.0" :unchecked true}
(fact "integrates databases through xt.db"
  (!.js
   (var registry (db/make-registry))
   (var schema {"User"
                {"id" {"ident" "id"
                       "type" "text"
                       "order" 0}
                 "name" {"ident" "name"
                         "type" "text"
                         "order" 1}}})
   (var lookup {"User" {"position" 0}})
   (db/create-db registry
                 "cache"
                 {"::" "db.cache"}
                 schema
                 lookup
                 nil)
   (db/db-sync registry
               "cache"
               {"User" [{"id" "u1"
                         "name" "Chris"}]})
   (var query0 (db/db-query registry
                            "cache"
                            ["User"
                             ["id" "name"]]))
   (db/db-remove registry
                 "cache"
                 {"User" [{"id" "u1"}]})
   (var remove0 (db/db-query registry
                             "cache"
                             ["User"
                              ["id"]]))
   (var out {"dbs" (db/list-dbs registry)
             "query" query0
             "remove" remove0})
   out)
  => {"dbs" ["cache"]
      "query" [{"id" "u1"
                "name" "Chris"}]
      "remove" []})
