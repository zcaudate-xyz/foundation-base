(ns app.manage.units
  "Read-only functional-unit/test indexing through code.framework and code.manage."
  (:require [app.manage :as manage]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [code.framework :as framework]
            [code.framework.test.fact :as fact]
            [code.manage.unit :as unit]
            [code.test.base.runtime :as runtime]
            [std.block.navigate :as nav]))

(defn files
  [project roots]
  (vec (sort
        (distinct
         (mapcat (fn [path]
                   (let [root (manage/project-file project path)]
                     (for [file (if (.isDirectory root)
                                  (with-open [stream (java.nio.file.Files/walk
                                                      (.toPath root)
                                                      (make-array java.nio.file.FileVisitOption 0))]
                                    (mapv #(.toFile %) (.toList stream)))
                                  [root])
                           :when (and (.isFile file) (str/ends-with? (str file) ".clj"))
                           :let [relative (str (.relativize (.toPath (manage/project-root project))
                                                            (.toPath file)))]]
                       ;; Revalidate children, including symlinks, before reading them.
                       (do (manage/project-file project relative) relative))))
          roots)))))

(defn snippet
  [text {:keys [row end-row]}]
  (str/join "\n"
            (take (inc (- (or end-row row 1) (or row 1)))
                  (drop (dec (or row 1)) (str/split-lines text)))))

(defn assertion-count
  [form]
  (cond (and (seq? form) (#{'quote 'comment 'clojure.core/comment} (first form))) 0
        (= form '=>) 1
        (coll? form) (+ (if (and (seq? form) (#{'is 'clojure.test/is} (first form))) 1 0)
                        (reduce + 0 (map assertion-count form)))
        :else 0))

(defn top-forms
  [text]
  (loop [node (nav/down (nav/parse-root text))
         out []]
    (if-not node
      out
      (recur (nav/right node)
             (cond-> out
               (nav/expression? node) (conj {:form (nav/value node)
                                             :line (nav/line-info node)}))))))

(defn aliases
  [requires]
  (into {}
        (for [spec requires
              :when (vector? spec)
              :let [options (apply hash-map (rest spec))]
              :when (:as options)]
          [(:as options) (first spec)])))

(defn call-symbols
  [form]
  (cond (and (seq? form) (#{'quote 'comment 'clojure.core/comment} (first form))) []
        (coll? form) (concat (when (and (seq? form) (symbol? (first form)))
                               [(first form)])
                             (mapcat call-symbols form))
        :else []))

(defn runtime-evidence
  [f scripts clj-aliases]
  (let [script-aliases (apply merge (map #(aliases (get-in % [:config :require])) scripts))
        resolve-call (fn [sym]
                       (if-let [owner (get (merge clj-aliases script-aliases)
                                           (some-> (namespace sym)
                                                   symbol))]
                         (str owner "/" (name sym))
                         (str sym)))
        calls (set (map resolve-call (call-symbols (:sexp f))))
        invoked? (contains? calls (str (:refer f)))
        nodes (tree-seq coll? seq (:sexp f))
        expected (keep-indexed (fn [i value]
                                 (when (= value '=>)
                                   (nth (vec nodes) (inc i) nil)))
                               nodes)
        weak? (and (seq expected)
                   (every? #(and (symbol? %)
                                 (#{"fn?" "ifn?" "some?" "map?" "vector?" "ptr?" "pointer?"}
                                  (name %)))
                           expected))]
    {:invokes-unit invoked?
     :pointer-only (boolean (and (not invoked?)
                                 (some #(re-find #"/(ptr\?|pointer\?|fn\?|ifn\?)$" %) calls)))
     :type-only-assertions (boolean weak?)
     :calls (vec (sort calls))
     :scripts (mapv #(dissoc % :config) scripts)}))

(defn analyse-file
  [project kind path]
  (let [text (slurp (manage/project-file project path))]
    (binding [*read-eval* false]
      (let [forms (top-forms text)
            scripts
            (for [{:keys [form line]} forms
                  :when (and (seq? form) (symbol? (first form)) (= "script-" (name (first form))))]
              {:language (name (second form))
               :runtime (some-> (get (nth form 2 {}) :runtime)
                                name)
               :config (nth form 2 {})
               :line line
               :code (snippet text line)})
            ns-form (:form (first (filter #(and (seq? (:form %)) (= 'ns (first (:form %)))) forms)))
            clj-aliases (aliases (mapcat rest
                                  (filter #(and (seq? %) (= :require (first %))) (drop 2 ns-form))))
            analysis (framework/analyse-file [kind (str (manage/project-file project path))])
            entries (for [[ns vars] analysis
                          [id entry] vars]
                      (assoc entry :path path :qualified (str ns "/" (:var entry))))
            test-ns (when (= kind :test)
                      (second (framework/read-ns-form (str (manage/project-file project path)))))
            facts (when (= kind :test)
                    (concat (keep fact/gather-fact (fact/top-level-fact-navs (nav/parse-root text)))
                      (for [{:keys [form line]} forms
                            :when (and (seq? form) (symbol? (first form)) (= "fact" (name (first form))))
                            :when (not (:refer (meta form)))]
                        {:sexp form :line line :intro (when (string? (second form)) (second form))
                         :id (or (:id (meta form)) (symbol (str "unassigned-" (:row line))))}))) ]
        {:path path
         :kind kind
         :entries (vec entries)
         :namespace (second ns-form)
         :declared (vec (for [{:keys [form]} forms
                              :when (and (seq? form)
                                         (symbol? (second form))
                                         (re-find #"^(def|deftype|defenum)" (str (first form))))]
                          (str (second ns-form) "/" (second form))))
         :facts (mapv (fn [f]
                        {:namespace (str test-ns)
                         :refer (str (:refer f))
                         :id (str (runtime/fact-id f))
                         :path path
                         :line (:line f)
                         :intro (:intro f)
                         :setup-line (some (fn [{:keys [form line]}]
                                             (when (and (seq? form) (symbol? (first form)) (= "fact:global" (name (first form)))) line)) forms)
                         :assertions (assertion-count (:sexp f))
                         :code (snippet text (:line f))
                         :runtime-evidence (runtime-evidence f scripts clj-aliases)})
                      facts)}))))

(defn fingerprint [project]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")
        paths (files project (concat (get-in project [:unit-analysis :sources]) (get-in project [:unit-analysis :tests])))]
    (doseq [path paths]
      (.update digest (.getBytes (str path "\n" (slurp (manage/project-file project path))) "UTF-8")))
    (doseq [path (:project-sources project)
            :let [file (manage/project-file project path)] :when (.isFile file)]
      (.update digest (.getBytes (str path "\n" (slurp file)) "UTF-8")))
    (doseq [path ["app/manage/testing.clj" "app/manage/integration.clj" "code/test/base/process.clj"]
            :let [resource (io/resource path)] :when resource]
      (.update digest (.getBytes (str path "\n" (slurp resource)) "UTF-8")))
    (.update digest (.getBytes (pr-str (select-keys project [:test-profiles :project-sources])) "UTF-8"))
    (.encodeToString (java.util.Base64/getEncoder) (.digest digest))))

(defn index
  [project]
  (let [config (:unit-analysis project)
        paths (concat (map #(vector :source %) (files project (:sources config)))
                      (map #(vector :test %) (files project (:tests config))))
        reports (mapv (fn [[kind path]]
                        (try (analyse-file project kind path)
                             (catch Exception e {:path path :kind kind :error (.getMessage e)})))
                      paths)
        sources (mapcat :entries (filter #(= :source (:kind %)) reports))
        facts (mapcat :facts reports)
        lookup (into {}
                     (for [report reports
                           :let [entry (first (:entries report))]
                           :when entry]
                       [(if (= :source (:kind report)) (:ns entry) (:namespace report))
                        (str (manage/project-file project (:path report)))]))
        ;; code.manage supplies its established missing/TODO policy. Analysis never
        ;; requires an application's source or executes a test.
        incomplete
        (into
         #{}
         (mapcat (fn [ns]
                   (try (map
                         #(str ns "/" %)
                         (unit/incomplete ns {} lookup {:root (str (manage/project-root project))}))
                        (catch Exception _ [])))
          (distinct (map :ns sources))))
        duplicates
        (set (for [[key xs] (group-by (juxt :namespace :id) facts) :when (> (count xs) 1)] key))
        linked (group-by :refer facts)
        units
        (mapv
         (fn [entry]
           (let [tests (vec (get linked (:qualified entry)))
                 excluded? (boolean (framework/no-test (:ns entry)
                                                       {}
                                                       lookup
                                                       {:root (str (manage/project-root project))}))
                 dsl? (boolean (re-find #"^\(defn\.(pg|js|xt|lua|py|c)\b"
                                        (get-in entry [:source :code] "")))
                 flags
                 (cond-> []
                   (empty? tests) (conj (if (some #(some #{(:qualified entry)} (get-in % [:runtime-evidence :calls])) facts) :missing-test-link :missing-test))
                   (and (seq tests) (every? #(get-in % [:runtime-evidence :pointer-only]) tests))
                   (conj :pointer-only-tests)
                   (and dsl?
                        (seq tests)
                        (not-any? #(seq (get-in % [:runtime-evidence :scripts])) tests))
                   (conj :missing-script-runtime)
                   (and dsl?
                        (seq tests)
                        (not-any? #(get-in % [:runtime-evidence :invokes-unit]) tests))
                   (conj :indirect-test-review)
                   (and (seq tests)
                        (every? #(get-in % [:runtime-evidence :type-only-assertions]) tests))
                   (conj :type-only-tests)
                   (some #(= "TODO" (:intro %)) tests) (conj :todo)
                   (some #(zero? (:assertions %)) tests) (conj :no-assertions)
                   (some #(duplicates [(:namespace %) (:id %)]) tests) (conj :duplicate-fact-id)
                   (and (seq tests) (incomplete (:qualified entry))) (conj :incomplete))]
             {:id (:qualified entry)
              :namespace (str (:ns entry))
              :name (str (:var entry))
              :requires-runtime dsl?
              :source {:path (:path entry) :line (get-in entry [:source :line])}
              :tests (mapv #(dissoc % :code) tests)
              :flags (if excluded? [] flags)
              :status (cond excluded? :excluded
                            (seq flags) :attention
                            :else :linked-not-run)}))
         sources)
        known (set (concat (map :id units)
                           (mapcat :declared (filter #(= :source (:kind %)) reports))))
        namespaces (set (map (comp str :namespace) (filter #(= :source (:kind %)) reports)))]
    {:fingerprint (fingerprint project)
     :enabled (boolean config)
     :units (vec (sort-by :id units))
     :summary {:units (count units)
               :attention (count (filter #(seq (:flags %)) units))
               :linked (count (filter #(= :linked-not-run (:status %)) units))
               :excluded (count (filter #(= :excluded (:status %)) units))}
     :diagnostics
     (vec (concat (for [report reports :when (:error report)] (select-keys report [:path :error]))
                  (for [f facts
                        :when (and (seq (:refer f)) (not (known (:refer f))))]
                    {:path (:path f)
                     :line (:line f)
                     :code (if (namespaces (namespace (symbol (:refer f))))
                             :orphaned-reference
                             :external-reference)
                     :refer (:refer f)})))
     :test-inventory (mapv (fn [f] (assoc (dissoc f :code)
                              :link-kind (if (seq (:refer f)) :declared :unassigned)
                              :inferred-references (vec (filter known (get-in f [:runtime-evidence :calls]))))) facts)
     :files (mapv #(select-keys % [:path :kind :namespace]) reports)
     :facts facts}))

(defn report
  [project query]
  (let [data (index project)]
    (if-let [id (:unit query)]
      (if-let [entry (some #(when (= id (:id %))
                              %)
                           (:units data))]
        (assoc entry
               :source (assoc (:source entry)
                              :code
                              (snippet (slurp (manage/project-file project
                                                                   (get-in entry [:source :path])))
                                       (get-in entry [:source :line])))
               :tests (mapv (fn [f] (-> f (assoc :link-kind (if (= id (:refer f)) :declared :inferred))
                                            (assoc-in [:runtime-evidence :invokes-unit] (boolean (some #{id} (get-in f [:runtime-evidence :calls]))))))
                                  (filter #(or (= id (:refer %)) (some #{id} (get-in % [:runtime-evidence :calls]))) (:facts data))))
        (throw (ex-info "Unknown functional unit" {:http-status 404})))
      (-> data
          (dissoc :facts)
          (update :units
                  (fn [units]
                    (filterv #(and (or (str/blank? (:q query)) (str/includes? (str/lower-case (:id %)) (str/lower-case (:q query))))
                                   (or (not= "true" (:issues query)) (seq (:flags %))))
                             units)))))))
