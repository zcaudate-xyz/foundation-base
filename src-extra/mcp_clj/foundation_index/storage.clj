(ns mcp-clj.foundation-index.storage
  (:require [clojure.data.json :as json]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :as str])
  "SQLite storage layer for Clojure symbol index"
  (:import [java.sql DriverManager]))

;; ============================================================================
;; Configuration
;; ============================================================================

(def ^:private db-path 
  (or (System/getenv "CLOJURE_INDEX_DB")
      ;; Use cache/foundation's database if it exists, otherwise local
      (if (.exists (java.io.File. "cache/foundation/.clojure-mcp/symbol-index.db"))
        "cache/foundation/.clojure-mcp/symbol-index.db"
        ".clojure-mcp/symbol-index.db")))

;; ============================================================================
;; Connection Management
;; ============================================================================

(def ^:private db-connection (atom nil))

(defn- ensure-dir [path]
  (let [file (java.io.File. path)]
    (.mkdirs (.getParentFile file))
    path))

(defn get-connection
  "Get or create database connection"
  []
  (or @db-connection
      (let [path (ensure-dir db-path)
            db-spec {:classname "org.sqlite.JDBC"
                    :subprotocol "sqlite"
                    :subname path}]
        (Class/forName "org.sqlite.JDBC")
        (reset! db-connection db-spec)
        db-spec)))

;; ============================================================================
;; Schema
;; ============================================================================

(def ^:private db-schema
  [
   ;; Files table
   "CREATE TABLE IF NOT EXISTS files (
     path TEXT PRIMARY KEY,
     hash TEXT NOT NULL,
     namespace TEXT,
     indexed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     symbol_count INTEGER DEFAULT 0
   )"
   
   ;; Symbols table
   "CREATE TABLE IF NOT EXISTS symbols (
     id INTEGER PRIMARY KEY AUTOINCREMENT,
     name TEXT NOT NULL,
     qualified_name TEXT UNIQUE NOT NULL,
     kind TEXT NOT NULL,
     namespace TEXT NOT NULL,
     file TEXT NOT NULL,
     line INTEGER,
     column INTEGER,
     end_line INTEGER,
     end_column INTEGER,
     docstring TEXT,
     arglists TEXT,
     private BOOLEAN DEFAULT 0,
     deprecated BOOLEAN DEFAULT 0,
     added TEXT,
     meta TEXT
   )"
   
   ;; Indexes
   "CREATE INDEX IF NOT EXISTS idx_symbols_ns ON symbols(namespace)"
   "CREATE INDEX IF NOT EXISTS idx_symbols_file ON symbols(file)"
   "CREATE INDEX IF NOT EXISTS idx_symbols_kind ON symbols(kind)"
   ])

(defn init-database!
  "Initialize database with schema"
  []
  (let [conn (get-connection)]
    (doseq [ddl db-schema]
      (jdbc/execute! conn [ddl]))
    {:initialized true :path db-path}))

;; ============================================================================
;; File Operations
;; ============================================================================

(defn get-file-hash
  "Get stored hash for a file"
  [file-path]
  (let [conn (get-connection)]
    (-> (jdbc/query conn
          ["SELECT hash FROM files WHERE path = ?" file-path]
          {:result-set-fn first})
        :hash)))

(defn upsert-file!
  "Insert or update file record"
  [file-data]
  (let [conn (get-connection)
        {:keys [path hash namespace symbol_count]} file-data]
    (jdbc/execute! conn
      ["INSERT INTO files (path, hash, namespace, symbol_count) 
        VALUES (?, ?, ?, ?)
        ON CONFLICT(path) DO UPDATE SET
          hash = excluded.hash,
          namespace = excluded.namespace,
          symbol_count = excluded.symbol_count,
          indexed_at = CURRENT_TIMESTAMP"
       path hash namespace (or symbol_count 0)])))

(defn delete-file-symbols!
  "Delete all symbols for a file"
  [file-path]
  (let [conn (get-connection)]
    (jdbc/execute! conn ["DELETE FROM symbols WHERE file = ?" file-path])
    (jdbc/execute! conn ["DELETE FROM files WHERE path = ?" file-path])))

;; ============================================================================
;; Symbol Operations
;; ============================================================================

