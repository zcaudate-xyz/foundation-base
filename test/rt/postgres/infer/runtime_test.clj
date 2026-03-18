(ns rt.postgres.infer.runtime-test
  "Tests for rt.postgres.infer.runtime namespace.
   Provides runtime table loading and registration."
  (:use code.test)
  (:require [rt.postgres.infer.runtime :as runtime]
            [rt.postgres.infer.types :as types]))

;; -----------------------------------------------------------------------------
;; Parse Runtime Table Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.runtime/parse-runtime-table :added "0.1"}
(fact "parse-runtime-table parses table from pg/app format"
  (let [entries [:id {:type :uuid :primary true}
                 :name {:type :text}
                 :email {:type :citext}]
        table (runtime/parse-runtime-table :User entries "gwdb.core")]
    (:name table) => "User"
    (:ns table) => "gwdb.core"
    (count (:columns table)) => 3
    (:primary-key table) => :id))

^{:refer rt.postgres.infer.runtime/parse-runtime-table :added "0.1"}
(fact "parse-runtime-table handles ref columns with :link -> :key transformation"
  (let [entries [:id {:type :uuid :primary true}
                 :org-id {:type :ref :foreign {:link {:module :Organisation :id :id}}}]
        table (runtime/parse-runtime-table :User entries "gwdb.core")]
    (:name table) => "User"
    (count (:columns table)) => 2))

^{:refer rt.postgres.infer.runtime/parse-runtime-table :added "0.1"}
(fact "parse-runtime-table uses :id as default primary key"
  (let [entries [:id {:type :uuid}
                 :name {:type :text}]
        table (runtime/parse-runtime-table :Item entries "test.ns")]
    (:primary-key table) => :id))

^{:refer rt.postgres.infer.runtime/parse-runtime-table :added "0.1"}
(fact "parse-runtime-table handles various column types"
  (let [entries [:id {:type :uuid :primary true}
                 :handle {:type :citext :required true}
                 :settings {:type :jsonb}
                 :count {:type :integer}
                 :active {:type :boolean}]
        table (runtime/parse-runtime-table :Test entries "test.ns")]
    (count (:columns table)) => 5
    (-> table :columns first :name) => :id))

;; -----------------------------------------------------------------------------
;; Load Runtime Tables Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.runtime/load-runtime-tables :added "0.1"}
(fact "load-runtime-tables loads multiple tables from map"
  (let [tables-map {:User [:id {:type :uuid :primary true}
                           :name {:type :text}]
                    :Organisation [:id {:type :uuid :primary true}
                                   :handle {:type :citext}]}
        loaded (runtime/load-runtime-tables tables-map)]
    (count loaded) => 2
    (contains? loaded :User) => true
    (contains? loaded :Organisation) => true
    (:name (:User loaded)) => "User"
    (:name (:Organisation loaded)) => "Organisation"))

^{:refer rt.postgres.infer.runtime/load-runtime-tables :added "0.1"}
(fact "load-runtime-tables returns empty map for empty input"
  (runtime/load-runtime-tables {}) => {})

^{:refer rt.postgres.infer.runtime/load-runtime-tables :added "0.1"}
(fact "load-runtime-tables sets namespace on all tables"
  (let [tables-map {:Table1 [:id {:type :uuid}]
                    :Table2 [:id {:type :uuid}]}
        loaded (runtime/load-runtime-tables tables-map)]
    (:ns (:Table1 loaded)) => "gwdb.core"
    (:ns (:Table2 loaded)) => "gwdb.core"))

;; -----------------------------------------------------------------------------
;; Register Runtime Tables Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.runtime/register-runtime-tables! :added "0.1"}
(fact "register-runtime-tables! registers tables in global registry"
  (types/clear-registry!)
  (let [tables {:User (types/make-table-def "gwdb.core" "User"
                                             [{:name :id :type {:name :uuid}}]
                                             :id)
                :Organisation (types/make-table-def "gwdb.core" "Organisation"
                                                     [{:name :id :type {:name :uuid}}]
                                                     :id)}]
    (runtime/register-runtime-tables! tables)
    (types/get-type :User) => (:User tables)
    (types/get-type :Organisation) => (:Organisation tables)))

^{:refer rt.postgres.infer.runtime/register-runtime-tables! :added "0.1"}
(fact "register-runtime-tables! registers both keyword and symbol keys"
  (types/clear-registry!)
  (let [tables {:TestTable (types/make-table-def "gwdb.core" "TestTable"
                                                [{:name :id :type {:name :uuid}}]
                                                :id)}]
    (runtime/register-runtime-tables! tables)
    ;; Should be registered under both :TestTable (keyword) and TestTable (symbol)
    (types/get-type :TestTable) => (:TestTable tables)
    (types/get-type 'TestTable) => (:TestTable tables)))

^{:refer rt.postgres.infer.runtime/register-runtime-tables! :added "0.1"}
(fact "register-runtime-tables! handles empty table map"
  (types/clear-registry!)
  (runtime/register-runtime-tables! {}) 
  => nil)
