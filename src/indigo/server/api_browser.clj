(ns indigo.server.api-browser
  (:require [std.lang :as l]
            [std.lang.base.book :as book]
            [code.test.base.context :as context]
            [std.lib :as h]
            [std.string :as str]
            [code.project :as project]
            [code.test.base.runtime :as rt]
            [clojure.repl :as repl]
            [code.framework :as framework]))

;; Existing endpoints -------------------------------------------------------

(defn list-namespaces
  "lists all namespaces for a given language"
  {:added "4.0"}
  [lang]
  (let [book (l/get-book (l/default-library) (keyword lang))
        modules (book/list-entries book :module)]
    (->> modules
         (map str)
         (sort))))

(defn list-components
  "lists all components for a given namespace and language"
  {:added "4.0"}
  [lang ns]
  (let [book (l/get-book (l/default-library) (keyword lang))
        entry (book/get-module book (symbol ns))]
    (if entry
      (->> (concat (map (fn [[k v]] [k v :code]) (:code entry))
                   (map (fn [[k v]] [k v :fragment]) (:fragment entry)))
           (map (fn [[k v type]]
                  (let [op (:op v)]
                    {:name (str k)
                     :type type
                     :op   (str op)
                     :meta (:meta v)})))
           (sort-by :name))
      [])))

(defn get-any-entry
  "helper to get either code or fragment entry"
  [book ns component]
  (let [ns-sym (symbol ns)
        comp-sym (symbol component)]
    (or (book/get-base-entry book ns-sym comp-sym :code)
        (book/get-base-entry book ns-sym comp-sym :fragment))))

(defn get-component
  "gets the component source code"
  {:added "4.0"}
  [lang ns component]
  (let [book (l/get-book (l/default-library) (keyword lang))
        entry (get-any-entry book ns component)]
    (if entry
      (str (:form entry))
      "")))

;; Clojure Runtime Endpoints ------------------------------------------------

