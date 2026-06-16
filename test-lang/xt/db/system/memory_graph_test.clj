(ns xt.db.system.memory-graph-test
  (:use code.test)
  (:require [hara.lang :as l]
            [postgres.gen.bind-macro :as gen]
            [xt.db.helpers.seed-system-test :as data]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.db.text.base-flatten :as f]
             [xt.db.text.sql-util :as ut]
             [xt.db.system.memory-graph :as g]
             [xt.db.system.memory-util :as util]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.memory-graph/check-ilike-clause :added "4.1"}
(fact "matches case-insensitive like clauses"

  (!.js
    [(g/check-ilike-clause "US Dollar" "%dollar")
     (g/check-ilike-clause "US Dollar" "%EURO%")
     (g/check-ilike-clause 1 "%1%")])
  => [true false false])

^{:refer xt.db.system.memory-graph/custom-params :added "4.1"}
(fact "extracts standard custom controls"

  (!.js
    (g/custom-params
     [{"::" "sql/count"}
      (ut/ORDER-BY ["id"])
      (ut/ORDER-SORT "DESC")
      (ut/LIMIT 3)
      (ut/OFFSET 1)]))
  => {"count" true
      "order_by" ["id"]
      "order_sort" "desc"
      "limit" 3
      "offset" 1})

^{:refer xt.db.system.memory-graph/check-clause-value :added "4.1"}
(fact "checks scalar and ref-id clauses against a record"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"UserAccount" [sample/RootUser]}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (var account (xtd/get-in rows ["UserAccount"
                                   "00000000-0000-0000-0000-000000000000"
                                   "record"]))
    (var profile (xtd/get-in rows ["UserProfile"
                                   "c4643895-b0ce-44cc-b07b-2386bf18d43b"
                                   "record"]))
    [(g/check-clause-value account "nickname" "root")
     (g/check-clause-value profile "account_id" "00000000-0000-0000-0000-000000000000")
     (g/check-clause-value account "nickname" "other")])
  => [true true false])

^{:refer xt.db.system.memory-graph/check-clause-function :added "4.1"}
(fact "checks value, forward-link and reverse-link predicates"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"UserAccount" [sample/RootUser]}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (var account (xtd/get-in rows ["UserAccount"
                                   "00000000-0000-0000-0000-000000000000"
                                   "record"]))
    (var profile (xtd/get-in rows ["UserProfile"
                                   "c4643895-b0ce-44cc-b07b-2386bf18d43b"
                                   "record"]))
    [(g/check-clause-function account nil "nickname" (. g/PULL_CHECK ["eq"]) ["root"])
     (g/check-clause-function profile "forward" "account" (. g/PULL_CHECK ["eq"]) ["00000000-0000-0000-0000-000000000000"])
     (g/check-clause-function account "reverse" "profile" (. g/PULL_CHECK ["eq"]) ["c4643895-b0ce-44cc-b07b-2386bf18d43b"])
     (g/check-clause-function account nil "nickname" nil ["root"])])
  => [true true true false])

^{:refer xt.db.system.memory-graph/where-clause :added "4.1"}
(fact "evaluates scalar, predicate and nested ref clauses"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"UserAccount" [sample/RootUser]}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (var record (xtd/get-in rows ["UserAccount"
                                  "00000000-0000-0000-0000-000000000000"
                                  "record"]))
    [(g/where-clause rows sample/Schema "UserAccount" record g/where "nickname" "root")
     (g/where-clause rows sample/Schema "UserAccount" record g/where "nickname" (fn:> [x] (== x "root")))
     (g/where-clause rows sample/Schema "UserAccount" record g/where "profile" {"first_name" "Root"})])
  => [true true true])

^{:refer xt.db.system.memory-graph/where :added "4.1"}
(fact "evaluates function, empty, or-list and map predicates"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"UserAccount" [sample/RootUser]}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (var record (xtd/get-in rows ["UserAccount"
                                  "00000000-0000-0000-0000-000000000000"
                                  "record"]))
    [(g/where rows sample/Schema "UserAccount" (fn:> [input _table] (== (xtd/get-in input ["data" "nickname"]) "root")) record)
     (g/where rows sample/Schema "UserAccount" {} record)
     (g/where rows sample/Schema "UserAccount" [{"nickname" "other"} {"nickname" "root"}] record)
     (g/where rows sample/Schema "UserAccount" {"nickname" "other"} record)])
  => [true true true false])

^{:refer xt.db.system.memory-graph/data-field :added "4.1"}
(fact "projects scalar and ref-id data fields"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"UserAccount" [sample/RootUser]}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (var profile (xtd/get-in rows ["UserProfile"
                                   "c4643895-b0ce-44cc-b07b-2386bf18d43b"
                                   "record"]))
    [(g/data-field profile "first_name")
     (g/data-field profile "account_id")])
  => ["Root" "00000000-0000-0000-0000-000000000000"])

