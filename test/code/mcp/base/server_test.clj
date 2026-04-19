(ns code.mcp.base.server-test
  (:require [code.mcp.base.server :as server]
            [code.mcp.tool.basic :as basic]
            [code.test :refer :all])
  (:import [io.modelcontextprotocol.server.transport StdioServerTransportProvider]))

^{:refer code.mcp.base.server/create-transport-provider :added "4.1"}
(fact "creates a stdio transport provider"
  (instance? StdioServerTransportProvider
             (server/create-transport-provider {:type :stdio}))
  => true)

^{:refer code.mcp.base.server/create-server :added "4.1"}
(fact "creates a sync server and tracks configured tool specs"
  (let [instance (server/create-server {:tools [basic/echo-tool]
                                        :instructions "Test server"})]
    [(instance? StdioServerTransportProvider (:provider instance))
     (contains? instance :server)
     (keys @(:tool-specs instance))])
  => [true true '("echo")])

^{:refer code.mcp.base.server/add-tool! :added "4.1"}
(fact "adds tool specs to the server registry"
  (let [instance (server/create-server {:tools [basic/echo-tool]})]
    [(server/remove-tool! instance "echo")
     (contains? @(:tool-specs instance) "echo")
     (boolean (server/add-tool! instance basic/echo-tool))
     (contains? @(:tool-specs instance) "echo")])
  => ["echo" false true true])

^{:refer code.mcp.base.server/remove-tool! :added "4.1"}
(fact "removes tool specs from the server registry"
  (let [instance (server/create-server {:tools [basic/echo-tool]})]
    [(server/remove-tool! instance "echo")
     (contains? @(:tool-specs instance) "echo")])
  => ["echo" false])

^{:refer code.mcp.base.server/close! :added "4.1"}
(fact "closes the underlying sync server"
  (let [instance (server/create-server {:tools [basic/echo-tool]})
        result (deref (future (server/close! instance)) 2000 :timeout)]
    result)
  => nil)
