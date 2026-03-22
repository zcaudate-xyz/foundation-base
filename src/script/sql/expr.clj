(ns script.sql.expr
  (:require [clojure.string]))

(defn as-?
  "Given a hash map of column names and values, or a vector of column names,
   return a string of `?` placeholders for them."
  {:added "3.0"}
  ([key-map _]
   (clojure.string/join ", " (repeat (count key-map) "?"))))

(defn as-cols
  "Given a sequence of raw column names, return a string of all the
   formatted column names.
 
   If a raw column name is a keyword, apply `:column-fn` to its name,
   from the options if present.
 
   If a raw column name is a vector pair, treat it as an expression with
   an alias. If the first item is a keyword, apply `:column-fn` to its
   name, else accept it as-is. The second item should be a keyword and
   that will have `:column-fn` applied to its name.
 
   This allows columns to be specified as simple names, e.g., `:foo`,
   as simple aliases, e.g., `[:foo :bar]`, or as expressions with an
   alias, e.g., `[\"count(*)\" :total]`."
  {:added "3.0"}
  ([cols opts]
   (let [col-fn (:column-fn opts identity)]
     (clojure.string/join ", " (map (fn [raw]
                           (if (vector? raw)
                             (if (keyword? (first raw))
                               (str (col-fn (name (first raw)))
                                    " AS "
                                    (col-fn (name (second raw))))
                               (str (first raw)
                                    " AS "
                                    (col-fn (name (second raw)))))
                             (col-fn (name raw))))
                         cols)))))

(defn as-keys
  "Given a hash map of column names and values, return a string of all the
   column names.
 
   Applies any `:column-fn` supplied in the options."
  {:added "3.0"}
  ([key-map opts]
   (as-cols (keys key-map) opts)))

(defn by-keys
  "Given a hash map of column names and values and a clause type
   (`:set`, `:where`), return a vector of a SQL clause and its parameters.
 
   Applies any `:column-fn` supplied in the options."
  {:added "3.0"}
  ([key-map clause opts]
   (let [entity-fn      (:column-fn opts identity)
         [where params] (reduce-kv (fn [[conds params] k v]
                                     (let [e (entity-fn (name k))]
                                       (if (and (= :where clause) (nil? v))
                                         [(conj conds (str e " IS NULL")) params]
                                         [(conj conds (str e " = ?")) (conj params v)])))
                                   [[] []]
                                   key-map)]
     (assert (seq where) "key-map may not be empty")
     (into [(str (clojure.string/upper-case (name clause)) " "
                 (clojure.string/join (if (= :where clause) " AND " ", ") where))]
           params))))

(defn for-delete
  "Given a table name and either a hash map of column names and values or a
   vector of SQL (where clause) and its parameters, return a vector of the
   full `DELETE` SQL string and its parameters.
 
   Applies any `:table-fn` / `:column-fn` supplied in the options.
 
   If `:suffix` is provided in `opts`, that string is appended to the
   `DELETE ...` statement."
  {:added "3.0"}
  ([table where-params opts]
   (let [entity-fn    (:table-fn opts identity)
         where-params (if (map? where-params)
                        (by-keys where-params :where opts)
                        (into [(str "WHERE " (first where-params))]
                              (rest where-params)))]
     (into [(str "DELETE FROM " (entity-fn (name table))
                 " " (first where-params)
                 (when-let [suffix (:suffix opts)]
                   (str " " suffix)))]
           (rest where-params)))))

(defn for-insert
  "Given a table name and a hash map of column names and their values,
   return a vector of the full `INSERT` SQL string and its parameters.
 
   Applies any `:table-fn` / `:column-fn` supplied in the options.
 
   If `:suffix` is provided in `opts`, that string is appended to the
   `INSERT ...` statement."
  {:added "3.0"}
  ([table key-map opts]
   (let [entity-fn (:table-fn opts identity)
         params    (as-keys key-map opts)
         places    (as-? key-map opts)]
     (assert (seq key-map) "key-map may not be empty")
     (into [(str "INSERT INTO " (entity-fn (name table))
                 "\n (" params ")"
                 "\n VALUES"
                 "\n (" places ")"
                 (when-let [suffix (:suffix opts)]
                   (str " " suffix)))]
           (vals key-map)))))

(defn for-insert-multi
  "Given a table name, a vector of column names, and a vector of row values
   (each row is a vector of its values), return a vector of the full `INSERT`
   SQL string and its parameters.
 
   Applies any `:table-fn` / `:column-fn` supplied in the options.
 
   If `:suffix` is provided in `opts`, that string is appended to the
   `INSERT ...` statement."
  {:added "3.0"}
  ([table cols rows opts]
   (assert (apply = (count cols) (map count rows))
           "column counts are not consistent across cols and rows")
   ;; to avoid generating bad SQL
   (assert (seq cols) "cols may not be empty")
   (assert (seq rows) "rows may not be empty")
   (let [table-fn  (:table-fn opts identity)
         column-fn (:column-fn opts identity)
         params    (clojure.string/join ", " (map (comp column-fn name) cols))
         places    (as-? (first rows) opts)]
     (into [(str "INSERT INTO " (table-fn (name table))
                 "\n (" params ")"
                 "\n VALUES\n "
                 (clojure.string/join ",\n " (repeat (count rows) (str "(" places ")")))
                 (when-let [suffix (:suffix opts)]
                   (str " " suffix)))]
           cat
           rows))))

