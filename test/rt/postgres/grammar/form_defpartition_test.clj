(ns rt.postgres.grammar.form-defpartition-test
  (:use code.test)
  (:require [rt.postgres.grammar.form-defpartition :refer :all]
            [rt.postgres.grammar.common :as common]
            [std.lib :as h]))

(fact "pg-defpartition-format"
  (pg-defpartition-format '(defpartition.pg Rev_Token [-/Token] {:for ["Token"]}))
  => vector?)

(fact "pg-defpartition"
  (with-redefs [common/pg-full-token (fn [s sch] (if sch (str sch "." s) (str s)))]
    (pg-defpartition [nil 'Rev_Token ['-/Token] {:for ["Token"] :partition-by [:list #{"class_table"}]}]))
  => '(do [:create-table :if-not-exists "Rev_Token"
           :partition-of "Token"
           :for :values :in '["Token"]
           :partition-by :list '#{"class_table"}])

  (with-redefs [common/pg-full-token (fn [s sch] (if sch (str sch "." s) (str s)))]
    (pg-defpartition [nil 'My_Range_Part ['-/Parent] {:for '("2020-01-01" "2021-01-01")}]))
  => '(do [:create-table :if-not-exists "My_Range_Part"
           :partition-of "Parent"
           :for :values :from '("2020-01-01") :to '("2021-01-01")]))
