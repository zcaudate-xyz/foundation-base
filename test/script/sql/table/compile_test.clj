(ns script.sql.table.compile-test
  (:use code.test)
  (:require [script.sql.table.compile :refer :all]
            [std.lib.schema :as schema]
            [std.lib :as h]
            [script.sql.table-test :as table-test]))

(fact:ns
 (:clone script.sql.table-test))

^{:refer script.sql.table.compile/in:fn-map :added "3.0"
  :use [|schema|]}
(fact "constructs an function map for sql input"
  ^:hidden

  (keys (in:fn-map |schema| :meat))
  => (contains [:type :grade] :in-any-order)

  (-> (in:fn-map |schema| :meat)
      :grade
      (h/invoke :bad))
  => [:grade "bad"])

^{:refer script.sql.table.compile/out:fn-map :added "3.0"
  :use [|schema|]}
(fact "constructs a function map for sql output"
  ^:hidden

  (keys (out:fn-map |schema| :meat))
  => (contains [:type :grade] :in-any-order)

  (-> (out:fn-map |schema| :meat)
      :grade
      (h/invoke "bad"))
  => [:grade :bad])

^{:refer script.sql.table.compile/transform:fn :added "3.0"
  :use [|schema|]}
(fact "constructs a data transform function"
  ^:hidden

  ((transform:fn in:fn-map |schema| :meat)
   {:id "a"
    :type :beef
    :amount 100
    :grade :bad})
  => {:id "a", :type ":beef", :amount 100, :grade "bad"}

  ((transform:fn out:fn-map |schema| :meat)
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
  ^:hidden

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
  ^:hidden

  (transform:out {:id "account-0" :wallet-id "w0"}
                 :account
                 {:schema (schema/schema
                           [:account [:id {}
                                      :wallet {:type :ref :ref {:ns :wallet}}]
                            :wallet-access [:wallet {:type :ref}
                                            :account {:type :ref}]])})
  => {:id "account-0", :wallet :wallet.id/w0})
