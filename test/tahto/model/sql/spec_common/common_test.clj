(ns tahto.model.sql.spec-common.common-test
  (:use code.test)
  (:require [tahto.model.sql.spec-common.common :refer :all]))

^{:refer tahto.model.sql.spec-common.common/sql-dialect :added "4.1"}
(fact "generates SQL dialect")

^{:refer tahto.model.sql.spec-common.common/sql-string :added "4.1"}
(fact "generates SQL string")

^{:refer tahto.model.sql.spec-common.common/sql-ident-base :added "4.1"}
(fact "generates SQL ident base")

^{:refer tahto.model.sql.spec-common.common/sql-ident :added "4.1"}
(fact "generates SQL ident")

^{:refer tahto.model.sql.spec-common.common/sql-qualified-ident :added "4.1"}
(fact "generates SQL qualified ident")

^{:refer tahto.model.sql.spec-common.common/sql-type-name :added "4.1"}
(fact "generates SQL type name")

^{:refer tahto.model.sql.spec-common.common/sql-sym-meta :added "4.1"}
(fact "generates SQL sym meta")

^{:refer tahto.model.sql.spec-common.common/sql-hydrate :added "4.1"}
(fact "generates SQL hydrate")

^{:refer tahto.model.sql.spec-common.common/sql-indent :added "4.1"}
(fact "generates SQL indent")

^{:refer tahto.model.sql.spec-common.common/sql-resolve-entry :added "4.1"}
(fact "generates SQL resolve entry")

^{:refer tahto.model.sql.spec-common.common/sql-enum-entry :added "4.1"}
(fact "generates SQL enum entry")

^{:refer tahto.model.sql.spec-common.common/sql-enum-values-from-type :added "4.1"}
(fact "generates SQL enum values from type")

^{:refer tahto.model.sql.spec-common.common/sql-render :added "4.1"}
(fact "generates SQL render")

^{:refer tahto.model.sql.spec-common.common/sql-body :added "4.1"}
(fact "generates SQL body")

^{:refer tahto.model.sql.spec-common.common/sql-column-spec :added "4.1"}
(fact "generates SQL column spec")

^{:refer tahto.model.sql.spec-common.common/sql-column-name :added "4.1"}
(fact "generates SQL column name")

^{:refer tahto.model.sql.spec-common.common/sql-column-type :added "4.1"}
(fact "generates SQL column type")

^{:refer tahto.model.sql.spec-common.common/sql-reference-target :added "4.1"}
(fact "generates SQL reference target")

^{:refer tahto.model.sql.spec-common.common/sql-reference-column :added "4.1"}
(fact "generates SQL reference column")

^{:refer tahto.model.sql.spec-common.common/sql-column-definition :added "4.1"}
(fact "generates SQL column definition")

^{:refer tahto.model.sql.spec-common.common/sql-enum-values :added "4.1"}
(fact "generates SQL enum values")
