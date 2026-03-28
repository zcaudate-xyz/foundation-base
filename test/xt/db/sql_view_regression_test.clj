(ns xt.db.sql-view-regression-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.sql-view :as v]
             [xt.lang.base-lib :as k]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.db.sql-view :as v]
             [xt.lang.base-lib :as k]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.db.sql-view :as v]
             [xt.lang.base-lib :as k]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :js)
             (l/rt:scaffold :lua)
             (l/rt:scaffold :python)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.sql-view/tree-combined.ret-omit :added "4.0"
  :setup [(def +schema+
            {"UserAccount" {"id" {:ident "id" :type "string"}
                            "organisation" {:ident "organisation"
                                            :type "string"}}})
          (def +select+
            {"view" {"table" "UserAccount"
                     "query" [{"organisation" "ORG-1"}]}
             "control" {}})
          (def +return+
            {"view" {"table" "UserAccount"
                     "query" []}})
          (def +tree+
            {"not_in" [["USER-0"]]})]}
(fact "ret-omit should be part of the query tree"
  ^:hidden

  (!.js
   (-> (v/tree-combined (@! +schema+)
                        (@! +select+)
                        (@! +return+)
                        ["USER-0"]
                        []
                        {})
       (k/second)
       (k/get-key "where")
       (k/first)
       (k/get-key "id")))
  => +tree+

  (!.lua
   (-> (v/tree-combined (@! +schema+)
                        (@! +select+)
                        (@! +return+)
                        ["USER-0"]
                        []
                        {})
       (k/second)
       (k/get-key "where")
       (k/first)
       (k/get-key "id")))
  => (l/as-lua +tree+)

  (!.py
   (-> (v/tree-combined (@! +schema+)
                        (@! +select+)
                        (@! +return+)
                        ["USER-0"]
                        []
                        {})
       (k/second)
       (k/get-key "where")
       (k/first)
       (k/get-key "id")))
  => +tree+)
