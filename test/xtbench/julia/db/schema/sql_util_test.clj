(ns xtbench.julia.db.schema.sql-util-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :julia
  {:runtime :basic
   :require [[xt.db.schema.sql-util :as ut]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-lib :as k]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.schema.sql-util/encode-query-string.more :added "4.0" :adopt true}
(fact "encodes a query segment"

  (!.julia
    (ut/encode-query-string [{:name "hello"}
                             {:name "world"}]
                            "WHERE"
                            {:column-fn (fn:> [col] (xt/x:cat "\"SCHEMA\"." col))}))
  => "WHERE (\"SCHEMA\".name = 'hello') OR (\"SCHEMA\".name = 'world')")

^{:refer xt.db.schema.sql-util/sqlite-json-values :added "4.0"}
(fact "select values from json"

  (!.julia
    (ut/sqlite-json-values "'[1,2,3,4]'"))
  => "(SELECT value from json_each('[1,2,3,4]'))")

^{:refer xt.db.schema.sql-util/sqlite-json-keys :added "4.0"}
(fact "select keys from json"

  (!.julia
    (ut/sqlite-json-keys "'{\"a\":1}'"))
  => "(SELECT key from json_each('{\"a\":1}'))")

^{:refer xt.db.schema.sql-util/encode-bool :added "4.0"}
(fact "encodes a boolean to sql"

  (!.julia
    [(ut/encode-bool true)
     (ut/encode-bool false)])
  => ["TRUE" "FALSE"])

^{:refer xt.db.schema.sql-util/encode-number :added "4.0"}
(fact "encodes a number (for lua dates)"

  (!.julia
    [(ut/encode-number 100000000000)])
  => ["'100000000000'"])

^{:refer xt.db.schema.sql-util/encode-operator :added "4.0"}
(fact "encodes an operator to sql"

  (!.julia
    [(ut/encode-operator "eq" {})
     (ut/encode-operator "lte" {})
     (ut/encode-operator "gte" {})])
  => ["=" "<=" ">="])

^{:refer xt.db.schema.sql-util/encode-json :added "4.0"}
(fact "encodes a json value"

  (!.julia
    (ut/encode-json {:a 1}))
  => (fn [m]
       (= (std.json/read (std.json/read m))
          {"a" 1})))

^{:refer xt.db.schema.sql-util/encode-value :added "4.0"}
(fact "encodes a value to sql"

  (!.julia
    [(ut/encode-value nil)
     (ut/encode-value 1.235)
     (ut/encode-value 100000000000000000)
     (ut/encode-value "hel'lo")
     (ut/encode-value {:a 1})
     (ut/encode-value {:a "he'llo"})])
  => ["NULL" "'1.235'" "'100000000000000000'" "'hel''lo'" "'{\"a\":1}'" "'{\"a\":\"he''llo\"}'"])

^{:refer xt.db.schema.sql-util/encode-sql-arg :added "4.0"}
(fact "encodes an sql arg (for functions)"

  (!.julia
    (ut/encode-sql-arg {"::" "sql/arg"
                        :name "hello"}
                       ut/default-quote-fn
                       {}
                       nil))
  => "'hello'")

^{:refer xt.db.schema.sql-util/encode-sql-column :added "4.0"}
(fact "encodes a sql column"

  (!.julia
    (ut/encode-sql-column {"::" "sql/column"
                           :name "hello"}
                          ut/default-quote-fn
                          {}
                          nil))
  => "\"hello\"")

^{:refer xt.db.schema.sql-util/encode-sql-tuple :added "4.0"}
(fact "encodes a sql tuple"

  (!.julia
    (ut/encode-sql-tuple {"::" "sql/tuple"
                          :args [1 2 3]}
                         ut/default-quote-fn
                         {:strict true}
                         ut/encode-loop-fn))
  => "'1', '2', '3'")

^{:refer xt.db.schema.sql-util/encode-sql-table :added "4.0"}
(fact "encodes an sql table"

  (!.julia
    (ut/encode-sql-table {"::" "sql/table"
                          :name "hello"
                          :schema "world"}
                         ut/default-quote-fn
                         {:strict true}
                         nil))
  => "\"world\".\"hello\"")

^{:refer xt.db.schema.sql-util/encode-sql-cast :added "4.0"}
(fact "encodes an sql cast"

  (!.julia
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

^{:refer xt.db.schema.sql-util/encode-sql-keyword :added "4.0"}
(fact "encodes an sql keyword"

  (!.julia
    (ut/encode-sql-keyword {:name "hello"}
                           ut/default-quote-fn
                           {}
                           ut/encode-loop-fn))
  => "hello")

^{:refer xt.db.schema.sql-util/encode-sql-fn :added "4.0"}
(fact "encodes an sql function"

  (!.julia
    (ut/encode-sql-fn {"::" "sql/fn"
                       :name "jsonb_object_keys"
                       :args [{:a 1}]}
                      ut/default-quote-fn
                      {:strict true
                       :values {:replace ut/SQLITE_FN}}
                      ut/encode-loop-fn))
  => "(SELECT key from json_each('{\"a\":1}'))"

  (!.julia
    (ut/encode-sql-fn {"::" "sql/fn"
                       :name "jsonb_build_object"
                       :args ["a" {:a 1}]}
                      ut/default-quote-fn
                      {:strict true
                       :values {:replace ut/SQLITE_FN}}
                      ut/encode-loop-fn))
  => "json_object(a, '{\"a\":1}')")

^{:refer xt.db.schema.sql-util/encode-sql-select :added "4.0"}
(fact "encodes an sql select statement"

  (!.julia
    (ut/encode-sql-select {"::" "sql/select"
                           :args ["*" "from" {"::" "sql/fn"
                                              :name "jsonb_each"
                                              :args ["'[1,2,3]'" true]}]}
                          ut/default-quote-fn
                          {:strict true
                           :values {:replace {}}}
                          ut/encode-loop-fn))
  => "(SELECT * from jsonb_each('[1,2,3]', TRUE))")

^{:refer xt.db.schema.sql-util/encode-sql :added "4.0"
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

  (!.julia
    (xtd/arr-map
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

^{:refer xt.db.schema.sql-util/encode-loop-fn :added "4.0"}
(fact "loop function to encode"

  (!.julia
    [(ut/encode-loop-fn {"::" "sql/fn"
                         :name "+"
                         :args ["k" {"::" "sql/fn"
                                     :name "+"
                                     :args [1 2 3]}]}
                        ut/default-quote-fn
                        {:strict true
                         :values {:replace {}}}
                        ut/encode-loop-fn)
     (ut/encode-loop-fn "hello"
                        ut/default-quote-fn
                        {:strict true
                         :values {:replace {}}}
                        ut/encode-loop-fn)
     (ut/encode-loop-fn {:a 1}
                        ut/default-quote-fn
                        {:strict true
                         :values {:replace {}}}
                        ut/encode-loop-fn)])
  => ["(k + ('1' + '2' + '3'))"
      "hello"
      "'{\"a\":1}'"])

^{:refer xt.db.schema.sql-util/encode-query-value :added "4.1"}
(fact "encodes a query value recursively"

  (!.julia
    [(ut/encode-query-value {"::" "sql/fn"
                             :name "+"
                             :args ["k" {"::" "sql/fn"
                                         :name "+"
                                         :args [1 2 3]}]}
                            k/identity
                            {})
     (ut/encode-query-value [["hello" "world"]]
                            k/identity
                            {})
     (ut/encode-query-value ["RAW_SQL_FRAGMENT"]
                            k/identity
                            {})
     (ut/encode-query-value ["and" {"::" "sql/fn"
                                    :name "+"
                                    :args ["k" {"::" "sql/fn"
                                                :name "+"
                                                :args [1 2 3]}]}]
                            k/identity
                            {})
     (ut/encode-query-value "or"
                            k/identity
                            {})
     (ut/encode-query-value {:a 1}
                            k/identity
                            {})])
  => ["(k + ('1' + '2' + '3'))"
      "('hello', 'world')"
      "RAW_SQL_FRAGMENT"
      "and (k + ('1' + '2' + '3'))"
      "or"
      "'{\"a\":1}'"])

^{:refer xt.db.schema.sql-util/encode-query-segment :added "4.0"
  :setup [(def +out+
            ["name = 'hello'"
             "name != 'hell''o'"
             "name in ('hello', 'hello')"
             "name != (k + ('1' + '2' + '3'))"
             "data = '{\"a\":1}'"])]}
(fact "encodes a query segment"

  (!.julia
    [(ut/encode-query-segment "name" "hello" k/identity {})
     (ut/encode-query-segment "name" ["neq" "hell'o"] k/identity {})
     (ut/encode-query-segment "name" ["in" [["hello" "hello"]]] k/identity {})
     (ut/encode-query-segment "name" ["neq" {"::" "sql/fn"
                                             :name "+"
                                             :args ["k" {"::" "sql/fn"
                                                         :name "+"
                                                         :args [1 2 3]}]}] k/identity {})
     (ut/encode-query-segment "data" {:a 1} k/identity {})])
  => +out+)

^{:refer xt.db.schema.sql-util/encode-query-single-string :added "4.0"}
(fact "helper for encode-query-string"

  (!.julia
    [(ut/encode-query-single-string {} {})
     (ut/encode-query-single-string {:name ["neq" "hello"]
                                     :data {:a 1}}
                                    {:column-fn (fn:> [col]
                                                      (xt/x:cat "\"T\"." col))})])
  => ["" "\"T\".data = '{\"a\":1}' AND \"T\".name != 'hello'"])

^{:refer xt.db.schema.sql-util/encode-query-string :added "4.0"}
(fact "encodes a query string"

  (!.julia
    [(ut/encode-query-string {} "WHERE" {})
     (ut/encode-query-string {:name "hello"}
                             "WHERE"
                             {:column-fn (fn:> [col] (xt/x:cat "\"SCHEMA\"." col))})
     (ut/encode-query-string (tab :data {:a 1}
                                  :name "hello")
                             "WHERE"
                             {})])
  => (contains-in
      [""
       "WHERE \"SCHEMA\".name = 'hello'"
       (any "WHERE data = '{\"a\":1}' AND name = 'hello'"
            "WHERE name = 'hello' AND data = '{\"a\":1}'")]))

^{:refer xt.db.schema.sql-util/LIMIT :added "4.0"}
(fact "creates a LIMIT keyword"

  (!.julia
    (ut/encode-sql-keyword (ut/LIMIT 10)
                           ut/default-quote-fn
                           {:strict true
                            :values {:replace {}}}
                           ut/encode-loop-fn))
  => "LIMIT 10")

^{:refer xt.db.schema.sql-util/OFFSET :added "4.0"}
(fact "creates a OFFSET keyword"

  (!.julia
    (ut/encode-sql-keyword (ut/OFFSET 10)
                           ut/default-quote-fn
                           {:strict true
                            :values {:replace {}}}
                           ut/encode-loop-fn))
  => "OFFSET 10")

^{:refer xt.db.schema.sql-util/ORDER-BY :added "4.0"}
(fact "creates an ORDER BY keyword"

  (!.julia
    (ut/encode-sql-keyword (ut/ORDER-BY ["name"])
                           ut/default-quote-fn
                           {:strict true
                            :values {:replace {}}}
                           ut/encode-loop-fn))
  => "ORDER BY \"name\"")

^{:refer xt.db.schema.sql-util/ORDER-SORT :added "4.0"}
(fact "creates an ORDER BY keyword"

  (!.julia
    (ut/encode-sql-keyword (ut/ORDER-SORT "desc")
                           ut/default-quote-fn
                           {:strict true
                            :values {:replace {}}}
                           ut/encode-loop-fn))
  => "DESC")

^{:refer xt.db.schema.sql-util/default-quote-fn :added "4.0"}
(fact "wraps a column in double quotes"

  (!.julia
    (ut/default-quote-fn "hello"))
  => "\"hello\"")

^{:refer xt.db.schema.sql-util/default-return-format-fn :added "4.0"}
(fact "default return format-fn"

  (!.julia
    (ut/default-return-format-fn
     "hello"
     k/identity
     ut/default-quote-fn
     {}))
  => "\"hello\"")

^{:refer xt.db.schema.sql-util/default-table-fn :added "4.0"}
(fact "wraps a table in schema"

  (!.julia
    (ut/default-table-fn "hello" {:hello {:schema "test.schema"}}))
  => "\"test.schema\".\"hello\"")

^{:refer xt.db.schema.sql-util/postgres-wrapper-fn :added "4.0"}
(fact "wraps a call for postgres"

  (!.julia
    (ut/postgres-wrapper-fn "SELECT * FROM <TABLE>" 2))
  => "WITH j_ret AS (\n  SELECT * FROM <TABLE>\n) SELECT jsonb_agg(j_ret) FROM j_ret")

^{:refer xt.db.schema.sql-util/postgres-opts :added "4.0"}
(fact "constructs postgres options"

  (!.julia
    (xt/x:is-object? (ut/postgres-opts {})))
  => true)

^{:refer xt.db.schema.sql-util/sqlite-return-format-fn :added "4.0"}
(fact "sqlite return format function"

  (!.julia
    [(ut/sqlite-return-format-fn {:expr "\"name\""
                                  :as "n"}
                                 (fn:> [arr] (xt/x:str-join "|" arr))
                                 ut/default-quote-fn
                                 {})
     (ut/sqlite-return-format-fn ["name" "data"]
                                 (fn:> [arr] (xt/x:str-join "|" arr))
                                 ut/default-quote-fn
                                 {})
     (ut/sqlite-return-format-fn "name"
                                 (fn:> [arr] (xt/x:str-join "|" arr))
                                 ut/default-quote-fn
                                 {})])
  => ["'n', \"name\""
      "name|data"
      "'name', \"name\""])

^{:refer xt.db.schema.sql-util/sqlite-to-boolean :added "4.0"}
(fact "coerces 1 to true and 0 to false"

  (!.julia
    [(ut/sqlite-to-boolean 0)
     (ut/sqlite-to-boolean 1)])
  => [false true])

^{:refer xt.db.schema.sql-util/sqlite-opts :added "4.0"}
(fact "constructs sqlite options"

  (!.julia
    (xt/x:is-object? (ut/sqlite-opts {})))
  => true)

(comment
  (s/pedantic ['xt.db.schema.sql-util])
  
  (s/run ['xt.db.schema.sql-util])
  (s/seedgen-benchadd '[xt.lang.spec] {:lang [:r] :write true})
  (s/seedgen-benchadd '[xt.db.schema] {:lang [:julia :dart] :write true})
  
  
  (s/seedgen-langadd '[xt.db.schema] {:lang [:lua :python] :write true})
  (s/seedgen-langadd 'xt.db.schema.sql-util {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.db.schema.sql-util {:lang [:lua :python] :write true}))
