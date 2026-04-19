(ns
 xtbench.python.db.sql-view-test
 (:require
  [rt.postgres :as pg]
  [std.lang :as l]
  [std.string.prose :as prose]
  [xt.db.sample-data-test :as data]
  [xt.db.sample-user-test :as user])
 (:use code.test))

(l/script-
 :python
 {:runtime :basic,
  :require
  [[xt.db.sql-view :as v]
   [xt.lang.common-lib :as k]
   [xt.db.sql-util :as ut]
   [xt.db.base-schema :as sch]
   [xt.db.base-scope :as scope]
   [xt.db.sample-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart) (do (l/rt:scaffold :python) true)],
  :teardown [(l/rt:stop)]})

^{:added "4.0",
  :setup
  [(def +select+ (pg/bind-view user/organisation-all-as-admin))
   (def
    +tree+
    ["Organisation"
     {"custom" [], "where" [], "links" [], "data" ["id"]}])],
  :guard true,
  :refer xt.db.sql-view/tree-select.organisation-all-as-admin,
  :adopt true}
(fact
 "provides a view select query"
 ^{:hidden true}
 (!.py (v/tree-select sample/Schema (@! +select+) {} {}))
 =>
 +tree+)

^{:refer xt.db.sql-view/tree-return.organisation-view-default,
  :adopt true,
  :added "4.0",
  :setup
  [(def +return+ (pg/bind-view user/organisation-view-default))
   (def
    +tree+
    ["Organisation"
     {"custom" [],
      "where" [{"id" "{{RETURN}}"}],
      "links" [],
      "data" ["id" "name" "title" "description" "tags"]}])]}
(fact
 "provides a view return query"
 ^{:hidden true}
 (!.py
  (v/tree-return
   sample/Schema
   (@! +return+)
   {"id" "{{RETURN}}"}
   {}
   {}))
 =>
 +tree+)

^{:refer xt.db.sql-view/query-select.organisation-all-as-admin,
  :adopt true,
  :added "4.0",
  :setup
  [(def +select+ (pg/bind-view user/organisation-all-as-admin))
   (def
    +out+
    [["Organisation"
      {"custom" [], "where" [], "links" [], "data" ["id"]}]
     "SELECT id FROM Organisation"])]}
(fact
 "provides a view select query"
 ^{:hidden true}
 (!.py
  [(v/query-select
    sample/Schema
    (@! +select+)
    ["00000000-0000-0000-0000-000000000000"]
    {}
    true)
   (v/query-select
    sample/Schema
    (@! +select+)
    ["00000000-0000-0000-0000-000000000000"]
    {}
    false)])
 =>
 +out+)

^{:refer xt.db.sql-view/tree-base.control,
  :adopt true,
  :added "4.0",
  :setup
  [(def
    +out+
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
      "data" ["id" "name"]}])]}
(fact
 "creates a tree base"
 ^{:hidden true}
 (!.py
  (v/tree-base
   sample/Schema
   "RegionCountry"
   []
   []
   ["id" "name" (ut/LIMIT 20) (ut/ORDER-BY ["name"])]
   {}))
 =>
 +out+)

^{:refer xt.db.sql-view/tree-control-array,
  :added "4.0",
  :setup
  [(def
    +out+
    [{"args"
      [{"args" [{"::" "sql/column", "name" "name"}],
        "::" "sql/tuple"}],
      "::" "sql/keyword",
      "name" "ORDER BY"}
     {"args" [{"::" "sql/keyword", "name" 20}],
      "::" "sql/keyword",
      "name" "LIMIT"}])]}
(fact
 "creates a control array"
 ^{:hidden true}
 (!.py (v/tree-control-array {:limit 20, :order-by ["name"]}))
 =>
 +out+)

