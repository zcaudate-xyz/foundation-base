(ns code.mcp.server-test
  (:require [code.mcp.server :as server]
            [code.test :refer :all])
  (:import [io.modelcontextprotocol.server.transport StdioServerTransportProvider]))

^{:refer code.mcp.server/create-transport-provider :added "4.1"}
(fact "creates a stdio transport provider"
  (instance? StdioServerTransportProvider
             (server/create-transport-provider {:type :stdio}))
  => true)
