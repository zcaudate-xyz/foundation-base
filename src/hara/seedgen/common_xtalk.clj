(ns hara.seedgen.common-xtalk
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [code.project :as project]
            [std.fs :as fs]
            [hara.common.grammar :as grammar]
            [hara.lang.impl :as impl]
            [hara.lang.library :as lib]
            [hara.lang.registry :as reg]
            [hara.seedgen.common-meta :as runtime]
            [hara.typed.xtalk-ops :as typed]))

(def +status-mark+
  {:implemented "Y"
   :abstract "A"
   :missing "."})

(def +special-feature-status+
  {[:js 'x:return-run] :implemented
   [:lua 'x:return-run] :implemented
   [:python 'x:return-run] :implemented
   [:dart 'x:return-run] :implemented})

(def ^:dynamic *inventory-path*
  "config/xtalk/xtalk_ops.edn")

(def ^:dynamic *model-roots*
  ["src/hara.lang/model/spec_xtalk"
   "src/hara.lang/model_annex/spec_xtalk"])

(def ^:dynamic *test-roots*
  ["test/hara.lang/model/spec_xtalk"
   "test/hara.lang/model_annex/spec_xtalk"])

(def ^:dynamic *runtime-langs*
  runtime/+runtime-langs+)

(defn- xtalk-category?
  [category]
  (str/starts-with? (name category) "xtalk"))

(defn- coerce-symbol
  [x]
  (cond
    (var? x)
    (symbol (str (ns-name (:ns (meta x))))
            (str (:name (meta x))))

    (symbol? x)
    x

    :else
    x))

(defn- compact-map
  [m]
  (reduce-kv (fn [out k v]
               (if (nil? v)
                 out
                 (assoc out k v)))
             {}
             m))

(defn- symbol-doc
  [sym]
  (when (symbol? sym)
    (when-let [ns-sym (some-> sym namespace symbol)]
      (require ns-sym))
    (when-let [v (or (resolve sym)
                     (when-let [ns-sym (some-> sym namespace symbol)]
                       (ns-resolve ns-sym (symbol (name sym)))))]
      (:doc (meta v)))))

(defn grammar-entries
  "Returns xtalk grammar entries enriched with `:op` and `:category`."
  {:added "4.1"}
  []
  (->> (grammar/ops-list)
       (filter xtalk-category?)
       (mapcat (fn [category]
                 (for [[op entry] (grammar/ops-detail category)
                       :when (and (keyword? op)
                                  (map? entry))]
                   (assoc entry
                          :op op
                          :category category))))
       vec))

(defn- list-xtalk-files
  [root]
  (if (fs/exists? root)
    (->> (keys (fs/list root {:recursive true
                              :include [".clj$"]}))
         (map str)
         sort
         vec)
    []))

(defn- parse-lang-file
  [path]
  (let [name (str (fs/file-name path))]
    (when-let [[_ kind lang] (re-find #"(?i)^(fn|com)_([a-z0-9]+)(?:_test)?\.clj$" name)]
      {:kind (keyword (str/lower-case kind))
       :lang (keyword (str/lower-case lang))})))

(defn- collect-lang-files
  [root roots files-key forms-key]
  (reduce
   (fn [out rel-root]
     (let [scan-root (str (fs/path root rel-root))]
       (reduce
        (fn [m path]
          (if-let [{:keys [kind lang]} (parse-lang-file path)]
            (let [entry (get m lang {files-key []
                                     forms-key #{}})
                  files (conj (get entry files-key)
                              (str (fs/relativize (fs/path root)
                                                  (fs/path path))))
                  forms (conj (get entry forms-key) kind)]
              (assoc m lang (assoc entry
                                   files-key (vec (sort files))
                                   forms-key forms)))
            m))
        out
        (list-xtalk-files scan-root))))
   (sorted-map)
   roots))

(defn categories
  "Returns xtalk categories in declaration order."
  {:added "4.1"}
  []
  (->> (grammar-entries)
       (map :category)
       distinct
       vec))

(defn op-map
  "Returns xtalk grammar entries keyed by op."
  {:added "4.1"}
  []
  (->> (grammar-entries)
       (map (juxt :op identity))
       (into (sorted-map))))

(defn symbols
  "Returns all x:* symbols used by xtalk grammar entries."
  {:added "4.1"}
  []
  (->> (vals (op-map))
       (mapcat :symbol)
       (filter #(str/starts-with? (name %) "x:"))
       distinct
       sort
       vec))

(defn installed-languages
  "Loads all default book namespaces and returns installed languages."
  {:added "4.1"}
  []
  (let [library (impl/default-library)]
    (doseq [[lang key] (reg/registry-book-list)
            :when (= :default key)
            :let [ns-sym (reg/registry-book-ns lang key)]
            :when ns-sym]
      (require ns-sym))
    (->> (keys (lib/get-snapshot library))
         sort
         vec)))

(defn parent-languages
  "Returns installed languages whose loaded book has `:parent :xtalk`."
  {:added "4.1"}
  []
  (let [library (impl/default-library)]
    (installed-languages)
    (->> (lib/get-snapshot library)
         (keep (fn [[lang {:keys [book]}]]
                 (when (= :xtalk (:parent book))
                   lang)))
         sort
         vec)))

(defn languages
  "Returns languages to include in xtalk audits."
  {:added "4.1"}
  ([] (languages nil))
  ([langs]
   (let [installed (set (installed-languages))
         selected  (or langs (parent-languages))]
     (->> selected
          (filter installed)
          vec))))

(defn feature-status
  "Returns xtalk feature status for a language."
  {:added "4.1"}
  [lang feature]
  (or (get +special-feature-status+ [lang feature])
      (if-let [entry (get-in (impl/grammar lang) [:reserved feature])]
        (if (= :abstract (:emit entry))
          :abstract
          :implemented)
        :missing)))

(defn support
  "Returns xtalk support data per language and feature.

   Options:
   - `:langs`    selected languages
   - `:features` selected features"
  {:added "4.1"}
  ([] (support {}))
  ([{:keys [langs features]}]
   (let [langs    (languages langs)
         features (vec (or features (symbols)))
         status   (into {}
                        (for [lang langs]
                          [lang (into (sorted-map)
                                      (for [feature features]
                                        [feature (feature-status lang feature)]))]))
         summary  (into {}
                        (for [[lang entries] status]
                          [lang (merge {:implemented 0
                                        :abstract 0
                                        :missing 0}
                                       (frequencies (vals entries)))]))]
     {:languages langs
      :features features
      :status status
      :summary summary})))

(defn model-inventory
  "Returns model specification coverage grouped by xtalk language."
  {:added "4.1"}
  ([] (model-inventory {}))
  ([{:keys [roots]
     :or {roots *model-roots*}}]
   (let [proj (project/project)
         by-lang (collect-lang-files (:root proj) roots :model-files :model-forms)]
     (into (sorted-map)
           (for [[lang entry] by-lang]
             [lang (assoc entry
                          :lang lang
                          :model-count (count (:model-files entry))
                          :model-forms (vec (sort (:model-forms entry))))])))))

(defn test-inventory
  "Returns xtalk test coverage grouped by language."
  {:added "4.1"}
  ([] (test-inventory {}))
  ([{:keys [roots]
     :or {roots *test-roots*}}]
   (let [proj (project/project)
         by-lang (collect-lang-files (:root proj) roots :test-files :test-forms)]
     (into (sorted-map)
           (for [[lang entry] by-lang]
             [lang (assoc entry
                          :lang lang
                          :test-count (count (:test-files entry))
                          :test-forms (vec (sort (:test-forms entry))))])))))

(defn runtime-inventory
  "Returns runtime inventory with installation and spec support status."
  {:added "4.1"}
  ([] (runtime-inventory {}))
  ([{:keys [langs]
     :or {langs *runtime-langs*}}]
   (let [langs     (->> langs (map runtime/normalize-runtime-lang) distinct sort vec)
         matrix    (support {:langs langs})
         installed (set (installed-languages))]
     (into (sorted-map)
           (for [lang langs
                 :let [{:keys [script dispatch suffix]} (runtime/runtime-lang-config lang)
                       summary (get-in matrix [:summary lang] {})]]
             [lang {:lang lang
                    :script script
                    :dispatch dispatch
                    :suffix suffix
                    :runtime? true
                    :runtime-type (runtime/runtime-type lang)
                    :runtime-check-mode (runtime/runtime-check-mode lang)
                    :runtime-installed? (contains? installed lang)
                    :runtime-executable? (contains? runtime/+runtime-executable-langs+ lang)
                    :spec-implemented (or (:implemented summary) 0)
                    :spec-abstract (or (:abstract summary) 0)
                    :spec-missing (or (:missing summary) 0)}])))))

(defn spec-inventory
  "Returns xtalk support matrix summary grouped by language."
  {:added "4.1"}
  ([] (spec-inventory {}))
  ([{:keys [langs features]}]
   (let [matrix        (support {:langs langs :features features})
         tracked       (set (:languages matrix))
         langs         (->> (or langs (:languages matrix)) (map keyword) distinct sort vec)
         feature-count (count (:features matrix))]
     (into (sorted-map)
           (for [lang langs
                 :let [summary (get-in matrix [:summary lang] {})]]
             [lang {:lang lang
                    :spec-tracked? (contains? tracked lang)
                    :spec-feature-count feature-count
                    :spec-implemented (or (:implemented summary) 0)
                    :spec-abstract (or (:abstract summary) 0)
                    :spec-missing (or (:missing summary) 0)}])))))

(defn language-status
  "Returns unified model/runtime/spec/test status by language."
  {:added "4.1"}
  ([] (language-status {}))
  ([{:keys [langs features]
     :as opts}]
   (let [model     (model-inventory opts)
         tests     (test-inventory opts)
         runtime   (runtime-inventory {:langs langs})
         spec      (spec-inventory {:langs langs :features features})
         all-langs (or langs
                       (->> (concat (keys model)
                                    (keys tests)
                                    (keys runtime)
                                    (keys spec))
                            distinct
                            sort
                            vec))]
     (into (sorted-map)
           (for [lang all-langs
                 :let [entry (merge {:lang lang
                                     :runtime-installed? false
                                     :runtime-executable? false
                                     :model-count 0
                                     :test-count 0
                                     :spec-implemented 0
                                     :spec-abstract 0
                                     :spec-missing 0}
                                    (get model lang)
                                    (get tests lang)
                                    (get runtime lang)
                                    (get spec lang))
                       model-count (:model-count entry)
                       test-count  (:test-count entry)
                       coverage    (if (pos? model-count)
                                     (double (/ test-count model-count))
                                     0.0)
                       ready?      (and (:runtime-installed? entry)
                                        (zero? (:spec-missing entry))
                                        (pos? test-count))]]
             [lang (assoc entry
                          :coverage coverage
                          :ready? ready?)])))))

(defn coverage-summary
  "Summarises language readiness and test/spec coverage."
  {:added "4.1"}
  ([] (coverage-summary {}))
  ([opts]
   (let [status (vals (language-status opts))
         total  (count status)]
     {:languages total
      :runtime-installed (count (filter :runtime-installed? status))
      :runtime-executable (count (filter :runtime-executable? status))
      :ready (count (filter :ready? status))
      :spec-missing-total (reduce + (map :spec-missing status))
      :models-total (reduce + (map :model-count status))
      :tests-total (reduce + (map :test-count status))})))

(defn missing-by-language
  "Returns non-implemented xtalk features grouped by language."
  {:added "4.1"}
  ([] (missing-by-language {}))
  ([opts]
   (let [{:keys [languages status]} (support opts)]
     (into {}
           (for [lang languages]
             [lang (into (sorted-map)
                         (remove (comp #{:implemented} val))
                         (get status lang))])))))

(defn missing-by-feature
  "Returns languages missing or leaving features abstract."
  {:added "4.1"}
  ([] (missing-by-feature {}))
  ([opts]
   (let [{:keys [languages features status]} (support opts)]
     (into (sorted-map)
           (for [feature features]
             [feature (into (sorted-map)
                            (for [lang languages
                                  :let [state (get-in status [lang feature])]
                                  :when (not= :implemented state)]
                              [lang state]))])))))

(defn inventory-path
  "Returns the absolute path to the xtalk inventory file."
  {:added "4.1"}
  ([] (inventory-path {}))
  ([{:keys [project path]
     :or {project (project/project)}}]
   (str (fs/path (:root project)
                 (or path *inventory-path*)))))

(defn read-inventory
  "Reads the persisted xtalk inventory EDN file when it exists."
  {:added "4.1"}
  [path]
  (when (fs/exists? path)
    (edn/read-string (slurp path))))

(defn- inventory-entry
  [entry existing]
  (let [canonical-symbol (typed/canonical-symbol-from-entry entry)
        symbols          (->> (:symbol entry)
                              (filter symbol?)
                              (sort-by str)
                              vec)]
    (merge
     (-> {:op (:op entry)
          :category (:category entry)
          :canonical-symbol canonical-symbol
          :symbols symbols
          :class (:class entry)
          :requires (not-empty (vec (sort-by str (:requires entry))))
          :emit (:emit entry)
          :type (:type entry)
          :macro (coerce-symbol (:macro entry))
          :raw (coerce-symbol (:raw entry))
          :doc (or (:doc existing)
                   (symbol-doc (coerce-symbol (:macro entry))))
          :default (:default entry)
          :cases []}
         compact-map)
     (select-keys existing [:status
                            :notes
                            :cases
                            :impls
                            :scaffold
                            :skip?]))))

(defn inventory-entries
  "Builds xtalk inventory entries from grammar tables, preserving authored data
   from an existing inventory when supplied with `{:existing [...]}`."
  {:added "4.1"}
  ([] (inventory-entries {}))
  ([{:keys [existing]}]
   (let [existing-map (into {}
                            (map (fn [entry]
                                   [(:op entry) entry]))
                            (or existing []))]
     (->> (grammar-entries)
          (map (fn [entry]
                 (inventory-entry entry
                                  (get existing-map (:op entry)))))
          (sort-by (juxt (comp name :category)
                         (comp str :canonical-symbol)))
          vec))))

(defn render-inventory
  "Renders xtalk inventory entries as pretty-printed EDN."
  {:added "4.1"}
  [entries]
  (binding [pprint/*print-right-margin* 100
            *print-length* nil
            *print-level* nil]
    (with-out-str
      (pprint/pprint entries))))

(defn generate-inventory
  "Generates xtalk operator inventory EDN from grammar tables.

   Options:
   - `:path`  alternate output path relative to project root
   - `:write` whether to persist the generated inventory"
  {:added "4.1"}
  ([] (generate-inventory {}))
  ([{:keys [project path write]
     :or {project (project/project)
          write false}}]
   (let [out-path  (inventory-path {:project project :path path})
         existing  (read-inventory out-path)
         entries   (inventory-entries {:existing existing})
         content   (render-inventory entries)
         original  (when (fs/exists? out-path)
                     (slurp out-path))
         updated   (not= original content)]
     (when write
       (fs/create-directory (fs/parent out-path))
       (spit out-path content))
     {:path out-path
      :count (count entries)
      :updated updated
      :entries entries})))

(defn- pad-right
  [s width]
  (let [s (str s)]
    (if (< (count s) width)
      (str s (apply str (repeat (- width (count s)) " ")))
      s)))

(defn- render-status
  [status]
  (get +status-mark+ status "?"))

(defn- render-summary*
  [{:keys [languages summary]}]
  (let [rows   (for [lang languages]
                 [(name lang)
                  (str (get-in summary [lang :implemented]))
                  (str (get-in summary [lang :abstract]))
                  (str (get-in summary [lang :missing]))])
        table  (cons ["language" "implemented" "abstract" "missing"] rows)
        widths (apply map max (map #(map count %) table))]
    (str/join
     "\n"
     (map (fn [row]
            (str/join "  "
                      (map pad-right row widths)))
          table))))

(defn- render-matrix*
  [{:keys [languages features status]}]
  (let [rows   (for [feature features]
                 (cons (name feature)
                       (for [lang languages]
                         (render-status (get-in status [lang feature])))))
        table  (cons (cons "feature" (map name languages)) rows)
        widths (apply map max (map #(map count %) table))]
    (str/join
     "\n"
     (concat
      ["Y=implemented  A=abstract-only  .=missing"]
      [(str/join "  "
                 (map pad-right (first table) widths))]
      (map (fn [row]
             (str/join "  "
                       (map pad-right row widths)))
           (rest table))))))

(defn render-support
  "Renders a summary or matrix view of xtalk support.

   Options:
   - `:langs`
   - `:features`
   - `:view`     either `:summary` or `:matrix`"
  {:added "4.1"}
  ([] (render-support {}))
  ([{:keys [view]
     :or {view :summary}
     :as opts}]
   (let [matrix (support opts)]
     (case view
       :matrix  (render-matrix* matrix)
       :summary (render-summary* matrix)
       (throw (ex-info "Unsupported xtalk support view"
                       {:view view
                        :supported #{:summary :matrix}}))))))
