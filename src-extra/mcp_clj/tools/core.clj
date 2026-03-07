(ns mcp-clj.tools.core
  "Tool definitions and validation for MCP servers"
  (:require
    [mcp-clj.tools.clj-eval :as clj-eval]
    [mcp-clj.tools.ls :as ls]
    [mcp-clj.tools.foundation-index :as foundation-index]))

(defn valid-tool?
  "Validate a tool definition"
  [{:keys [name description inputSchema implementation] :as _tool}]
  (and (string? name)
       (string? description)
       (map? inputSchema)
       (ifn? implementation)))

(defn tool-definition
  [tool]
  (dissoc tool :implementation))

(def default-tools
  "Default set of built-in tools"
  {"clj-eval" clj-eval/clj-eval-tool
   "ls" ls/ls-tool
   
   ;; Foundation library indexing tools
   "clojure-index" foundation-index/clojure-index-mcp-tool
   "clojure-search" foundation-index/clojure-search-mcp-tool
   "clojure-get-symbol" foundation-index/clojure-get-symbol-mcp-tool
   "clojure-file-outline" foundation-index/clojure-file-outline-mcp-tool
   "clojure-index-stats" foundation-index/clojure-index-stats-mcp-tool
   "clojure-list-namespaces" foundation-index/clojure-list-namespaces-mcp-tool})
