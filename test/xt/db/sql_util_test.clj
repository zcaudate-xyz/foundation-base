(ns xt.db.sql-util-test
  (:use code.test)
  (:require [std.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.sql-util :as ut]
             [xt.lang.base-lib :as k]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.db.sql-util :as ut]
             [xt.lang.base-lib :as k]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.db.sql-util :as ut]
             [xt.lang.base-lib :as k]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.sql-util/encode-query-string.more :adopt true :added "4.0"}
(fact "encodes a query segment"
  ^:hidden

  (!.js
   (ut/encode-query-string [{:name "hello"}
                            {:name "world"}]
                           "WHERE"
                           {:column-fn (fn:> [col] (k/cat "\"SCHEMA\"." col))}))
  => "WHERE (\"SCHEMA\".name = 'hello') OR (\"SCHEMA\".name = 'world')")

^{:refer xt.db.sql-util/sqlite-json-values :added "4.0"}
(fact "select values from json"
  ^:hidden

  (!.js
   (ut/sqlite-json-values "'[1,2,3,4]'"))
  => "(SELECT value from json_each('[1,2,3,4]'))")

^{:refer xt.db.sql-util/sqlite-json-keys :added "4.0"}
(fact "select keys from json"

  (!.js
   (ut/sqlite-json-keys "'{\"a\":1}'"))
  => "(SELECT key from json_each('{\"a\":1}'))")

^{:refer xt.db.sql-util/encode-bool :added "4.0"}
(fact "encodes a boolean to sql"
  ^:hidden
  
  (!.js
   [(ut/encode-bool true)
    (ut/encode-bool false)])
  => ["TRUE" "FALSE"]

  (!.lua
   [(ut/encode-bool true)
    (ut/encode-bool false)])
  => ["TRUE" "FALSE"]

  (!.py
   [(ut/encode-bool true)
    (ut/encode-bool false)])
  => ["TRUE" "FALSE"])

^{:refer xt.db.sql-util/encode-number :added "4.0"}
(fact "encodes a number (for lua dates)"
  ^:hidden
  
  (!.js
   [(ut/encode-number 100000000000)])
  => ["'100000000000'"]

  (!.lua
   [(ut/encode-number 100000000000)])
  => ["'100000000000'"])

^{:refer xt.db.sql-util/encode-operator :added "4.0"}
(fact "encodes an operator to sql"
  ^:hidden
  
  (!.js
   [(ut/encode-operator "eq")
    (ut/encode-operator "lte")
    (ut/encode-operator "gte")])
  => ["=" "<=" ">="]

  (!.lua
   [(ut/encode-operator "eq")
    (ut/encode-operator "lte")
    (ut/encode-operator "gte")])
  => ["=" "<=" ">="]

  (!.py
   [(ut/encode-operator "eq" {})
    (ut/encode-operator "lte" {})
    (ut/encode-operator "gte" {})])
  => ["=" "<=" ">="])

^{:refer xt.db.sql-util/encode-json :added "4.0"}
(fact "encodes a json value")

^{:refer xt.db.sql-util/encode-value :added "4.0"}
(fact "encodes a value to sql"
  ^:hidden

  (!.js (k/json-encode 100000000000000000))
  (!.lua (k/json-encode 100000000000000000))
  
  (!.lua
   (string.format "%0.f" 100000000000000000))
  
  (!.js
   [(ut/encode-value nil)
    (ut/encode-value 1.235)
    (ut/encode-value 100000000000000000)
    (ut/encode-value "hel'lo")
    (ut/encode-value {:a 1})
    (ut/encode-value {:a "he'llo"})])
  => ["NULL" "'1.235'" "'100000000000000000'" "'hel''lo'" "'{\"a\":1}'" "'{\"a\":\"he''llo\"}'"]

  (!.lua
   [(ut/encode-value nil)
    (ut/encode-value 1.235)
    (ut/encode-value 100000000000000000)
    (ut/encode-value "hel'lo")
    (ut/encode-value {:a 1})
    (ut/encode-value {:a "he'llo"})])
  => ["NULL" "'1.235'" "'100000000000000000'" "'hel''lo'" "'{\"a\":1}'" "'{\"a\":\"he''llo\"}'"]
  
  (!.py
   [(ut/encode-value nil)
    (ut/encode-value 1.235)
    (ut/encode-value 100000000000000000)
    (ut/encode-value "hel'lo")
    (ut/encode-value {:a 1})
    (ut/encode-value {:a "he'llo"})])
  => ["NULL" "'1.235'" "'100000000000000000'" "'hel''lo'" "'{\"a\": 1}'" "'{\"a\": \"he''llo\"}'"])

