(ns indigo.server.api-browser
  (:require [std.lang :as l]
            [std.lang.base.book :as book]
            [std.lib :as h]
            [std.string :as str]
            [code.project :as project]
            [code.test.base.context :as context]
            [code.test.base.runtime :as rt]
            [clojure.repl :as repl]))

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
      (->> (:code entry)
           (keys)
           (map str)
           (sort))
      [])))

(defn get-component
  "gets the component source code"
  {:added "4.0"}
  [lang ns component]
  (let [book (l/get-book (l/default-library) (keyword lang))
        entry (book/get-code-entry book (symbol (str ns "/" component)))]
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
    (let [files (project/all-files
                 (:source-paths (project/project)))
          ns-sym (symbol ns-str)
          file   (get files ns-sym)]
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
        entry (book/get-code-entry book (symbol (str ns "/" component)))]
    (if entry
      (select-keys entry [:doc :meta :type :form])
      {})))

(defn component-preview
  "returns the compiled JavaScript source for a component, suitable for live rendering.
   For now it simply returns the DSL source (same as get-component) – the client can evaluate it.
   In the future this could invoke the std.lang compiler.
  "
  {:added "4.0"}
  [lang ns component]
  (get-component lang ns component))

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
