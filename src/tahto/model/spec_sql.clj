(ns tahto.model.spec-sql
  (:require [tahto.model.sql.spec-common :as common]
            [tahto.core.script :as script]))

(def +book+
  (common/build-book :sql
                     :sql
                     common/+dialect-sql+
                     "sql"))

(def +init+
  (script/install +book+))
