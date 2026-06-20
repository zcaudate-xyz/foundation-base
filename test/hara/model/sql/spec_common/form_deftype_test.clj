(ns hara.model.sql.spec-common.form-deftype-test
  (:use code.test)
  (:require [hara.model.sql.spec-common.form-deftype :refer :all]))

^{:refer hara.model.sql.spec-common.form-deftype/sql-deftype-format :added "4.1"}
(fact "generates SQL deftype format")

^{:refer hara.model.sql.spec-common.form-deftype/sql-deftype :added "4.1"}
(fact "generates SQL deftype")