(defn- db-row->sym
  "Convert database row (snake_case) to symbol map (kebab-case)"
  [row]
  (when row
    (-> (into {} (map (fn [[k v]] [(keyword (str/replace (name k) #"_" "-")) v])) row)
        (update :arglists #(when % (try (json/read-str %) (catch Exception _ %))))
        (update :meta #(when % (try (json/read-str %) (catch Exception _ %)))))))

(defn insert-symbol!
  "Insert a symbol into the database"
  [symbol-data]
  (let [conn (get-connection)
        ;; Convert kebab-case to snake_case for DB
        sym (into {} (map (fn [[k v]] [(keyword (str/replace (name k) #"-" "_")) v])) symbol-data)
        {:keys [name qualified_name kind namespace file line column
                end_line end_column docstring arglists private
                deprecated added meta]} sym
        arglists-json (when arglists (json/write-str arglists))
        meta-json (when meta (json/write-str meta))]
    (try
      (jdbc/execute! conn
        ["INSERT INTO symbols 
          (name, qualified_name, kind, namespace, file, 
           line, column, end_line, end_column,
           docstring, arglists, private, deprecated, added, meta)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
         name qualified_name kind namespace file
         line column end_line end_column
         docstring arglists-json private deprecated added meta-json])
      {:inserted true}
      (catch Exception e
        ;; Update existing
        (jdbc/execute! conn
          ["UPDATE symbols SET
             name = ?, kind = ?, namespace = ?, file = ?,
             line = ?, column = ?, end_line = ?, end_column = ?,
             docstring = ?, arglists = ?, private = ?, 
             deprecated = ?, added = ?, meta = ?
           WHERE qualified_name = ?"
           name kind namespace file
           line column end_line end_column
           docstring arglists-json private
           deprecated added meta-json qualified_name])
        {:updated true}))))

;; ============================================================================
;; Search
;; ============================================================================

(defn search-symbols
  "Search symbols using LIKE pattern matching"
  [{:keys [query kind namespace file exact? limit offset]
    :or {limit 50 offset 0}}]
  (let [conn (get-connection)
        rows (if exact?
               ;; Exact match on qualified name
               (jdbc/query conn
                 ["SELECT * FROM symbols 
                   WHERE qualified_name = ?
                   ORDER BY qualified_name
                   LIMIT ? OFFSET ?"
                  query limit offset])
               ;; Substring search with LIKE
               (let [pattern (str "%" query "%")
                     base-sql "SELECT * FROM symbols 
                               WHERE (name LIKE ? OR namespace LIKE ? OR docstring LIKE ?)"
                     filters (cond-> []
                               kind (conj (str "AND kind = '" kind "'"))
                               namespace (conj (str "AND namespace = '" namespace "'"))
                               file (conj (str "AND file LIKE '%" file "%'")))
                     sql (str base-sql " " (str/join " " filters)
                             " ORDER BY qualified_name
                             LIMIT ? OFFSET ?")]
                 (jdbc/query conn [sql pattern pattern pattern limit offset])))]
    (map db-row->sym rows)))

(defn get-symbol-by-qualified-name
  "Get symbol by qualified name"
  [qualified-name]
  (let [conn (get-connection)
        result (jdbc/query conn
                 ["SELECT * FROM symbols WHERE qualified_name = ?" qualified-name]
                 {:result-set-fn first})]
    (db-row->sym result)))

(defn get-file-symbols
  "Get all symbols in a file"
  [file-path]
  (let [conn (get-connection)
        rows (jdbc/query conn
               ["SELECT * FROM symbols WHERE file = ? ORDER BY line"
                file-path])]
    (map db-row->sym rows)))

(defn get-namespace-symbols
  "Get all symbols in a namespace"
  [ns-name]
  (let [conn (get-connection)
        rows (jdbc/query conn
               ["SELECT * FROM symbols WHERE namespace = ? ORDER BY name"
                ns-name])]
    (map db-row->sym rows)))

(defn list-namespaces
  "List all indexed namespaces"
  []
  (let [conn (get-connection)
        rows (jdbc/query conn ["SELECT DISTINCT namespace FROM symbols ORDER BY namespace"])]
    (map #(hash-map :name (:namespace %)) rows)))

;; ============================================================================
;; Statistics
;; ============================================================================

(defn get-stats
  "Get index statistics"
  []
  (let [conn (get-connection)
        file-count (-> (jdbc/query conn ["SELECT COUNT(*) as c FROM files"]
                                   {:result-set-fn first}) :c)
        symbol-count (-> (jdbc/query conn ["SELECT COUNT(*) as c FROM symbols"]
                                     {:result-set-fn first}) :c)
        ns-count (-> (jdbc/query conn ["SELECT COUNT(DISTINCT namespace) as c FROM symbols"]
                                 {:result-set-fn first}) :c)
        kinds (jdbc/query conn ["SELECT kind, COUNT(*) as count FROM symbols GROUP BY kind"])]
    {:files file-count
     :symbols symbol-count
     :namespaces ns-count
     :by-kind (into {} (map #(vector (keyword (:kind %)) (:count %))) kinds)}))

(defn clear-index!
  "Clear all index data"
  []
  (let [conn (get-connection)]
    (jdbc/execute! conn ["DELETE FROM symbols"])
    (jdbc/execute! conn ["DELETE FROM files"])
    {:cleared true}))