^{:refer xt.db.sql-util/encode-sql-arg :added "4.0"}
(fact "encodes an sql arg (for functions)"
  ^:hidden
  
  (!.js
   (ut/encode-sql-arg {"::" "sql/arg"
                       :name "hello"}
                      ut/default-quote-fn
                      {}
                      nil))
  => "'hello'"

  (!.lua
   (ut/encode-sql-arg {"::" "sql/arg"
                       :name "hello"}
                      ut/default-quote-fn
                      {}
                      nil))
  => "'hello'"

  (!.py
   (ut/encode-sql-arg {"::" "sql/arg"
                       :name "hello"}
                      ut/default-quote-fn
                      {}
                      nil))
  => "'hello'")

^{:refer xt.db.sql-util/encode-sql-column :added "4.0"}
(fact "encodes a sql column"
  ^:hidden
  
  (!.js
   (ut/encode-sql-column {"::" "sql/column"
                          :name "hello"}
                         ut/default-quote-fn
                         {}
                         nil))
  => "\"hello\""

  (!.lua
   (ut/encode-sql-column {"::" "sql/column"
                          :name "hello"}
                         ut/default-quote-fn
                         {}
                         nil))
  => "\"hello\""

  (!.py
   (ut/encode-sql-column {"::" "sql/column"
                          :name "hello"}
                         ut/default-quote-fn
                         {}
                         nil))
  => "\"hello\"")

^{:refer xt.db.sql-util/encode-sql-tuple :added "4.0"}
(fact "encodes a sql tuple"
  ^:hidden
  
  (!.js
   (ut/encode-sql-tuple {"::" "sql/tuple"
                         :args [1 2 3]}
                        ut/default-quote-fn
                        {:strict true}
                        ut/encode-loop-fn))
  => "'1', '2', '3'"

  (!.lua
   (ut/encode-sql-tuple {"::" "sql/tuple"
                         :args [1 2 3]}
                        ut/default-quote-fn
                        {:strict true}
                        ut/encode-loop-fn))
  => "'1', '2', '3'"

  (!.py
   (ut/encode-sql-tuple {"::" "sql/tuple"
                         :args [1 2 3]}
                        ut/default-quote-fn
                        {:strict true}
                        ut/encode-loop-fn))
  => "'1', '2', '3'")

^{:refer xt.db.sql-util/encode-sql-table :added "4.0"}
(fact "encodes an sql table"
  ^:hidden
  
  (!.js
   (ut/encode-sql-table {"::" "sql/table"
                         :name "hello"
                         :schema "world"}
                        ut/default-quote-fn
                        {:strict true}
                        nil))
  => "\"world\".\"hello\""

  (!.lua
   (ut/encode-sql-table {"::" "sql/table"
                         :name "hello"
                         :schema "world"}
                        ut/default-quote-fn
                        {:strict true}
                        nil))
  => "\"world\".\"hello\""

  (!.py
   (ut/encode-sql-table {"::" "sql/table"
                         :name "hello"
                         :schema "world"}
                        ut/default-quote-fn
                        {:strict true}
                        nil))
  => "\"world\".\"hello\"")

