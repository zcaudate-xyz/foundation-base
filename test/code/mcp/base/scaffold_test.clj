(ns code.mcp.base.scaffold-test
  (:require [code.mcp.base.rag :as rag]
            [code.mcp.base.scaffold :as scaffold]
            [code.test :refer :all]))

^{:refer code.mcp.base.scaffold/base-tools :added "4.1"}
(fact "scaffold exposes base and rag toolsets"
  (let [store (rag/create-store)]
    [(count (scaffold/base-tools))
     (count (scaffold/rag-tools store))])
  => [2 2])

^{:refer code.mcp.base.scaffold/rag-tools :added "4.1"}
(fact "returns the rag indexing and retrieval tools"
  (let [store (rag/create-store)]
    (mapv :name (scaffold/rag-tools store)))
  => ["rag-index-documents" "rag-retrieve"])

^{:refer code.mcp.base.scaffold/create-rag-server :added "4.1"}
(fact "creates a server config with base, rag and custom tools"
  (let [store (rag/create-store)
        custom-tool {:name "custom-tool"}]
    (with-redefs [code.mcp.base.server/create-server (fn [opts] {:opts opts})]
      (let [result (scaffold/create-rag-server {:store store
                                                :tools [custom-tool]})]
        [(identical? store (:rag/store result))
         (mapv :name (-> result :opts :tools))])))
  => [true ["echo" "ping" "rag-index-documents" "rag-retrieve" "custom-tool"]])
