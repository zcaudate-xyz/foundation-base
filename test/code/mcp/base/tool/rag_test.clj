(ns code.mcp.base.tool.rag-test
  (:require [code.mcp.base.rag :as rag]
            [code.mcp.base.tool.rag :as rag-tool]
            [code.test :refer :all]))

^{:refer code.mcp.base.tool.rag/index-documents-fn :added "4.1"}
(fact "rag tools index and retrieve documents"
  (let [store (rag/create-store)]
    (rag-tool/index-documents-fn store nil {:documents [{:id "doc-1" :text "MCP wrappers in clojure"}]})
    (-> (rag-tool/retrieve-fn store nil {:query "clojure wrapper" :limit 1})
        :structuredContent
        :results
        first
        :id))
  => "doc-1")

^{:refer code.mcp.base.tool.rag/retrieve-fn :added "4.1"}
(fact "retrieves formatted context and structured matches"
  (let [store (rag/create-store)]
    (rag-tool/index-documents-fn store nil {:documents [{:id "doc-1" :text "Semantic retrieval in clojure"}]})
    (let [result (rag-tool/retrieve-fn store nil {:query "semantic" :limit 1})]
      [(-> result :content first :text)
       (-> result :structuredContent :results first :id)]))
  => ["[1] doc-1\nSemantic retrieval in clojure" "doc-1"])

^{:refer code.mcp.base.tool.rag/index-documents-tool :added "4.1"}
(fact "describes the indexing tool"
  (let [store (rag/create-store)
        tool (rag-tool/index-documents-tool store)]
    [(:name tool)
     (fn? (:implementation tool))
     (get-in tool [:inputSchema :required])])
  => ["rag-index-documents" true ["documents"]])

^{:refer code.mcp.base.tool.rag/retrieve-tool :added "4.1"}
(fact "describes the retrieval tool"
  (let [store (rag/create-store)
        tool (rag-tool/retrieve-tool store)]
    [(:name tool)
     (fn? (:implementation tool))
     (get-in tool [:inputSchema :required])])
  => ["rag-retrieve" true ["query"]])

^{:refer code.mcp.base.tool.rag/rag-tools :added "4.1"}
(fact "returns both rag tools"
  (let [store (rag/create-store)]
    (mapv :name (rag-tool/rag-tools store)))
  => ["rag-index-documents" "rag-retrieve"])