(defn scan-namespaces
  "scans all project files for l/script forms and returns a map of {language [namespaces]}"
  {:added "4.0"}
  []
  (try
    (let [files (project/all-files (:source-paths (project/project)))
          results (atom {})]
      (doseq [[ns-sym file-path] files]
        (try
          (let [content (slurp file-path)
                ;; Regex to find (l/script :<lang> ...)
                matches (re-seq #"\(\s*l/script\s+:([a-zA-Z0-9\-\.]+)" content)]
            (doseq [m matches]
              (let [lang (keyword (second m))
                    ns-str (str ns-sym)]
                (swap! results update lang (fnil conj []) ns-str))))
          (catch Throwable t
            (println "Error scanning file:" file-path (.getMessage t)))))

      ;; Sort namespaces for each language
      (reduce-kv (fn [m k v] (assoc m k (sort v))) {} @results))
    (catch Throwable t
      {:error (str "Scan failed: " (.getMessage t))})))

(defn list-clj-namespaces
  "lists all loaded clojure namespaces"
  {:added "4.0"}
  []
  (let [files (project/all-files
               (:source-paths (project/project)))
        namespaces (->> (keys files)
                        (map str)
                        (sort)
                        (vec))]
    namespaces))

(defn list-clj-vars
  "lists all public vars for a clojure namespace"
  {:added "4.0"}
  [ns-str]
  (if (empty? ns-str)
    []
    (let [ns-sym (symbol ns-str)]
      (try (require ns-sym) (catch Throwable t))
      (if (find-ns ns-sym)
        (->> (ns-publics ns-sym)
             (keys)
             (map str)
             (sort))
        []))))

(defn get-clj-var-source
  "gets the source code for a clojure var"
  {:added "4.0"}
  [ns-str var-str]
  (try
    (let [sym (symbol ns-str var-str)]
      (or (repl/source-fn sym)
          (str ";; Source not found for " sym)))
    (catch Throwable _
      (str ";; Error retrieving source for " ns-str "/" var-str))))

(defn get-namespace-source
  "gets the source code for a clojure namespace"
  {:added "4.0"}
  [ns-str]
  (try
    (let [project (project/project)
          paths   (concat (:source-paths project) (:test-paths project))
          files   (project/all-files paths)
          ns-sym  (symbol ns-str)
          file    (get files ns-sym)]
      (if file
        (slurp file)
        (str ";; File not found for namespace: " ns-str)))
    (catch Throwable t
      (str ";; Error retrieving source for " ns-str ": " (.getMessage t)))))

;; Test Registry Endpoints ------------------------------------------------

(defn list-test-namespaces
  "lists all namespaces with tests in the registry"
  {:added "4.0"}
  []
  (->> (keys @context/*registry*)
       (map str)
       (sort)))

(defn list-test-facts
  "lists all facts for a test namespace"
  {:added "4.0"}
  [ns-str]
  (let [ns-sym (symbol ns-str)]
    (try
      (binding [context/*eval-mode* false]
        (require ns-sym))
      (catch Throwable _))
    (if (get @context/*registry* ns-sym)
      (->> (get-in @context/*registry* [ns-sym :facts])
           (keys)
           (map str)
           (sort))
      [])))

(defn get-test-fact-source
  "gets the source code for a test fact"
  {:added "4.0"}
  [ns-str fact-id]
  (let [ns-sym (symbol ns-str)
        fact-sym (symbol fact-id)
        fact (get-in @context/*registry* [ns-sym :facts fact-sym])]
    (if fact
      (try
        (h/pp-str (:full fact))
        (catch Throwable t
          (str ";; Error formatting source: " (.getMessage t))))
      (str ";; Fact not found: " ns-str "/" fact-id))))

(defn list-tests-for-var
  "lists all tests that refer to a specific var"
  {:added "4.0"}
  [ns-str var-str]
  (let [test-ns-sym (symbol (str ns-str "-test"))
        var-sym     (symbol var-str)]
    (try
      (binding [context/*eval-mode* false]
        (require test-ns-sym))
      (catch Throwable _))
    (if (get @context/*registry* test-ns-sym)
      (->> (get-in @context/*registry* [test-ns-sym :facts])
           (vals)
           (filter (fn [fact]
                     (= (:refer fact) var-sym)))
           (map :id)
           (map str)
           (sort))
      [])))

;; New endpoints -----------------------------------------------------------

(defn list-libraries
  "returns the set of languages for which a std.lang library is available"
  {:added "4.0"}
  []
  (let [libs (l/default-library)]
    (->> libs
         (keys)
         (map name)
         (sort))))

(defn component-metadata
  "returns metadata for a specific component (if any). Returns a map with keys such as :doc, :meta, :type etc."
  {:added "4.0"}
  [lang ns component]
  (let [book (l/get-book (l/default-library) (keyword lang))
        entry (get-any-entry book ns component)]
    (if entry
      (select-keys entry [:doc :meta :type :form])
      {})))

(defn component-preview
  "returns the compiled JavaScript source for a component"
  {:added "4.0"}
  [lang ns component]
  (get-component lang ns component))

(defn emit-component
  "emits the component source code as the target language"
  {:added "4.0"}
  [lang ns component]
  (let [book (l/get-book (l/default-library) (keyword lang))
        entry (get-any-entry book ns component)]
    (if entry
      (try
        (l/emit-as (keyword lang) (list (:form entry)))
        (catch Throwable t
          (str "// Error emitting code: " (.getMessage t))))
      "// Component not found")))

(defn search-components
  "searches component names across all namespaces for a given language.
   Returns a vector of maps {:ns <namespace> :component <name>} matching the query (case‑insensitive substring).
  "
  {:added "4.0"}
  [lang query]
  (let [book (l/get-book (l/default-library) (keyword lang))
        modules (book/list-entries book :module)
        q (str/lower-case query)
        matches (for [mod modules
                      :let [ns (name mod)
                            entry (book/get-module book (symbol ns))
                            comps (keys (:code entry))]
                      comp comps
                      :let [cname (name comp)]
                      :when (str/includes? (str/lower-case cname) q)]
                  {:ns ns :component cname})]
    (vec matches)))

(defn save-namespace-source
  "saves the source code for a clojure namespace"
  {:added "4.0"}
  [ns-str source]
  (try
    (let [files (project/all-files
                 (:source-paths (project/project)))
          ns-sym (symbol ns-str)
          file   (get files ns-sym)]
      (if file
        (do
          (spit file source)
          {:status "ok" :message (str "Saved " ns-str)})
        (throw (Exception. (str "File not found for namespace: " ns-str)))))
    (catch Throwable t
      (throw (Exception. (str "Error saving source for " ns-str ": " (.getMessage t)))))))

(defn get-completions
  "gets completion suggestions for a prefix in a namespace"
  {:added "4.0"}
  [ns-str prefix]
  (try
    (let [ns-sym (symbol ns-str)
          _ (try (require ns-sym) (catch Throwable _))
          all-vars (concat (keys (ns-publics ns-sym))
                           (keys (ns-publics 'clojure.core))
                           (keys (ns-refers (find-ns ns-sym))))
          matches (->> all-vars
                       (map str)
                       (filter #(str/starts-with? % prefix))
                       (sort)
                       (distinct)
                       (take 50))]
      matches)
    (catch Throwable t
      [])))

(defn scaffold-test
  "scaffolds a test for a namespace"
  {:added "4.0"}
  [ns-str]
  (try
    (let [ns-sym (symbol ns-str)
          ;; Use code.manage to scaffold
          ;; We need to dynamically require code.manage to avoid circular deps if any,
          ;; or just require it at top level.
          _ (require 'code.manage)
          scaffold-fn (resolve 'code.manage/scaffold)]
      (if scaffold-fn
        (do
          (scaffold-fn ns-sym {:write true})
          {:status "ok" :message (str "Scaffolded test for " ns-str)})
        (throw (Exception. "code.manage/scaffold not found"))))
    (catch Throwable t
      {:status "error" :message (.getMessage t)})))

(defn get-doc-path
  "gets the documentation path for a namespace"
  {:added "4.0"}
  [ns-str]
  (try
    (require 'code.doc.executive)
    (let [all-pages-fn (resolve 'code.doc.executive/all-pages)
          make-project-fn (resolve 'code.doc/make-project)

          ;; Get all doc pages
          doc-project (make-project-fn)
          pages (all-pages-fn doc-project)

          ;; Heuristic: match namespace to page key suffix
          ;; e.g. code.doc -> code-doc, matches core/code-doc
          ns-slug (str/replace ns-str "." "-")
          page (some (fn [[k p]]
                       (let [k-str (str k)]
                         (when (or (= k-str ns-slug)
                                   (str/ends-with? k-str (str "/" ns-slug)))
                           p)))
                     pages)]
      (if page
        {:path (:input page)
         :found true}
        {:found false
         :message (str "No documentation found for " ns-str)}))
    (catch Throwable t
      {:found false
       :error (.getMessage t)})))

(defn get-file-content
  "gets the content of a file by path"
  {:added "4.0"}
  [path]
  (try
    (let [f (java.io.File. ^String path)]
      (if (.exists f)
        (slurp f)
        (str ";; File not found: " path)))
    (catch Throwable t
      (str ";; Error reading file: " (.getMessage t)))))

(defn get-namespace-entries
  "gets the entries (source and test) for a namespace"
  {:added "4.0"}
  [ns-str]
  (try
    (let [project (project/project)
          ns-sym (symbol ns-str)

          ;; Resolve source file
          source-path (or (get (project/all-files (:source-paths project)) ns-sym)
                          (get (project/all-files (:test-paths project)) ns-sym))

          ;; Resolve test file
          test-ns-sym (project/test-ns ns-sym)
          test-path   (get (project/all-files (:test-paths project)) test-ns-sym)

          ;; Analyse
          source-analysis (when source-path
                            (framework/analyse-source-code (slurp source-path)))
          test-analysis   (when test-path
                            (framework/analyse-test-code (slurp test-path)))

          ;; Merge
          source-entries (get source-analysis ns-sym {})
          test-entries   (or (get test-analysis ns-sym)
                             (get test-analysis test-ns-sym)
                             {})

          all-vars (sort (into (set (keys source-entries)) (keys test-entries)))

          entries (map (fn [v]
                         {:var (str v)
                          :source (get source-entries v)
                          :test   (get test-entries v)})
                       all-vars)]
      {:entries entries})
    (catch Throwable t
      {:error (.getMessage t)})))
