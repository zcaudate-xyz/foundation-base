(ns xt.db.system.memory-graph-test
  (:require [hara.lang :as l]
            [postgres.gen.bind-macro :as gen]
            [xt.db.helpers.seed-system-test :as data])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.db.text.base-flatten :as f]
             [xt.db.runtime.cache-util :as util]
             [xt.db.text.sql-util :as ut]
             [xt.db.system.memory-graph :as g]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.memory-graph/fetch :added "4.1"}
(fact "fetches tree ir data, query forms, and respects custom controls"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"Currency" (@! sample/+currency+)}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (g/fetch rows
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
    (g/fetch rows
             sample/Schema
             ["Currency"
             {"id" "USD"}
             ["id" "name"]]
             {}))
  => [{"id" "USD"
       "name" "US Dollar"}])

^{:refer xt.db.system.memory-graph/query-select :added "4.1"}
(fact "plans and fetches a select query"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"Currency" (@! sample/+currency+)}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (g/query-select rows
                    sample/Schema
                    (@! (gen/bind-view data/currency-all-crypto))
                    []
                    {}))
  => [{"id" "XLM"}
      {"id" "XLM.T"}])

^{:refer xt.db.system.memory-graph/query-count :added "4.1"}
(fact "plans and fetches a count query"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"Currency" (@! sample/+currency+)}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (g/query-count rows
                   sample/Schema
                   (@! (gen/bind-view data/currency-all-crypto))
                   []
                   {}))
  => 2)

^{:refer xt.db.system.memory-graph/query-return :added "4.1"}
(fact "plans and fetches a return query"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"Currency" (@! sample/+currency+)}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (g/query-return rows
                    sample/Schema
                    (@! (gen/bind-view data/currency-info))
                    "STATS"
                    []
                    {}))
  => [{"id" "STATS"
       "description" "Default Currency for Statstrade"}])

^{:refer xt.db.system.memory-graph/query-return-bulk :added "4.1"}
(fact "plans and fetches a bulk return query"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"Currency" (@! sample/+currency+)}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (g/query-return-bulk rows
                         sample/Schema
                         (@! (gen/bind-view data/currency-info))
                         ["STATS" "USD"]
                         []
                         {}))
  => [{"id" "STATS"
       "description" "Default Currency for Statstrade"}
      {"id" "USD"
       "description" "Default Current for the United States of America"}])

^{:refer xt.db.system.memory-graph/query-combined :added "4.1"}
(fact "plans and fetches a combined query"

  (!.js
    (var rows {})
    (var flat (f/flatten-bulk sample/Schema
                              {"Currency" (@! sample/+currency+)}))
    (util/merge-bulk rows flat nil)
    (util/add-bulk-links rows sample/Schema flat)
    (g/query-combined rows
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
