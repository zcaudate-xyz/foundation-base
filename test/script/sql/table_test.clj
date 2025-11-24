(ns script.sql.table-test
  (:use code.test)
  (:require [script.sql.table :refer :all]
            [script.sql.expr :as builder]
            [script.sql.common :as common]
            [std.lib.schema :as schema]
            [std.string :as str]
            [script.sql.table.compile :as compile]))

(fact:global
 {:component
  {|schema| {:create
             (schema/schema
              [:meat      [:id     {:sql {:primary true}}
                           :type   {:sql {:format :edn}}
                           :amount {:type :int}
                           :grade  {:type :enum
                                    :enum {:ns :meat.grade
                                           :values #{:good :bad :ok :nasty
                                                     :fair :horrible :awesome}}}]
               :vegetable [:id     {:sql {:primary true}}
                           :cost   {:type :int}
                           :grade  {:type :real}]])}}})

^{:refer script.sql.table/sql-tmpl :added "4.0"}
(fact "creating sql functions"
  (with-redefs [sql:insert (fn [t d & _] (str "insert " t " " d))]
    (sql:insert :person {:id "a" :age 8})
    => "insert :person {:id \"a\", :age 8}"))

^{:refer script.sql.table/table-common-options :added "4.0"}
(fact "returns the common options"
  ^:hidden
  
  (table-common-options)
  => (contains
      {:table-fn fn?
       :column-fn fn?}))

^{:refer script.sql.table/schema:order :added "3.0"}
(fact "produces an ordered list of data from schema"
  ^:hidden
  
  (schema:order {:vec [:account {}
                       :wallet  {}]}
                {:account {}
                 :wallet  {}})
  => [[:account {}] [:wallet {}]])

^{:refer script.sql.table/schema:ids:alias :added "3.0"
  :use [|schema|]}
(fact "retreives the alias for a given id"
  ^:hidden
  
  (schema:ids:alias |schema| :meat [:type :amount])
  => [:type :amount])

^{:refer script.sql.table/schema:ids :added "3.0"}
(fact "constructs an access vector for ids"
  ^:hidden
  
  (schema:ids (schema/schema
               [:access [:id {:type :alias
                              :alias {:keys [:first :nick]}}
                         :first {}
                         :nick  {:type :ref :ref {:ref :nick}}]])
              :access)
  => [:first :nick-id])

^{:refer script.sql.table/table:query:id :added "3.0"}
(fact "constructs an sql query"
  ^:hidden

  (table:query:id :user "id-0")
  => "SELECT * FROM \"user\" WHERE id = 'id-0'")

^{:refer script.sql.table/table:update :added "3.0"}
(fact "constructs an update query"

  (table:update :user {:id "id-0" :name "User-0"})
  => "UPDATE \"user\" SET \"name\" = 'User-0' WHERE \"id\" = 'id-0'")

^{:refer script.sql.table/table-compile :added "4.0"
  :use [|schema|]}
