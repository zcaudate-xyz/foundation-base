(ns xtbench.dart.db.schema.sql-view-test
  (:require [hara.lang :as l]
            [hara.model.spec-postgres.gen-bind :as gen]
            [std.string.prose :as prose]
            [xt.db.helpers.seed-system-test :as data]
            [xt.db.helpers.seed-user-test :as user])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.db.text.sql-view :as v]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-raw :as raw]
             [xt.lang.common-lib :as k]
             [xt.db.text.base-schema :as sch]
             [xt.db.text.base-scope :as scope]
             [xt.db.helpers.data-main-test :as sample]]})

^{:refer xt.db.text.sql-view/tree-select.organisation-all-as-admin :added "4.0"
  :setup [(def +input-organisation-all-as-admin+
            ;; (pg/bind-view user/organisation-all-as-admin)
            {:input [{:symbol "i_account_id", `:type "uuid"}], :return "jsonb",
             :schema "scratch-sample-db", :id "organisation_all_as_admin",
             :flags {:personal true}, :view {:table "Organisation", :type "select", :tag "all_as_admin", :query nil}})
                   (def +check-organisation-all-as-admin+
                     ["Organisation"
                      {"custom" [],
                       "where" [],
                       "links" [],
                       "data" ["id"]}])] :guard true :adopt true}
(fact "provides a view select query"

  (!.dt
    (v/tree-select sample/Schema
                   (@! +input-organisation-all-as-admin+)
                   {}
                   {}))
  => +check-organisation-all-as-admin+)

^{:refer xt.db.text.sql-view/tree-return.organisation-view-default :added "4.0"
  :setup [(def +input-organisation-view-default+
            ;; (pg/bind-view user/organisation-view-default)
            {:input [{:symbol "i_organisation_id", :type "uuid"}],
             :return "jsonb",
             :schema "scratch-sample-db",
             :id "organisation_view_default",
             :flags {:public true},
             :view
             {:table "Organisation",
              :type "return",
              :tag "view_default",
              :query ["*/data"]}})
                   (def +check-organisation-view-default+
                     ["Organisation"
                      {"custom" [],
                       "where" [{"id" "{{RETURN}}"}],
                       "links" [],
                       "data" ["id" "name" "title" "description" "tags"]}])] :adopt true}
(fact "provides a view return query"

  (!.dt
    (v/tree-return sample/Schema
                   (@! +input-organisation-view-default+)
                   {"id" "{{RETURN}}"}
                   {}
                   {}))
  => +check-organisation-view-default+)

^{:refer xt.db.text.sql-view/query-select.organisation-all-as-admin :added "4.0"
  :setup [(def +input-organisation-all-as-admin+
            ;; (pg/bind-view user/organisation-all-as-admin)
            {:input [{:symbol "i_account_id", :type "uuid"}],
             :return "jsonb",
             :schema "scratch-sample-db",
             :id "organisation_all_as_admin",
             :flags {:personal true},
             :view
             {:table "Organisation",
              :type "select",
              :tag "all_as_admin",
              :query nil}})
                   (def +check-organisation-all-as-admin+
                   [["Organisation"
                    {"custom" [],
                     "where" [],
                     "links" [],
                     "data" ["id"]}]
                   "SELECT id FROM Organisation"])] :adopt true}
(fact "provides a view select query"

  (!.dt
    [(v/query-select sample/Schema
                     (@! +input-organisation-all-as-admin+)
                     ["00000000-0000-0000-0000-000000000000"]
                     {}
                     true)
     (v/query-select sample/Schema
                     (@! +input-organisation-all-as-admin+)
                     ["00000000-0000-0000-0000-000000000000"]
                     {}
                     false)])
  => +check-organisation-all-as-admin+)

^{:refer xt.db.text.sql-view/tree-base.control :added "4.0"
  :setup [(def +check-tree-base-control+
            ["RegionCountry"
             {"custom"
              [{"args" [{"::" "sql/keyword", "name" 20}],
                "::" "sql/keyword",
                "name" "LIMIT"}
               {"args"
                [{"args" [{"::" "sql/column", "name" "name"}],
                  "::" "sql/tuple"}],
                "::" "sql/keyword",
                "name" "ORDER BY"}],
              "where" [],
              "links" [],
              "data" ["id" "name"]}])] :adopt true}
(fact "creates a tree base"

  (!.dt
    (v/tree-base sample/Schema
                 "RegionCountry"
                 []
                 []
                 ["id" "name"
                  (ut/LIMIT 20)
                  (ut/ORDER-BY ["name"])]
                 {}))
  => +check-tree-base-control+)

^{:refer xt.db.text.sql-view/tree-control-array :added "4.0"
  :setup [(def +check-tree-control-array+
            [{"args"
              [{"args" [{"::" "sql/column", "name" "name"}],
                "::" "sql/tuple"}],
              "::" "sql/keyword",
              "name" "ORDER BY"}
             {"args" [{"::" "sql/keyword", "name" 20}],
              "::" "sql/keyword",
              "name" "LIMIT"}])]}
(fact "creates a control array"

  (!.dt
    (v/tree-control-array {:limit 20
                           :order-by ["name"]}))
  => +check-tree-control-array+)

^{:refer xt.db.text.sql-view/tree-base :added "4.0"
  :setup [(def +check-tree-base+
            ["Currency"
             {"custom" [],
              "where"
              [{"id" "USD", "type" "fiat"} {"id" "AUD", "type" "fiat"}],
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

  (!.dt
    (v/tree-base sample/Schema
                 "Currency"
                 [{:id "USD"}
                  {:id "AUD"}]
                 {:type "fiat"}
                 ["*/data"]
                 {}))
  => +check-tree-base+)

^{:refer xt.db.text.sql-view/tree-count :added "4.0"
  :setup [(def +input-tree-count+
            ;;(pg/bind-view data/currency-by-type)
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
(fact "provides a view count query"

  (!.dt
    (v/tree-count sample/Schema
                  (@! +input-tree-count+)
                  {}
                  {}))
  => +check-tree-count+)

^{:refer xt.db.text.sql-view/tree-select :added "4.0"
  :setup [(def +input-tree-select+
            ;;(pg/bind-view data/currency-by-type)
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
(fact "provides a view select query"

  (!.dt
    (v/tree-select sample/Schema
                   (@! +input-tree-select+)
                   {}
                   {}))
  => +check-tree-select+)

^{:refer xt.db.text.sql-view/tree-return :added "4.0"
  :setup [(def +return+
            (gen/bind-view data/currency-default))
                   (def +out+
                     ["Currency"
                      {"custom" [],
                       "where" [{"id" "{{RETURN}}"}],
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
(fact "provides a view return query"

  (!.dt
    (v/tree-return sample/Schema
                   (@! +return+)
                   {"id" "{{RETURN}}"}
                   {}
                   {}))
  => +out+)

^{:refer xt.db.text.sql-view/tree-combined :added "4.0"
  :setup [(def +select+
            (gen/bind-view user/organisation-all-as-admin))
                   (def +return+
                     (gen/bind-view user/organisation-view-membership))
                   (def +out+
                     ["Organisation"
                      {"custom" [],
                       "where" [],
                       "links"
                       [["access"
                         "reverse"
                         ["OrganisationAccess"
                          {"custom" [],
                           "where" [{"organisation" ["eq" ["Organisation.id"]]}],
                           "links"
                           [["account"
                             "forward"
                             ["UserAccount"
                              {"custom" [],
                               "where"
                               [{"id" ["eq" ["OrganisationAccess.account_id"]]}],
                               "links" [],
                               "data" ["id" "nickname"]}]]],
                           "data" ["id" "role"]}]]],
                       "data" ["id" "name" "title" "description" "tags"]}])]}
(fact "provides a view return query"

  (!.dt
    (v/tree-combined sample/Schema
                     (@! +select+)
                     (@! +return+)
                     nil
                     {}
                     {}))
  => +out+)

^{:refer xt.db.text.sql-view/query-fill-input :added "4.0"
  :setup [(def +out+
            ["Organisation"
             {"custom" [],
              "where" [],
              "links" [],
              "data" ["id"]}])]}
(fact "fills out the tree for a given input"

  (!.dt
    (var entry (@! (gen/bind-view user/organisation-all-as-member)))
    (var tree  (v/tree-select sample/Schema entry {} {}))
    (v/query-fill-input tree ["<ORG-ID>"] (. entry ["input"]) false))
  => +out+)

^{:refer xt.db.text.sql-view/query-select :added "4.0"
  :setup [(def +select+
            (gen/bind-view data/currency-all-crypto))
                   (def +out+
                     [["Currency"
                       {"custom" [],
                        "where" [{"type" "crypto"}],
                        "links" [],
                        "data" ["id"]}]
                      "SELECT id FROM Currency\n  WHERE type = 'crypto'"])]}
(fact "provides a view select query"

  (!.dt
    [(v/query-select sample/Schema
                     (@! +select+)
                     []
                     {}
                     true)
     (v/query-select sample/Schema
                     (@! +select+)
                     []
                     {}
                     false)])
  => +out+)

^{:refer xt.db.text.sql-view/query-count :added "4.0"
  :setup [(def +select+
            (gen/bind-view data/currency-all-crypto))
                   (def +out+
                     [["Currency"
                       {"custom" [{"::" "sql/count"}],
                        "where" [{"type" "crypto"}],
                        "links" [],
                        "data" []}]
                      "SELECT count(*) FROM Currency\n  WHERE type = 'crypto'"])]}
(fact "provides the count statement"

  (!.dt
    [(v/query-count sample/Schema
                    (@! +select+)
                    []
                    {}
                    true)
     (v/query-count sample/Schema
                    (@! +select+)
                    []
                    {}
                    false)])
  => +out+)

^{:refer xt.db.text.sql-view/query-return :added "4.0"
  :setup [(def +return+
            (gen/bind-view data/currency-info))
                   (def +out+
                     [["Currency"
                       {"custom" [],
                        "where" [{"id" "STATS"}],
                        "links" [],
                        "data" ["id" "description"]}]
                      "SELECT id, description FROM Currency\n  WHERE id = 'STATS'"])]}
(fact "provides a view return query"

  (!.dt
    [(v/query-return sample/Schema
                     (@! +return+)
                     "STATS"
                     []
                     {}
                     true)
     (v/query-return sample/Schema
                     (@! +return+)
                     "STATS"
                     []
                     {}
                     false)])
  => +out+)

^{:refer xt.db.text.sql-view/query-return-bulk :added "4.0"}
(fact "creates a bulk return statement"

  (!.dt
    (v/query-return-bulk sample/Schema
                         (@! +return+)
                         ["STATS" "USD"]
                         []
                         {}
                         false))
  => "SELECT id, description FROM Currency\n  WHERE id in ('STATS', 'USD')")

^{:refer xt.db.text.sql-view/query-combined :added "4.0"
  :setup [(def +select+
            (gen/bind-view data/currency-all-crypto))
                   (def +return+
                     (gen/bind-view data/currency-info))
                   (def +out+
                     [["Currency"
                       {"custom" [],
                        "where" [{"type" "crypto"}],
                        "links" [],
                        "data" ["id" "description"]}]
                      "SELECT id, description FROM Currency\n  WHERE type = 'crypto'"])]}
(fact "provides a view combine query"

  (!.dt
    [(v/query-combined sample/Schema
                       (@! +select+)
                       []
                       (@! +return+)
                       []
                       nil
                       {}
                       true)
     (v/query-combined sample/Schema
                       (@! +select+)
                       []
                       (@! +return+)
                       []
                       nil
                       {}
                       false)])
  => +out+)

(comment
  (s/pedantic ['xt.db.text.sql-view])
  
  (s/run ['xt.db.text.sql-view])
  
  (s/seedgen-benchadd   '[xt.db.text.sql-view] {:lang [:dart :julia] :write true})
  (s/seedgen-langadd    '[xt.db.text.sql-view] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.db.text.sql-view] {:lang [:lua :python] :write true}))
