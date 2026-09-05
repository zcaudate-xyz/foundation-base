(ns lang.model.sql.spec-common.form-defn-test
  (:use code.test)
  (:require [lang.model.sql.spec-common.form-defn :refer :all]))

^{:refer lang.model.sql.spec-common.form-defn/sql-defn-format :added "4.1"}
(fact "generates SQL defn format")

^{:refer lang.model.sql.spec-common.form-defn/sql-defn :added "4.1"}
(fact "generates SQL defn")
