(ns hara.model.sql.spec-oracle-test
  (:require [code.framework :as framework]
            [hara.lang :as l]
            [hara.model.sql.spec-oracle :refer :all]
            [std.block.layout :as layout])
  (:use code.test))

(l/script- :oracle)

(defenum.oracle OracleStatus [:pending :done])

(deftype.oracle OracleAccount
  [:id {:type :uuid :primary true}
   :status {:type OracleStatus :default :pending}])

(defn.oracle ^{:- [:integer]}
  oracle-add-values
  [:integer lhs :integer rhs]
  (return (+ lhs rhs)))

(fact "emit Oracle enum support as a comment"
  (l/emit-as :oracle '[(defenum Status [:pending :done])])
  => "-- ENUM \"Status\": 'pending', 'done'")

(fact "emit Oracle table with enum check"
  (l/emit-as :oracle '[(deftype Account
                         [:id {:type :uuid :primary true}
                          :status {:type OracleStatus :default :pending}])])
  => "CREATE TABLE \"Account\" (\n  \"id\" VARCHAR2(36) PRIMARY KEY,\n  \"status\" VARCHAR2(255) DEFAULT 'pending' CHECK (\"status\" IN ('pending', 'done'))\n);")

(fact "emit Oracle function syntax"
  (l/emit-as :oracle '[(defn ^{:- [:integer]}
                         add-values
                         [:integer lhs :integer rhs]
                         (return (+ lhs rhs)))])
  => "CREATE OR REPLACE FUNCTION \"add_values\"(\"lhs\" NUMBER(10), \"rhs\" NUMBER(10))\nRETURN NUMBER(10)\nIS\nBEGIN\n  RETURN lhs + rhs;\nEND;")

(fact "registers oracle forms in top-level helpers"
  [(boolean ('defn.oracle framework/*toplevel-forms*))
   (boolean ('deftype.oracle framework/*toplevel-forms*))
   (boolean ('defenum.oracle framework/*toplevel-forms*))
   (boolean (layout/+defs+ 'defn.oracle))
   (boolean (layout/+defs+ 'deftype.oracle))
   (boolean (layout/+defs+ 'defenum.oracle))]
  => [true true true true true true])
