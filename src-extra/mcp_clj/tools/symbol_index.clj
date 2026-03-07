(ns mcp-clj.tools.symbol-index
  "Clojure symbol indexing and search for foundation library"
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [clojure.edn :as edn])
  (:import
    [java.io File]
    [java.nio.file Paths Path Files FileVisitResult SimpleFileVisitor]
    [java.nio.file.attribute BasicFileAttributes]))

;; Simple in-memory index
(defonce ^:private symbol-index (atom {}))
(defonce ^:private file-index (atom {}))

;; ============================================================================
;; File Discovery
;; ============================================================================

(defn- clojure-file? [^Path path]
  (let [name (str (.getFileName path))]
    (or (str/ends-with? name ".clj")
        (str/ends-with? name ".cljc")
        (str/ends-with? name ".cljs"))))

(defn- find-clojure-files [^String root-path]
  (let [results (atom [])]
    (Files/walkFileTree
      (Paths/get root-path (into-array String []))
      (proxy [SimpleFileVisitor] []
        (visitFile [path attrs]
          (when (clojure-file? path)
            (swap! results conj (str path)))
          FileVisitResult/CONTINUE)
        (preVisitDirectory [dir attrs]
          (let [dir-name (str (.getFileName dir))]
            (if (or (= dir-name ".git")
                    (= dir-name "target")
                    (= dir-name "node_modules")
                    (= dir-name ".clj-kondo"))
              FileVisitResult/SKIP_SUBTREE
              FileVisitResult/CONTINUE)))))
    @results))

;; ============================================================================
;; Simple Symbol Extraction
;; ============================================================================

(defn- read-file-content [path]
  (try
    (slurp path)
    (catch Exception e
      (println "Error reading" path ":" (.getMessage e))
      nil)))