^{:refer xt.db.system.memory-graph/project-record :added "4.1"}
(fact "projects one record using selected data and links"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"UserAccount" [sample/RootUser]}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (var tree ["UserAccount"
               {"where" []
                "data" ["nickname"]
                "links" [["profile"
                          "reverse"
                          ["UserProfile"
                           {"where" []
                            "data" ["first_name"]
                            "links" []
                            "custom" []}]]]
                "custom" []}])
    (g/project-record rows
                      sample/Schema
                      tree
                      (xtd/get-in rows ["UserAccount"
                                        "00000000-0000-0000-0000-000000000000"
                                        "record"])
                      {}
                      g/pull-entries))
  => {"nickname" "root"
      "profile" [{"first_name" "Root"}]})

^{:refer xt.db.system.memory-graph/apply-custom :added "4.1"}
(fact "applies ordering, reversing, offset and limit controls"

  (!.js
    (g/apply-custom
     [{"id" "b"} {"id" "a"} {"id" "c"}]
     {"count" false
      "order_by" ["id"]
      "order_sort" "desc"
      "offset" 1
      "limit" 1}))
  => [{"id" "b"}])

^{:refer xt.db.system.memory-graph/pull-entries :added "4.1"}
(fact "filters, projects and customises an explicit entry list"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"Currency" (@! sample/+currency+)}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (g/pull-entries
     rows
     sample/Schema
     ["Currency"
      {"where" {"id" ["ilike" "xlm%"]}
       "data" ["id"]
       "links" []
       "custom" [(ut/ORDER-BY ["id"])]}]
     (xtd/obj-vals (xtd/get-in rows ["Currency"]))
     {}))
  => [{"id" "XLM"}
      {"id" "XLM.T"}])

^{:refer xt.db.system.memory-graph/pull :added "4.1"}
(fact "pulles tree ir data, query forms, and respects custom controls"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"Currency" (@! sample/+currency+)}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (g/pull rows
            sample/Schema
            ["Currency"
             {"where" []
              "data" ["id"]
              "links" []
              "custom" [(ut/ORDER-BY ["id"])
                        (ut/LIMIT 2)
                        (ut/OFFSET 2)]}]
            {}))
  => [{"id" "XLM"}
      {"id" "XLM.T"}]

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"Currency" (@! sample/+currency+)}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (g/pull rows
            sample/Schema
            ["Currency"
             {"id" "USD"}
             ["id" "name"]]
            {}))
  => [{"id" "USD"
       "name" "US Dollar"}])

^{:refer xt.db.system.memory-graph/view-select :added "4.1"}
(fact "plans and pulles a select query"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"Currency" (@! sample/+currency+)}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (g/view-select rows
                   sample/Schema
                   (@! (gen/bind-view data/currency-all-crypto))
                   []
                   {}))
  => [{"id" "XLM"}
      {"id" "XLM.T"}])

^{:refer xt.db.system.memory-graph/view-count :added "4.1"}
(fact "plans and pulles a count query"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"Currency" (@! sample/+currency+)}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (g/view-count rows
                  sample/Schema
                  (@! (gen/bind-view data/currency-all-crypto))
                  []
                  {}))
  => 2)

^{:refer xt.db.system.memory-graph/view-return :added "4.1"}
(fact "plans and pulles a return query"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"Currency" (@! sample/+currency+)}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (g/view-return rows
                   sample/Schema
                   (@! (gen/bind-view data/currency-info))
                   "STATS"
                   []
                   {}))
  => [{"id" "STATS"
       "description" "Default Currency for Statstrade"}])

^{:refer xt.db.system.memory-graph/view-return-bulk :added "4.1"}
(fact "plans and pulles a bulk return query"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"Currency" (@! sample/+currency+)}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (g/view-return-bulk rows
                        sample/Schema
                        (@! (gen/bind-view data/currency-info))
                        ["STATS" "USD"]
                        []
                        {}))
  => [{"id" "STATS"
       "description" "Default Currency for Statstrade"}
      {"id" "USD"
       "description" "Default Current for the United States of America"}])

^{:refer xt.db.system.memory-graph/view-combined :added "4.1"}
(fact "plans and pulles a combined query"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"Currency" (@! sample/+currency+)}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (g/view-combined rows
                     sample/Schema
                     (@! (gen/bind-view data/currency-all-crypto))
                     []
                     (@! (gen/bind-view data/currency-info))
                     []
                     nil
                     {}))
  => [{"id" "XLM"
       "description" "Default Currency for the Stellar Blockchain"}
      {"id" "XLM.T"
       "description" "Default Currency for the Stellar TestNet Blockchain"}])


^{:refer xt.db.system.memory-graph/check-in-clause :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.memory-graph/like-char-at :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.memory-graph/check-like-clause :added "4.1"}
(fact "TODO")