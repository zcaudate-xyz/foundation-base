(ns script.sql.table.manage
  (:require [clojure.string]
            [script.sql.common :as common]
            [std.lib.foundation :as f]))

(defn table-create
  "generates create table statement"
  {:added "3.0"}
  ([table specs] (table-create table specs "" common/*options*))
  ([table specs constraints {:keys [table-fn column-fn] :as opts
                             :or {table-fn identity column-fn identity}}]
   (let [stringify      (fn [x] (if (keyword? x) (name x) (str x)))
         spec-to-string (fn [[column & options]]
                          (clojure.string/join " " (cons (column-fn (name column))
                                              (map stringify options))))]
     (format "CREATE TABLE IF NOT EXISTS %s (\n %s%s\n)"
             (table-fn (name table))
             (clojure.string/join ",\n " (map spec-to-string specs))
             constraints))))

(defn table-drop
  "generates drop table statement"
  {:added "3.0"}
  ([table]
   (table-drop table common/*options*))
  ([table {:keys [table-fn] :as opts
           :or {table-fn identity}}]
   (format "DROP TABLE IF EXISTS %s CASCADE"
           (.replaceAll ^String (table-fn (name table)) "\\." "_"))))

(defn single-enum
  "create statements for single enum"
  {:added "3.0"}
  ([enum]
   (single-enum enum common/*options*))
  ([{:keys [ns values] :as enum} {:keys [table-fn column-fn] :as opts
                                  :or  {table-fn identity column-fn identity}}]
   (let [table  (.replaceAll ^String (table-fn (name ns))  "\\." "_")]
     [(str "CREATE TABLE IF NOT EXISTS " table " (value text PRIMARY KEY, comment text)")
      (str "INSERT INTO " table "\n (value, comment)\n VALUES\n "
           (clojure.string/join ",\n " (map (fn [value]
                                   (format "('%s', null)" (name value)))
                                 values)))])))

(defn single-table:column
  "generate statements for table column"
  {:added "3.0"}
  ([input]
   (single-table:column input common/*options*))
  ([[column {:keys [type required unique sql] :as attrs}]
    {:keys [table-fn column-fn] :as opts
     :or {table-fn identity column-fn identity}}]
   (if-not (= type :alias)
     (cond-> [(if (= type :ref)
                (keyword (str (name column) "-id"))
                column)
              (common/sql:type type)]
       required       (conj "NOT NULL")
       unique         (conj "UNIQUE")
       (:primary sql) (conj "PRIMARY KEY")
       (= type :ref)  (conj (format "references %s(%s)"
                                    (column-fn (name (:ns (:ref attrs))))
                                    (column-fn (name (or (:column (:ref attrs))
                                                         :id)))))
       (= type :enum) (conj (format "references %s(%s)"
                                    (.replaceAll ^String (table-fn (name (:ns (:enum attrs))))  "\\." "_")
                                    (column-fn (name (or (:column (:enum attrs))
                                                         :value)))))))))

(defn single-table:constraints
  "generate statements for constraints on composite keys"
  {:added "3.0"}
  ([input]
   (single-table:constraints input common/*options*))
  ([[table values] {:keys [table-fn column-fn] :as opts
                    :or {table-fn identity column-fn identity}}]
   (let [constraint (str (name table) "-pkey")
         composites (->> (partition 2 values)
                         (keep (fn [[column {:keys [sql type]}]]

                                 (if (:composite sql)
                                   (cond-> (name column)
                                     (= type :ref) (str "-id")
                                     :then (column-fn))))))]
     (if-not (empty? composites)
       (format ", CONSTRAINT %s PRIMARY KEY (%s)"
               (table-fn constraint)
               (clojure.string/join ", " composites))
       ""))))

(defn single-table
  "generate create statement for single table"
  {:added "3.0"}
  ([input]
   (single-table input common/*options*))
  ([[table values :as input] opts]
   (let [constraints (single-table:constraints input opts)]
     (f/->> (partition 2 values)
            (keep #(single-table:column % opts))
            (table-create table % constraints opts)))))

(defn parse:enums
  "parses enum entries from schema"
  {:added "3.0"}
  ([schema]
   (->> (:vec schema)
        (partition 2)
        (mapcat second)
        (partition 2)
        (map second)
        (filter (comp #{:enum} :type))
        (map :enum)
        (group-by :ns)
        (vals)
        (map first))))

(defn parse:tables
  "parses table entries from schema"
  {:added "3.0"}
  ([schema]
   (->> (:vec schema)
        (partition 2)
        (keep (fn [[k v]] {:ns k})))))

(defn parse:formats
  "parses format entries from schema"
  {:added "3.0"}
  ([schema]
   (->> (:vec schema)
        (partition 2)
        (keep (fn [[k columns]]
                (let [ts (->> (partition 2 columns)
                              (keep (fn [[col {:keys [sql]}]]
                                      (if-let [t (:format sql)]
                                        [col t]))))]
                  (if-not (empty? ts)
                    [k (into {} ts)]))))
        (into {}))))

(defn parse:aliases
  "parses alias entries from schema"
  {:added "3.0"}
  ([schema]
   (->> (:vec schema)
        (partition 2)
        (keep (fn [[k columns]]
                (let [ts (->> (partition 2 columns)
                              (keep (fn [[col {:keys [type] :as attr}]]
                                      (if (= type :alias)
                                        [col (:alias attr)]))))]
                  (if-not (empty? ts)
                    [k (into {} ts)]))))
        (into {}))))

(defn parse:relationships
  "parses relationships form schema"
  {:added "3.0"}
  ([schema tag]
   (->> (:vec schema)
        (partition 2)
        (mapcat (fn [[table columns]]
                  (keep (fn [[col attrs]]
                          (if (= (:type attrs) tag)
                            {:ref  (-> attrs tag :ns)
                             :rval (-> attrs tag :rval)
                             :table table
                             :column col}))
                        (partition 2 columns)))))))

(defn create:enums
  "generate create statements for schema enums"
  {:added "3.0"}
  ([schema]
   (create:enums schema common/*options*))
  ([schema opts]
   (->> (parse:enums schema)
        (map (juxt (juxt :ns :values)
                   #(single-enum % opts))))))

(defn drop:enums
  "generate drop statements for schema enums
 
   (drop:enums -schema-)"
  {:added "3.0"}
  ([schema]
   (drop:enums schema common/*options*))
  ([schema opts]
   (->> (parse:enums schema)
        (map (juxt (juxt :ns :values)
                   (comp #(table-drop % opts) :ns))))))

(defn create:tables
  "generate create statements for schema tables"
  {:added "3.0"}
  ([schema]
   (create:tables schema common/*options*))
  ([schema opts]
   (->> (:vec schema)
        (partition 2)
        (map (juxt (juxt first #(map first (partition 2 (second %))))
                   #(single-table % opts))))))

(defn drop:tables
  "generate drop statements for schema tables"
  {:added "3.0"}
  ([schema]
   (drop:tables schema common/*options*))
  ([schema opts]
   (->> (parse:tables schema)
        (map (juxt :ns
                   (comp #(table-drop % opts) :ns))))))
