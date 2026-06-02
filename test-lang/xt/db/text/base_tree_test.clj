(ns xt.db.text.base-tree-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.text.base-tree :as v]
             [xt.db.text.sql-util :as ut]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.text.base-tree/tree-control-array :added "4.1"
  :setup [(def +check-tree-control-array+
            [{"::" "sql/keyword",
              "name" "ORDER BY",
              "args" [{"args" [{"::" "sql/column", "name" "name"}],
                       "::" "sql/tuple"}]}
             {"::" "sql/keyword",
              "name" "LIMIT",
              "args" [{"::" "sql/keyword", "name" 20}]}])]}
(fact "creates a control array"

  (!.js
    (v/tree-control-array {:limit 20
                           :order-by ["name"]}))
  => +check-tree-control-array+)

^{:refer xt.db.text.base-tree/tree-base :added "4.1"
  :setup [(def +check-tree-base+
            ["Currency"
             {"custom" [],
              "where"
              [{"id" "USD", "type" "fiat"}
               {"id" "AUD", "type" "fiat"}],
              "links" [],
              "data"
              ["id"
               "type"
               "symbol"
               "native"
               "decimal"
               "name"
               "plural"
               "description"]}])]}
(fact "creates a tree base"

  (!.js
    (v/tree-base sample/Schema
                 "Currency"
                 [{"id" "USD"}
                  {"id" "AUD"}]
                 {:type "fiat"}
                 ["*/data"]
                 {}))
  => +check-tree-base+)

^{:refer xt.db.text.base-tree/tree-count :added "4.1"
  :setup [(def +input-tree-count+
            {:input [{:symbol "i_type", :type "text"}],
             :return "jsonb",
             :schema "scratch-sample-db",
             :id "currency_by_type",
             :flags {:public true},
             :view
             {:table "Currency",
              :type "select",
              :tag "by_type",
              :query
              {"type"
               {"::" "sql/cast",
                "args"
                [{"::" "sql/arg", "name" "{{i_type}}"}
                 {"::" "sql/defenum",
                  :schema "scratch-sample-db",
                  :name "EnumCurrencyType"}]}}}})
          (def +check-tree-count+
            ["Currency"
             {"custom" [{"::" "sql/count"}],
              "where"
              [{"type"
                {"args"
                 [{"name" "{{i_type}}", "::" "sql/arg"}
                  {"schema" "scratch-sample-db",
                   "name" "EnumCurrencyType",
                   "::" "sql/defenum"}],
                 "::" "sql/cast"}}],
              "links" [],
              "data" []}])]}
(fact "provides a count tree"

  (!.js
    (v/tree-count sample/Schema
                  (@! +input-tree-count+)
                  {}
                  {}))
  => +check-tree-count+)

^{:refer xt.db.text.base-tree/tree-select :added "4.1"
  :setup [(def +input-tree-select+
            {:input [{:symbol "i_type", :type "text"}],
             :return "jsonb",
             :schema "scratch-sample-db",
             :id "currency_by_type",
             :flags {:public true},
             :view
             {:table "Currency",
              :type "select",
              :tag "by_type",
              :query
              {"type"
               {"::" "sql/cast",
                "args"
                [{"::" "sql/arg", "name" "{{i_type}}"}
                 {"::" "sql/defenum",
                  :schema "scratch-sample-db",
                  :name "EnumCurrencyType"}]}}}})
          (def +check-tree-select+
            ["Currency"
             {"custom" [],
              "where"
              [{"type"
                {"args"
                 [{"name" "{{i_type}}", "::" "sql/arg"}
                  {"schema" "scratch-sample-db",
                   "name" "EnumCurrencyType",
                   "::" "sql/defenum"}],
                 "::" "sql/cast"}}],
              "links" [],
              "data" ["id"]}])]}
(fact "provides a select tree"

  (!.js
    (v/tree-select sample/Schema
                   (@! +input-tree-select+)
                   {}
                   {}))
  => +check-tree-select+)

^{:refer xt.db.text.base-tree/tree-return :added "4.1"
  :setup [(def +input-tree-return+
            {:view
             {:table "Currency"
              :type "return"
              :query ["id" "description"]}})
          (def +check-tree-return+
            ["Currency"
             {"custom" [],
              "where" [{"id" "{{RETURN}}"}],
              "links" [],
              "data" ["id" "description"]}])]}
(fact "provides a return tree"

  (!.js
    (v/tree-return sample/Schema
                   (@! +input-tree-return+)
                   {"id" "{{RETURN}}"}
                   {}
                   {}))
  => +check-tree-return+)

^{:refer xt.db.text.base-tree/tree-combined :added "4.1"
  :setup [(def +input-tree-combined-select+
            {:control {:limit 5}
             :view {:table "Currency"
                    :type "select"
                    :query {"type" "fiat"}}})
          (def +input-tree-combined-return+
            {:view {:table "Currency"
                    :type "return"
                    :query ["id" "name"]}})
          (def +check-tree-combined+
            ["Currency"
             {"custom"
              [{"::" "sql/keyword",
                "name" "LIMIT",
                "args" [{"::" "sql/keyword", "name" 5}]}],
              "where" [{"type" "fiat"}],
              "links" [],
              "data" ["id" "name"]}])]}
