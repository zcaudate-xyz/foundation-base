(ns code.mcp.base.rag-test
  (:require [code.mcp.base.rag :as rag]
            [code.test :refer :all]))

^{:refer code.mcp.base.rag/upsert-documents :added "4.1"}
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

^{:refer code.mcp.base.rag/query-context :added "4.1"}
(fact "retrieved context is rendered as text blocks"
  (let [store (rag/create-store)]
    (rag/upsert-documents store [{:id "rag" :text "Embeddings can drive retrieval."}])
    (rag/query-context store "embeddings"))
  => #"\[1\] rag\nEmbeddings can drive retrieval\.")

^{:refer code.mcp.base.rag/create-store :added "4.1"}
(fact "creates an empty store with weighted retrieval settings"
  (let [store (rag/create-store {:vector-weight 0.7
                                 :keyword-weight 0.3})]
    [(instance? clojure.lang.Atom (:documents store))
     (:vector-weight store)
     (:keyword-weight store)])
  => [true 0.7 0.3])

^{:refer code.mcp.base.rag/normalize-document :added "4.1"}
(fact "normalizes document shape and retains the original payload"
  (let [document {:content "hello" :metadata {:topic :demo}}
        normalized (rag/normalize-document document)]
    [(string? (:id normalized))
     (:text normalized)
     (:metadata normalized)
     (:document normalized)])
  => [true "hello" {:topic :demo} {:content "hello" :metadata {:topic :demo}}])

^{:refer code.mcp.base.rag/upsert-document :added "4.1"}
(fact "stores normalized documents with embeddings"
  (let [store (rag/create-store)
        document (rag/upsert-document store {:id "doc-1" :text "hello world"})]
    [(:id document)
     (contains? @(:documents store) "doc-1")
     (count (:embedding document))])
  => ["doc-1" true 128])

^{:refer code.mcp.base.rag/retrieve :added "4.1"}
(fact "retrieves the most relevant indexed document"
  (let [store (rag/create-store)]
    (rag/upsert-documents store [{:id "doc-1" :text "Clojure wrapper for MCP tools"}
                                 {:id "doc-2" :text "Garden notes about herbs"}])
    (-> (rag/retrieve store "mcp wrapper" {:limit 1})
        first
        :id))
  => "doc-1")

^{:refer code.mcp.base.rag/retrieve-context :added "4.1"}
(fact "renders ranked results into numbered context text"
  (rag/retrieve-context [{:id "doc-1" :text "First"}
                         {:id "doc-2" :text "Second"}])
  => "[1] doc-1\nFirst\n\n[2] doc-2\nSecond")