(defn- extract-ns-decl [content]
  (when content
    (when-let [match (re-find #"\(ns\s+([a-zA-Z0-9.-]+)" content)]
      (second match))))

(defn- extract-def-forms [content file-path]
  (when content
    (let [forms (atom [])
          ns (or (extract-ns-decl content) "unknown")]
      ;; Extract defn forms
      (doseq [[_ name] (re-seq #"\(defn\.?\s+\^?\{[^}]*\}\s*([a-zA-Z0-9*-]+)" content)]
        (swap! forms conj {:name name
                          :kind "function"
                          :ns ns
                          :file file-path
                          :qualified-name (str ns "/" name)}))
      ;; Extract defn- forms (private)
      (doseq [[_ name] (re-seq #"\(defn-\s+\^?\{[^}]*\}\s*([a-zA-Z0-9*-]+)" content)]
        (swap! forms conj {:name name
                          :kind "function"
                          :ns ns
                          :file file-path
                          :qualified-name (str ns "/" name)
                          :private true}))
      ;; Extract def forms (constants/vars)
      (doseq [[_ name] (re-seq #"\(def\s+(?!n\s)(?!macro\s)(?!multi\s)(?!method\s)(?!record\s)(?!type\s)([a-zA-Z0-9*-]+)" content)]
        (swap! forms conj {:name name
                          :kind "var"
                          :ns ns
                          :file file-path
                          :qualified-name (str ns "/" name)}))
      ;; Extract defmacro forms
      (doseq [[_ name] (re-seq #"\(defmacro\s+([a-zA-Z0-9*-]+)" content)]
        (swap! forms conj {:name name
                          :kind "macro"
                          :ns ns
                          :file file-path
                          :qualified-name (str ns "/" name)}))
      ;; Extract deftype/defrecord forms
      (doseq [[_ name] (re-seq #"\(def(?:type|record)\.?\s+\^?\{[^}]*\}\s*([a-zA-Z0-9]+)" content)]
        (swap! forms conj {:name name
                          :kind "type"
                          :ns ns
                          :file file-path
                          :qualified-name (str ns "/" name)}))
      ;; Extract protocol forms
      (doseq [[_ name] (re-seq #"\(defprotocol\s+([a-zA-Z0-9]+)" content)]
        (swap! forms conj {:name name
                          :kind "protocol"
                          :ns ns
                          :file file-path
                          :qualified-name (str ns "/" name)}))
      @forms)))

;; ============================================================================
;; Indexing
;; ============================================================================

(defn index-directory
  "Index all Clojure files in a directory"
  [root-path & {:keys [exclude] :or {exclude []}}]
  (println "Indexing Clojure files in:" root-path)
  (let [files (find-clojure-files root-path)
        _ (println "Found" (count files) "Clojure files")
        symbols (atom [])
        file-data (atom {})]
    (doseq [file files]
      (when-not (some #(str/includes? file %) exclude)
        (let [content (read-file-content file)
              ns (extract-ns-decl content)
              file-symbols (extract-def-forms content file)]
          (swap! file-data assoc file {:namespace ns
                                        :symbol-count (count file-symbols)
                                        :path file})
          (swap! symbols concat file-symbols))))
    (reset! symbol-index (group-by :qualified-name @symbols))
    (reset! file-index @file-data)
    {:files-indexed (count files)
     :symbols-found (count @symbols)
     :root-path root-path}))

(defn clear-index []
  (reset! symbol-index {})
  (reset! file-index {})
  {:cleared true})

(defn get-index-stats []
  {:symbol-count (count (keys @symbol-index))
   :file-count (count (keys @file-index))
   :total-symbols (reduce + (map count (vals @symbol-index)))})

;; ============================================================================
;; Search
;; ============================================================================

(defn search-symbols
  "Search for symbols matching query"
  [query & {:keys [kind ns file exact?]}]
  (let [query-lower (str/lower-case query)
        matches (atom [])]
    (doseq [[qualified-name syms] @symbol-index]
      (doseq [sym syms]
        (when (and (or (and exact? (= (:name sym) query))
                       (and (not exact?)
                            (or (str/includes? (str/lower-case (:name sym)) query-lower)
                                (str/includes? (str/lower-case qualified-name) query-lower))))
                   (or (nil? kind) (= (:kind sym) kind))
                   (or (nil? ns) (= (:ns sym) ns))
                   (or (nil? file) (str/includes? (:file sym) file)))
          (swap! matches conj sym))))
    (sort-by :qualified-name @matches)))

(defn get-symbol-by-qualified-name
  "Get full symbol details by qualified name"
  [qualified-name]
  (first (get @symbol-index qualified-name)))

(defn get-file-outline
  "Get all symbols in a file"
  [file-path]
  (->> (vals @symbol-index)
       (apply concat)
       (filter #(= (:file %) file-path))
       (sort-by :name)))

(defn get-file-content
  "Get file content with line count"
  [file-path]
  (when-let [content (read-file-content file-path)]
    {:path file-path
     :content content
     :line-count (count (str/split-lines content))}))

;; ============================================================================
;; MCP Tool Implementation
;; ============================================================================

(defn index-directory-tool
  "MCP tool implementation for index-directory"
  [_context {:keys [path exclude]}]
  (try
    (let [result (index-directory path :exclude (or exclude []))]
      {:content [{:type "text"
                  :text (str "Indexed Clojure directory:\n"
                             "- Files: " (:files-indexed result) "\n"
                             "- Symbols: " (:symbols-found result) "\n"
                             "- Path: " (:root-path result))}]
       :isError false})
    (catch Exception e
      {:content [{:type "text"
                  :text (str "Error indexing directory: " (.getMessage e))}]
       :isError true})))

(defn search-symbols-tool
  "MCP tool implementation for search-symbols"
  [_context {:keys [query kind ns file exact]}]
  (try
    (let [results (search-symbols query
                                   :kind kind
                                   :ns ns
                                   :file file
                                   :exact? exact)]
      (if (seq results)
        {:content [{:type "text"
                    :text (str "Found " (count results) " symbols:\n\n"
                               (str/join "\n"
                                 (for [sym results]
                                   (str "- " (:qualified-name sym)
                                        " (" (:kind sym) ")"
                                        " [" (:file sym) "]"))))}]
         :isError false}
        {:content [{:type "text"
                    :text (str "No symbols found matching: " query)}]
         :isError false}))
    (catch Exception e
      {:content [{:type "text"
                  :text (str "Error searching symbols: " (.getMessage e))}]
       :isError true})))

(defn get-symbol-tool
  "MCP tool implementation for get-symbol"
  [_context {:keys [qualified-name]}]
  (try
    (if-let [sym (get-symbol-by-qualified-name qualified-name)]
      (let [file-data (get-file-content (:file sym))
            content (:content file-data)]
        {:content [{:type "text"
                    :text (str "Symbol: " (:qualified-name sym) "\n"
                               "Kind: " (:kind sym) "\n"
                               "Namespace: " (:ns sym) "\n"
                               "File: " (:file sym) "\n"
                               (when (:private sym) "Private: true\n")
                               "\n--- File Content ---\n"
                               (if content
                                 (str "(first 50 lines)\n"
                                      (str/join "\n" (take 50 (str/split-lines content))))
                                 "Could not read file"))}]
         :isError false})
      {:content [{:type "text"
                  :text (str "Symbol not found: " qualified-name)}]
       :isError false})
    (catch Exception e
      {:content [{:type "text"
                  :text (str "Error getting symbol: " (.getMessage e))}]
       :isError true})))

(defn get-file-outline-tool
  "MCP tool implementation for get-file-outline"
  [_context {:keys [file-path]}]
  (try
    (let [symbols (get-file-outline file-path)]
      {:content [{:type "text"
                  :text (str "File: " file-path "\n"
                             "Symbols found: " (count symbols) "\n\n"
                             (str/join "\n"
                               (for [sym symbols]
                                 (str "- " (:name sym) " (" (:kind sym) ")"))))}]
       :isError false})
    (catch Exception e
      {:content [{:type "text"
                  :text (str "Error getting file outline: " (.getMessage e))}]
       :isError true})))

(defn get-index-stats-tool
  "MCP tool implementation for get-index-stats"
  [_context _args]
  (try
    (let [stats (get-index-stats)]
      {:content [{:type "text"
                  :text (str "Index Statistics:\n"
                             "- Unique symbols: " (:symbol-count stats) "\n"
                             "- Files indexed: " (:file-count stats) "\n"
                             "- Total symbols: " (:total-symbols stats))}]
       :isError false})
    (catch Exception e
      {:content [{:type "text"
                  :text (str "Error getting stats: " (.getMessage e))}]
       :isError true})))

;; ============================================================================
;; Tool Definitions
;; ============================================================================

(def clojure-index-tool
  {:name "clojure-index"
   :description "Indexes a directory of Clojure code for symbol search"
   :inputSchema {:type "object"
                 :properties {"path" {:type "string"
                                      :description "Path to directory to index"}
                              "exclude" {:type "array"
                                         :items {:type "string"}
                                         :description "Patterns to exclude"}}
                 :required ["path"]}
   :implementation index-directory-tool})

(def clojure-search-tool
  {:name "clojure-search"
   :description "Search for symbols in indexed Clojure code"
   :inputSchema {:type "object"
                 :properties {"query" {:type "string"
                                       :description "Search query"}
                              "kind" {:type "string"
                                      :description "Filter by kind: function, var, macro, type, protocol"}
                              "ns" {:type "string"
                                    :description "Filter by namespace"}
                              "file" {:type "string"
                                      :description "Filter by file path pattern"}
                              "exact" {:type "boolean"
                                       :description "Exact match only"}}
                 :required ["query"]}
   :implementation search-symbols-tool})

(def clojure-get-symbol-tool
  {:name "clojure-get-symbol"
   :description "Get details of a specific symbol by qualified name"
   :inputSchema {:type "object"
                 :properties {"qualified-name" {:type "string"
                                                :description "Fully qualified symbol name (e.g., std.lang/emit)"}}
                 :required ["qualified-name"]}
   :implementation get-symbol-tool})

(def clojure-file-outline-tool
  {:name "clojure-file-outline"
   :description "Get all symbols defined in a file"
   :inputSchema {:type "object"
                 :properties {"file-path" {:type "string"
                                           :description "Path to Clojure file"}}
                 :required ["file-path"]}
   :implementation get-file-outline-tool})

(def clojure-index-stats-tool
  {:name "clojure-index-stats"
   :description "Get statistics about the current index"
   :inputSchema {:type "object"
                 :properties {}}
   :implementation get-index-stats-tool})
