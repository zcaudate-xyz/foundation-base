(ns postgres.typed.typed-view
  (:require [clojure.string :as string]
            [postgres.gen.bind-macro :as bind]
            [std.string.case :as string-case]))

(defn- descriptor-id
  [descriptor]
  (or (:id descriptor)
      (throw (ex-info "Typed descriptor has no :id"
                      {:descriptor descriptor}))))

(defn- assert-unique-ids!
  [kind entries]
  (let [duplicates (->> entries
                        (group-by (comp descriptor-id second))
                        (keep (fn [[id matches]]
                                (when (< 1 (count matches))
                                  {:id id
                                   :sources (mapv (comp str first) matches)})))
                        seq)]
    (when duplicates
      (throw (ex-info (str "Duplicate typed " (name kind) " ids")
                      {:kind kind
                       :duplicates duplicates})))
    entries))

(defn- normalize-view-id
  [{:keys [id view] :as bound}]
  (let [table-tag (string-case/snake-case (:table view))
        select-prefix (str table-tag "_sel_")
        suffix (when (string/starts-with? id select-prefix)
                 (subs id (count select-prefix)))
        canonical-id (if (and (= "select" (:type view))
                              suffix
                              (not= "all" suffix)
                              (not (string/starts-with? suffix "by_")))
                       (str select-prefix "by_" suffix)
                       id)]
    (-> bound
        (assoc :id canonical-id)
        (assoc-in [:view :tag]
                  (subs canonical-id (inc (count table-tag)))))))

(defn- bind-view-entry
  [ptr]
  (normalize-view-id
   (try
     (bind/bind-view ptr)
     (catch clojure.lang.ExceptionInfo ex
       (let [entry (bind/bind-entry ptr)
             {:keys [table type scope query query-base args identity]}
             (:static/view entry)
             {:keys [id] :as bound} (bind/bind-function ptr)
             table-name (name table)
             table-tag (string-case/snake-case table-name)
             select-prefix (str "sel_" table-tag "_")
             return-prefix (str "ret_" table-tag "_")
             canonical-id
             (cond
               (string/starts-with? id select-prefix)
               (str table-tag "_sel_" (subs id (count select-prefix)))

               (string/starts-with? id return-prefix)
               (str table-tag "_ret_" (subs id (count return-prefix)))

               :else
               (throw ex))
             tag (subs canonical-id (inc (count table-tag)))]
         (binding [*ns* (the-ns (:namespace entry))]
           (-> bound
               (assoc :id canonical-id)
               (update :flags merge (bind/to-lookup scope))
               (merge
                {:view (cond-> {:table table-name
                                :type (name type)
                                :tag tag
                                :query (bind/transform-query
                                        (or query-base query)
                                        (set (filter symbol? args)))}
                         identity (assoc :identity
                                         (bind/transform-query identity)))}
                ))))))))

(defn view-entries
  "Binds defsel.pg and defret.pg declarations into typed view descriptors."
  {:added "4.1"}
  ([source-namespaces]
   (view-entries source-namespaces {}))
  ([source-namespaces {:keys [preserve-source-order?]}]
   (let [entries (->> source-namespaces
                      (mapcat (fn [ns-sym]
                                (require ns-sym)
                                (map (fn [[_ sym]]
                                       (let [{:keys [id input view]}
                                             (bind-view-entry @(resolve sym))
                                             entry {:input input
                                                    :view view}
                                             entry-key (if (= "select" (:type view))
                                                         :select-entry
                                                         :return-entry)]
                                         [sym {:id id
                                               :table (:table view)
                                               entry-key entry
                                               :select-args []
                                               :return-args []}]))
                                     (concat (bind/list-view ns-sym :select)
                                             (bind/list-view ns-sym :return)))))
                      distinct
                      vec)]
     (->> (if preserve-source-order?
            entries
            (sort-by (juxt (comp descriptor-id second)
                           (comp str first))
                     entries))
          vec
          (assert-unique-ids! :view)))))
