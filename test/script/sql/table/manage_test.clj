(ns script.sql.table.manage-test
  (:use code.test)
  (:require [script.sql.table.manage :refer :all]
            [std.lib.schema :as schema]
            [std.string :as str]))

(def -schema-
  (schema/schema
   [:meat      [:id     {:sql {:primary true}}
                :type   {}
                :amount {:type :int}
                :grade  {:type :enum
                         :enum {:ns :meat.grade
                                :values #{:good :bad :ok :nasty :fair :horrible :awesome}}}]
    :vegetable [:id     {:sql {:primary true}}
                :cost   {:type :int}
                :grade  {:type :real}]]))

^{:refer script.sql.table.manage/table-create :added "3.0"}
(fact "generates create table statement"
  ^:hidden
  
  (table-create :user-access [[:id :text "PRIMARY KEY"]])
  => (str/|
      "CREATE TABLE IF NOT EXISTS \"user_access\" ("
      " \"id\" text PRIMARY KEY"
      ")"))

^{:refer script.sql.table.manage/table-drop :added "3.0"}
(fact "generates drop table statement"
  ^:hidden
  
  (table-drop :user-access)
  => "DROP TABLE IF EXISTS \"user_access\" CASCADE")

^{:refer script.sql.table.manage/single-enum :added "3.0"}
(fact "create statements for single enum"
  ^:hidden
  
  (single-enum {:ns :user.plan :values #{:free :pro}})
  => ["CREATE TABLE IF NOT EXISTS \"user_plan\" (value text PRIMARY KEY, comment text)"
      (str/|
       "INSERT INTO \"user_plan\""
       " (value, comment)"
       " VALUES"
       " ('free', null),"
       " ('pro', null)")])

^{:refer script.sql.table.manage/single-table:column :added "3.0"}
(fact "generate statements for table column"
  ^:hidden

  (single-table:column [:id {:required true :unique true}])
  => [:id :text "NOT NULL" "UNIQUE"]

  (single-table:column [:id {:type :string :sql {:primary true}}])
  => [:id :text "PRIMARY KEY"])

^{:refer script.sql.table.manage/single-table:constraints :added "3.0"}
(fact "generate statements for constraints on composite keys"
  ^:hidden

  (single-table:constraints [:wallet-access
                             [:wallet     {:type :ref :ref {:ns :wallet}
                                           :sql {:composite true}}
                              :account    {:type :ref :ref {:ns :account}
                                           :sql {:composite true}}]])
      => ", CONSTRAINT \"wallet_access_pkey\" PRIMARY KEY (\"wallet_id\", \"account_id\")")

^{:refer script.sql.table.manage/single-table :added "3.0"}
(fact "generate create statement for single table"
  ^:hidden
  
  (single-table
   [:user [:id      {:required true :unique true}
           :profile {:type :ref :ref {:ns :profile}}]])
  => (str/|
      "CREATE TABLE IF NOT EXISTS \"user\" ("
      " \"id\" text NOT NULL UNIQUE,"
      " \"profile_id\" text references \"profile\"(\"id\")"
      ")")  
  
  (single-table
   [:wallet-access
    [:wallet     {:type :ref :ref {:ns :wallet}
                  :sql {:composite true}}
     :account    {:type :ref :ref {:ns :account}
                  :sql {:composite true}}]])
  => (str/|
      "CREATE TABLE IF NOT EXISTS \"wallet_access\" ("
      " \"wallet_id\" text references \"wallet\"(\"id\"),"
      " \"account_id\" text references \"account\"(\"id\"), CONSTRAINT \"wallet_access_pkey\" PRIMARY KEY (\"wallet_id\", \"account_id\")"
      ")"))

^{:refer script.sql.table.manage/parse:enums :added "3.0"}
(fact "parses enum entries from schema"
  ^:hidden

  (parse:enums
   {:vec [:user
          [:plan {:type :enum
                  :enum {:ns :plan :values #{:pro :free}}}]]})
  => [{:ns :plan, :values #{:free :pro}}])

^{:refer script.sql.table.manage/parse:tables :added "3.0"}
(fact "parses table entries from schema"
  ^:hidden

  (parse:tables
   {:vec [:user    [:id {:required true :unique true}]
          :profile [:id {:required true :unique true}]]})
  => [{:ns :user} {:ns :profile}])

^{:refer script.sql.table.manage/parse:formats :added "3.0"}
(fact "parses format entries from schema"
  ^:hidden

  (parse:formats
   {:vec [:login
          [:value {:type :keyword
                   :sql {:format :edn}}]]})
  => {:login {:value :edn}})

^{:refer script.sql.table.manage/parse:aliases :added "3.0"}
(fact "parses alias entries from schema"
  ^:hidden

  (parse:aliases
   {:vec [:wallet-access
          [:id  {:type :alias
                 :alias {:type :compound
                         :keys [:wallet :account]}}]]})
  => {:wallet-access {:id {:type :compound, :keys [:wallet :account]}}})

^{:refer script.sql.table.manage/parse:relationships :added "3.0"}
(fact "parses relationships form schema" ^:hidden

  (parse:relationships -schema- :enum)
  => '({:ref :meat.grade, :rval nil, :table :meat, :column :grade})

  (parse:relationships -schema- :ref)
  => ())

^{:refer script.sql.table.manage/create:enums :added "3.0"}
(fact "generate create statements for schema enums"
  ^:hidden

  (->> (create:enums -schema-)
       (map second)
       first)
  => (contains
      ["CREATE TABLE IF NOT EXISTS \"meat_grade\" (value text PRIMARY KEY, comment text)"
       (str/|
        "INSERT INTO \"meat_grade\""
        " (value, comment)"
        " VALUES"
        " ('horrible', null),"
        " ('good', null),"
        " ('awesome', null),"
        " ('ok', null),"
        " ('bad', null),"
        " ('nasty', null),"
        " ('fair', null)")]))

^{:refer script.sql.table.manage/drop:enums :added "3.0"}
(fact "generate drop statements for schema enums"

  (drop:enums -schema-) ^:hidden
  => '([[:meat.grade #{:horrible :good :awesome :ok :bad :nasty :fair}]
        "DROP TABLE IF EXISTS \"meat_grade\" CASCADE"]))

^{:refer script.sql.table.manage/create:tables :added "3.0"}
(fact "generate create statements for schema tables"
  ^:hidden
  
  (->> (create:tables -schema-)
       (map second))
  => [(std.string/|
       "CREATE TABLE IF NOT EXISTS \"meat\" ("
       " \"id\" text PRIMARY KEY,"
       " \"type\" text,"
       " \"amount\" int,"
       " \"grade\" text references \"meat_grade\"(\"value\")"
       ")")       
      
      (std.string/|
       "CREATE TABLE IF NOT EXISTS \"vegetable\" ("
       " \"id\" text PRIMARY KEY,"
       " \"cost\" int,"
       " \"grade\" real"
       ")")])

^{:refer script.sql.table.manage/drop:tables :added "3.0"}
(fact "generate drop statements for schema tables" ^:hidden

  (->> (drop:tables -schema-)
       (map second))
  => ["DROP TABLE IF EXISTS \"meat\" CASCADE"
      "DROP TABLE IF EXISTS \"vegetable\" CASCADE"])

(comment
  (./import))