^{:refer xt.db.sql-util/encode-sql-cast :added "4.0"}
(fact "encodes an sql cast"
  ^:hidden
  
  (!.js
   [(ut/encode-sql-cast {"::" "sql/cast"
                         :args ["k" {"::" "sql/table"
                                     :name "hello"
                                     :schema "ENUM"}]}
                        ut/default-quote-fn
                        {:strict true}
                        ut/encode-loop-fn)
    (ut/encode-sql-cast {"::" "sql/cast"
                        :args ["k" {"::" "sql/table"
                                    :name "hello"
                                    :schema "ENUM"}]}
                       ut/default-quote-fn
                       {:strict false}
                       ut/encode-loop-fn)])
  => ["k::\"ENUM\".\"hello\"" "k"]

  (!.lua
   [(ut/encode-sql-cast {"::" "sql/cast"
                         :args ["k" {"::" "sql/table"
                                     :name "hello"
                                     :schema "ENUM"}]}
                        ut/default-quote-fn
                        {:strict true}
                        ut/encode-loop-fn)
    (ut/encode-sql-cast {"::" "sql/cast"
                        :args ["k" {"::" "sql/table"
                                    :name "hello"
                                    :schema "ENUM"}]}
                       ut/default-quote-fn
                       {:strict false}
                       ut/encode-loop-fn)])
  => ["k::\"ENUM\".\"hello\"" "k"]

  (!.py
   [(ut/encode-sql-cast {"::" "sql/cast"
                         :args ["k" {"::" "sql/table"
                                     :name "hello"
                                     :schema "ENUM"}]}
                        ut/default-quote-fn
                        {:strict true}
                        ut/encode-loop-fn)
    (ut/encode-sql-cast {"::" "sql/cast"
                        :args ["k" {"::" "sql/table"
                                    :name "hello"
                                    :schema "ENUM"}]}
                       ut/default-quote-fn
                       {:strict false}
                       ut/encode-loop-fn)])
  => ["k::\"ENUM\".\"hello\"" "k"])

^{:refer xt.db.sql-util/encode-sql-keyword :added "4.0"}
(fact "encodes an sql keyword"
  ^:hidden

  (!.js
   (ut/encode-sql-keyword {:name "hello"}
                          ut/default-quote-fn
                          {}
                          ut/encode-loop-fn))
  => "hello"

  (!.lua
   (ut/encode-sql-keyword {:name "hello"}
                          ut/default-quote-fn
                          {}
                          ut/encode-loop-fn))
  => "hello"

  (!.py
   (ut/encode-sql-keyword {:name "hello"}
                          ut/default-quote-fn
                          {}
                          ut/encode-loop-fn))
  => "hello")

^{:refer xt.db.sql-util/encode-sql-fn :added "4.0"}
(fact "encodes an sql function"
  ^:hidden

  (!.js
   (ut/encode-sql-fn {"::" "sql/fn"
                      :name "jsonb_object_keys"
                      :args [{:a 1}]}
                     ut/default-quote-fn
                     {:strict true
                      :values {:replace ut/SQLITE_FN}}
                     ut/encode-loop-fn))
  => "(SELECT key from json_each('{\"a\":1}'))"

  (!.js
   (ut/encode-sql-fn {"::" "sql/fn"
                      :name "jsonb_build_object"
                      :args ["a" {:a 1}]}
                     ut/default-quote-fn
                     {:strict true
                      :values {:replace ut/SQLITE_FN}}
                     ut/encode-loop-fn))
  => "json_object(a, '{\"a\":1}')")

^{:refer xt.db.sql-util/encode-sql-select :added "4.0"}
(fact "encodes an sql select statement"
  ^:hidden
  
  (!.js
   (ut/encode-sql-select {"::" "sql/select"
                          :args ["*" "from" {"::" "sql/fn"
                                             :name "jsonb_each"
                                             :args ["'[1,2,3]'" true]}]}
                         ut/default-quote-fn
                         {:strict true
                          :values {:replace {}}}
                         ut/encode-loop-fn))
  => "(SELECT * from jsonb_each('[1,2,3]', TRUE))"

  (!.lua
   (ut/encode-sql-select {"::" "sql/select"
                          :args ["*" "from" {"::" "sql/fn"
                                             :name "jsonb_each"
                                             :args ["'[1,2,3]'" true]}]}
                         ut/default-quote-fn
                         {:strict true
                          :values {:replace {}}}
                         ut/encode-loop-fn))
  => "(SELECT * from jsonb_each('[1,2,3]', TRUE))"

  (!.py
   (ut/encode-sql-select {"::" "sql/select"
                          :args ["*" "from" {"::" "sql/fn"
                                             :name "jsonb_each"
                                             :args ["'[1,2,3]'" true]}]}
                         ut/default-quote-fn
                         {:strict true
                          :values {:replace {}}}
                         ut/encode-loop-fn))
  => "(SELECT * from jsonb_each('[1,2,3]', TRUE))")

^{:refer xt.db.sql-util/encode-sql :added "4.0"
  :setup [(def +inputs+
            [{"::" "sql/column"
                :name "hello"}
             {"::" "sql/cast"
              :args ["k" {"::" "sql/table"
                          :name "hello"
                          :schema "ENUM"}]}
             {"::" "sql/fn"
              :name "+"
              :args ["k" {"::" "sql/fn"
                          :name "+"
                          :args [1 2 3]}]}
             {"::" "sql/select"
              :args ["*" "from" {"::" "sql/fn"
                                 :name "jsonb_each"
                                 :args ["'[1,2,3]'" true]}]}])]}
