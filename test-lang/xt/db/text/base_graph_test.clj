(ns xt.db.text.base-graph-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.db.text.base-graph :as base-graph]
            [xt.db.helpers.data-main-test :as sample]
            [xt.db.text.sql-util :as ut]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.db.text.base-graph :as base-graph]
             [xt.db.helpers.data-main-test :as sample]
             [xt.db.text.sql-util :as ut]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.text.base-graph/tree-params? :added "4.1"}
(fact "checks if params are already in canonical tree format"

  (!.js
    [(base-graph/tree-params? {"where" []})
     (base-graph/tree-params? {"data" []})
     (base-graph/tree-params? {"links" []})
     (base-graph/tree-params? {"custom" []})
     (base-graph/tree-params? {"where" [] "data" [] "links" [] "custom" []})
     (base-graph/tree-params? [])
     (base-graph/tree-params? "hello")
     (base-graph/tree-params? nil)])
  => [true true true true true false false false])

^{:refer xt.db.text.base-graph/tree? :added "4.1"}
(fact "checks if a query is already a canonical tree"

  (!.js
    [(base-graph/tree? ["Currency" {"where" []}])
     (base-graph/tree? ["Currency" {"where" [] "data" []}])
     (base-graph/tree? ["Currency"])
     (base-graph/tree? "Currency")
     (base-graph/tree? ["Currency" ["id"]])
     (base-graph/tree? nil)])
  => [true true false false false false])

^{:refer xt.db.text.base-graph/normalise-tree-params :added "4.1"}
(fact "fills missing tree buckets with defaults"

  (!.js
    [(base-graph/normalise-tree-params {"where" [{"id" "USD"}]})
     (base-graph/normalise-tree-params {"data" ["id"] "custom" [(ut/ORDER-BY ["id"])]})
     (base-graph/normalise-tree-params nil)
     (base-graph/normalise-tree-params {})])
  => [{"where" [{"id" "USD"}]
       "data" []
       "links" []
       "custom" []}
      {"where" []
       "data" ["id"]
       "links" []
       "custom" [(ut/ORDER-BY ["id"])]}
      {"where" []
       "data" []
       "links" []
       "custom" []}
      {"where" []
       "data" []
       "links" []
       "custom" []}])

^{:refer xt.db.text.base-graph/normalise-tree :added "4.1"}
(fact "normalises a canonical tree into the full explicit params shape"

  (!.js
    [(base-graph/normalise-tree ["Currency" {"where" []}])
     (base-graph/normalise-tree ["Currency" {"data" ["id"]}])
     (base-graph/normalise-tree "Currency")
     (base-graph/normalise-tree ["Currency" ["id"]])])
  => [["Currency"
       {"where" []
        "data" []
        "links" []
        "custom" []}]
      ["Currency"
       {"where" []
        "data" ["id"]
        "links" []
        "custom" []}]
      "Currency"
      ["Currency" ["id"]]])

^{:refer xt.db.text.base-graph/base-query-inputs :added "4.1"}
(fact "formats query input into table clause and return"

  (!.js
    [(base-graph/base-query-inputs ["Currency"])
     (base-graph/base-query-inputs ["Currency" ["id" "name"]])
     (base-graph/base-query-inputs ["Currency" {"id" "USD"}])
     (base-graph/base-query-inputs ["Currency" {"id" "USD"} ["id" "name"]])])
  => [["Currency" {} nil]
      ["Currency" {} ["id" "name"]]
      ["Currency" {"id" "USD"} nil]
      ["Currency" {"id" "USD"} ["id" "name"]]])

^{:refer xt.db.text.base-graph/select-tree :added "4.1"}
(fact "normalises a query into canonical tree ir"

  (!.js
    [(base-graph/select-tree sample/Schema ["Currency"] {})
     (base-graph/select-tree sample/Schema ["Currency" ["id" "name"]] {})
     (base-graph/select-tree sample/Schema
                              ["Currency"
                               {"where" []
                                "data" ["id"]
                                "links" []
                                "custom" []}]
                              {})
     (base-graph/select-tree sample/Schema ["Currency" {"id" "USD"} ["id"]] {})])
  => [["Currency"
       {"where" []
        "data" ["id"
                "type"
                "symbol"
                "native"
                "decimal"
                "name"
                "plural"
                "description"]
        "links" []
        "custom" []}]
      ["Currency"
       {"where" []
        "data" ["id" "name"]
        "links" []
        "custom" []}]
      ["Currency"
       {"where" []
        "data" ["id"]
        "links" []
        "custom" []}]
      ["Currency"
       {"where" [{"id" "USD"}]
        "data" ["id"]
        "links" []
        "custom" []}]])