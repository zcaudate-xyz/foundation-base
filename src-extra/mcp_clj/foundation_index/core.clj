(ns mcp-clj.foundation-index.core
  "Main coordinator for foundation library indexing"
  (:require
    [mcp-clj.foundation-index.storage :as storage]
    [mcp-clj.foundation-index.parser :as parser])
  (:import
    [java.util.concurrent.atomic AtomicInteger]))

;; ============================================================================
;; Index Management
;; ============================================================================

(defn init!
  "Initialize the index database"
  []
  (storage/init-database!))

(defn clear!
  "Clear the entire index"
  []
  (storage/clear-index!))

(defn stats
  "Get index statistics"
  []
  (storage/get-stats))

;; ============================================================================
;; Indexing Operations
;; ============================================================================

(defn index-file
  "Index a single file if changed"
  [file-path & {:keys [force?] :or {force? false}}]
  (let [current-hash (try (parser/file-hash file-path) (catch Exception _ ""))
        stored-hash (when-not force? (storage/get-file-hash file-path))]
    (if (and (not force?) (= current-hash stored-hash))
      {:file file-path :status :unchanged}
      (do
        ;; Delete old symbols if re-indexing
        (when stored-hash
          (storage/delete-file-symbols! file-path))
        ;; Parse and store
        (let [parsed (parser/parse-file file-path)]
          (storage/upsert-file!
            {:path file-path
             :hash current-hash
             :namespace (:namespace parsed)
             :symbol_count (:symbol-count parsed)})
          ;; Insert all symbols
          (doseq [sym (:symbols parsed)]
            (storage/insert-symbol! sym))
          {:file file-path
           :status (if stored-hash :updated :new)
           :symbols (:symbol-count parsed)})))))

(defn index-directory
  "Index all Clojure files in a directory"
  [root-path & {:keys [exclude force? progress-fn]
                :or {exclude [] force? false}}]
  (let [files (parser/find-clojure-files root-path :exclude exclude)
        total (count files)
        indexed (AtomicInteger. 0)
        updated (AtomicInteger. 0)
        unchanged (AtomicInteger. 0)
        errors (atom [])]
    
    (doseq [file files]
      (try
        (let [result (index-file file :force? force?)]
          (case (:status result)
            :new (.incrementAndGet indexed)
            :updated (.incrementAndGet updated)
            :unchanged (.incrementAndGet unchanged)
            nil)
          (when progress-fn
            (progress-fn (+ (.get indexed) (.get updated) (.get unchanged))
                        total
                        (:status result)
                        file)))
        (catch Exception e
          (swap! errors conj {:file file :error (.getMessage e)})
          (when progress-fn
            (progress-fn (+ (.get indexed) (.get updated) (.get unchanged))
                        total
                        :error
                        file)))))
    
    {:files-scanned total
     :new (.get indexed)
     :updated (.get updated)
     :unchanged (.get unchanged)
     :errors @errors
     :root-path root-path}))

(defn index-directories
  "Index multiple directories"
  [paths & opts]
  (let [results (mapv #(apply index-directory % opts) paths)]
    {:directories (count paths)
     :total-files (reduce + (map :files-scanned results))
     :total-new (reduce + (map :new results))
     :total-updated (reduce + (map :updated results))}))

;; ============================================================================
;; Search Operations
;; ============================================================================

(defn search
  "Search for symbols"
  [query & {:as opts}]
  (storage/search-symbols (merge {:query query} opts)))

(defn get-symbol
  "Get symbol by qualified name"
  [qualified-name]
  (storage/get-symbol-by-qualified-name qualified-name))

(defn get-file-symbols
  "Get all symbols in a file"
  [file-path]
  (storage/get-file-symbols file-path))

(defn get-namespace-symbols
  "Get all symbols in a namespace"
  [ns-name]
  (storage/get-namespace-symbols ns-name))

;; ============================================================================
;; High-level API
;; ============================================================================

(defn ensure-initialized
  "Ensure database is initialized"
  []
  (init!))

(defn quick-search
  "Quick symbol search with simple query"
  [query]
  (search query :limit 20))

(defn describe-symbol
  "Get detailed symbol description"
  [qualified-name]
  (when-let [sym (get-symbol qualified-name)]
    (let [file-syms (get-file-symbols (:file sym))]
      {:symbol sym
       :same-file (filter #(not= (:qualified-name %) qualified-name) file-syms)
       :file-info {:path (:file sym)
                   :namespace (:namespace sym)}})))

(defn list-namespaces
  "List all indexed namespaces"
  []
  (storage/list-namespaces))
