(ns hara.model.sql.spec-common.common-test
  (:use code.test)
  (:require [hara.model.sql.spec-common.common :refer :all]))

^{:refer hara.model.sql.spec-common.common/sql-dialect :added "4.1"}
(fact "generates SQL dialect")

^{:refer hara.model.sql.spec-common.common/sql-string :added "4.1"}
(fact "generates SQL string")

^{:refer hara.model.sql.spec-common.common/sql-ident-base :added "4.1"}
(fact "generates SQL ident base")

^{:refer hara.model.sql.spec-common.common/sql-ident :added "4.1"}
(fact "generates SQL ident")

^{:refer hara.model.sql.spec-common.common/sql-qualified-ident :added "4.1"}
(fact "generates SQL qualified ident")

^{:refer hara.model.sql.spec-common.common/sql-type-name :added "4.1"}
(fact "generates SQL type name")

^{:refer hara.model.sql.spec-common.common/sql-sym-meta :added "4.1"}
(fact "generates SQL sym meta")

^{:refer hara.model.sql.spec-common.common/sql-hydrate :added "4.1"}
(fact "generates SQL hydrate")

^{:refer hara.model.sql.spec-common.common/sql-indent :added "4.1"}
(fact "generates SQL indent")

^{:refer hara.model.sql.spec-common.common/sql-resolve-entry :added "4.1"}
(fact "generates SQL resolve entry")

^{:refer hara.model.sql.spec-common.common/sql-enum-entry :added "4.1"}
(fact "generates SQL enum entry")

^{:refer hara.model.sql.spec-common.common/sql-enum-values-from-type :added "4.1"}
(fact "generates SQL enum values from type")

^{:refer hara.model.sql.spec-common.common/sql-render :added "4.1"}
(fact "generates SQL render")

^{:refer hara.model.sql.spec-common.common/sql-body :added "4.1"}
(fact "generates SQL body")

^{:refer hara.model.sql.spec-common.common/sql-column-spec :added "4.1"}
(fact "generates SQL column spec")

^{:refer hara.model.sql.spec-common.common/sql-column-name :added "4.1"}
(fact "generates SQL column name")

^{:refer hara.model.sql.spec-common.common/sql-column-type :added "4.1"}
(fact "generates SQL column type")

^{:refer hara.model.sql.spec-common.common/sql-reference-target :added "4.1"}
(fact "generates SQL reference target")

^{:refer hara.model.sql.spec-common.common/sql-reference-column :added "4.1"}
(fact "generates SQL reference column")

^{:refer hara.model.sql.spec-common.common/sql-column-definition :added "4.1"}
(fact "generates SQL column definition")

^{:refer hara.model.sql.spec-common.common/sql-enum-values :added "4.1"}
(fact "generates SQL enum values")
