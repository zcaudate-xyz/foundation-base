(ns hara.model.spec-sql-test
  (:require [code.framework :as framework]
            [hara.lang :as l]
            [hara.model.spec-sql :refer :all]
            [std.block.layout :as layout])
  (:use code.test))

(l/script- :sql)

(defenum.sql SqlStatus [:pending :done])

(deftype.sql SqlAccount
  [:id {:type :uuid :primary true}
   :status {:type SqlStatus :default :pending}])

(defn.sql ^{:- [:integer]}
  sql-add-values
  [:integer lhs :integer rhs]
  (return (+ lhs rhs)))

(fact "emit minimal SQL enum"
  (l/emit-as :sql '[(defenum Status [:pending :done])])
  => "CREATE TYPE \"Status\" AS ENUM ('pending', 'done');")

(fact "emit minimal SQL table"
  (l/emit-as :sql '[(deftype Account
                      [:id {:type :uuid :primary true}
                       :name {:type :text :required true}
                       :status {:type Status :default :pending}])])
  => "CREATE TABLE \"Account\" (\n  \"id\" UUID PRIMARY KEY,\n  \"name\" TEXT NOT NULL,\n  \"status\" \"Status\" DEFAULT 'pending'\n);")

(fact "emit minimal SQL function"
  (l/emit-as :sql '[(defn ^{:- [:integer]}
                      add-values
                      [:integer lhs :integer rhs]
                      (return (+ lhs rhs)))])
  => "CREATE FUNCTION \"add_values\"(\"lhs\" INTEGER, \"rhs\" INTEGER)\nRETURNS INTEGER\nBEGIN\n  RETURN lhs + rhs;\nEND;")

(fact "registers sql forms in top-level helpers"
  [(boolean ('defn.sql framework/*toplevel-forms*))
   (boolean ('deftype.sql framework/*toplevel-forms*))
   (boolean ('defenum.sql framework/*toplevel-forms*))
   (boolean (layout/+defs+ 'defn.sql))
   (boolean (layout/+defs+ 'deftype.sql))
   (boolean (layout/+defs+ 'defenum.sql))]
  => [true true true true true true])
