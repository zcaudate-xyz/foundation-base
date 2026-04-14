(ns code.mcp.base.tool.rag
  (:require [code.mcp.base.rag :as rag]))

(defn index-documents-fn
  [store _ {:keys [documents]}]
  (let [indexed (rag/upsert-documents store (or documents []))]
    {:content [{:type "text"
                :text (format "Indexed %s document(s)." (count indexed))}]
     :structuredContent {:documents indexed}
     :isError false}))

(defn retrieve-fn
  [store _ {:keys [query limit]}]
  (let [results (rag/retrieve store query {:limit (or limit 5)})]
    {:content [{:type "text"
                :text (rag/retrieve-context results)}]
     :structuredContent {:results results}
     :isError false}))

(defn index-documents-tool
  [store]
  {:name "rag-index-documents"
   :description "Indexes documents into the in-memory RAG store"
   :inputSchema {:type "object"
                 :properties {"documents" {:type "array"
                                            :items {:type "object"}}}
                 :required ["documents"]}
   :implementation (fn [exchange args]
                     (index-documents-fn store exchange args))})

(defn retrieve-tool
  [store]
  {:name "rag-retrieve"
   :description "Retrieves indexed RAG context for a query"
   :inputSchema {:type "object"
                 :properties {"query" {:type "string"}
                              "limit" {:type "integer"}}
                 :required ["query"]}
   :implementation (fn [exchange args]
                     (retrieve-fn store exchange args))})

(defn rag-tools
  [store]
  [(index-documents-tool store)
   (retrieve-tool store)])
