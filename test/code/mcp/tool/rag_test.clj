(ns code.mcp.tool.rag-test
  (:require [code.mcp.rag :as rag]
            [code.mcp.tool.rag :as rag-tool]
            [code.test :refer :all]))

^{:refer code.mcp.tool.rag/index-documents-fn :added "4.1"}
(fact "rag tools index and retrieve documents"
  (let [store (rag/create-store)]
    (rag-tool/index-documents-fn store nil {:documents [{:id "doc-1" :text "MCP wrappers in clojure"}]})
    (-> (rag-tool/retrieve-fn store nil {:query "clojure wrapper" :limit 1})
        :structuredContent
        :results
        first
        :id))
  => "doc-1")
