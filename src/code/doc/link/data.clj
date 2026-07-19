(ns code.doc.link.data
  (:require [clojure.string :as string]
            [code.doc.manage :as manage]
            [std.fs :as fs]))

(def ^:dynamic *default-data-path* "config/publish/related.edn")

(def ^:dynamic *max-auto-related* 8)

(defonce ^:private +coverage-cache+ (atom {}))

(defn load-data
  "loads the shared code.doc data file (related libraries, links, etc)

   (load-data {:project {:root \".\" :publish {}}})
   => map?"
  {:added "4.1"}
  [{:keys [project] :as interim}]
  (let [path (fs/path (:root project)
                      (or (get-in project [:publish :data])
                          *default-data-path*))]
    (if (fs/exists? path)
      (read-string (slurp path))
      {})))

(defn select-items
  "selects data entries for an element, honouring `:group`, `:only` and `:exclude`

   (select-items [{:name \"a\" :group :x} {:name \"b\" :group :y}]
                 {:group :x})
   => [{:name \"a\" :group :x}]"
  {:added "4.1"}
  [entries {:keys [group only exclude]}]
  (let [groups  (cond (set? group) group
                      (keyword? group) #{group})
        only    (set only)
        exclude (set exclude)
        key-of  (fn [e] (or (:name e) (:label e)))]
    (->> entries
         (filter (fn [e] (or (nil? groups)
                             (contains? groups (:group e)))))
         (filter (fn [e] (or (empty? only)
                             (contains? only (key-of e)))))
         (remove (fn [e] (contains? exclude (key-of e))))
         vec)))

(defn attach-items
  "attaches selected items to an element, or an `:error` when empty"
  {:added "4.1"}
  [elem entries]
  (let [items (select-items entries elem)]
    (if (seq items)
      (assoc elem :items items)
      (assoc elem :error (str "code.doc data: no " (name (:type elem)) " entries for "
                              (pr-str (select-keys elem [:group :only :exclude])))))))

;;
;; automatic per-page related sections
;;

(defn ns-group
  "groups a namespace for related-page computation

   (ns-group 'jvm.monitor)          => \"jvm\"
   (ns-group 'std.lib.collection)   => \"std.lib\"
   (ns-group 'std.concurrent.queued) => \"std.concurrent\""
  {:added "4.1"}
  [ns-sym]
  (let [segs (string/split (str ns-sym) #"\.")]
    (if (and (> (count segs) 1)
             (contains? #{"std" "code" "xt" "hara"} (first segs)))
      (str (first segs) "." (second segs))
      (first segs))))

(defn documented-coverage
  "returns the cached namespace -> pages coverage map for the project"
  {:added "4.1"}
  [{:keys [root] :as project}]
  (or (get @+coverage-cache+ root)
      (let [coverage (manage/documented-coverage project)]
        (get (swap! +coverage-cache+ assoc root coverage) root))))

(defn page-namespaces
  "returns all namespaces referenced by a page's elements"
  {:added "4.1"}
  [elements]
  (->> elements
       (mapcat manage/element-namespaces)
       set))

(defn related-pages
  "computes sibling pages documenting namespaces in the same groups"
  {:added "4.1"}
  [{:keys [project ns] :as interim} name namespaces]
  (let [sites    (get-in project [:publish :sites])
        pages    (:pages (get sites (keyword ns)))
        groups   (set (map ns-group namespaces))
        coverage (documented-coverage project)]
    (->> (for [[cns page-ids] coverage
               :when (contains? groups (ns-group cns))
               page-id page-ids
               :when (and (= (namespace page-id) ns)
                          (not= (clojure.core/name page-id) name)
                          (not= (clojure.core/name page-id) "index"))]
           page-id)
         distinct
         sort
         (keep (fn [page-id]
                 (when-let [{:keys [title subtitle]} (get pages (symbol (clojure.core/name page-id)))]
                   {:name title
                    :href (str (clojure.core/name page-id) ".html")
                    :description subtitle
                    :group :docs})))
         (take *max-auto-related*)
         vec)))

(defn curated-items
  "returns curated `:namespaces` entries from the data file for a page"
  {:added "4.1"}
  [data namespaces]
  (->> namespaces
       (mapcat #(get (:namespaces data) %))
       vec))

(defn auto-related-element
  "builds an automatic `:related` element for a page, or nil when not applicable"
  {:added "4.1"}
  [{:keys [project] :as interim} name elements data]
  (let [meta       (get-in interim [:articles name :meta])
        namespaces (page-namespaces elements)]
    (when (and (not-any? #(= :related (:type %)) elements)
               (not= "home.html" (:base meta))
               (seq namespaces))
      (let [items (vec (concat (related-pages interim name namespaces)
                               (curated-items @data namespaces)))]
        (when (seq items)
          {:type  :related
           :title "Related"
           :items items
           :auto  true})))))

(defn link-data
  "attaches shared data entries to `:related` and `:links` elements,
   appending an automatic related section when the page has none"
  {:added "4.1"}
  [{:keys [project] :as interim} name]
  (update-in interim [:articles name :elements]
             (fn [elements]
               (let [data     (delay (load-data interim))
                     elements (mapv (fn [{:keys [type] :as elem}]
                                      (case type
                                        :related (attach-items elem (:libraries @data))
                                        :links   (attach-items elem (:links @data))
                                        elem))
                                    elements)]
                 (if-let [auto (auto-related-element interim name elements data)]
                   (conj elements auto)
                   elements)))))
