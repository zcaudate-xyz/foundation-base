(ns postgres.typed.typed-route
  (:require [clojure.string :as string]
            [postgres.gen.bind-macro :as bind]
            [postgres.typed.typed-common :as types]))

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

(defn route-entries
  "Binds functions from generated PostgreSQL RPC namespaces."
  {:added "4.1"}
  ([source-namespaces]
   (route-entries source-namespaces #(= :defn (:op-key %)) {}))
  ([source-namespaces pred]
   (route-entries source-namespaces pred {}))
  ([source-namespaces pred {:keys [preserve-source-order?]}]
   (let [entries (->> source-namespaces
                      (mapcat (fn [ns-sym]
                                (require ns-sym)
                                (map (fn [[_ sym]]
                                       [sym (bind/bind-function @(resolve sym))])
                                     (bind/list-api ns-sym pred))))
                      vec)]
     (->> (if preserve-source-order?
            entries
            (sort-by (juxt (comp descriptor-id second)
                           (comp str first))
                     entries))
          vec
          (assert-unique-ids! :route)))))

(defn route-operation-ids
  "Returns normalized OpenAPI operation ids for bound route entries."
  [route-entries]
  (->> route-entries
       (map (fn [[_ descriptor]]
              (types/normalize-key (:id descriptor))))
       set))

(defn route-descriptors
  "Returns normalized, publication-ready descriptors for bound routes."
  [route-entries]
  (mapv (fn [[symbol descriptor]]
          (let [operation-id (types/normalize-key (:id descriptor))
                grant (get-in (meta (requiring-resolve symbol))
                              [:api/meta :sb/grant])]
            [operation-id
             (assoc
              (update descriptor
                      :flags
                      merge
                      {:expose (if (= :all grant) :sb/public :sb/auth)})
              :id operation-id
              :symbol (last (string/split (str symbol) #"/")))]))
        route-entries))

(defn function-filter
  "Creates a typed function predicate from normalized operation ids."
  [operation-ids]
  (let [operation-ids (set (map types/normalize-key operation-ids))]
    (fn [fn-def]
      (contains? operation-ids
                 (types/normalize-key (:name fn-def))))))
