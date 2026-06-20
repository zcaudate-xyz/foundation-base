(ns hara.model.sql.spec-common.form-defenum-test
  (:use code.test)
  (:require [hara.model.sql.spec-common.form-defenum :refer :all]))

^{:refer hara.model.sql.spec-common.form-defenum/sql-defenum-format :added "4.1"}
(fact "generates SQL defenum format")

^{:refer hara.model.sql.spec-common.form-defenum/sql-defenum :added "4.1"}
(fact "generates SQL defenum")