(fact "encodes an sql value"
  ^:hidden
  
  (!.js
   (k/arr-map
    (@! +inputs+)
    (fn [v]
      (return (ut/encode-sql v
                             ut/default-quote-fn
                             {:strict true
                              :values {:replace {}}}
                             ut/encode-loop-fn)))))
  => ["\"hello\""
      "k::\"ENUM\".\"hello\""
      "(k + ('1' + '2' + '3'))"
      "(SELECT * from jsonb_each('[1,2,3]', TRUE))"]

  (!.lua
   (k/arr-map
    (@! +inputs+)
    (fn [v]
      (return (ut/encode-sql v
                             ut/default-quote-fn
                             {:strict true
                              :values {:replace {}}}
                             ut/encode-loop-fn)))))
  => ["\"hello\""
      "k::\"ENUM\".\"hello\""
      "(k + ('1' + '2' + '3'))"
      "(SELECT * from jsonb_each('[1,2,3]', TRUE))"]

  (!.py
   (k/arr-map
    (@! +inputs+)
    (fn [v]
      (return (ut/encode-sql v
                             ut/default-quote-fn
                             {:strict true
                              :values {:replace {}}}
                             ut/encode-loop-fn)))))
  => ["\"hello\""
      "k::\"ENUM\".\"hello\""
      "(k + ('1' + '2' + '3'))"
      "(SELECT * from jsonb_each('[1,2,3]', TRUE))"])

^{:refer xt.db.sql-util/encode-loop-fn :added "4.0"}
(fact "loop function to encode")

^{:refer xt.db.sql-util/encode-query-segment :added "4.0"
  :setup [(def +out+
            ["name = 'hello'"
             "name != 'hell''o'"
             "name in ('hello', 'hello')"
             "name != (k + ('1' + '2' + '3'))"
             "data = '{\"a\":1}'"])]}
(fact "encodes a query segment"
  ^:hidden
  
  
  (!.js
   [(ut/encode-query-segment "name" "hello" k/identity {})
    (ut/encode-query-segment "name" ["neq" "hell'o"] k/identity {})
    (ut/encode-query-segment "name" ["in" [["hello" "hello"]]] k/identity {})
    (ut/encode-query-segment "name" ["neq" {"::" "sql/fn"
                                            :name "+"
                                            :args ["k" {"::" "sql/fn"
                                                        :name "+"
                                                        :args [1 2 3]}]}] k/identity {})
    (ut/encode-query-segment "data" {:a 1} k/identity {})])
  => +out+
  
  (!.lua
   [(ut/encode-query-segment "name" "hello" k/identity {})
    (ut/encode-query-segment "name" ["neq" "hell'o"] k/identity {})
    (ut/encode-query-segment "name" ["in" [["hello" "hello"]]] k/identity {})
    (ut/encode-query-segment "name" ["neq" {"::" "sql/fn"
                                            :name "+"
                                            :args ["k" {"::" "sql/fn"
                                                        :name "+"
                                                        :args [1 2 3]}]}] k/identity {})
    (ut/encode-query-segment "data" {:a 1} k/identity {})])
  => +out+

  (!.py
   [(ut/encode-query-segment "name" "hello" k/identity {})
    (ut/encode-query-segment "name" ["neq" "hell'o"] k/identity {})
    (ut/encode-query-segment "name" ["in" [["hello" "hello"]]] k/identity {})
    (ut/encode-query-segment "name" ["neq" {"::" "sql/fn"
                                            :name "+"
                                            :args ["k" {"::" "sql/fn"
                                                        :name "+"
                                                        :args [1 2 3]}]}] k/identity {})
    (ut/encode-query-segment "data" {:a 1} k/identity {})])
  => (assoc +out+ 4 "data = '{\"a\": 1}'"))

^{:refer xt.db.sql-util/encode-query-single-string :added "4.0"}
(fact "helper for encode-query-string")

