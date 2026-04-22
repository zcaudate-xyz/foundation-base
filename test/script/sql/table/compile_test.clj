(ns script.sql.table.compile-test
  (:require [script.sql.table-test :as table-test]
            [script.sql.table.compile :refer :all]
            [std.lib.foundation :as f]
            [std.lib.schema :as schema])
  (:use code.test))

(fact:ns
 (:clone script.sql.table-test))

^{:refer script.sql.table.compile/in:fn-map :added "3.0"}
(fact "constructs an function map for sql input"

  (keys (in:fn-map table-test/|schema| :meat))
  => (contains [:type :grade] :in-any-order)

  (-> (in:fn-map table-test/|schema| :meat)
      :grade
      (f/invoke :bad))
  => [:grade "bad"])

^{:refer script.sql.table.compile/out:fn-map :added "3.0"}
(fact "constructs a function map for sql output"

  (keys (out:fn-map table-test/|schema| :meat))
  => (contains [:type :grade] :in-any-order)

  (-> (out:fn-map table-test/|schema| :meat)
      :grade
      (f/invoke "bad"))
  => [:grade :bad])

^{:refer script.sql.table.compile/transform:fn :added "3.0"}
(fact "constructs a data transform function"

  ((transform:fn in:fn-map table-test/|schema| :meat)
   {:id "a"
    :type :beef
    :amount 100
    :grade :bad})
  => {:id "a", :type ":beef", :amount 100, :grade "bad"}

  ((transform:fn out:fn-map table-test/|schema| :meat)
   {:id "a", :type ":beef", :amount 100, :grade "bad"})
  => {:id "a"
      :type :beef
      :amount 100
      :grade :bad})

^{:refer script.sql.table.compile/transform :added "3.0"}
(fact "constructs a transform function for data pipeline"
  (transform nil nil [] [1 2 3])
  => '(1 2 3))

^{:refer script.sql.table.compile/transform:in :added "3.0"}
(fact "transforms data in"

  (transform:in {:id "account-0" :wallet :wallet.id/w0}
                :account
                {:schema (schema/schema
                          [:account [:id {}
                                     :wallet {:type :ref :ref {:ns :wallet}}]
                           :wallet-access [:wallet {:type :ref}
                                           :account {:type :ref}]])})
  => {:id "account-0", :wallet-id "w0"})

^{:refer script.sql.table.compile/transform:out :added "3.0"}
(fact "transforms data out"

  (transform:out {:id "account-0" :wallet-id "w0"}
                 :account
                 {:schema (schema/schema
                           [:account [:id {}
                                      :wallet {:type :ref :ref {:ns :wallet}}]
                            :wallet-access [:wallet {:type :ref}
                                            :account {:type :ref}]])})
  => {:id "account-0", :wallet :wallet.id/w0})
