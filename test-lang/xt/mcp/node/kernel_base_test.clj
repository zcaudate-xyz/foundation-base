(ns xt.mcp.node.kernel-base-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.transport-memory :as transport-memory]
             [xt.mcp.node.kernel-base :as kernel]
             [xt.mcp.node.client-base :as client]
             [xt.mcp.node.proxy-util :as proxy-util]
             [xt.mcp.node.runtime :as runtime]]})

(fact:global
 {:setup [(l/rt:restart :js)]
  :teardown [(l/rt:stop)]})

(def.js EchoTool
  {"name" "sample_echo"
   "description" "Echoes a snake_case input."
   "input_schema" {"type" "object"
                   "properties" {"snake_case" {"type" "string"}}
                   "required" ["snake_case"]
                   "additional_properties" false}})

^{:refer xt.mcp.node.kernel-base/register-tool :added "4.1"}
(fact "combines MCP data with a generic handler on a named node service"
  (!.js
    (var node (substrate/node-create {}))
    (runtime/init-server node "mcp/example" {})
    (kernel/register-tool node "mcp/example" -/EchoTool
                          (fn [args context]
                            (return {"echo" (. args ["snake_case"])
                                     "service" (. context ["service_id"])}))
                          {})
    [(xt/x:obj-keys (. (kernel/get-service node "mcp/example") ["tools"]))
     (kernel/list-tools node "mcp/example")])
  => [["sample_echo"]
      [{"name" "sample_echo"
        "description" "Echoes a snake_case input."
        "inputSchema" {"type" "object"
                       "properties" {"snake_case" {"type" "string"}}
                       "required" ["snake_case"]
                       "additionalProperties" false}}]])

^{:refer xt.mcp.node.kernel-base/handle-message :added "4.1"}
(fact "implements initialize, tools/list, and tools/call over decoded JSON-RPC"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (runtime/init-server node "mcp/example" {})
    (kernel/register-tool node "mcp/example" -/EchoTool
                          (fn [args context]
                            (return {"echo" (. args ["snake_case"])}))
                          {})
    (var context {"session_id" "session-1"})
    (var init-result nil)
    (-> (client/initialize node "mcp/example" 1
                           {"name" "test" "version" "1"}
                           context {})
        (promise/x:promise-then
         (fn [init]
           (:= init-result init)
           (return (client/initialized node "mcp/example" context {}))))
        (promise/x:promise-then
         (fn [_]
           (return
            (client/call-tool node "mcp/example" 2 "sample_echo"
                              {"snake_case" "hello"} context {}))))
        (promise/x:promise-then
         (fn [called]
           (repl/notify [init-result called])))))
  => (contains-in
      [{"jsonrpc" "2.0" "id" 1
        "result" {"protocolVersion" "2025-11-25"
                  "capabilities" {"tools" {"listChanged" false}}
                  "serverInfo" {"name" "xt.mcp" "version" "0.1.0"}}}
       {"jsonrpc" "2.0" "id" 2
        "result" {"isError" false
                  "structuredContent" {"echo" "hello"}}}]))

(fact "returns protocol errors separately from tool execution errors"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (runtime/init-server node "mcp/example" {})
    (kernel/register-tool node "mcp/example" -/EchoTool
                          (fn [args context]
                            (xt/x:err "handler failed"))
                          {})
    (var context {"session_id" "session-2"})
    (-> (client/initialize node "mcp/example" 1 {} context {})
        (promise/x:promise-then
         (fn [_]
           (return
            (client/initialized node "mcp/example" context {}))))
        (promise/x:promise-then
         (fn [_]
           (return
            (client/call-tool node "mcp/example" 2 "sample_echo"
                              {"snake_case" "hello"} context {}))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains-in
      {"jsonrpc" "2.0" "id" 2
       "result" {"isError" true
                 "content" [{"type" "text" "text" "handler failed"}]}}))

^{:refer xt.mcp.node.runtime/init-server-proxy :added "4.1"}
(fact "keeps proxy installation separate from the server registry"
  (!.js
    (var node (substrate/node-create {}))
    (runtime/init-server-proxy node "server")
    [(xt/x:not-nil? (substrate/get-handler node "@xt.mcp/message"))
     (kernel/get-service node "mcp/example")])
  => [true nil])

(fact "forwards the same client API through the MCP proxy layer"
  (notify/wait-on :js
    (var server (substrate/node-create {"id" "server"}))
    (var client-node (substrate/node-create {"id" "client"}))
    (runtime/init-server server "mcp/example" {})
    (runtime/init-server-proxy client-node "server")
    (kernel/register-tool server "mcp/example" -/EchoTool
                          (fn [args context]
                            (return {"echo" (. args ["snake_case"])}))
                          {})
    (var context {"session_id" "remote-session"})
    (-> (transport-memory/link-pair server client-node)
        (promise/x:promise-then
         (fn [_]
           (proxy-util/set-default-transport client-node "server")
           (return
            (client/initialize client-node "mcp/example" 1 {} context {}))))
        (promise/x:promise-then
         (fn [_]
           (return (client/initialized client-node "mcp/example" context {}))))
        (promise/x:promise-then
         (fn [_]
           (return
            (client/call-tool client-node "mcp/example" 2 "sample_echo"
                              {"snake_case" "remote"} context {}))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains-in
      {"jsonrpc" "2.0" "id" 2
       "result" {"isError" false
                 "structuredContent" {"echo" "remote"}}}))
