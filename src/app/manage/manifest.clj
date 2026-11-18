(ns app.manage.manifest
  "Deterministic application metadata shared by publication and management."
  (:require [clojure.data.json :as json]))

(defn canonical [value]
  (cond
    (map? value) (into (sorted-map) (map (fn [[k v]] [(name k) (canonical v)]) value))
    (set? value) (vec (sort-by pr-str (map canonical value)))
    (sequential? value) (mapv canonical value)
    (keyword? value) (name value)
    (symbol? value) (str value)
    :else value))

(defn compile-manifest [pages]
  (let [pages (if (map? pages) (map (fn [[id page]] (assoc page "id" (name id))) pages) pages)
        pages (mapv canonical pages)
        ids (mapv #(get % "id") pages)
        routes (mapcat #(get % "routes") pages)]
    (when (or (some #(not (and (string? %) (seq %))) ids)
              (not= (count ids) (count (distinct ids))))
      (throw (ex-info "Page IDs must be nonempty and unique" {:ids ids})))
    (when (not= (count routes) (count (distinct routes)))
      (throw (ex-info "Duplicate page routes" {:routes routes})))
    (doseq [page pages kind ["models" "actions"] [id entry] (get page kind)]
      (when-not (and (get entry "operation") (get entry "operation-spec"))
        (throw (ex-info "Unresolved operation" {:page (get page "id") :member id :kind kind})))
      (when (and (= kind "models") (get-in entry ["operation-spec" "write"]))
        (throw (ex-info "Mutation declared as a read model" {:page (get page "id") :member id}))))
    (doseq [page pages model (get page "order")]
      (when-not (contains? (get page "models") model)
        (throw (ex-info "Unknown model in page order" {:page (get page "id") :model model}))))
    (canonical {"version" 1 "pages" (sort-by #(get % "id") pages)})))

(defn provider [value]
  (if (symbol? value) ((requiring-resolve value)) (value)))

(defn pages [project]
  (let [config (:workbench project)
        raw (provider (:application config))
        operations (when-let [source (:operations config)] (provider source))
        raw (if (map? raw)
              (into {} (for [[id page] raw]
                         [id (reduce (fn [page kind]
                                       (update page kind
                                         (fn [entries]
                                           (into {} (for [[name entry] entries]
                                                      [name (assoc entry "operation-spec"
                                                                   (get operations (get entry "operation")))])))))
                                     page ["models" "actions"])])) raw)]
    (mapv (fn [page] (into {} (map (fn [[k v]] [(keyword k) v]) page)))
          (get (compile-manifest raw) "pages"))))

(defn with-workbench [project config]
  (when (or (:workbench project) (:pages project))
    (throw (ex-info "Conflicting legacy and generated workbench providers" {})))
  (let [project (assoc project :workbench config)]
    (-> project
        (assoc :pages #(pages project))
        (assoc-in [:targets :application-manifest]
          {:depends (vec (:depends config)) :sources (vec (:sources config))
           :outputs ["generated/workbench"]
           :render (fn [_]
                     (let [manifest (compile-manifest (pages project))]
                       {"generated/workbench/manifest.edn" (str (pr-str manifest) "\n")
                        "generated/workbench/manifest.json" (str (json/write-str manifest) "\n")}))})
        (update :default-targets (fn [targets] (conj (vec targets) :application-manifest))))))
