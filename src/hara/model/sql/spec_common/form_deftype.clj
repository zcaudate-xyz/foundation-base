(ns hara.model.sql.spec-common.form-deftype
  (:require [clojure.string :as str]
            [hara.model.sql.spec-common.common :as common]
            [hara.common.grammar-spec :as grammar-spec]))

(defn sql-deftype-format
  "formats a sql table form"
  {:added "4.1"}
  [form]
  (let [[mdefn [op sym spec params]] (grammar-spec/format-defn form)
        msym (assoc (common/sql-sym-meta sym)
                    :static/dbtype :table)]
    [(merge mdefn msym)
     (list op (with-meta sym msym) spec params)]))

(defn sql-deftype
  "emits a dialect-specific sql table"
  {:added "4.1"}
  [[_ sym spec params] grammar mopts]
  (let [{:keys [schema]} (meta sym)
        params    (if (seq? params) (first params) params)
        cols      (->> (common/sql-column-spec spec)
                       (map (fn [entry]
                              (common/sql-column-definition entry grammar mopts))))
        prefix    (cond-> "CREATE TABLE"
                    (:if-not-exists params) (str " IF NOT EXISTS"))
        table-str (common/sql-qualified-ident sym schema grammar mopts)]
    (str prefix
         " "
         table-str
         " (\n"
         (common/sql-indent (str/join ",\n" cols) 2)
         "\n);")))
