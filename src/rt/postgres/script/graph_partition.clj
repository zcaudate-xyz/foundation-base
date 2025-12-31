(ns rt.postgres.script.graph-partition
  (:require [rt.postgres.grammar.common-application :as app]
            [rt.postgres.grammar.common :as common]
            [std.string :as str]
            [std.lib :as h]
            [std.lang :as l]))

(defn resolve-schema
  "Resolves the schema for a given symbol or context"
  [sym]
  (let [ns-part (namespace sym)]
    (if (or (nil? ns-part) (= ns-part "-"))
      (if-let [app-context (try (app/app (first (:application (:static (l/rt:module :postgres)))))
                                (catch Exception _ nil))]
        (:schema app-context)
        "szn_type") ;; Default fallback if no app context
      ns-part)))

(defn quote-ident
  "Quotes a postgres identifier"
  [s]
  (str "\"" (str/replace (str s) "\"" "\"\"") "\""))

(defn quote-literal
  "Quotes a postgres literal"
  [s]
  (str "'" (str/replace (str s) "'" "''") "'"))

(defn generate-partition-sql
  "Generates SQL statements recursively"
  [parent-full-name root-name root-schema suffixes specs]
  (let [current-spec (first specs)
        remaining-specs (rest specs)]
    (if current-spec
      (if (:default current-spec)
         ;; Default Partition
         (let [new-suffixes (cons "default" suffixes)
               table-name (str root-name "_" "default" (if (seq suffixes) (str "_" (str/join "_" suffixes)) ""))
               full-table-name (str (quote-ident root-schema) "." (quote-ident table-name))]
            [(format "CREATE TABLE IF NOT EXISTS %s PARTITION OF %s DEFAULT;"
                     full-table-name
                     parent-full-name)])
         ;; List Partition
         (let [{:keys [use in]} current-spec]
            (mapcat (fn [val]
                      (let [val-str (name val)
                            ;; Naming Convention based on observation:
                            ;; Level 1 (Rev_user): Root + "_" + val
                            ;; Level 2 (Rev_ChatChannel_user): Root + "_" + val + "_" + prev_val
                            new-suffixes (cons val-str suffixes)
                            table-name (str root-name "_" (str/join "_" new-suffixes))
                            full-table-name (str (quote-ident root-schema) "." (quote-ident table-name))

                            ;; Partition By Clause for the next level
                            next-spec (first remaining-specs)
                            partition-by-clause (if next-spec
                                                  (let [col-name (if (= :class-table (:use next-spec))
                                                                    "class_table"
                                                                    (str/snake-case (name (:use next-spec))))]
                                                    (format " PARTITION BY LIST (%s)" (quote-ident col-name)))
                                                  "")]
                        (cons (format "CREATE TABLE IF NOT EXISTS %s PARTITION OF %s FOR VALUES IN (%s)%s;"
                                      full-table-name
                                      parent-full-name
                                      (quote-literal val-str)
                                      partition-by-clause)
                              (generate-partition-sql full-table-name
                                                      root-name
                                                      root-schema
                                                      new-suffixes
                                                      remaining-specs))))
                    in)))
      [])))

(defn defpartition-pg-fn
  "Function to generate partition definitions"
  [&form sym [parent] specs]
  (let [parent-sym (if (vector? parent) (first parent) parent) ;; Handle [-/Rev] input
        parent-name (name parent-sym)
        schema (resolve-schema parent-sym)
        full-parent-name (str (quote-ident schema) "." (quote-ident parent-name))

        sql-statements (generate-partition-sql full-parent-name
                                               parent-name
                                               schema
                                               []
                                               specs)]
    `(def ~sym ~(vec sql-statements))))

(defmacro defpartition.pg
  "Macro to define postgres partitions"
  [sym [parent] specs]
  (defpartition-pg-fn &form sym [parent] specs))
