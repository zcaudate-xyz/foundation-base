(ns mcp-clj.tools.foundation-index
  (:require [mcp-clj.foundation-index.core :as index])
  "MCP tool implementations for foundation library indexing")

;; ============================================================================
;; Tool Implementations
;; ============================================================================

(defn clojure-index-tool
  "Index a directory of Clojure code"
  [_context {:keys [path exclude force]}]
  (try
    (index/ensure-initialized)
    (let [result (index/index-directory 
                   path 
                   :exclude (or exclude [])
                   :force? force)]
      {:content [{:type "text"
                  :text (str "Indexed Clojure directory: " path "\n"
                             "- Files scanned: " (:files-scanned result) "\n"
                             "- New symbols: " (:new result) "\n"
                             "- Updated: " (:updated result) "\n"
                             "- Unchanged: " (:unchanged result) "\n"
                             (when (seq (:errors result))
                               (str "- Errors: " (count (:errors result)) "\n")))}]
       :isError false})
    (catch Exception e
      {:content [{:type "text"
                  :text (str "Error indexing directory: " (.getMessage e))}]
       :isError true})))

(defn clojure-search-tool
  "Search for symbols in the index"
  [_context {:keys [query kind namespace file exact limit offset]
             :or {limit 50 offset 0}}]
  (try
    (index/ensure-initialized)
    (let [results (index/search 
                   query
                   :kind kind
                   :namespace namespace
                   :file file
                   :exact? exact
                   :limit limit
                   :offset offset)]
      (if (seq results)
        {:content [{:type "text"
                    :text (str "Found " (count results) " symbols:\n\n"
                               (clojure.string/join "\n"
                                 (for [sym results]
                                   (str "- " (:qualified-name sym)
                                        " (" (:kind sym) ")"
                                        (when (:arglists sym)
                                          (str " " (pr-str (:arglists sym))))
                                        "\n  " (:file sym) ":" (:line sym)
                                        (when (:docstring sym)
                                          (str "\n  \"" 
                                               (subs (:docstring sym) 0 (min 60 (count (:docstring sym))))
                                               (when (> (count (:docstring sym)) 60) "...")
                                               "\"")))))}]
         :isError false}
        {:content [{:type "text"
                    :text (str "No symbols found matching: " query)}]
         :isError false}))
    (catch Exception e
      {:content [{:type "text"
                  :text (str "Error searching symbols: " (.getMessage e))}]
       :isError true})))

(defn clojure-get-symbol-tool
  "Get full details of a specific symbol"
  [_context {:keys [qualified-name]}]
  (try
    (index/ensure-initialized)
    (if-let [info (index/describe-symbol qualified-name)]
      (let [sym (:symbol info)
            content (str "Symbol: " (:qualified-name sym) "\n"
                        "Kind: " (:kind sym) "\n"
                        "Namespace: " (:namespace sym) "\n"
                        "File: " (:file sym) 
                        (when (:line sym) (str ":" (:line sym))) "\n"
                        (when (:private sym) "Private: true\n")
                        (when (:deprecated sym) "Deprecated: true\n")
                        (when (:arglists sym)
                          (str "Args: " (pr-str (:arglists sym)) "\n"))
                        (when (:added sym)
                          (str "Added: " (:added sym) "\n"))
                        "\n--- Docstring ---\n"
                        (or (:docstring sym) "No docstring available."))]
        {:content [{:type "text" :text content}]
         :isError false})
      {:content [{:type "text"
                  :text (str "Symbol not found: " qualified-name)}]
       :isError false})
    (catch Exception e
      {:content [{:type "text"
                  :text (str "Error getting symbol: " (.getMessage e))}]
       :isError true})))

(defn clojure-file-outline-tool
  "Get all symbols defined in a file"
  [_context {:keys [file-path]}]
  (try
    (index/ensure-initialized)
    (let [symbols (index/get-file-symbols file-path)]
      {:content [{:type "text"
                  :text (str "File: " file-path "\n"
                             "Symbols found: " (count symbols) "\n\n"
                             (clojure.string/join "\n"
                               (for [sym symbols]
                                 (str "- [" (:kind sym) "] " (:name sym)
                                      (when (:line sym) 
                                        (str " (line " (:line sym) ")"))
                                      (when (:arglists sym)
                                        (str " " (pr-str (:arglists sym)))))))))}]
       :isError false})
    (catch Exception e
      {:content [{:type "text"
                  :text (str "Error getting file outline: " (.getMessage e))}]
       :isError true})))

