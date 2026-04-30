(ns xtbench.ruby.db.schema.sql-raw-test
  (:require [std.lang :as l]
            [std.string.prose :as prose])
  (:use code.test))

(l/script- :ruby
  {:runtime :basic
   :require [[xt.db.schema.sql-raw :as raw]
             [xt.lang.common-lib :as k]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.schema.sql-raw/raw-delete :added "4.0"}
(fact "encodes a delete query"

  (!.rb
   [(raw/raw-delete "Currency"
                    {:id "XLM"}
                    {})
    (raw/raw-delete "Currency"
                    {:id ["in" [["XLM" "USD"]]]}
                    {})])
  => ["DELETE FROM Currency WHERE id = 'XLM';"
      "DELETE FROM Currency WHERE id in ('XLM', 'USD');"])

^{:refer xt.db.schema.sql-raw/raw-insert-array :added "4.0"}
(fact "constructs an array for insert and upsert"

  (!.rb
   (raw/raw-insert-array "Currency"
                         ["id" "name" "type"]
                         [{:id "XLM"
                           :name "XLM"
                           :type "crypto"}]
                         {}))
  => ["INSERT INTO Currency"
      " (id, name, type)"
      " VALUES\n ('XLM','XLM','crypto')"])

^{:refer xt.db.schema.sql-raw/raw-insert :added "4.0"}
(fact "encodes an insert query"

  (!.rb
   (raw/raw-insert "Currency"
                   ["id" "name" "type"]
                   [{:id "XLM"
                     :name "XLM"
                     :type "crypto"}]
                   {}))
  => (prose/|
      "INSERT INTO Currency"
      " (id, name, type)"
      " VALUES"
      " ('XLM','XLM','crypto');"))

^{:refer xt.db.schema.sql-raw/raw-upsert :added "4.0"}
(fact "encodes an upsert query"

  (!.rb
   (raw/raw-upsert "Currency"
                   "id"
                   ["id" "name" "type"]
                   [{:id "XLM"
                     :name "XLM"
                     :type "crypto"}]
                   {}))
  => (prose/|
      "INSERT INTO Currency"
      " (id, name, type)"
      " VALUES"
      " ('XLM','XLM','crypto')"
      "ON CONFLICT (id) DO UPDATE SET"
      "name=coalesce(\"excluded\".name,name),"
      "type=coalesce(\"excluded\".type,type);"))

^{:refer xt.db.schema.sql-raw/raw-upsert.more :added "4.0"
  :setup [(def +input+
            (prose/|
             "INSERT INTO Currency"
             " (id, name, type)"
             " VALUES"
             " ('XLM','XLM','crypto')"
             "ON CONFLICT (id) DO UPDATE SET"
             "name=coalesce(\"excluded\".name,name),"
             "type=coalesce(\"excluded\".type,type)"
             "WHERE \"excluded\".time_updated < time_updated;"))] :adopt true}
(fact "encodes an upsert query"

  (!.rb
   (raw/raw-upsert "Currency"
                   "id"
                   ["id" "name" "type"]
                   [{:id "XLM"
                     :name "XLM"
                     :type "crypto"}]
                   {:upsert-clause "\"excluded\".time_updated < time_updated"}))
  => +input+)

^{:refer xt.db.schema.sql-raw/raw-update :added "4.0"}
(fact "encodes an update query"

  (!.rb
   (raw/raw-update "Currency"
                   {:id "XLM"}
                   {:name "Stellar"}
                   {}))
  => "UPDATE Currency\n SET name = 'Stellar'\n WHERE id = 'XLM';")

^{:refer xt.db.schema.sql-raw/raw-select :added "4.0"}
(fact "encodes an select query"

  (!.rb
   (raw/raw-select "Currency"
                   {:id "XLM"}
                   ["id" "name" "type"]
                   {}))
  => "SELECT id, name, type\n  FROM Currency\n WHERE id = 'XLM';")

(comment
  (./import)
  )

(comment
  (s/pedantic ['xt.db.schema.sql-raw])
  
  (s/run ['xt.db.schema.sql-raw])
  
  (s/seedgen-benchadd   '[xt.db.schema.sql-raw] {:lang [:dart :julia] :write true})
  (s/seedgen-langadd    '[xt.db.schema.sql-raw] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.db.schema.sql-raw] {:lang [:lua :python] :write true}))
