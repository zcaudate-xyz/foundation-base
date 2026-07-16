(ns xt.db.text.pgrest-tree-test
  (:require [hara.lang :as l]
            [postgres.gen.bind-macro :as gen]
            [xt.db.helpers.seed-system-test :as data])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.text.pgrest-tree :as v]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.text.pgrest-tree/pgrest-query-select :added "4.1"}
(fact "plans and renders a select request"

  (!.js
    (v/pgrest-query-select sample/Schema
                           (@! (gen/bind-view data/currency-all-crypto))
                           []
                           {}))
  => {"type" "query",
      "table" "Currency",
      "method" "GET",
      "path" "/rest/v1/Currency",
      "select" "id",
      "filters" [{"type" "crypto"}],
      "params" ["select=id"
                "type=eq.crypto"],
      "query" "select=id&type=eq.crypto",
      "url" "/rest/v1/Currency?select=id&type=eq.crypto",
      "headers" {}})

^{:refer xt.db.text.pgrest-tree/pgrest-query-count :added "4.1"}
(fact "plans and renders a count request"

  (!.js
    (v/pgrest-query-count sample/Schema
                          (@! (gen/bind-view data/currency-all-crypto))
                          []
                          {}))
  => {"type" "query",
      "table" "Currency",
      "method" "GET",
      "path" "/rest/v1/Currency",
      "select" "count",
      "filters" [{"type" "crypto"}],
      "params" ["select=count"
                "type=eq.crypto"],
      "query" "select=count&type=eq.crypto",
      "url" "/rest/v1/Currency?select=count&type=eq.crypto",
      "headers" {}})

^{:refer xt.db.text.pgrest-tree/pgrest-query-return :added "4.1"}
(fact "plans and renders a return request"

  (!.js
    (v/pgrest-query-return sample/Schema
                           (@! (gen/bind-view data/currency-info))
                           "STATS"
                           []
                           {}))
  => {"type" "query",
      "table" "Currency",
      "method" "GET",
      "path" "/rest/v1/Currency",
      "select" "id,description",
      "filters" [{"id" "STATS"}],
      "params" ["select=id,description"
                "id=eq.STATS"],
      "query" "select=id,description&id=eq.STATS",
      "url" "/rest/v1/Currency?select=id,description&id=eq.STATS",
      "headers" {}})

^{:refer xt.db.text.pgrest-tree/pgrest-query-return-bulk :added "4.1"}
(fact "plans and renders a bulk return request"

  (!.js
    (v/pgrest-query-return-bulk sample/Schema
                                (@! (gen/bind-view data/currency-info))
                                ["STATS" "USD"]
                                []
                                {}))
  => {"type" "query",
      "table" "Currency",
      "method" "GET",
      "path" "/rest/v1/Currency",
      "select" "id,description",
      "filters" [{"id" ["in" [["STATS" "USD"]]]}],
      "params" ["select=id,description"
                "id=in.(STATS,USD)"],
      "query" "select=id,description&id=in.(STATS,USD)",
      "url" "/rest/v1/Currency?select=id,description&id=in.(STATS,USD)",
      "headers" {}})

^{:refer xt.db.text.pgrest-tree/pgrest-query-combined :added "4.1"}
(fact "plans and renders a combined request"

  (!.js
    (v/pgrest-query-combined sample/Schema
                             (@! (gen/bind-view data/currency-all-crypto))
                             []
                             (@! (gen/bind-view data/currency-info))
                             []
                             nil
                             {}))
  => {"type" "query",
      "table" "Currency",
      "method" "GET",
      "path" "/rest/v1/Currency",
      "select" "id,description",
      "filters" [{"type" "crypto"}],
      "params" ["select=id,description"
                "type=eq.crypto"],
      "query" "select=id,description&type=eq.crypto",
      "url" "/rest/v1/Currency?select=id,description&type=eq.crypto",
      "headers" {}})