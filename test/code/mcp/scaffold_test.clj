(ns code.mcp.scaffold-test
  (:require [code.mcp.rag :as rag]
            [code.mcp.scaffold :as scaffold]
            [code.test :refer :all]))

^{:refer code.mcp.scaffold/base-tools :added "4.1"}
(fact "scaffold exposes base and rag toolsets"
  (let [store (rag/create-store)]
    [(count (scaffold/base-tools))
     (count (scaffold/rag-tools store))])
  => [2 2])
