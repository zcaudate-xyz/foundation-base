(ns lib.supabase.query
  (:require [clojure.string :as str]
            [lib.supabase.common :as common])
  (:refer-clojure :exclude [update]))

(defn url-encode [s]
  (java.net.URLEncoder/encode (str s) "UTF-8"))

(defn append-query [route params]
  (if (empty? params)
    route
    (str route
         (if (str/includes? route "?") "&" "?")
         (str/join "&"
                   (map (fn [[k v]]
                          (str (url-encode k) "=" (url-encode v)))
                        params)))))

(defn entry-meta [entry]
  (let [entry (if (instance? clojure.lang.IDeref entry)
                @entry
                entry)]
    {:id (cond (map? entry) (or (:id entry) (get entry "id"))
               :else entry)
     :schema (or (:static/schema entry)
                 (get entry "static/schema")
                 (:schema entry)
                 (get entry "schema"))}))

(defn table-name [table]
  (let [{:keys [id]} (entry-meta table)]
    (cond (string? id) id
          (keyword? id) (name id)
          (symbol? id) (name id)
          :else (str id))))

(defn schema-headers [table]
  (when-let [schema (:schema (entry-meta table))]
    {"Content-Profile" schema}))

(defn filter-clause [value]
  (cond (string? value) value
        (and (vector? value)
             (= 2 (count value))) (let [[op v] value]
                                    (str (name op) "." v))
        :else (str value)))

(defn query-params [{:keys [select filters order limit offset]}]
  (vec
   (concat
    [["select" (or select "*")]]
    (map (fn [[k v]] [(name k) (filter-clause v)]) filters)
    (map (fn [[k dir]]
           ["order" (str (name k) "." (name (or dir :asc)))])
         order)
    (when limit [["limit" limit]])
    (when offset [["offset" offset]]))))

(defn api-select-all
  "Selects all rows from a table through PostgREST."
  {:added "4.1.4"}
  [table & [opts]]
  (let [route (str "/rest/v1/" (table-name table) "?select=*")]
    (common/api-call (merge opts
                            {:method :get
                             :headers (schema-headers table)
                             :route route})
                     {})))

(defn select
  "Selects rows from a table."
  {:added "4.1.4"}
  [client table & [opts]]
  (let [opts (or opts {})
        route (append-query (str "/rest/v1/" (table-name table))
                            (query-params opts))]
    (common/api-call (merge opts
                            {:client client
                             :method :get
                             :headers (merge (schema-headers table)
                                             (:headers opts))
                             :route route})
                     {})))

(defn prefer-header [{:keys [returning count upsert?]}]
  (->> [(when returning (str "return=" (name returning)))
        (when count (str "count=" (name count)))
        (when upsert? "resolution=merge-duplicates")]
       (remove nil?)
       seq
       (str/join ",")))

(defn write-op [client method table rows opts]
  (let [opts (or opts {})
        headers (merge (schema-headers table)
                       (:headers opts)
                       (when-let [prefer (prefer-header opts)]
                         {"Prefer" prefer}))]
    (common/api-call (merge opts
                            {:client client
                             :method method
                             :headers headers
                             :route (str "/rest/v1/" (table-name table))})
                     rows)))

(defn insert
  "Inserts rows through PostgREST."
  {:added "4.1.4"}
  [client table rows & [opts]]
  (write-op client :post table rows opts))

(defn upsert
  "Upserts rows through PostgREST."
  {:added "4.1.4"}
  [client table rows & [opts]]
  (write-op client :post table rows (assoc (or opts {}) :upsert? true)))

(defn update
  "Updates rows through PostgREST."
  {:added "4.1.4"}
  [client table values & [opts]]
  (let [opts (or opts {})
        route (append-query (str "/rest/v1/" (table-name table))
                            (map (fn [[k v]] [(name k) (filter-clause v)])
                                 (:filters opts)))]
    (common/api-call (merge opts
                            {:client client
                             :method :patch
                             :headers (merge (schema-headers table)
                                             (:headers opts)
                                             (when-let [prefer (prefer-header opts)]
                                               {"Prefer" prefer}))
                             :route route})
                     values)))

(defn delete
  "Deletes rows through PostgREST."
  {:added "4.1.4"}
  [client table & [opts]]
  (let [opts (or opts {})
        route (append-query (str "/rest/v1/" (table-name table))
                            (map (fn [[k v]] [(name k) (filter-clause v)])
                                 (:filters opts)))]
    (common/api-call (merge opts
                            {:client client
                             :method :delete
                             :headers (merge (schema-headers table)
                                             (:headers opts))
                             :route route})
                     {})))
