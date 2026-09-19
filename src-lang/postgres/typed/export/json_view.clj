(ns postgres.typed.export.json-view
  "Compiles bound postgres view descriptors into the published JSON view registry."
  (:require [postgres.typed.typed-order :as typed-order]))

(def ^:private +view-types+
  ["select" "return"])

(defn- ordered-view
  [view]
  (let [known-keys [:table :type :tag :query :identity]
        present-known-keys (filter #(contains? view %) known-keys)
        extra-keys (sort-by str
                            (remove (set known-keys) (keys view)))]
    (typed-order/ordered-map
     (map (fn [key]
            [key (typed-order/ordered-value (get view key))])
          (concat present-known-keys extra-keys)))))

(defn- ordered-descriptor
  [descriptor]
  (let [known-keys [:input :return :schema :id :flags :view]
        extra-keys (sort-by str
                            (remove (set known-keys) (keys descriptor)))]
    (typed-order/ordered-map
     (map (fn [key]
            [key (if (= :view key)
                   (ordered-view (get descriptor key))
                   (typed-order/ordered-value (get descriptor key)))])
          (concat (filter #(contains? descriptor %) known-keys)
                  extra-keys)))))

(defn- assert-unique-view-keys!
  [entries]
  (let [duplicates (->> entries
                         (group-by (fn [[_ descriptor]]
                                     [(get-in descriptor [:view :table])
                                      (get-in descriptor [:view :type])
                                      (get-in descriptor [:view :tag])]))
                         (keep (fn [[key matches]]
                                 (when (< 1 (count matches))
                                   {:key key
                                    :sources (mapv (comp str first) matches)})))
                         seq)]
    (when duplicates
      (throw (ex-info "Duplicate typed view table/type/tag keys"
                      {:duplicates duplicates})))
    entries))

(defn- type-publication
  [entries]
  (typed-order/ordered-map
   (map (fn [[_ descriptor]]
          [(get-in descriptor [:view :tag])
           (ordered-descriptor descriptor)])
        (sort-by (fn [[_ descriptor]]
                   [(get-in descriptor [:view :tag])
                    (:id descriptor)])
                 entries))))

(defn- table-publication
  [entries]
  (let [by-type (group-by (fn [[_ descriptor]]
                            (get-in descriptor [:view :type]))
                          entries)]
    (when-let [unknown (seq (remove (set +view-types+) (keys by-type)))]
      (throw (ex-info "Unsupported typed view type"
                      {:types unknown})))
    (typed-order/ordered-map
     (keep (fn [type]
             (when-let [type-entries (seq (get by-type type))]
               [type (type-publication type-entries)]))
           +view-types+))))

(defn generate-views
  "Builds the versioned JSON publication for typed postgres views.

   Entries are the `[symbol descriptor]` values returned by
   `postgres.typed.typed-view/view-entries`."
  [entries]
  (let [entries (->> entries
                     assert-unique-view-keys!
                     (sort-by (fn [[_ descriptor]]
                                [(get-in descriptor [:view :table])
                                 (get-in descriptor [:view :type])
                                 (get-in descriptor [:view :tag])
                                 (:id descriptor)]))
                     vec)
        by-table (group-by (fn [[_ descriptor]]
                             (get-in descriptor [:view :table]))
                          entries)]
    (typed-order/ordered-map
     [[:version 1]
      [:views
       (typed-order/ordered-map
        (map (fn [table]
               [table (table-publication (get by-table table))])
             (sort (keys by-table))))]])))
