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
