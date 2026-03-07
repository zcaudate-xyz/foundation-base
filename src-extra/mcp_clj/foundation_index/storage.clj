(ns mcp-clj.foundation-index.storage
  "SQLite storage layer for Clojure symbol index"
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.string :as str]
    [clojure.data.json :as json])
  (:import
    [java.sql DriverManager]))

;; ============================================================================
;; Helper Functions
;; ============================================================================

(defn- db-row->sym
  "Convert database row (snake_case) to symbol map (kebab-case)"
  [row]
  (when row
    (-> (into {} (map (fn [[k v]] [(keyword (str/replace (name k) #"_" "-")) v])) row)
        (update :arglists #(when % (try (json/read-str %) (catch Exception _ %))))
        (update :meta #(when % (try (json/read-str %) (catch Exception _ %)))))))

;; ============================================================================
;; Schema
;; ============================================================================

(def db-schema
  "SQLite schema for symbol index"
  [
   ;; Files table - tracks indexed files and their hashes
   "CREATE TABLE IF NOT EXISTS files (
     path TEXT PRIMARY KEY,
     hash TEXT NOT NULL,
     namespace TEXT,
     indexed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     symbol_count INTEGER DEFAULT 0
   )"
   
   ;; Symbols table - all Clojure symbols
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
     meta TEXT,
     FOREIGN KEY (file) REFERENCES files(path)
   )"
   
   ;; FTS5 virtual table for full-text search
   "CREATE VIRTUAL TABLE IF NOT EXISTS symbols_fts USING fts5(
     name, docstring, namespace,
     content='symbols',
     content_rowid='id'
   )"
   
   ;; References table - cross-references between symbols
   "CREATE TABLE IF NOT EXISTS symbol_refs (
     id INTEGER PRIMARY KEY AUTOINCREMENT,
     from_symbol TEXT NOT NULL,
     to_symbol TEXT NOT NULL,
     file TEXT NOT NULL,
     line INTEGER,
     column INTEGER,
     context TEXT,
     FOREIGN KEY (file) REFERENCES files(path)
   )"
   
   ;; Namespaces table - namespace metadata
   "CREATE TABLE IF NOT EXISTS namespaces (
     name TEXT PRIMARY KEY,
     file TEXT NOT NULL,
     docstring TEXT,
     author TEXT,
     added TEXT,
     FOREIGN KEY (file) REFERENCES files(path)
   )"
   
   ;; Index metadata
   "CREATE TABLE IF NOT EXISTS index_meta (
     key TEXT PRIMARY KEY,
     value TEXT,
     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   )"
   
   ;; Indexes for performance
   "CREATE INDEX IF NOT EXISTS idx_symbols_ns ON symbols(namespace)"
   "CREATE INDEX IF NOT EXISTS idx_symbols_file ON symbols(file)"
   "CREATE INDEX IF NOT EXISTS idx_symbols_kind ON symbols(kind)"
   "CREATE INDEX IF NOT EXISTS idx_refs_from ON symbol_refs(from_symbol)"
   "CREATE INDEX IF NOT EXISTS idx_refs_to ON symbol_refs(to_symbol)"
   ])

;; ============================================================================
;; Connection Management
;; ============================================================================

(def ^:private db-connection (atom nil))

(defn- db-path []
  (or (System/getenv "CLOJURE_INDEX_DB")
      ".clojure-mcp/symbol-index.db"))

(defn- ensure-dir [path]
  (let [file (java.io.File. path)]
    (.mkdirs (.getParentFile file))
    path))

(defn get-connection
  "Get or create database connection"
  []
  (or @db-connection
      (let [path (ensure-dir (db-path))
            db-spec {:classname "org.sqlite.JDBC"
                    :subprotocol "sqlite"
                    :subname path}]
        (Class/forName "org.sqlite.JDBC")
        (let [conn db-spec]
          (reset! db-connection conn)
          conn))))

(defn init-database!
  "Initialize database with schema"
  []
  (let [conn (get-connection)]
    (doseq [ddl db-schema]
      (jdbc/execute! conn [ddl]))
    (jdbc/execute! conn 
      ["INSERT OR REPLACE INTO index_meta (key, value) VALUES (?, ?)"
       "version" "1.0"])
    {:initialized true :path (db-path)}))

(defn close-connection!
  "Close database connection"
  []
  (when-let [conn @db-connection]
    (reset! db-connection nil)
    {:closed true}))

;; ============================================================================
;; File Operations
;; ============================================================================

(defn file-hash-exists?
  "Check if file with given hash is already indexed"
  [file-path hash]
  (let [conn (get-connection)
        result (jdbc/query conn
                 ["SELECT 1 FROM files WHERE path = ? AND hash = ?"
                  file-path hash])]
    (boolean (seq result))))

(defn get-file-hash
  "Get stored hash for a file"
  [file-path]
  (let [conn (get-connection)
        result (jdbc/query conn
                 ["SELECT hash FROM files WHERE path = ?"
                  file-path]
                 {:result-set-fn first})]
    (:hash result)))

(defn get-indexed-files
  "Get all indexed files"
  []
  (let [conn (get-connection)]
    (jdbc/query conn ["SELECT * FROM files ORDER BY path"])))

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
  "Delete all symbols for a file (for re-indexing)"
  [file-path]
  (let [conn (get-connection)]
    ;; Delete from FTS first (maintains consistency)
    (jdbc/execute! conn
      ["DELETE FROM symbols_fts WHERE rowid IN 
        (SELECT id FROM symbols WHERE file = ?)"
       file-path])
    ;; Delete symbols
    (jdbc/execute! conn
      ["DELETE FROM symbols WHERE file = ?" file-path])
    ;; Delete references
    (jdbc/execute! conn
      ["DELETE FROM symbol_refs WHERE file = ?" file-path])
    ;; Delete file record
    (jdbc/execute! conn
      ["DELETE FROM files WHERE path = ?" file-path])))

;; ============================================================================
;; Symbol Operations
;; ============================================================================

(defn insert-symbol!
  "Insert a symbol into the database"
  [symbol-data]
  (let [conn (get-connection)
        ;; Convert kebab-case keys to snake_case for database
        sym (into {} (map (fn [[k v]] [(keyword (str/replace (name k) #"-" "_")) v])) symbol-data)
        {:keys [name qualified_name kind namespace file line column
                end_line end_column docstring arglists private
                deprecated added meta]} sym
        arglists-json (when arglists (json/write-str arglists))
        meta-json (when meta (json/write-str meta))]
    (try
      (let [result (jdbc/execute! conn
                     ["INSERT INTO symbols 
                       (name, qualified_name, kind, namespace, file, 
                        line, column, end_line, end_column,
                        docstring, arglists, private, deprecated, added, meta)
                       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                       name qualified_name kind namespace file
                       line column end_line end_column
                       docstring arglists-json private deprecated added meta-json]
                     {:return-keys true})
            row-id (-> result first :id (or 1))]
        ;; Insert into FTS
        (jdbc/execute! conn
          ["INSERT INTO symbols_fts (rowid, name, docstring, namespace)
            VALUES (?, ?, ?, ?)"
           row-id name (or docstring "") namespace])
        {:inserted true :id row-id})
      (catch Exception e
        ;; Symbol might already exist, update it
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

(defn search-symbols
  "Search symbols with filters"
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
               ;; Full-text search
               (let [fts-query (if (str/includes? query " ")
                                (str "\"" query "\"")  ; phrase search
                                (str query "*"))       ; prefix search
                     base-sql (str "SELECT s.* FROM symbols s
                                   JOIN symbols_fts fts ON s.id = fts.rowid
                                   WHERE symbols_fts MATCH ?")
                     filters (cond-> []
                               kind (conj (str "AND s.kind = '" kind "'"))
                               namespace (conj (str "AND s.namespace = '" namespace "'"))
                               file (conj (str "AND s.file LIKE '%" file "%'")))
                     sql (str base-sql " " (str/join " " filters)
                             " LIMIT ? OFFSET ?")]
                 (jdbc/query conn [sql fts-query limit offset])))]
    (map db-row->sym rows)))

(defn get-symbol-by-qualified-name
  "Get full symbol details"
  [qualified-name]
  (let [conn (get-connection)
        result (jdbc/query conn
                 ["SELECT * FROM symbols WHERE qualified_name = ?"
                  qualified-name]
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

;; ============================================================================
;; Namespace Operations
;; ============================================================================

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
                                   {:result-set-fn first})
                      :c)
        symbol-count (-> (jdbc/query conn ["SELECT COUNT(*) as c FROM symbols"]
                                     {:result-set-fn first})
                        :c)
        ns-count (-> (jdbc/query conn ["SELECT COUNT(DISTINCT namespace) as c FROM symbols"]
                                 {:result-set-fn first})
                    :c)
        kinds (jdbc/query conn ["SELECT kind, COUNT(*) as count FROM symbols GROUP BY kind"])]
    {:files file-count
     :symbols symbol-count
     :namespaces ns-count
     :by-kind (into {} (map #(vector (keyword (:kind %)) (:count %))) kinds)}))

(defn clear-index!
  "Clear all index data"
  []
  (let [conn (get-connection)]
    (jdbc/execute! conn ["DELETE FROM symbols_fts"])
    (jdbc/execute! conn ["DELETE FROM symbol_refs"])
    (jdbc/execute! conn ["DELETE FROM symbols"])
    (jdbc/execute! conn ["DELETE FROM namespaces"])
    (jdbc/execute! conn ["DELETE FROM files"])
    (jdbc/execute! conn ["DELETE FROM index_meta"])
    {:cleared true}))
