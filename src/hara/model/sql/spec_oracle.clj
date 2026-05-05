(ns hara.model.sql.spec-oracle
  (:require [hara.model.sql.spec-common :as common]
            [hara.lang.script :as script]))

(def +dialect+
  (merge common/+dialect-sql+
         {:bool-literal {true "1"
                         false "0"}
          :enum-column-mode :check
          :enum-column-type "VARCHAR2(255)"
          :enum-mode :comment
          :function-before-body "IS"
          :function-prefix "CREATE OR REPLACE FUNCTION"
          :function-return-keyword "RETURN"
          :type-alias {:array "CLOB"
                       :bigint "NUMBER(19)"
                       :boolean "NUMBER(1)"
                       :double "BINARY_DOUBLE"
                       :float "BINARY_FLOAT"
                       :integer "NUMBER(10)"
                       :json "CLOB"
                       :keyword "VARCHAR2(255)"
                       :long "NUMBER(19)"
                       :map "CLOB"
                       :numeric "NUMBER"
                       :object "CLOB"
                       :string "VARCHAR2(4000)"
                       :text "CLOB"
                       :time "TIMESTAMP"
                       :uuid "VARCHAR2(36)"
                       :void "VOID"}}))

(def +book+
  (common/build-book :oracle
                     :oracle
                     +dialect+
                     "oracle.sql"))

(def +init+
  (script/install +book+))
