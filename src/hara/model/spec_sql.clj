(ns hara.model.spec-sql
  (:require [hara.model.sql.spec-common :as common]
            [hara.lang.script :as script]))

(def +book+
  (common/build-book :sql
                     :sql
                     common/+dialect-sql+
                     "sql"))

(def +init+
  (script/install +book+))