(defn for-update
  "Given a table name, a vector of column names to set and their values, and
   either a hash map of column names and values or a vector of SQL (where clause)
   and its parameters, return a vector of the full `UPDATE` SQL string and its
   parameters.
 
   Applies any `:table-fn` / `:column-fn` supplied in the options.
 
   If `:suffix` is provided in `opts`, that string is appended to the
   `UPDATE ...` statement."
  {:added "3.0"}
  ([table key-map where-params opts]
   (let [entity-fn    (:table-fn opts identity)
         set-params   (by-keys key-map :set opts)
         where-params (if (map? where-params)
                        (by-keys where-params :where opts)
                        (into [(str "WHERE " (first where-params))]
                              (rest where-params)))]
     (-> [(str "UPDATE " (entity-fn (name table))
               " " (first set-params)
               " " (first where-params)
               (when-let [suffix (:suffix opts)]
                 (str " " suffix)))]
         (into (rest set-params))
         (into (rest where-params))))))

(defn for-upsert
  "generate upsert command"
  {:added "0.1"}
  ([table key-map opts]
   (let [table-fn  (:table-fn opts identity)
         column-fn (:column-fn opts identity)
         pkeys     (:primary-keys opts [:id])
         params    (as-keys key-map opts)
         places    (as-? key-map opts)
         up-map    (apply dissoc key-map pkeys)
         up-arr    (as-keys up-map opts)
         up-fin    (as-keys up-map (assoc opts :column-fn
                                          (comp #(str "EXCLUDED." %) column-fn)))]
     (assert (seq key-map) "key-map may not be empty")
     (into [(str "INSERT INTO " (table-fn (name table))
                 "\n (" params ")"
                 "\n VALUES (" places ")"
                 "\n ON CONFLICT (" (clojure.string/join "," (map (comp column-fn name) pkeys)) ")"
                 "\n DO UPDATE SET (" up-arr ")"
                 "\n = ROW(" up-fin ")")]
           (vals key-map)))))

(defn for-upsert-multi
  "generate upsert-multi command"
  {:added "0.1"}
  ([table cols rows opts]
   (assert (apply = (count cols) (map count rows))
           "column counts are not consistent across cols and rows")
   (assert (seq cols) "cols may not be empty")
   (assert (seq rows) "rows may not be empty")
   (let [table-fn  (:table-fn opts identity)
         column-fn (:column-fn opts identity)
         columns   (map (comp column-fn name) cols)
         params    (clojure.string/join ", " columns)
         places    (as-? (first rows) opts)
         pkeys     (map (comp column-fn name) (:primary-keys opts [:id]))
         up-arr    (remove #((set pkeys) %) columns)
         up-fin    (map #(str "EXCLUDED." %) up-arr)]
     (into [(str "INSERT INTO " (table-fn (name table))
                 "\n (" params ")"
                 "\n VALUES\n "
                 (clojure.string/join ",\n " (repeat (count rows) (str "(" places ")")))
                 "\n ON CONFLICT (" (clojure.string/join "," pkeys) ")"
                 "\n DO UPDATE SET (" (clojure.string/join ", " up-arr) ")"
                 "\n = ROW(" (clojure.string/join ", " up-fin) ")")]
           cat
           rows))))

(defn for-cas
  "generates cas command"
  {:added "3.0"}
  ([table old-map new-map opts]
   (let [table-fn   (:table-fn opts identity)
         column-fn  (:column-fn opts identity)
         pkeys      (:primary-keys opts [:id])
         params     (as-keys new-map opts)
         places     (as-? new-map opts)
         old-params (by-keys old-map :where opts)
         table-name (table-fn (name table))]
     (assert (seq new-map) "new-map may not be empty")
     (into [(str "DO $$"
                 "\n DECLARE v_id TEXT;"
                 "\n BEGIN"
                 "\n UPDATE " table-name
                 "\n SET (" params ") = (" places ")"
                 "\n WHERE id = (SELECT id FROM " table-name " " (first old-params) " LIMIT 1)"
                 "\n RETURNING id INTO v_id;"
                 "\n IF count(v_id) = 0 THEN"
                 "\n  RAISE EXCEPTION 'Cas Error for (" table ", " (:id old-map) ")';"
                 "\n END IF;"
                 "\nEND; $$")]
           (concat (vals new-map)
                   (vals old-map))))))
