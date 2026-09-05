(ns lang.model.sql.spec-common.common-test
  (:use code.test)
  (:require [lang.model.sql.spec-common.common :refer :all]))

^{:refer lang.model.sql.spec-common.common/sql-dialect :added "4.1"}
(fact "generates SQL dialect")

^{:refer lang.model.sql.spec-common.common/sql-string :added "4.1"}
(fact "generates SQL string")

^{:refer lang.model.sql.spec-common.common/sql-ident-base :added "4.1"}
(fact "generates SQL ident base")

^{:refer lang.model.sql.spec-common.common/sql-ident :added "4.1"}
(fact "generates SQL ident")

^{:refer lang.model.sql.spec-common.common/sql-qualified-ident :added "4.1"}
(fact "generates SQL qualified ident")

^{:refer lang.model.sql.spec-common.common/sql-type-name :added "4.1"}
(fact "generates SQL type name")

^{:refer lang.model.sql.spec-common.common/sql-sym-meta :added "4.1"}
(fact "generates SQL sym meta")

^{:refer lang.model.sql.spec-common.common/sql-hydrate :added "4.1"}
(fact "generates SQL hydrate")

^{:refer lang.model.sql.spec-common.common/sql-indent :added "4.1"}
(fact "generates SQL indent")

^{:refer lang.model.sql.spec-common.common/sql-resolve-entry :added "4.1"}
(fact "generates SQL resolve entry")

^{:refer lang.model.sql.spec-common.common/sql-enum-entry :added "4.1"}
(fact "generates SQL enum entry")

^{:refer lang.model.sql.spec-common.common/sql-enum-values-from-type :added "4.1"}
(fact "generates SQL enum values from type")

^{:refer lang.model.sql.spec-common.common/sql-render :added "4.1"}
(fact "generates SQL render")

^{:refer lang.model.sql.spec-common.common/sql-body :added "4.1"}
(fact "generates SQL body")

^{:refer lang.model.sql.spec-common.common/sql-column-spec :added "4.1"}
(fact "generates SQL column spec")

^{:refer lang.model.sql.spec-common.common/sql-column-name :added "4.1"}
(fact "generates SQL column name")

^{:refer lang.model.sql.spec-common.common/sql-column-type :added "4.1"}
(fact "generates SQL column type")

^{:refer lang.model.sql.spec-common.common/sql-reference-target :added "4.1"}
(fact "generates SQL reference target")

^{:refer lang.model.sql.spec-common.common/sql-reference-column :added "4.1"}
(fact "generates SQL reference column")

^{:refer lang.model.sql.spec-common.common/sql-column-definition :added "4.1"}
(fact "generates SQL column definition")

^{:refer lang.model.sql.spec-common.common/sql-enum-values :added "4.1"}
(fact "generates SQL enum values")