(defn clojure-index-stats-tool
  "Get statistics about the index"
  [_context _args]
  (try
    (index/ensure-initialized)
    (let [stats (index/stats)]
      {:content [{:type "text"
                  :text (str "Index Statistics:\n"
                             "- Files indexed: " (:files stats) "\n"
                             "- Total symbols: " (:symbols stats) "\n"
                             "- Unique namespaces: " (:namespaces stats) "\n"
                             "\nBy kind:\n"
                             (clojure.string/join "\n"
                               (for [[kind count] (:by-kind stats)]
                                 (str "  " (name kind) ": " count))))}]
       :isError false})
    (catch Exception e
      {:content [{:type "text"
                  :text (str "Error getting stats: " (.getMessage e))}]
       :isError true})))

(defn clojure-list-namespaces-tool
  "List all indexed namespaces"
  [_context _args]
  (try
    (index/ensure-initialized)
    (let [nss (index/list-namespaces)]
      {:content [{:type "text"
                  :text (str "Indexed Namespaces (" (count nss) "):\n\n"
                             (clojure.string/join "\n"
                               (for [ns nss]
                                 (str "- " (:namespace ns)))))}]
       :isError false})
    (catch Exception e
      {:content [{:type "text"
                  :text (str "Error listing namespaces: " (.getMessage e))}]
       :isError true})))

;; ============================================================================
;; Tool Definitions
;; ============================================================================

(def clojure-index-mcp-tool
  {:name "clojure-index"
   :description "Indexes a directory of Clojure code for symbol search. Creates or updates the symbol database."
   :inputSchema {:type "object"
                 :properties {"path" {:type "string"
                                      :description "Path to directory to index (e.g., 'cache/foundation/src')"}
                              "exclude" {:type "array"
                                         :items {:type "string"}
                                         :description "File patterns to exclude (e.g., ['**/test/**', '**/target/**'])"}
                              "force" {:type "boolean"
                                       :description "Force re-index even if unchanged"}}
                 :required ["path"]}
   :implementation clojure-index-tool})

(def clojure-search-mcp-tool
  {:name "clojure-search"
   :description "Search for symbols in the indexed Clojure code using full-text search"
   :inputSchema {:type "object"
                 :properties {"query" {:type "string"
                                       :description "Search query (supports wildcards with *)"}
                              "kind" {:type "string"
                                      :description "Filter by kind: function, var, macro, type, protocol, multimethod"}
                              "namespace" {:type "string"
                                          :description "Filter by namespace (exact match)"}
                              "file" {:type "string"
                                      :description "Filter by file path pattern"}
                              "exact" {:type "boolean"
                                       :description "Exact match on qualified name only"}
                              "limit" {:type "integer"
                                       :default 50
                                       :description "Max results to return"}
                              "offset" {:type "integer"
                                        :default 0
                                        :description "Result offset for pagination"}}
                 :required ["query"]}
   :implementation clojure-search-tool})

(def clojure-get-symbol-mcp-tool
  {:name "clojure-get-symbol"
   :description "Get detailed information about a specific symbol by its qualified name"
   :inputSchema {:type "object"
                 :properties {"qualified-name" {:type "string"
                                                :description "Fully qualified symbol name (e.g., 'std.lang/emit')"}}
                 :required ["qualified-name"]}
   :implementation clojure-get-symbol-tool})

(def clojure-file-outline-mcp-tool
  {:name "clojure-file-outline"
   :description "Get all symbols defined in a specific file"
   :inputSchema {:type "object"
                 :properties {"file-path" {:type "string"
                                           :description "Path to Clojure file"}}
                 :required ["file-path"]}
   :implementation clojure-file-outline-tool})

(def clojure-index-stats-mcp-tool
  {:name "clojure-index-stats"
   :description "Get statistics about the current symbol index"
   :inputSchema {:type "object"
                 :properties {}}
   :implementation clojure-index-stats-tool})

(def clojure-list-namespaces-mcp-tool
  {:name "clojure-list-namespaces"
   :description "List all indexed namespaces"
   :inputSchema {:type "object"
                 :properties {}}
   :implementation clojure-list-namespaces-tool})
