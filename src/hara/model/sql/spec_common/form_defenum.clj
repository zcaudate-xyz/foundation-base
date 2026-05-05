(ns hara.model.sql.spec-common.form-defenum
  (:require [clojure.string :as str]
            [hara.model.sql.spec-common.common :as common]
            [hara.common.grammar-spec :as grammar-spec]))

(defn sql-defenum-format
  "formats a sql enum form"
  {:added "4.1"}
  [form]
  (let [[mdefn [op sym values]] (grammar-spec/format-defn form)
        msym (assoc (common/sql-sym-meta sym)
                    :static/dbtype :enum
                    :static/enum-values values)]
    [(merge mdefn msym)
     (list op (with-meta sym msym) values)]))

(defn sql-defenum
  "emits dialect-specific enum support"
  {:added "4.1"}
  [[_ sym values] grammar mopts]
  (let [{:keys [schema]} (meta sym)
        {:keys [comment-prefix enum-mode]
         :or {comment-prefix "--"
              enum-mode :native}} (common/sql-dialect grammar mopts)]
    (case enum-mode
      :comment
      (str comment-prefix
           " ENUM "
           (common/sql-qualified-ident sym schema grammar mopts)
           ": "
           (str/join ", " (common/sql-enum-values values)))

      (str "CREATE TYPE "
           (common/sql-qualified-ident sym schema grammar mopts)
           " AS ENUM ("
           (str/join ", " (common/sql-enum-values values))
           ");"))))