^{:refer xt.db.sql-view/tree-base,
  :added "4.0",
  :setup
  [(def
    +out+
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
(fact
 "creates a tree base"
 ^{:hidden true}
 (!.py
  (v/tree-base
   sample/Schema
   "Currency"
   [{:id "USD"} {:id "AUD"}]
   {:type "fiat"}
   ["*/data"]
   {}))
 =>
 +out+)

^{:refer xt.db.sql-view/tree-count,
  :added "4.0",
  :setup
  [(def +count+ (pg/bind-view data/currency-by-type))
   (def
    +tree+
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
(fact
 "provides a view count query"
 ^{:hidden true}
 (!.py (v/tree-count sample/Schema (@! +count+) {} {}))
 =>
 +tree+)

^{:refer xt.db.sql-view/tree-select,
  :added "4.0",
  :setup
  [(def +select+ (pg/bind-view data/currency-by-type))
   (def
    +tree+
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
(fact
 "provides a view select query"
 ^{:hidden true}
 (!.py (v/tree-select sample/Schema (@! +select+) {} {}))
 =>
 +tree+)

^{:refer xt.db.sql-view/tree-return,
  :added "4.0",
  :setup
  [(def +return+ (pg/bind-view data/currency-default))
   (def
    +tree+
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
(fact
 "provides a view return query"
 ^{:hidden true}
 (!.py
  (v/tree-return
   sample/Schema
   (@! +return+)
   {"id" "{{RETURN}}"}
   {}
   {}))
 =>
 +tree+)

^{:refer xt.db.sql-view/tree-combined,
  :added "4.0",
  :setup
  [(def +select+ (pg/bind-view user/organisation-all-as-admin))
   (def +return+ (pg/bind-view user/organisation-view-membership))
   (def
    +tree+
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
(fact
 "provides a view return query"
 ^{:hidden true}
 (!.py
  (v/tree-combined
   sample/Schema
   (@! +select+)
   (@! +return+)
   nil
   {}
   {}))
 =>
 +tree+)

^{:refer xt.db.sql-view/query-fill-input,
  :added "4.0",
  :setup
  [(def
    +out+
    ["Organisation"
     {"custom" [], "where" [], "links" [], "data" ["id"]}])]}
(fact
 "fills out the tree for a given input"
 ^{:hidden true}
 (!.py
  (var entry (@! (pg/bind-view user/organisation-all-as-member)))
  (var tree (v/tree-select sample/Schema entry {} {}))
  (v/query-fill-input tree ["<ORG-ID>"] (. entry ["input"]) false))
 =>
 +out+)

^{:refer xt.db.sql-view/query-select,
  :added "4.0",
  :setup
  [(def +select+ (pg/bind-view data/currency-all-crypto))
   (def
    +out+
    [["Currency"
      {"custom" [],
       "where" [{"type" "crypto"}],
       "links" [],
       "data" ["id"]}]
     "SELECT id FROM Currency\n  WHERE type = 'crypto'"])]}
(fact
 "provides a view select query"
 ^{:hidden true}
 (!.py
  [(v/query-select sample/Schema (@! +select+) [] {} true)
   (v/query-select sample/Schema (@! +select+) [] {} false)])
 =>
 +out+)

^{:refer xt.db.sql-view/query-count,
  :added "4.0",
  :setup
  [(def +select+ (pg/bind-view data/currency-all-crypto))
   (def
    +out+
    [["Currency"
      {"custom" [{"::" "sql/count"}],
       "where" [{"type" "crypto"}],
       "links" [],
       "data" []}]
     "SELECT count(*) FROM Currency\n  WHERE type = 'crypto'"])]}
(fact
 "provides the count statement"
 ^{:hidden true}
 (!.py
  [(v/query-count sample/Schema (@! +select+) [] {} true)
   (v/query-count sample/Schema (@! +select+) [] {} false)])
 =>
 +out+)

^{:refer xt.db.sql-view/query-return,
  :added "4.0",
  :setup
  [(def +return+ (pg/bind-view data/currency-info))
   (def
    +out+
    [["Currency"
      {"custom" [],
       "where" [{"id" "STATS"}],
       "links" [],
       "data" ["id" "description"]}]
     "SELECT id, description FROM Currency\n  WHERE id = 'STATS'"])]}
(fact
 "provides a view return query"
 ^{:hidden true}
 (!.py
  [(v/query-return sample/Schema (@! +return+) "STATS" [] {} true)
   (v/query-return sample/Schema (@! +return+) "STATS" [] {} false)])
 =>
 +out+)

^{:refer xt.db.sql-view/query-return-bulk, :added "4.0"}
(fact
 "creates a bulk return statement"
 ^{:hidden true}
 (!.py
  (v/query-return-bulk
   sample/Schema
   (@! +return+)
   ["STATS" "USD"]
   []
   {}
   false))
 =>
 "SELECT id, description FROM Currency\n  WHERE id in ('STATS', 'USD')")

^{:refer xt.db.sql-view/query-combined,
  :added "4.0",
  :setup
  [(def +select+ (pg/bind-view data/currency-all-crypto))
   (def +return+ (pg/bind-view data/currency-info))
   (def
    +out+
    [["Currency"
      {"custom" [],
       "where" [{"type" "crypto"}],
       "links" [],
       "data" ["id" "description"]}]
     "SELECT id, description FROM Currency\n  WHERE type = 'crypto'"])]}
(fact
 "provides a view combine query"
 ^{:hidden true}
 (!.py
  [(v/query-combined
    sample/Schema
    (@! +select+)
    []
    (@! +return+)
    []
    nil
    {}
    true)
   (v/query-combined
    sample/Schema
    (@! +select+)
    []
    (@! +return+)
    []
    nil
    {}
    false)])
 =>
 +out+)
