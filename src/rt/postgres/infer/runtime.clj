(ns rt.postgres.infer.runtime
  "Runtime table integration for rt.postgres.infer.
   
   Parses table definitions from (pg/app ...) runtime output
   and registers them in the type registry."
  (:require [rt.postgres.infer.types :as types]
            [rt.postgres.infer.parse :as parse]))

(defn- transform-col-opts
  "Transforms runtime column opts to deftype.pg format.
   Runtime uses :ref/:link, deftype.pg uses :ref/:key"
  [opts]
  (if-let [link (get-in opts [:ref :link])]
    (assoc opts :ref {:key (keyword (str (:module link)) (name (:id link)))})
    opts))

(defn parse-runtime-table
  "Parses a table from pg/app format.
   Input: [:col1 {:type :uuid} :col2 {:type :ref :foreign {:link {...}}} ...]"
  [table-name table-entries ns-name]
  (let [columns (->> (partition 2 table-entries)
                  (mapv (fn [[col-name opts]]
                          (parse/parse-column-spec
                           [col-name (transform-col-opts opts)]))))
        primary-key (->> columns
                      (filter #(get-in % [:constraints :primary]))
                      first
                      :name
                      (or :id))]
    (types/make-table-def ns-name (name table-name) columns primary-key nil nil)))

(defn load-runtime-tables
  "Loads tables from (pg/app app-name) or an EDN file.
   Input: {:TableName [:col1 {...} :col2 {...}] ...}"
  [tables-map]
  (into {}
        (map (fn [[table-name entries]]
               [table-name (parse-runtime-table table-name entries "gwdb.core")]))
        tables-map))

(defn register-runtime-tables!
  "Registers all runtime tables into the global type registry."
  [runtime-tables]
  (doseq [[table-name table-def] runtime-tables]
    (types/register-type! table-name table-def)
    (types/register-type! (symbol (name table-name)) table-def)))
