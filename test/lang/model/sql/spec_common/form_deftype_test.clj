(ns lang.model.sql.spec-common.form-deftype-test
  (:use code.test)
  (:require [lang.model.sql.spec-common.form-deftype :refer :all]))

^{:refer lang.model.sql.spec-common.form-deftype/sql-deftype-format :added "4.1"}
(fact "generates SQL deftype format")

^{:refer lang.model.sql.spec-common.form-deftype/sql-deftype :added "4.1"}
(fact "generates SQL deftype")
