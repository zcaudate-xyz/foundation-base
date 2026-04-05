(ns code.mcp.rag-test
  (:require [code.mcp.rag :as rag]
            [code.test :refer :all]))

^{:refer code.mcp.rag/upsert-documents :added "4.1"}
(fact "rag store indexes and retrieves relevant documents"
  (let [store (rag/create-store)]
    (rag/upsert-documents store [{:id "mcp" :text "Clojure MCP server wrapper with stdio transport"}
                                 {:id "rag" :text "RAG embeddings module for semantic retrieval"}
                                 {:id "other" :text "Something completely unrelated"}])
    [(->> (rag/retrieve store "clojure mcp" {:limit 1})
          first
          :id)
     (->> (rag/retrieve store "semantic retrieval" {:limit 1})
          first
          :id)])
  => ["mcp" "rag"])

^{:refer code.mcp.rag/query-context :added "4.1"}
(fact "retrieved context is rendered as text blocks"
  (let [store (rag/create-store)]
    (rag/upsert-documents store [{:id "rag" :text "Embeddings can drive retrieval."}])
    (rag/query-context store "embeddings"))
  => #"\[1\] rag\nEmbeddings can drive retrieval\.")