^{:refer xt.db.sql-util/encode-query-string :added "4.0"}
(fact "encodes a query string"
  ^:hidden
  
  (!.js
   [(ut/encode-query-string {} "WHERE" {})
    (ut/encode-query-string {:name "hello"} "WHERE"
                            {:column-fn (fn:> [col] (k/cat "\"SCHEMA\"." col))})
    (ut/encode-query-string {:data {:a 1}
                             :name "hello"}
                           "WHERE"
                           {})])
  
  => [""
      "WHERE \"SCHEMA\".name = 'hello'"
      "WHERE data = '{\"a\":1}' AND name = 'hello'"]

  (!.lua
   [(ut/encode-query-string {} "WHERE" {})
    (ut/encode-query-string {:name "hello"} "WHERE"
                           {:column-fn (fn:> [col] (k/cat "\"SCHEMA\"." col))})
    (ut/encode-query-string {:data {:a 1}}
                           "WHERE"
                           {})])
  => [""
      "WHERE \"SCHEMA\".name = 'hello'"
      "WHERE data = '{\"a\":1}'"]

  (!.py
   [(ut/encode-query-string {} "WHERE" {})
    (ut/encode-query-string {:name "hello"} "WHERE"
                            {:column-fn (fn:> [col] (k/cat "\"SCHEMA\"." col))})
    (ut/encode-query-string {:data {:a 1}
                            :name "hello"}
                           "WHERE"
                           {})])
  => [""
      "WHERE \"SCHEMA\".name = 'hello'"
      "WHERE data = '{\"a\": 1}' AND name = 'hello'"])

^{:refer xt.db.sql-util/LIMIT :added "4.0"}
(fact "creates a LIMIT keyword"
  ^:hidden
  
  (!.js
   (ut/encode-sql-keyword (ut/LIMIT 10)
                          ut/default-quote-fn
                          {:strict true
                           :values {:replace {}}}
                          ut/encode-loop-fn))
  => "LIMIT 10"

  (!.lua
   (ut/encode-sql-keyword (ut/LIMIT 10)
                          ut/default-quote-fn
                          {:strict true
                           :values {:replace {}}}
                          ut/encode-loop-fn))
  => "LIMIT 10"

  (!.py
   (ut/encode-sql-keyword (ut/LIMIT "10")
                          ut/default-quote-fn
                          {:strict true
                           :values {:replace {}}}
                          ut/encode-loop-fn))
  => "LIMIT 10")

^{:refer xt.db.sql-util/OFFSET :added "4.0"}
(fact "creates a OFFSET keyword"
  ^:hidden
  
  (!.js
   (ut/encode-sql-keyword (ut/OFFSET 10)
                          ut/default-quote-fn
                          {:strict true
                           :values {:replace {}}}
                          ut/encode-loop-fn))
  => "OFFSET 10"

  (!.lua
   (ut/encode-sql-keyword (ut/OFFSET 10)
                          ut/default-quote-fn
                          {:strict true
                           :values {:replace {}}}
                          ut/encode-loop-fn))
  => "OFFSET 10"

  (!.py
   (ut/encode-sql-keyword (ut/OFFSET "10")
                          ut/default-quote-fn
                          {:strict true
                           :values {:replace {}}}
                          ut/encode-loop-fn))
  => "OFFSET 10")

^{:refer xt.db.sql-util/ORDER-BY :added "4.0"}
(fact "creates an ORDER BY keyword"
  ^:hidden
  
  (!.js
   (ut/encode-sql-keyword (ut/ORDER-BY ["name"])
                          ut/default-quote-fn
                          {:strict true
                           :values {:replace {}}}
                          ut/encode-loop-fn))
  => "ORDER BY \"name\""

  (!.lua
   (ut/encode-sql-keyword (ut/ORDER-BY ["name"])
                          ut/default-quote-fn
                          {:strict true
                           :values {:replace {}}}
                          ut/encode-loop-fn))
  => "ORDER BY \"name\""

  (!.py
   (ut/encode-sql-keyword (ut/ORDER-BY ["name"])
                          ut/default-quote-fn
                          {:strict true
                           :values {:replace {}}}
                          ut/encode-loop-fn))
  => "ORDER BY \"name\"")

