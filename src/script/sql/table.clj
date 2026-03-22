(ns script.sql.table
  (:require [script.sql.common :as common]
            [script.sql.expr :as expr]
            [script.sql.table.compile :as compile]
            [script.sql.table.select :as select]
            [std.lib.foundation]
            [std.lib.invoke :refer [definvoke]]
            [std.string.common]))

(defn sql-tmpl
  "creating sql functions"
  {:added "4.0"}
  ([fsym var]
   (let [{:keys [arglists]} (meta var)
         args (butlast (first arglists))]
     `(defn ~fsym
        ([~@args]
         (~fsym ~@args common/*options*))
        ([~@args ~'opts]
         (->> (~(std.lib.foundation/var-sym var) ~@args ~'opts)
              (apply common/sql:format)))))))

(std.lib.foundation/template-vars [sql-tmpl]
  (sql:insert expr/for-insert)
  (sql:delete expr/for-delete)
  (sql:update expr/for-update)
  (sql:upsert expr/for-upsert)
  (sql:insert-multi expr/for-insert-multi)
  (sql:upsert-multi expr/for-upsert-multi)
  (sql:cas    expr/for-cas))

(defn table-common-options
  "returns the common options"
  {:added "4.0"}
  []
  common/*options*)

(defn schema:order
  "produces an ordered list of data from schema"
  {:added "3.0"}
  ([schema data]
   (->> (take-nth 2 (:vec schema))
        (keep (fn [k] (if-let [val (get data k)]
                        [k val]))))))

(defn schema:ids:alias
  "retreives the alias for a given id"
  {:added "3.0"}
  ([schema table ids]
   (let [tsch (get-in schema [:tree table])]
     (mapv (fn [id]
             (let [[{:keys [type] :as attr}] (get tsch id)
                   id-name (if (= :ref type)
                             (str (name id) "-id")
                             (name id))]
               (keyword id-name)))
           ids))))

(defn schema:ids
  "constructs an access vector for ids"
  {:added "3.0"}
  ([schema table]
   (let [[{:keys [type] :as attr}] (get-in schema [:tree table :id])]
     (if (= :alias type)
       (schema:ids:alias schema table (-> attr :alias :keys))
       [:id]))))

(defn table:query:id
  "constructs an sql query"
  {:added "3.0"}
  ([table id]
   (table:query:id table id {}))
  ([table id opts]
   (let [{:keys [table-fn] :as opts
          :or {table-fn identity}} (merge common/*options* opts)]
     (format "SELECT * FROM %s WHERE id = %s"
             (table-fn (name table))
             (common/sql:entry id)))))

(def ^{:arglists '([schema table])} table-keys
  (memoize (fn [schema table]
             (->> (get-in schema [:tree table])
                  (keep (fn [[k [{:keys [type order]}]]]
                          (if order
                            [(if (= type :ref)
                               (keyword (str (name k) "-id"))
                               k)
                             order])))
                  (sort-by (fn [[k order]] order))
                  (map first)))))

(defn table:update
  "constructs an update query
 
   (table:update :user {:id \"id-0\" :name \"User-0\"})
   => \"UPDATE \\\"user\\\" SET \\\"name\\\" = 'User-0' WHERE \\\"id\\\" = 'id-0'\""
  {:added "3.0"}
  ([table data]
   (table:update table data {}))
  ([table data opts]
   (binding [compile/*skip* (:raw opts)]
     (let [opts  (merge common/*options* opts)
           id-params (select-keys data [:id])
           data  (-> (compile/transform:in data table opts)
                     (dissoc :id))]
       (->> (expr/for-update table data id-params opts)
            (apply common/sql:format))))))

(defn table-compile
  "compiles a table in the schema"
  {:added "4.0"}
  [f table values {:keys [schema raw] :as opts}]
  (let [values (map #(compile/transform:in % table opts) values)
        data   (first values)
        ks     (->> (table-keys schema table)
                    (filter (partial contains? data)))
        inputs (mapv #(map % ks) values)]
    (->> (f table ks inputs opts)
         (apply common/sql:format))))

(defn table-batch
  "helper function for batch calls"
  {:added "3.0"}
  ([f m {:keys [schema raw] :as opts}]
   (binding [compile/*skip* raw]
     (->> (schema:order schema m)
          (mapv (fn [[table values]]
                  [table (table-compile f table values opts)]))))))

(defn table:put:batch
  "constructs a batch upsert statement"
  {:added "3.0"}
  ([m]
   (table:put:batch m {}))
  ([m {:keys [schema] :as opts}]
   (table-batch expr/for-upsert-multi m (merge common/*options* opts))))

(defn table:put:single
  "constructs a single upsert statement"
  {:added "3.0"}
  ([table data]
   (table:put:single table data {}))
  ([table data {:keys [schema raw] :as opts}]
   (binding [compile/*skip* raw]
     (let [opts (merge common/*options* opts)
           data (compile/transform:in data table opts)
           ids  (schema:ids schema table)]
       (->> (expr/for-upsert table data (assoc opts
                                               :primary :id
                                               :primary-keys ids))
            (apply common/sql:format))))))

(defn table:set:batch
  "constructs a batch insert statement"
  {:added "3.0"}
  ([m]
   (table:set:batch m {}))
  ([m {:keys [schema] :as opts}]
   (table-batch expr/for-insert-multi m (merge common/*options* opts))))

(defn table:set:single
  "constructs a single insert statement"
  {:added "3.0"}
  ([table data]
   (table:set:single table data {}))
  ([table data {:keys [schema raw] :as opts}]
   (binding [compile/*skip* raw]
     (let [opts (merge common/*options* opts)
           data (compile/transform:in data table opts)]
       (->> (expr/for-insert table data opts)
            (apply common/sql:format))))))

(defn table:delete
  "constructs a delete statement"
  {:added "3.0"}
  ([table id-or-data]
   (table:set:single table id-or-data {}))
  ([table id-or-data {:keys [schema raw] :as opts}]
   (let [opts (merge common/*options* opts)
         params (cond (map? id-or-data) id-or-data
                      :else {:id id-or-data})]
     (->> (expr/for-delete table params opts)
          (apply common/sql:format)))))

(defn table:keys
  "constructs a key search statement
 
   (table:keys :meat {:schema |schema|})
   => \"SELECT \\\"id\\\" FROM \\\"meat\\\"\""
  {:added "3.0"}
  ([table]
   (table:keys table {}))
  ([table opts]
   (let [opts (merge common/*options* opts)
         {:keys [schema table-fn column-fn]
          :or {table-fn identity
               column-fn identity}} opts
         [{:keys [type] :as attr}] (get-in schema [:tree table :id])
         tabstr  (table-fn (name table))
         columns (map (comp column-fn name) (schema:ids schema table))
         colstr  (std.string.common/join "," columns)]
     (format "SELECT %s FROM %s" colstr tabstr))))

(defn table:clear
  "constructs a clear table statement"
  {:added "3.0"}
  ([table]
   (table:clear table {}))
  ([table opts]
   (let [{:keys [table-fn]
          :or {table-fn identity}} (merge common/*options* opts)]
     (str "DELETE FROM " (table-fn (name table))))))

(defn table:select
  "constructs sql select statement"
  {:added "3.0"}
  ([table query]
   (table:select table query {}))
  ([table query {:keys [schema alias] :as opts}]
   (let [opts (merge common/*options* opts)
         query (if query (compile/transform:in query table opts))]
     (select/sql:query (cond-> {:select (or (:return opts) "*")
                                :from table}
                         query (assoc :where query)
                         :then (merge (dissoc opts :schema :alias)))
                       opts))))

(defn from-string
  "converts a schema value from string
 
   (from-string \"hello\" :ref :account)
   => :account.id/hello"
  {:added "3.0"}
  ([value type table]
   (case type
     :ref  (keyword (str (name table) ".id") value)
     :enum (keyword value)
     :string value
     (throw (ex-info "Not implemented" {:type type :table table :value value})))))

(defn from-alias
  "converts an alias to expanded map"
  {:added "3.0"}
  ([k value table schema alias]
   (let [{:keys [type keys sep]} (get-in alias [table k])
         idx-fn (fn [^String v ^String sep] (.indexOf v sep))
         idx (idx-fn value (or sep "."))]
     (->> (map (fn [k v]
                 (let [[{:keys [type] :as attr}] (get-in schema [:tree table k])]
                   [k (from-string v type table)]))
               keys
               [(subs value 0 idx) (subs value (inc idx))])
          (into {})))))

(defn table:get
  "constructs an sql get statement"
  {:added "3.0"}
  ([table id-or-data]
   (table:get table id-or-data {}))
  ([table id-or-data {:keys [schema alias] :as opts}]
   (let [tsch (get-in schema [:tree table])
         clauses (-> (cond (map? id-or-data) id-or-data
                           :else
                           (let [[{:keys [type] :as attr}] (get tsch :id)]
                             (if (= :alias type)
                               (from-alias :id id-or-data table schema alias)
                               {:id id-or-data}))))]
     (table:select table clauses opts))))

(defn table:cas
  "constructs a cas statement"
  {:added "3.0"}
  ([table old new]
   (table:cas old new {}))
  ([table old new {:keys [raw] :as opts}]
   (binding [compile/*skip* raw]
     (let [opts     (merge common/*options* opts)
           old-data (compile/transform:in old table opts)
           new-data (compile/transform:in new table opts)]
       (->> (expr/for-cas table old-data new-data opts)
            (apply common/sql:format))))))

(defn table:count
  "constructs an sql count statement"
  {:added "3.0"}
  ([table query]
   (table:count table query {}))
  ([table query opts]
   (table:select table query (merge opts {:return "count(*)" :raw true}))))

(definvoke order-keys
  "order keys in the schema
 
   (order-keys (:vec |schema|))
   => {:meat 0, :vegetable 1}"
  {:added "3.0"}
  [:memoize]
  ([schema]
   (-> (take-nth 2 schema)
       (zipmap (range)))))

(defn order-data
  "orders data based on schema
 
   (order-data [:account {}
                :wallet  {}]
               {:account {}
                :wallet {}})
   => [[:account {}] [:wallet {}]]"
  {:added "3.0"}
  ([schema data]
   (let [lu (order-keys schema)]
     (sort-by (comp lu first) (seq data)))))

(defn table:batch
  "batched queries grouped by op and table"
  {:added "3.0"}
  ([input]
   (table:batch input {}))
  ([{:keys [put update set cas delete] :as input} {:keys [schema] :as opts}]
   (let [opts       (merge common/*options* opts)
         del-sql    (if delete
                      (mapv (fn [[table ids]]
                              (->> ids
                                   (mapv #(table:delete table % opts))
                                   (cons table)))
                            (reverse (order-data (-> schema :vec) delete))))
         cas-sql    (if cas
                      (mapv (fn [[table arr]]
                              (->> arr
                                   (mapv #(table:cas table (first %) (second %) opts))
                                   (cons table)))
                            (order-data (-> schema :vec) cas)))
         update-sql (if update
                      (mapv (fn [[table arr]]
                              (->> arr
                                   (mapv #(table:update table % opts))
                                   (cons table)))
                            (order-data (-> schema :vec) update)))
         put-sql    (if put (table:put:batch put opts))

         set-sql    (if set (table:set:batch set opts))]
     (cond-> {}
       delete (assoc :delete del-sql)
       cas (assoc :cas cas-sql)
       update (assoc :update update-sql)
       put (assoc :put put-sql)
       set (assoc :set set-sql)))))
