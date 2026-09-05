(ns lang.model.spec-sql
  (:require [lang.model.sql.spec-common :as common]
            [lang.core.script :as script]))

(def +book+
  (common/build-book :sql
                     :sql
                     common/+dialect-sql+
                     "sql"))

(def +init+
  (script/install +book+))
