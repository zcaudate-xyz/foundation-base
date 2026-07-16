(ns xt.db.text.sql-tree-test
  (:require [hara.lang :as l]
            [postgres.gen.bind-macro :as gen]
            [xt.db.helpers.seed-system-test :as data])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.text.sql-tree :as v]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.text.sql-tree/sql-query-select :added "4.1"}
(fact "plans and renders a select query"

  (!.js
    (v/sql-query-select sample/Schema
                        (@! (gen/bind-view data/currency-all-crypto))
                        []
                        {}))
  => "SELECT id FROM Currency\n  WHERE type = 'crypto'")

^{:refer xt.db.text.sql-tree/sql-query-count :added "4.1"}
(fact "plans and renders a count query"

  (!.js
    (v/sql-query-count sample/Schema
                       (@! (gen/bind-view data/currency-all-crypto))
                       []
                       {}))
  => "SELECT count(*) FROM Currency\n  WHERE type = 'crypto'")

^{:refer xt.db.text.sql-tree/sql-query-return :added "4.1"}
(fact "plans and renders a return query"

  (!.js
    (v/sql-query-return sample/Schema
                        (@! (gen/bind-view data/currency-info))
                        "STATS"
                        []
                        {}))
  => "SELECT id, description FROM Currency\n  WHERE id = 'STATS'")

^{:refer xt.db.text.sql-tree/sql-query-return-bulk :added "4.1"}
(fact "plans and renders a bulk return query"

  (!.js
    (v/sql-query-return-bulk sample/Schema
                             (@! (gen/bind-view data/currency-info))
                             ["STATS" "USD"]
                             []
                             {}))
  => "SELECT id, description FROM Currency\n  WHERE id in ('STATS', 'USD')")

^{:refer xt.db.text.sql-tree/sql-query-combined :added "4.1"}
(fact "plans and renders a combined query"

  (!.js
    (v/sql-query-combined sample/Schema
                          (@! (gen/bind-view data/currency-all-crypto))
                          []
                          (@! (gen/bind-view data/currency-info))
                          []
                          nil
                          {}))
  => "SELECT id, description FROM Currency\n  WHERE type = 'crypto'")