^{:refer xt.db.sql-util/ORDER-SORT :added "4.0"}
(fact "creates an ORDER BY keyword"
  ^:hidden
  
  (!.js
   (ut/encode-sql-keyword (ut/ORDER-SORT "desc")
                          ut/default-quote-fn
                          {:strict true
                           :values {:replace {}}}
                          ut/encode-loop-fn))
  => "DESC"

  (!.lua
   (ut/encode-sql-keyword (ut/ORDER-SORT "desc")
                          ut/default-quote-fn
                          {:strict true
                           :values {:replace {}}}
                          ut/encode-loop-fn))
  => "DESC"

  (!.py
   (ut/encode-sql-keyword (ut/ORDER-SORT "desc")
                          ut/default-quote-fn
                          {:strict true
                           :values {:replace {}}}
                          ut/encode-loop-fn))
  => "DESC")

^{:refer xt.db.sql-util/default-quote-fn :added "4.0"}
(fact "wraps a column in double quotes"
  ^:hidden
  
  (!.js
   (ut/default-quote-fn "hello"))
  => "\"hello\""

  (!.lua
   (ut/default-quote-fn "hello"))
  => "\"hello\""

  (!.py
   (ut/default-quote-fn "hello"))
  => "\"hello\"")

^{:refer xt.db.sql-util/default-return-format-fn :added "4.0"}
(fact "default return format-fn"
  ^:hidden

  (!.js
   (ut/default-return-format-fn
    "hello"
    k/identity
    ut/default-quote-fn))
  => "\"hello\""

  (!.lua
   (ut/default-return-format-fn
    "hello"
    k/identity
    ut/default-quote-fn))
  => "\"hello\""

  (!.py
   (ut/default-return-format-fn
    "hello"
    k/identity
    ut/default-quote-fn
    {}))
  => "\"hello\"")

^{:refer xt.db.sql-util/default-table-fn :added "4.0"}
(fact "wraps a table in schema"
  ^:hidden
  
  (!.js
   (ut/default-table-fn "hello" {:hello {:schema "test.schema"}}))
  => "\"test.schema\".\"hello\""

  (!.lua
   (ut/default-table-fn "hello" {:hello {:schema "test.schema"}}))
  => "\"test.schema\".\"hello\""

  (!.py
   (ut/default-table-fn "hello" {:hello {:schema "test.schema"}}))
  => "\"test.schema\".\"hello\"")

^{:refer xt.db.sql-util/postgres-wrapper-fn :added "4.0"}
(fact "wraps a call for postgres"
  ^:hidden
  
  (!.js
   (ut/postgres-wrapper-fn "SELECT * FROM <TABLE>" 2))
  => "WITH j_ret AS (\n  SELECT * FROM <TABLE>\n) SELECT jsonb_agg(j_ret) FROM j_ret"

  (!.lua
   (ut/postgres-wrapper-fn "SELECT * FROM <TABLE>" 2))
  => "WITH j_ret AS (\n  SELECT * FROM <TABLE>\n) SELECT jsonb_agg(j_ret) FROM j_ret"

  (!.py
   (ut/postgres-wrapper-fn "SELECT * FROM <TABLE>" 2))
  => "WITH j_ret AS (\n  SELECT * FROM <TABLE>\n) SELECT jsonb_agg(j_ret) FROM j_ret")

^{:refer xt.db.sql-util/postgres-opts :added "4.0"}
(fact "constructs postgres options"
  ^:hidden
  
  (!.js
   (ut/postgres-opts {}))
  => map?
  
  (!.lua
   (ut/postgres-opts {}))
  => any?
  
  (!.py
   (ut/postgres-opts {}))
  => any?)

^{:refer xt.db.sql-util/sqlite-return-format-fn :added "4.0"}
(fact "sqlite return format function")

^{:refer xt.db.sql-util/sqlite-to-boolean :added "4.0"}
(fact "coerces 1 to true and 0 to false"
  ^:hidden
  
  (!.js
   [(ut/sqlite-to-boolean 0)
    (ut/sqlite-to-boolean 1)])
  => [false true]

  (!.lua
   [(ut/sqlite-to-boolean 0)
    (ut/sqlite-to-boolean 1)])
  => [false true]

  (!.py
   [(ut/sqlite-to-boolean 0)
    (ut/sqlite-to-boolean 1)])
  => [false true])

^{:refer xt.db.sql-util/sqlite-opts :added "4.0"}
(fact "constructs sqlite options"
  ^:hidden
  
  (!.js
   (ut/sqlite-opts {}))
  => map?
  
  (!.lua
   (ut/sqlite-opts {}))
  => any?
  
  (!.py
   (ut/sqlite-opts {}))
  => any?)
