(ns app.manage.site-map
  "Project-independent reader and publisher for substrate page requirements.
   Page sources are read as data; only explicitly supplied registry resolvers run."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn page-files
  "Returns page files in stable order; missing roots are configuration errors."
  [root]
  (when-not (.isDirectory (io/file root))
    (throw (ex-info "Page source root does not exist" {:path (str root)})))
  (->> (file-seq (io/file root))
       (filter #(and (.isFile %) (str/ends-with? (.getName %) ".clj")))
       (sort-by str) vec))

(defn read-page
  "Reads namespace aliases and a literal requirements map without loading a page."
  [path]
  (binding [*read-eval* false]
    (with-open [reader (clojure.lang.LineNumberingPushbackReader. (io/reader path))]
      (loop [aliases {} namespace nil requirements nil]
        (let [form (read {:eof ::eof} reader)]
          (cond
            (= ::eof form)
            (do
              (when-not (and namespace (map? requirements))
                (throw (ex-info "Page needs a namespace and literal SITE-MAP-REQUIREMENTS"
                                {:path (str path)})))
              {:path (str path) :namespace namespace
               :aliases aliases :requirements requirements})

            (and (seq? form) (= 'ns (first form)))
            (recur (into aliases
                         (for [clause (drop 2 form)
                               :when (and (seq? clause) (= :require (first clause)))
                               spec (rest clause) :when (vector? spec)
                               :let [opts (apply hash-map (rest spec))]
                               :when (:as opts)]
                           [(:as opts) (first spec)]))
                   (second form) requirements)

            (and (seq? form) (= 'def (first form))
                 (= 'SITE-MAP-REQUIREMENTS (second form)))
            (do
              (when (or requirements (not (map? (nth form 2 nil))))
                (throw (ex-info "Requirements must be one literal map" {:path (str path)})))
              (recur aliases namespace (nth form 2)))

            :else (recur aliases namespace requirements)))))))

(defn qualified-reference
  [aliases reference]
  (when (and (symbol? reference) (namespace reference))
    (let [prefix (symbol (namespace reference))]
      (symbol (str (get aliases prefix prefix)) (name reference)))))

(defn resolve-reference
  "Resolves only configured seed/group namespaces. The resolver receives a qualified symbol."
  [registry kind aliases reference]
  (let [qualified (qualified-reference aliases reference)
        allowed (if (= kind :groups) (:group-namespaces registry) (:seed-namespaces registry))
        entry (when (and qualified (contains? (set allowed) (symbol (namespace qualified))))
                ((:resolve registry) qualified))
        valid? (case kind
                 :tables (and (map? entry) (contains? entry :columns))
                 :rpcs (and (map? entry) ((:rpc? registry) entry))
                 :groups (and (map? entry) (string? (get entry "id"))
                              (map? (get entry "models"))))]
    (when-not valid?
      (throw (ex-info "Unknown or wrong-kind application declaration"
                      {:kind kind :reference reference :qualified qualified})))
    entry))

(defn requirement-entry
  "Preserves legacy string IDs while resolving typed declaration references."
  [registry kind aliases reference]
  (if (string? reference) reference (resolve-reference registry kind aliases reference)))

(defn resolve-requirements
  [registry requirements aliases]
  (when-not (and (map? requirements)
                 (every? #{:tables :rpcs :groups} (keys requirements))
                 (every? vector? (vals requirements)))
    (throw (ex-info "Requirements need table/RPC/group vectors" {:requirements requirements})))
  (let [groups (mapv #(resolve-reference registry :groups aliases %) (:groups requirements))
        group-rpcs (for [group groups
                         entry (concat (vals (get group "models"))
                                       (vals (get group "actions")))
                         :let [rpc (get entry "rpc")] :when rpc]
                     (or ((:get-rpc registry) (or (:id rpc) (get rpc "id")))
                         (throw (ex-info "Unknown model RPC" {:rpc rpc}))))]
    {:tables (mapv #(requirement-entry registry :tables aliases %) (:tables requirements []))
     :rpcs (vec (distinct (concat (map #(requirement-entry registry :rpcs aliases %)
                                     (:rpcs requirements [])) group-rpcs)))}))

(defn page-declarations
  [registry root]
  (mapv (fn [path]
          (try
            (let [{:keys [aliases requirements] :as page} (read-page path)]
              (assoc page :requirements (resolve-requirements registry requirements aliases)))
            (catch Exception error
              (throw (ex-info (.getMessage error)
                              (merge {:path (str path)} (ex-data error)) error)))))
        (page-files root)))

(defn normalize-names [values]
  (->> values
       (map #(cond (map? %) (:name %) (keyword? %) (name %) :else (str %)))
       distinct sort vec))

(defn worker-schema
  "Retains composite identities and normalizes legacy id-based schema labels."
  [schema]
  (into {} (for [[table fields] schema]
             [table (into {} (for [[field attrs] fields]
                              [field (cond-> (dissoc attrs "primary")
                                       (if (contains? fields "id") (= field "id")
                                           (boolean (get attrs "primary")))
                                       (assoc "primary" true))]))])))

(defn table-dependencies [schema table]
  (->> (get schema table) vals
       (keep #(when (and (= "ref" (get % "type"))
                         (= "forward" (get-in % ["ref" "type"])))
                (get-in % ["ref" "ns"])))
       (filter #(contains? schema %)) set))

(defn table-closure [schema tables]
  (loop [pending (seq tables) included #{}]
    (if-let [table (first pending)]
      (if (contains? included table)
        (recur (next pending) included)
        (do
          (when-not (contains? schema table)
            (throw (ex-info "Unknown table in page requirements" {:table table})))
          (recur (concat (next pending) (sort (table-dependencies schema table)))
                 (conj included table))))
      included)))

(defn table-order
  "Deterministic dependency-first ordering, including mutually referring tables."
  [schema tables]
  (let [tables (table-closure schema tables) visited (atom #{}) output (atom [])]
    (letfn [(visit [table]
              (when-not (contains? @visited table)
                (swap! visited conj table)
                (doseq [dependency (sort (table-dependencies schema table))]
                  (when (contains? tables dependency) (visit dependency)))
                (swap! output conj table)))]
      (doseq [table (sort tables)] (visit table))
      @output)))

(defn rpc-specs [registry references]
  (reduce (fn [out reference]
            (let [entry (if (map? reference) reference ((:get-rpc registry) reference))
                  _ (when-not entry (throw (ex-info "Unknown RPC" {:rpc reference})))
                  spec ((:rpc-spec registry) entry)
                  id (or (:id spec) (get spec "id"))]
              (when-not (string? id) (throw (ex-info "RPC needs an ID" {:rpc spec})))
              (when (and (contains? out id) (not= (get out id) spec))
                (throw (ex-info "Conflicting RPC IDs" {:id id})))
              (assoc out id spec)))
          (sorted-map) references))

(defn publication
  "Pure site-map assembly from resolved pages and a supplied database registry."
  [registry {:keys [site public-root] :or {public-root "/worker/site-map"}} pages]
  (let [schema ((:schema registry))
        requirements (map :requirements pages)
        order (table-order schema (normalize-names (mapcat :tables requirements)))
        rpc (rpc-specs registry (mapcat :rpcs requirements))]
    {:manifest {"version" 1 "site" site
                "schema_url" (str public-root "/schema.json")
                "lookup_url" (str public-root "/lookup.json")
                "rpc_url" (str public-root "/rpc.json")
                "tables" order "rpcs" (vec (keys rpc))
                "pages" (mapv :namespace pages)}
     :schema (into (array-map) (map #(vector % (get schema %)) order))
     :lookup (into (array-map)
                   (map-indexed (fn [position table]
                                  [table {"position" position
                                          "schema" ((:table-schema registry) table)}]) order))
     :rpc rpc}))

(defn build-sections
  "Constructs std.make JSON sections without depending on std.make or a project."
  [publish {:keys [target] :or {target "public/worker/site-map"}}]
  (let [data (delay (publish))]
    (mapv (fn [key] {:type :json :target target :file (str (name key) ".json")
                     :main (fn [] (get @data key))})
          [:manifest :schema :lookup :rpc])))

(defn target
  "Constructs an app.manage target from a registry supplier and site configuration.
   encode is the project's JSON encoder, preserving existing publication formatting."
  [registry-fn {:keys [source-root output-root depends sources] :as config} encode]
  {:depends (vec depends)
   :sources (vec (distinct (cons source-root sources)))
   :outputs [output-root]
   :render (fn [{:keys [root]}]
             (let [registry (registry-fn)
                   pages (page-declarations registry (io/file root source-root))
                   data (publication registry config pages)]
               (into (sorted-map)
                     (for [[key value] data]
                       [(str output-root "/" (name key) ".json") (encode value)]))))})