(fact "compiles a table in the schema"
  ^:hidden
  
  (table-compile builder/for-insert-multi
                 :meat
                 [{:id "a0" :type :beef :amount 100 :grade :good}
                       {:id "b0" :type :pork :amount 10  :grade :bad}]
                 (merge {:schema |schema|}
                        common/*options*))
  => string?)

^{:refer script.sql.table/table-batch :added "3.0"
  :use [|schema|]}
(fact "helper function for batch calls"
  ^:hidden

  (table-batch builder/for-insert-multi
               {:meat [{:id "a0" :type :beef :amount 100 :grade :good}
                       {:id "b0" :type :pork :amount 10  :grade :bad}]}
               (merge {:schema |schema|}
                      common/*options*))
  => [[:meat (str/| "INSERT INTO \"meat\""
                    " (\"id\", \"type\", \"amount\", \"grade\")"
                    " VALUES" " ('a0', ':beef', '100', 'good'),"
                    " ('b0', ':pork', '10', 'bad')")]])

^{:refer script.sql.table/table:put:batch :added "3.0"
  :use [|schema|]}
(fact "constructs a batch upsert statement"
  ^:hidden

  (table:put:batch {:meat [{:id "a0" :type :beef :amount 100 :grade :good}
                           {:id "b0" :type :pork :amount 10  :grade :bad}
                           {:id "c0" :type :fish :amount 1   :grade :nasty}]}
                   {:schema |schema|})
  => [[:meat (str/|
              "INSERT INTO \"meat\""
              " (\"id\", \"type\", \"amount\", \"grade\")"
              " VALUES"
              " ('a0', ':beef', '100', 'good'),"
              " ('b0', ':pork', '10', 'bad'),"
              " ('c0', ':fish', '1', 'nasty')"
              " ON CONFLICT (\"id\")"
              " DO UPDATE SET (\"type\", \"amount\", \"grade\")"
              " = ROW(EXCLUDED.\"type\", EXCLUDED.\"amount\", EXCLUDED.\"grade\")")]])

^{:refer script.sql.table/table:put:single :added "3.0"
  :use [|schema|]}
(fact "constructs a single upsert statement"
  ^:hidden

  (table:put:single :meat {:id "a0" :type :chicken :amount 100 :grade :good}
                    {:schema |schema|})
  => (str/|
      "INSERT INTO \"meat\""
      " (\"id\", \"type\", \"amount\", \"grade\")"
      " VALUES ('a0', ':chicken', '100', 'good')"
      " ON CONFLICT (\"id\")"
      " DO UPDATE SET (\"type\", \"amount\", \"grade\")"
      " = ROW(EXCLUDED.\"type\", EXCLUDED.\"amount\", EXCLUDED.\"grade\")"))

^{:refer script.sql.table/table:set:batch :added "3.0"
  :use [|schema|]}
(fact "constructs a batch insert statement"
  ^:hidden

  (table:set:batch {:meat [{:id "a0" :type :beef :amount 100 :grade :good}
                           {:id "b0" :type :pork :amount 10  :grade :bad}
                           {:id "c0" :type :fish :amount 1   :grade :nasty}]}
                   {:schema |schema|})
  => [[:meat (str/|
              "INSERT INTO \"meat\""
              " (\"id\", \"type\", \"amount\", \"grade\")"
              " VALUES"
              " ('a0', ':beef', '100', 'good'),"
              " ('b0', ':pork', '10', 'bad'),"
              " ('c0', ':fish', '1', 'nasty')")]])

^{:refer script.sql.table/table:set:single :added "3.0"
  :use [|schema|]}
(fact "constructs a single insert statement"
  ^:hidden

  (table:set:single :meat {:id "a0" :type :chicken :amount 100 :grade :good}
                    {:schema |schema|})
  => (str/|
      "INSERT INTO \"meat\""
      " (\"id\", \"type\", \"amount\", \"grade\")"
      " VALUES"
      " ('a0', ':chicken', '100', 'good')"))

^{:refer script.sql.table/table:delete :added "3.0"
  :use [|schema|]}
(fact "constructs a delete statement"
  ^:hidden

  (table:delete :meat "id-0" {:schema |schema|})
  => "DELETE FROM \"meat\" WHERE \"id\" = 'id-0'")

^{:refer script.sql.table/table:keys :added "3.0"
  :use [|schema|]}
(fact "constructs a key search statement"

  (table:keys :meat {:schema |schema|})
  => "SELECT \"id\" FROM \"meat\"")

^{:refer script.sql.table/table:clear :added "3.0"}
(fact "constructs a clear table statement"
  ^:hidden

  (table:clear :meat)
  => "DELETE FROM \"meat\"")

^{:refer script.sql.table/table:select :added "3.0"
  :use [|schema|]}
(fact "constructs sql select statement"
  ^:hidden

  (table:select :meat
                {:amount 100}
                {:schema |schema|})
  => "SELECT * FROM \"meat\" WHERE \"amount\" = '100'")

^{:refer script.sql.table/from-string :added "3.0"}
(fact "converts a schema value from string"

  (from-string "hello" :ref :account)
  => :account.id/hello)

^{:refer script.sql.table/from-alias :added "3.0"}
(fact "converts an alias to expanded map"
  (from-alias :id "a.b" :table {} {}) => map?)

^{:refer script.sql.table/table:get :added "3.0"
  :use [|schema|]}
(fact "constructs an sql get statement"
  ^:hidden

  (table:get :meat "a0" {:schema |schema|})
  => "SELECT * FROM \"meat\" WHERE \"id\" = 'a0'")

^{:refer script.sql.table/table:cas :added "3.0"
  :use [|schema|]}
(fact "constructs a cas statement"
  ^:hidden

  (table:cas :meat
             {:id "i0" :amount 100}
             {:id "i0" :amount 200}
             {:schema |schema|})
  => (str/|
      "DO $$"
      " DECLARE v_id TEXT;"
      " BEGIN"
      " UPDATE \"meat\""
      " SET (\"id\", \"amount\") = ('i0', '200')"
      " WHERE id = (SELECT id FROM \"meat\" WHERE \"id\" = 'i0' AND \"amount\" = '100' LIMIT 1)"
      " RETURNING id INTO v_id;"
      " IF count(v_id) = 0 THEN"
      "  RAISE EXCEPTION 'Cas Error for (:meat, i0)';"
      " END IF;"
      "END; $$"))

^{:refer script.sql.table/table:count :added "3.0"
  :use [|schema|]}
(fact "constructs an sql count statement"
  ^:hidden

  (table:count :meat nil {:schema |schema|})
  => "SELECT count(*) FROM \"meat\"")

^{:refer script.sql.table/order-keys :added "3.0"
  :use [|schema|]}
(fact "order keys in the schema"

  (order-keys (:vec |schema|))
  => {:meat 0, :vegetable 1})

^{:refer script.sql.table/order-data :added "3.0"}
(fact "orders data based on schema"

  (order-data [:account {}
               :wallet  {}]
              {:account {}
               :wallet {}})
  => [[:account {}] [:wallet {}]])

^{:refer script.sql.table/table:batch :added "3.0"
  :use [|schema|]}
(fact "batched queries grouped by op and table"
  ^:hidden

  (table:batch {:set {:meat [{:id 1}]
                      :vegetable [{:id 2}]}}
               {:schema |schema|})
  => {:set [[:meat "INSERT INTO \"meat\"\n (\"id\")\n VALUES\n ('1')"]
            [:vegetable "INSERT INTO \"vegetable\"\n (\"id\")\n VALUES\n ('2')"]]})
