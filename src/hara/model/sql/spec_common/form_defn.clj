(ns hara.model.sql.spec-common.form-defn
  (:require [clojure.string :as str]
            [hara.model.sql.spec-common.common :as common]
            [hara.common.emit-helper :as helper]
            [hara.common.grammar-spec :as grammar-spec]))

(defn sql-defn-format
  "formats a sql defn form"
  {:added "4.1"}
  [form]
  (let [[mdefn [op sym args & body]] (grammar-spec/format-defn form)
        targs (vec (helper/basic-typed-args args))
        msym  (assoc (common/sql-sym-meta sym)
                     :static/input targs
                     :static/dbtype :function)]
    [(merge mdefn msym)
     (apply list op (with-meta sym msym) targs body)]))

(defn- sql-arg-entry
  [arg]
  (cond (symbol? arg)
        [arg (or (-> arg meta :-)
                 [:text])]

        (and (vector? arg)
             (= 2 (count arg)))
        [(second arg) [(first arg)]]

        :else
        [arg [:text]]))

(defn- sql-normalize-args
  [args]
  (let [args (cond (vector? args) args
                   (seq? args)    (vec args)
                   :else          [args])]
    (if (vector? (first args))
      args
      (vec (helper/basic-typed-args args)))))

(defn- sql-arg-str
  [args grammar mopts]
  (->> (sql-normalize-args args)
       (map sql-arg-entry)
       (map (fn [[sym type]]
              (str (common/sql-ident sym grammar mopts)
                   " "
                   (common/sql-type-name type grammar mopts))))
       (str/join ", ")))

(defn sql-defn
  "emits a dialect-specific sql function"
  {:added "4.1"}
  [[_ sym args & body] grammar mopts]
  (let [m       (meta sym)
        return  (or (:static/return m)
                    (:- m)
                    [:text])
        schema  (:schema m)
        fnstr   (common/sql-qualified-ident sym schema grammar mopts)
        argstr  (sql-arg-str args grammar mopts)
        retstr  (common/sql-type-name return grammar mopts)
        bodystr (common/sql-body body grammar mopts)
        {:keys [function-before-body function-prefix function-return-keyword]
         :or {function-prefix "CREATE FUNCTION"
              function-return-keyword "RETURNS"}} (common/sql-dialect grammar mopts)]
    (str function-prefix
         " "
         fnstr
         "(" argstr ")\n"
         function-return-keyword
         " "
         retstr
         (when function-before-body
           (str "\n" function-before-body))
         "\nBEGIN"
         (if (str/blank? bodystr)
           "\nEND;"
           (str "\n"
                (common/sql-indent bodystr 2)
                "\nEND;")))))
