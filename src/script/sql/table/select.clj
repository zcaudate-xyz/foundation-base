(ns script.sql.table.select
  (:require [clojure.string]
            [script.sql.common :as common]
            [std.lib.foundation :as f]))

(defn build-query-columns
  "build column string
 
   (build-query-columns [:id :value] {:column-fn identity})
   => \"id, value\""
  {:added "3.0"}
  ([columns opts]
   (let [column-fn (:column-fn opts identity)]
     (->> columns
          (map (fn [arg]
                 (cond (string? arg) arg
                       (keyword? arg) (column-fn (name arg))
                       :else (throw (ex-info "Cannot build columns" {:input arg})))))
          (clojure.string/join ", ")))))

(defn build-query-select
  "build query string
 
   (build-query-select \"select\" nil {})
   => \"select *\"
 
   (build-query-select \"select\" [:id :value] {})
   => \"select id, value\""
  {:added "3.0"}
  ([q-str select opts]
   (cond (nil? select)
         (str q-str " *")

         (string? select)
         (str q-str " " select)

         (vector? select)
         (if (empty? select)
           (build-query-select q-str nil opts)
           (let [sel-str (build-query-columns select opts)]
             (str q-str " " sel-str))))))

(defn build-query-from
  "build query from string
 
   (build-query-from \"\" :users {})
   => \"FROM users\""
  {:added "3.0"}
  ([q-str table opts]
   (let [table-fn  (:table-fn opts identity)]
     (str q-str " FROM " (table-fn (name table))))))

(defn build-query-where-string
  "build query where string
 
   (build-query-where-string [{:id \"a\"} {:value \"data\"}] {})
   => \"id = 'a' OR value = 'data'\""
  {:added "3.0"}
  ([where opts]
   (let [column-fn (:column-fn opts identity)]
     (cond (vector? where)
           (->> (map #(build-query-where-string % opts) where)
                (clojure.string/join " OR "))

           (map? where)
           (->> (map (fn [[k value]]
                       (let [column (column-fn (name k))
                             result  (if (vector? value)
                                       (str (f/strn (first value)) " " (str " '" (second value) "'"))
                                       (str "=" (str " '" value "'")))]
                         (str column " " result)))
                     where)
                (clojure.string/join " AND "))))))

(defn build-query-where
  "build query where
 
   (build-query-where \"\" [{:id \"a\"} {:value \"data\"}] {})
   => \"WHERE id = 'a' OR value = 'data'\""
  {:added "3.0"}
  ([q-str where opts]
   (let [column-fn (:column-fn opts identity)
         where-str (build-query-where-string where opts)]
     (str q-str " WHERE " where-str))))

(defn build-query-order-by
  "build query order by
 
   (build-query-order-by \"\" [:id] :desc {})
   => \"ORDER BY id DESC\""
  {:added "3.0"}
  ([q-str order-by order-type opts]
   (let [order-by (if (vector? order-by) order-by [order-by])
         type-str (if (= (f/strn order-type) "desc") "DESC" "ASC")
         by-str   (build-query-columns order-by opts)]
     (str q-str " ORDER BY " by-str " " type-str))))

(defn build-query-group-by
  "build query group by
 
   (build-query-group-by \"\" [:id] {})
   => \"GROUP BY id\""
  {:added "3.0"}
  ([q-str group-by opts]
   (let [group-by (if (vector? group-by) group-by [group-by])
         by-str   (build-query-columns group-by opts)]
     (str q-str " GROUP BY " by-str))))

(defn build-query-limit
  "build query limit
 
   (build-query-limit \"\" 20 {})
   => \"LIMIT 20\""
  {:added "3.0"}
  ([q-str limit opts]
   (str q-str " LIMIT " limit)))

(defn build-query-offset
  "build query offset
 
   (build-query-offset \"\" 20 {})
   => \"OFFSET 20\""
  {:added "3.0"}
  ([q-str offset opts]
   (str q-str " OFFSET " offset)))

(defn sql:query
  "builds select query"
  {:added "3.0"}
  ([query]
   (sql:query query common/*options*))
  ([{:keys [op select from where limit offset group-by order-by order-type then]} opts]
   (let [q (-> (or op "SELECT")
               (build-query-select select opts)
               (build-query-from from opts))
         q (cond-> q
             (not (empty? where))    (build-query-where where opts)
             (not (empty? order-by)) (build-query-order-by order-by order-type opts)
             (not (empty? group-by)) (build-query-group-by group-by opts)
             limit  (build-query-limit limit opts)
             offset (build-query-offset offset opts)
             then (str then))]
     q)))

(comment
  #_(defn for-query-update
      "generates SELECT ... FOR UPDATE command
  
   (for-query-update :user {:id \"a\"} {})
   => [\"SELECT * FROM user WHERE id = ? FOR UPDATE\" \"a\"]"
      {:added "0.1"}
      ([table key-map opts]
       (-> (for-query table key-map opts)
           (update 0 #(str % " FOR UPDATE"))))))