(fact "provides a combined tree"

  (!.js
    (v/tree-combined sample/Schema
                     (@! +input-tree-combined-select+)
                     (@! +input-tree-combined-return+)
                     nil
                     {}
                     {}))
  => +check-tree-combined+)

^{:refer xt.db.text.base-tree/tree-fill-input :added "4.1"}
(fact "fills placeholders from the input spec"

  (!.js
    (v/tree-fill-input
     ["Currency"
      {"custom" [],
       "where" [{"type" "{{i_type}}"}],
       "links" [],
       "data" ["id"]}]
     ["fiat"]
     [{"symbol" "i_type", "type" "text"}]
     false))
  => ["Currency"
      {"custom" [],
       "where" [{"type" "fiat"}],
       "links" [],
       "data" ["id"]}])

^{:refer xt.db.text.base-tree/plan-select :added "4.1"}
(fact "plans a select tree with filled input"

  (!.js
    (v/plan-select sample/Schema
                   (@! +input-tree-select+)
                   ["fiat"]
                   {}))
  => ["Currency"
      {"custom" [],
       "where"
       [{"type"
         {"args"
          [{"name" "fiat", "::" "sql/arg"}
           {"schema" "scratch-sample-db",
            "name" "EnumCurrencyType",
            "::" "sql/defenum"}],
          "::" "sql/cast"}}],
       "links" [],
       "data" ["id"]}])

^{:refer xt.db.text.base-tree/plan-count :added "4.1"}
(fact "plans a count tree with filled input"

  (!.js
    (v/plan-count sample/Schema
                  (@! +input-tree-count+)
                  ["crypto"]
                  {}))
  => ["Currency"
      {"custom" [{"::" "sql/count"}],
       "where"
       [{"type"
         {"args"
          [{"name" "crypto", "::" "sql/arg"}
           {"schema" "scratch-sample-db",
            "name" "EnumCurrencyType",
            "::" "sql/defenum"}],
          "::" "sql/cast"}}],
       "links" [],
       "data" []}])

^{:refer xt.db.text.base-tree/plan-return :added "4.1"
  :setup [(def +input-plan-return+
            {:input [{:symbol "i_currency_id", :type "text"}
                     {:symbol "i_note", :type "text"}],
             :view
             {:table "Currency"
              :type "return"
              :query ["id"
                      "description"
                      {"::" "sql/arg", "name" "{{i_note}}"}]}})]}
(fact "plans a return tree and drops the first input placeholder"

  (!.js
    (v/plan-return sample/Schema
                   (@! +input-plan-return+)
                   "STATS"
                   ["hello"]
                   {}))
  => ["Currency"
      {"custom" [{"::" "sql/arg", "name" "hello"}],
       "where" [{"id" "STATS"}],
       "links" [],
       "data" ["id" "description"]}])

^{:refer xt.db.text.base-tree/plan-return-bulk :added "4.1"
  :setup [(def +input-plan-return-bulk+
            {:input [{:symbol "i_currency_id", :type "text"}],
             :view
             {:table "Currency"
              :type "return"
              :query ["id" "description"]}})]}
(fact "plans a bulk return tree"

  (!.js
    (v/plan-return-bulk sample/Schema
                        (@! +input-plan-return-bulk+)
                        ["STATS" "USD"]
                        []
                        {}))
  => ["Currency"
      {"custom" [],
       "where" [{"id" ["in" [["STATS" "USD"]]]}],
       "links" [],
       "data" ["id" "description"]}])

^{:refer xt.db.text.base-tree/plan-combined :added "4.1"
  :setup [(def +input-plan-combined-select+
            {:input [{:symbol "i_type", :type "text"}],
             :control {:limit 2}
             :view {:table "Currency"
                    :type "select"
                    :query {"type" "{{i_type}}"}}})
          (def +input-plan-combined-return+
            {:input [{:symbol "i_currency_id", :type "text"}
                     {:symbol "i_note", :type "text"}],
             :view {:table "Currency"
                    :type "return"
                    :query ["id"
                            {"::" "sql/arg", "name" "{{i_note}}"}]}})]}
(fact "plans a combined tree with filled select and return input"

  (!.js
    (v/plan-combined sample/Schema
                     (@! +input-plan-combined-select+)
                     ["fiat"]
                     (@! +input-plan-combined-return+)
                     ["note"]
                     nil
                     {}
                     true))
  => ["Currency"
      {"custom"
       [{"::" "sql/arg", "name" "note"}
        {"::" "sql/keyword",
         "name" "LIMIT",
         "args" [{"::" "sql/keyword", "name" 2}]}],
       "where" [{"type" "fiat"}],
       "links" [],
       "data" ["id"]}])