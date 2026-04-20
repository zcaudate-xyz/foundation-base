(ns
 xtbench.js.db.sql-view-regression-test
 (:require [std.lang :as l])
 (:use code.test))

(l/script-
 :js
 {:runtime :basic,
  :require
  [[xt.db.sql-view :as v]
   [xt.lang.common-spec :as xt]
   [xt.lang.common-data :as xtd]]})

(fact:global
 {:setup [(l/rt:restart) (l/rt:scaffold :js)], :teardown [(l/rt:stop)]})

^{:refer xt.db.sql-view/tree-combined.ret-omit,
  :added "4.0",
  :setup
  [(def
    +schema+
    {"UserAccount"
     {"id" {:ident "id", :type "string"},
      "organisation" {:ident "organisation", :type "string"}}})
   (def
    +select+
    {"view"
     {"table" "UserAccount", "query" [{"organisation" "ORG-1"}]},
     "control" {}})
   (def +return+ {"view" {"table" "UserAccount", "query" []}})
   (def +tree+ {"not_in" [["USER-0"]]})]}
(fact
 "ret-omit should be part of the query tree"
 ^{:hidden true}
 (!.js
  (->
   (v/tree-combined
    (@! +schema+)
    (@! +select+)
    (@! +return+)
    ["USER-0"]
    []
    {})
   (xtd/second)
   (xt/x:get-key "where")
   (xtd/first)
   (xt/x:get-key "id")))
 =>
 +tree+)
