(ns rt.postgres.grammar.form-defpartition-test
  (:use code.test)
  (:require [rt.postgres.grammar.form-defpartition :refer :all]
            [rt.postgres.grammar.common :as common]
            [std.lib :as h]))

(fact "pg-defpartition-format"
  (pg-defpartition-format '(defpartition.pg my-part [:of parent :for ["a"]]))
  => vector?)

(fact "pg-defpartition"
  (with-redefs [common/pg-full-token (fn [s sch] (if sch (str sch "." s) (str s)))]
    (pg-defpartition [nil 'my-part [:of 'parent :for ["a"]] nil]))
  => '(do [:create-table :if-not-exists "my-part"
           :partition-of "parent"
           :for :values :in '["a"]]))
