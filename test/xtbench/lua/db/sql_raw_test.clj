(ns
 xtbench.lua.db.sql-raw-test
 (:require [std.lang :as l] [std.string.prose :as prose])
 (:use code.test))

(l/script-
 :lua
 {:runtime :basic,
  :require [[xt.db.sql-raw :as raw] [xt.lang.common-lib :as k]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.db.sql-raw/raw-delete, :added "4.0"}
(fact
 "encodes a delete query"
 ^{:hidden true}
 (!.lua
  [(raw/raw-delete "Currency" {:id "XLM"} {})
   (raw/raw-delete "Currency" {:id ["in" [["XLM" "USD"]]]} {})])
 =>
 ["DELETE FROM Currency WHERE id = 'XLM';"
  "DELETE FROM Currency WHERE id in ('XLM', 'USD');"])

^{:refer xt.db.sql-raw/raw-insert-array, :added "4.0"}
(fact
 "constructs an array for insert and upsert"
 ^{:hidden true}
 (!.lua
  (raw/raw-insert-array
   "Currency"
   ["id" "name" "type"]
   [{:id "XLM", :name "XLM", :type "crypto"}]
   {}))
 =>
 ["INSERT INTO Currency"
  " (id, name, type)"
  " VALUES\n ('XLM','XLM','crypto')"]
 (!.lua
  (raw/raw-insert-array
   "Currency"
   ["id" "name" "type"]
   [{:id "XLM", :name "XLM", :type "crypto"}
    {:id "BTC", :name "BTC", :type "crypto"}]
   {}))
 =>
 ["INSERT INTO Currency"
  " (id, name, type)"
  " VALUES\n ('XLM','XLM','crypto'),\n ('BTC','BTC','crypto')"])

^{:refer xt.db.sql-raw/raw-insert, :added "4.0"}
(fact
 "encodes an insert query"
 ^{:hidden true}
 (!.lua
  (raw/raw-insert
   "Currency"
   ["id" "name" "type"]
   [{:id "XLM", :name "XLM", :type "crypto"}]
   {}))
 =>
 (prose/|
  "INSERT INTO Currency"
  " (id, name, type)"
  " VALUES"
  " ('XLM','XLM','crypto');"))

^{:refer xt.db.sql-raw/raw-upsert, :added "4.0"}
(fact
 "encodes an upsert query"
 ^{:hidden true}
 (!.lua
  (raw/raw-upsert
   "Currency"
   "id"
   ["id" "name" "type"]
   [{:id "XLM", :name "XLM", :type "crypto"}]
   {}))
 =>
 (prose/|
  "INSERT INTO Currency"
  " (id, name, type)"
  " VALUES"
  " ('XLM','XLM','crypto')"
  "ON CONFLICT (id) DO UPDATE SET"
  "name=coalesce(\"excluded\".name,name),"
  "type=coalesce(\"excluded\".type,type);"))

^{:refer xt.db.sql-raw/raw-upsert.more,
  :adopt true,
  :added "4.0",
  :setup
  [(def
    +input+
    (prose/|
     "INSERT INTO Currency"
     " (id, name, type)"
     " VALUES"
     " ('XLM','XLM','crypto')"
     "ON CONFLICT (id) DO UPDATE SET"
     "name=coalesce(\"excluded\".name,name),"
     "type=coalesce(\"excluded\".type,type)"
     "WHERE \"excluded\".time_updated < time_updated;"))]}
(fact
 "encodes an upsert query"
 ^{:hidden true}
 (!.lua
  (raw/raw-upsert
   "Currency"
   "id"
   ["id" "name" "type"]
   [{:id "XLM", :name "XLM", :type "crypto"}]
   {:upsert-clause "\"excluded\".time_updated < time_updated"}))
 =>
 +input+)

^{:refer xt.db.sql-raw/raw-update, :added "4.0"}
(fact
 "encodes an update query"
 ^{:hidden true}
 (!.lua (raw/raw-update "Currency" {:id "XLM"} {:name "Stellar"} {}))
 =>
 "UPDATE Currency\n SET name = 'Stellar'\n WHERE id = 'XLM';")

^{:refer xt.db.sql-raw/raw-select, :added "4.0"}
(fact
 "encodes an select query"
 ^{:hidden true}
 (!.lua
  (raw/raw-select "Currency" {:id "XLM"} ["id" "name" "type"] {}))
 =>
 "SELECT id, name, type\n  FROM Currency\n WHERE id = 'XLM';")

(comment (./import))
